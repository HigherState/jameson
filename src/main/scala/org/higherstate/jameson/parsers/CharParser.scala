package org.higherstate.jameson.parsers

import util.{Failure, Success}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object CharParser extends Parser[Char] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) =>
      if (value.size == 1)  Success(value.head)
      else Failure(UnexpectedValueException("Expected a single character value", value, path))
    case token              => Failure(UnexpectedTokenException("Expected boolean token", token, path))
  }
 }
