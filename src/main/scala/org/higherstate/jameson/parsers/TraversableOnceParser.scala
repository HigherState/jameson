package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case class TraversableOnceParser[T](parser:Parser[T]) extends Parser[TraversableOnce[Valid[T]]] {

  def parse(tokenizer:Tokenizer, path: Path): Valid[TraversableOnce[Valid[T]]] = tokenizer.head match {
    case ArrayStartToken  =>
      Success(IteratorWrapper(tokenizer, parser, path))
    case token            =>
      Failure(InvalidTokenFailure(this, "Expected array start token", token, path))
  }

  private case class IteratorWrapper[+U](tokenizer:Tokenizer, parser:Parser[U], path:Path) extends Iterator[Valid[U]] {
    var index = -1
    var failed = false

    def hasNext = !failed && {
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
