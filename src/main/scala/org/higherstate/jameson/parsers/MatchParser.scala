package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

//TODO: limit to map like parsers
case class MatchParser[T, U](identifierKey:String, identifierParser:Parser[T], defaultKey:Option[T], matchParsers:Map[T, Parser[U]]) extends Parser[U]{

  def parse(tokenizer:Tokenizer, path: Path): Valid[U] =
    tokenizer.head match {
      case ObjectStartToken =>
        val buffer = tokenizer.getBuffer
        findMatch(buffer.getTokenizer.moveNext(), path).flatMap { key =>
          matchParsers.get(key).fold[Valid[U]](Failure(ConditionalKeyMatchNotFoundFailure(this, key, path)))(_.parse(buffer.getTokenizer, path))

        }
      case token =>
        Failure(InvalidTokenFailure(this, "Match expected an object start token", token, path))
    }


  //Returns tokenizer with match key and value removed
  private def findMatch(tokenizer:Tokenizer, path: Path):Valid[T] =
    tokenizer.head match {
      case KeyToken(key) if key == identifierKey =>
        identifierParser.parse(tokenizer.moveNext(), path + key)
      case KeyToken(key) =>
        findMatch(tokenizer.dropNext(), path)
      case ObjectEndToken =>
        defaultKey.fold[Valid[T]](Failure(ConditionalKeyNotFoundFailure(this, identifierKey, path)))(Success(_))
      case token =>
        Failure(UnexpectedTokenFailure("Expected a key or object end token", token, path))
    }

  def schema = Map("type" -> "object", "oneOf" -> matchParsers.map{mp =>
    val schema = mp._2.schema
    val properties = schema.get("properties").fold(Map.empty[String, Any])(_.asInstanceOf[Map[String, Any]])
    schema + ("properties" -> (properties + (identifierKey ->  mp._1)))
  })
}

case class PartialParser[T, U](identifierKey:String, identifierParser:Parser[T], defaultKey:Option[T], matchParsers:PartialFunction[T, Parser[U]]) extends Parser[U]{

  def parse(tokenizer:Tokenizer, path: Path): Valid[U] = tokenizer.head match {
    case ObjectStartToken => {
      val buffer = tokenizer.getBuffer
      findMatch(buffer.getTokenizer.moveNext(), path).flatMap{ key =>
        matchParsers.lift(key)
          .fold[Valid[U]](Failure(ConditionalKeyMatchNotFoundFailure(this, key, path)))(_.parse(buffer.getTokenizer, path))
      }
    }
    case token            => Failure(InvalidTokenFailure(this, "Match expected an object start token", token, path))
  }

  //Returns tokenizer with match key and value removed
  private def findMatch(tokenizer:Tokenizer, path: Path):Valid[T] = tokenizer.head match {
    case KeyToken(key) if key == identifierKey =>
      identifierParser.parse(tokenizer.moveNext(), path + key)
    case KeyToken(key) =>
      findMatch(tokenizer.dropNext(), path)
    case ObjectEndToken =>
      defaultKey
        .fold[Valid[T]](Failure(ConditionalKeyNotFoundFailure(this, identifierKey, path)))(Success(_))
    case token =>
      Failure(InvalidTokenFailure(this, "Expected a key or object end token", token, path))
  }

  def schema = Map("type" -> "object")
}