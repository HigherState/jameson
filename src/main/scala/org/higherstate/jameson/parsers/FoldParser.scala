package org.higherstate.jameson.parsers

import scala.util.{Failure, Success, Try}
import org.higherstate.jameson.tokenizers.{ArrayEndToken, ArrayStartToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.exceptions.InvalidTokenException
import scala.annotation.tailrec

case class FoldParser[T, U](parser:Parser[T], initial:U, func:(U,T) => U) extends Parser[U] {

  def parse(tokenizer:Tokenizer, path: Path): Try[U] = tokenizer.head match {
    case ArrayStartToken => foldLeft(tokenizer.moveNext(), path, 0, initial)
    case token           => Failure(InvalidTokenException(this, "Expected array start token", token, path))
  }

  @tailrec private def foldLeft(tokenizer:Tokenizer, path:Path, index:Int, accumulator:U):Try[U] = tokenizer.head match {
    case ArrayEndToken => Success(accumulator)
    case _             => {
      parser.parse(tokenizer, path + index).map(func(accumulator, _)) match {
        case f:Failure[U]   => f
        case Success(value) => foldLeft(tokenizer.moveNext(), path, index + 1, value)
      }
    }
  }

  override def default:Option[U] = Some(initial)

  def schema = Map("type" -> "array", "items" -> parser.schema)
}
