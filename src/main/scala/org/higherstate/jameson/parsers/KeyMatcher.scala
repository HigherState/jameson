package org.higherstate.jameson.parsers

import scala.util.{Success, Failure, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.{KeyMatchNotFoundException, InvalidTokenException}

case class KeyMatcher[U](matchParsers:Seq[(String, Parser[U])]) extends Parser[U]{

  def parse(tokenizer:Tokenizer, path: Path): Try[U] =
    tokenizer.head match {
      case ObjectStartToken => {
        val buffer = tokenizer.getBuffer()
        getKeys(buffer.getTokenizer.moveNext(), path).flatMap { keys =>
          matchParsers
            .find(p => keys.contains(p._1))
            .map(_._2.parse(buffer.getTokenizer, path))
            .getOrElse(Failure(KeyMatchNotFoundException(this, path)))
        }
      }
      case token            => Failure(InvalidTokenException(this, "Match expected an object start token", token, path))
    }

  //Returns tokenizer with match key and value removed
  private def getKeys(tokenizer:Tokenizer, path: Path):Try[Set[String]] = tokenizer.head match {
    case KeyToken(key)  => getKeys(tokenizer.dropNext(), path).map(_ + key)
    case ObjectEndToken => Success(Set.empty)
    case token          => Failure(InvalidTokenException(this, "Expected a key or object end token", token, path))
  }

  def schema = Map("type" -> "object", "oneOf" -> matchParsers.map(_._2.schema))
}

