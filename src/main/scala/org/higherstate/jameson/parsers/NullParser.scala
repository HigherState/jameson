package org.higherstate.jameson.parsers

import scala.util.{Failure, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.InvalidTokenException

case object NullParser extends Parser[Null] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case NullToken => Success(null)
    case token     => Failure(InvalidTokenException(this, "Expected null token", token, path))
  }
}
