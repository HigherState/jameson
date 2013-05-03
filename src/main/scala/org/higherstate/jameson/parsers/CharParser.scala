package org.higherstate.jameson.parsers

import util.{Failure, Success}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedValueException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object CharParser extends Parser[Char] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case StringToken(value) -: tail    =>
      if (value.size == 1)  Success(value.head -> tail)
      else Failure(UnexpectedValueException("Expected a single character value", value, path))
    case token -: tail               => Failure(UnexpectedTokenException("Expected boolean token", token, path))
  }
 }
