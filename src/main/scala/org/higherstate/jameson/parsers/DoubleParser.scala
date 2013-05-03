package org.higherstate.jameson.parsers

import scala.util.{Failure, Success}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case object DoubleParser extends Parser[Double] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case DoubleToken(value) -: tail => Success(value -> tail)
    case token -: tail               => Failure(UnexpectedTokenException("Expected double token", token, path))
  }
}

