package org.higherstate.jameson.parsers

import scala.util.{Failure, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case object BooleanParser extends Parser[Boolean] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case BooleanToken(value) => Success(value)
    case token               => Failure(InvalidTokenException(this, "Expected boolean token", token, path))
  }

  def schema = Map("type" -> "boolean")
}

