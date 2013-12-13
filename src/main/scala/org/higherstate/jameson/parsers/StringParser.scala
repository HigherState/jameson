package org.higherstate.jameson.parsers

import scala.util.{Failure, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case object StringParser extends Parser[String] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) => Success(value)
    case token              => Failure(InvalidTokenException(this, "Expected String token", token, path))
  }

  def schema = Map("type" -> "string")
}
