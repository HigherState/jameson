package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case class KeyMatcher[U](matchParsers:Seq[(String, Parser[U])]) extends Parser[U]{
  import KeyMatcher._

  def parse(tokenizer:Tokenizer, path: Path): Valid[U] =
    tokenizer.head match {
      case ObjectStartToken =>
        val buffer = tokenizer.getBuffer
        getKeys(buffer.getTokenizer.moveNext(), path).flatMap { keys =>
          matchParsers
            .find(p => keys.contains(p._1))
            .fold[Valid[U]](Failure(KeyMatchNotFoundFailure(this, path, keys)))(_._2.parse(buffer.getTokenizer, path))
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Match expected an object start token", token, path))
    }

  def schema = Map("type" -> "object", "oneOf" -> matchParsers.map(_._2.schema))

}

case class MaybeKeyMatcher[U](matchParsers:Seq[(String, Parser[U])]) extends Parser[Option[U]]{
  import KeyMatcher._

  def parse(tokenizer:Tokenizer, path: Path): Valid[Option[U]] =
    tokenizer.head match {
      case ObjectStartToken =>
        val buffer = tokenizer.getBuffer
        getKeys(buffer.getTokenizer.moveNext(), path).flatMap { keys =>
          matchParsers
            .find(p => keys.contains(p._1))
            .map(_._2.parse(buffer.getTokenizer, path)) match {
              case Some(Failure(f)) =>
                Failure(f)
              case Some(Success(s)) =>
                Success(Some(s))
              case None =>
                Success(None)
            }
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Match expected an object start token", token, path))
    }
  def schema = Map("type" -> "object", "maybeOneOf" -> matchParsers.map(_._2.schema))
}

object KeyMatcher {

  //Returns tokenizer with match key and value removed
  def getKeys(tokenizer:Tokenizer, path: Path):Valid[Set[String]] = tokenizer.head match {
    case KeyToken(key)  =>
      getKeys(tokenizer.dropNext(), path).map(_ + key)
    case ObjectEndToken =>
      Success(Set.empty)
    case token          =>
      Failure(UnexpectedTokenFailure("Expected a key or object end token", token, path))
  }
}

