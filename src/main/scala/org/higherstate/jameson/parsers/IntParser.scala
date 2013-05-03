package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object IntParser extends Parser[Int] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case LongToken(value) -: tail    =>
      if (value.toInt == value) Success(value.toInt -> tail)
      else Failure(UnexpectedValueException("Expected an int value", value, path))
    case token -: tail               => Failure(UnexpectedTokenException("Expected long token", token, path))
  }
}
