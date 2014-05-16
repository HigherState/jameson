package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case object CharParser extends Parser[Char] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) =>
      if (value.size == 1)  Success(value.head)
      else Failure(InvalidValueFailure(this, "Expected a single character value", value, path))
    case token              =>
      Failure(InvalidTokenFailure(this, "Expected boolean token", token, path))
  }

  def schema = Map("type" -> "string", "maxLength" -> 1)
 }
