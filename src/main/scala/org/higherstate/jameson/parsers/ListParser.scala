package org.higherstate.jameson.parsers

import util.{Failure, Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case class ListParser[T](parser:Parser[T]) extends Parser[List[T]] {

  def parse(tokenizer:Tokenizer, path: Path): Try[List[T]] = tokenizer.head match {
    case ArrayStartToken => append(tokenizer.moveNext(), path, 0)
    case token           => Failure(InvalidTokenException(this, "Expected array start token", token, path))
  }

  protected def append(tokenizer:Tokenizer, path:Path, index:Int):Try[List[T]] = tokenizer.head match {
    case ArrayEndToken => Success(Nil)
    case _             => parser.parse(tokenizer, path + index).flatMap(r => append(tokenizer.moveNext(), path, index + 1).map(r :: _))
  }

  override def default:Option[List[T]] = Some(Nil)

  def schema = Map("type" -> "array", "items" -> parser.schema)
}

