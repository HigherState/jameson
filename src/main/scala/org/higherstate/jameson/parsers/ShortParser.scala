package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{InvalidValueException, InvalidTokenException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object ShortParser extends Parser[Short] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) =>
      if (value.toShort == value) Success(value.toShort)
      else Failure(InvalidValueException(this, "Expected a short value", value, path))
    case token            => Failure(InvalidTokenException(this, "Expected long token", token, path))
  }
}
