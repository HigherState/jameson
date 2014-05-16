package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case object BooleanParser extends Parser[Boolean] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case BooleanToken(value) => Success(value)
    case token               => Failure(InvalidTokenFailure(this, "Expected boolean token", token, path))
  }

  def schema = Map("type" -> "boolean")
}

