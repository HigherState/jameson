package org.higherstate.jameson.parsers

import scala.util.{Failure, Try, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case class MapParser[T](parser:Parser[T]) extends Parser[Map[String,T]] {

  def parse(tokenizer:Tokenizer, path: Path): Try[Map[String, T]] = tokenizer.head match {
    case ObjectStartToken => toMap(tokenizer.moveNext(), path)
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path:Path):Try[Map[String, T]] = tokenizer.head match {
    case ObjectEndToken => Success(Map.empty[String, T])
    case KeyToken(key)  => parser.parse(tokenizer.moveNext, path + key).flatMap(r => toMap(tokenizer.moveNext(), path).map(_ + (key -> r)))
    case token          => Failure(InvalidTokenException(this, "Expected object end token or key token", token, path))
  }

  override def default:Option[Map[String,T]] = Some(Map.empty)
}