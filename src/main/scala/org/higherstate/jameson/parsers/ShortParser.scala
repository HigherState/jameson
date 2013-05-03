package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedValueException, UnexpectedTokenException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object ShortParser extends Parser[Short] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case LongToken(value) -: tail    =>
      if (value.toShort == value) Success(value.toShort -> tail)
      else Failure(UnexpectedValueException("Expected a short value", value, path))
    case token -: tail               => Failure(UnexpectedTokenException("Expected long token", token, path))
  }
}
