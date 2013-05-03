package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object FloatParser extends Parser[Float] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case DoubleToken(value) =>
      if (value.toFloat == value) Success(value.toFloat)
      else Failure(UnexpectedValueException("expected float value", value, path))
    case LongToken(value)   =>
      if (value.toFloat == value) Success(value.toFloat)
      else Failure(UnexpectedValueException("expected float value", value, path))
    case token              => Failure(UnexpectedTokenException("Expected double token", token, path))
  }
}
