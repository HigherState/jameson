package org.higherstate.jameson.parsers

import util.{Failure, Success}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object LongParser extends Parser[Long] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case LongToken(value) -: tail    => Success(value -> tail)
    case token -: tail               => Failure(UnexpectedTokenException("Expected int token", token, path))
  }
}

