package org.higherstate.jameson.parsers

import scala.util.{Failure, Try, Success}
import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case class MapParser[T](parser:Parser[T]) extends Parser[Map[String,T]] {

  def parse(tokenizer:Tokenizer, path: Path): Try[(Map[String, T], Tokenizer)] = tokenizer match {
    case ObjectStartToken -: tail => toMap(tail, path)
    case token -: tail            => Failure(UnexpectedTokenException("Expected object start token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path:Path):Try[(Map[String, T], Tokenizer)] = tokenizer match {
    case ObjectEndToken -: tail => Success(Map.empty[String, T] -> tail)
    case KeyToken(key) -: tail  => parser.parse(tail, path + key).flatMap {
      case (result, tail) => toMap(tail, path).map(_.mapLeft(_ + (key -> result)))
    }
    case token -: tail => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}