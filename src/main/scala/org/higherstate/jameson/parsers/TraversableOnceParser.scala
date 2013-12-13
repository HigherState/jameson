package org.higherstate.jameson.parsers

import util.{Failure, Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case class TraversableOnceParser[T](parser:Parser[T]) extends Parser[TraversableOnce[Try[T]]] {

  def parse(tokenizer:Tokenizer, path: Path): Try[TraversableOnce[Try[T]]] = tokenizer.head match {
    case ArrayStartToken  => Success(IteratorWrapper(tokenizer, parser, path))
    case token            => Failure(InvalidTokenException(this, "Expected array start token", token, path))
  }

  private case class IteratorWrapper[T](tokenizer:Tokenizer, parser:Parser[T], path:Path) extends Iterator[Try[T]] {
    var index = -1
    var failed = false

    def hasNext() = !failed && {
      index += 1
      val next = tokenizer.moveNext().head
      next != ArrayEndToken && next != EndToken
    }
    def next() = {
      val r = parser.parse(tokenizer, path + index)
      if (r.isFailure) failed = true
      r
    }
  }

  def schema = Map("type" -> "array", "items" -> parser.schema)
}
