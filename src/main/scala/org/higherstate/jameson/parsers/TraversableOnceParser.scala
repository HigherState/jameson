package org.higherstate.jameson.parsers

import util.{Failure, Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case class TraversableOnceParser[T](parser:Parser[T]) extends Parser[TraversableOnce[Try[T]]] {

  def parse(tokenizer:Tokenizer, path: Path): Try[(TraversableOnce[Try[T]], Tokenizer)] = tokenizer match {
    case ArrayStartToken -: tail => Success(TokenIterator(parser, path, tokenizer) -> End)
    case token  -: tail          => Failure(UnexpectedTokenException("Expected array start token", token, path))
  }

  private case class TokenIterator(parser:Parser[T], path:Path, var tokenizer:Tokenizer) extends Iterator[Try[T]]{
    private var index = -1
    private var failed = false
    def hasNext = !failed && tokenizer.tail != End && tokenizer.tail.head != ArrayEndToken
    def next() =
      parser.parse(tokenizer.tail, path + index) match {
        case Success((r, t)) => {
          index += 1
          tokenizer = t
          Success(r)
        }
        case f => failed = true; f.asInstanceOf[Failure[T]]
      }
  }
}
