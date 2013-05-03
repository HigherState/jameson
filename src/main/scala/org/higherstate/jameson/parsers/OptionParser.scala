package org.higherstate.jameson.parsers

import util.{Success, Try}
import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case class OptionParser[T](parser:Parser[T]) extends Parser[Option[T]] with HasDefault[Option[T]] {

  def parse(tokenizer:Tokenizer, path:Path): Try[(Option[T], Tokenizer)] = tokenizer match {
    case NullToken -: tail => Success(None -> tail)
    case tokenizer         => parser.parse(tokenizer, path).map(_.mapLeft(Some(_)))
  }

  def default = None

}

