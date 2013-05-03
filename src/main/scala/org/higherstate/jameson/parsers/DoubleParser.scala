package org.higherstate.jameson.parsers

import scala.util.{Failure, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case object DoubleParser extends Parser[Double] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case DoubleToken(value) => Success(value)
    case LongToken(value)   => Success(value)
    case token              => Failure(UnexpectedTokenException("Expected double token", token, path))
  }
}

