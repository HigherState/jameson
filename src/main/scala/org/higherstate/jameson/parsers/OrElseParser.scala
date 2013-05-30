package org.higherstate.jameson.parsers

import util.{Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case class OrElseParser[T](parser:Parser[T], _default:T) extends Parser[T] {

  def parse(tokenizer:Tokenizer, path:Path) = tokenizer.head match {
    case NullToken => Success(_default)
    case _         => parser.parse(tokenizer, path)
  }

  override def default = Some(_default)
}
