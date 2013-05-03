package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object FloatParser extends Parser[Float] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case DoubleToken(value) -: tail =>
      if (value.toFloat == value) Success(value.toFloat -> tail)
      else Failure(UnexpectedValueException("expected float value", value, path))
    case token -: tail              => Failure(UnexpectedTokenException("Expected double token", token, path))
  }
}
