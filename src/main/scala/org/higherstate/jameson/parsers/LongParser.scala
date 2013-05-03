package org.higherstate.jameson.parsers

import util.{Failure, Success}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object LongParser extends Parser[Long] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) => Success(value)
    case token            => Failure(UnexpectedTokenException("Expected int token", token, path))
  }
}

