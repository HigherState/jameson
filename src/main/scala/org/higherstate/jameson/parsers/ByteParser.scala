package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object ByteParser extends Parser[Byte] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case LongToken(value) -: tail    =>
      if (value.toByte == value) Success(value.toByte -> tail)
      else Failure(UnexpectedValueException("Expected a byte value", value, path))
    case token -: tail               => Failure(UnexpectedTokenException("Expected long token", token, path))
  }

}
