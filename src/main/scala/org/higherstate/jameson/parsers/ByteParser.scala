package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{InvalidTokenException, InvalidValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object ByteParser extends Parser[Byte] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) =>
      if (value.toByte == value) Success(value.toByte)
      else Failure(InvalidValueException(this, "Expected a byte value", value, path))
    case token            => Failure(InvalidTokenException(this, "Expected long token", token, path))
  }

}
