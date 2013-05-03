package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object IntParser extends Parser[Int] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) =>
      if (value.toInt == value) Success(value.toInt)
      else Failure(UnexpectedValueException("Expected an int value", value, path))
    case token            => Failure(UnexpectedTokenException("Expected long token", token, path))
  }
}
