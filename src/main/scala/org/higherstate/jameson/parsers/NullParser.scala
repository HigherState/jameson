package org.higherstate.jameson.parsers

import scala.util.{Failure, Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case object NullParser extends Parser[Null] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case NullToken => Success(null)
    case token     => Failure(UnexpectedTokenException("Expected int token", token, path))
  }
}
