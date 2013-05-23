package org.higherstate.jameson.parsers

import scala.util.{Success, Failure, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.ConditionalKeyNotFoundException
import org.higherstate.jameson.exceptions.InvalidTokenException
import org.higherstate.jameson.exceptions.ConditionalKeyMatchNotFoundException

//TODO: limit to map like parsers
case class MatchParser[T, U](identifierKey:String, identifierParser:Parser[T], default:Option[T], matchParsers:Map[T, Parser[U]]) extends Parser[U]{

  def parse(tokenizer:Tokenizer, path: Path): Try[U] = tokenizer.head match {
    case ObjectStartToken => {
      val buffer = tokenizer.getBuffer()
      findMatch(buffer.getTokenizer.moveNext(), path).flatMap{ key =>
        matchParsers.get(key).map(_.parse(buffer.getTokenizer, path))
          .getOrElse(Failure(ConditionalKeyMatchNotFoundException(this, identifierKey, path)))
      }
    }
    case token            => Failure(InvalidTokenException(this, "Match expected an object start token", token, path))
  }


  //Returns tokenizer with match key and value removed
  private def findMatch(tokenizer:Tokenizer, path: Path):Try[T] = tokenizer.head match {
    case KeyToken(key) if key == identifierKey => identifierParser.parse(tokenizer.moveNext(), path + key)
    case KeyToken(key)                         => findMatch(tokenizer.dropNext(), path)
    case ObjectEndToken                        => default.map(Success(_)).getOrElse(Failure(ConditionalKeyNotFoundException(this, identifierKey, path)))
    case token                                 => Failure(InvalidTokenException(this, "Expected a key or object end token", token, path))
  }
}

case class PartialParser[T, U](identifierKey:String, identifierParser:Parser[T], default:Option[T], matchParsers:PartialFunction[T, Parser[U]]) extends Parser[U]{

  def parse(tokenizer:Tokenizer, path: Path): Try[U] = tokenizer.head match {
    case ObjectStartToken => {
      val buffer = tokenizer.getBuffer()
      findMatch(buffer.getTokenizer.moveNext(), path).flatMap{ key =>
        matchParsers.lift(key).map(_.parse(buffer.getTokenizer, path))
          .getOrElse(Failure(ConditionalKeyMatchNotFoundException(this, identifierKey, path)))
      }
    }
    case token            => Failure(InvalidTokenException(this, "Match expected an object start token", token, path))
  }

  //Returns tokenizer with match key and value removed
  private def findMatch(tokenizer:Tokenizer, path: Path):Try[T] = tokenizer.head match {
    case KeyToken(key) if key == identifierKey => identifierParser.parse(tokenizer.moveNext(), path + key)
    case KeyToken(key)                         => findMatch(tokenizer.dropNext(), path)
    case ObjectEndToken                        => default.map(Success(_)).getOrElse(Failure(ConditionalKeyNotFoundException(this, identifierKey, path)))
    case token                                 => Failure(InvalidTokenException(this, "Expected a key or object end token", token, path))
  }


}