package org.higherstate.jameson.parsers

import scala.util.{Failure, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case object DoubleParser extends Parser[Double] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case DoubleToken(value) => Success(value)
    case LongToken(value)   => Success(value)
    case token              => Failure(InvalidTokenException(this, "Expected double token", token, path))
  }
}

