package org.higherstate.jameson.parsers

import util.{Success, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case class OrElseParser[T](parser:Parser[T], default:T) extends Parser[T] with HasDefault[T] {

  def parse(tokenizer:Tokenizer, path:Path): Try[(T, Tokenizer)] = tokenizer match {
    case NullToken -: tail => Success(default -> tail)
    case tokenizer         => parser.parse(tokenizer, path)
  }
}
