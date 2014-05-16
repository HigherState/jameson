package org.higherstate.jameson.parsers

import org.higherstate.jameson.failures._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object StringParser extends Parser[String] {
  def parse(tokenizer:Tokenizer, path: Path) =
    tokenizer.head match {
      case StringToken(value) =>
        Success(value)
      case token              =>
        Failure(InvalidTokenFailure(this, "Expected String token", token, path))
    }

  def schema = Map("type" -> "string")
}
