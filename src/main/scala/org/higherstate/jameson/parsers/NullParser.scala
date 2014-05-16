package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case object NullParser extends Parser[Null] {
  def parse(tokenizer:Tokenizer, path: Path) =
    tokenizer.head match {
      case NullToken =>
        Success(null)
      case token     =>
        Failure(InvalidTokenFailure(this, "Expected null token", token, path))
    }

  def schema = Map("type" -> "null")
}
