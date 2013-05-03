package org.higherstate.jameson.parsers

import util.{Success, Try}
import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case class OptionParser[T](parser:Parser[T]) extends Parser[Option[T]] with HasDefault[Option[T]] {

  def parse(tokenizer:Tokenizer, path:Path) = tokenizer.head match {
    case NullToken => Success(None)
    case _         => parser.parse(tokenizer, path).map(Some(_))
  }

  def default = None

}

