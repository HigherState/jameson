package org.higherstate.jameson.parsers

import scala.util.{Failure, Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case object NullParser extends Parser[Null] {
  def parse(tokenizer:Tokenizer, path: Path): Try[(Null, Tokenizer)] = tokenizer match {
    case NullToken -: tail    => Success((null, tail))
    case token -: tail        => Failure(UnexpectedTokenException("Expected int token", token, path))
  }
}
