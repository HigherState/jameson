package org.higherstate.jameson.parsers

import org.higherstate.jameson.validators.Validator
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.{Failure, Try}

case class ValidatorParser[+T](parser:Parser[T], validators:List[Validator]) extends Parser[T] {
  def parse(tokenizer: Tokenizer, path: Path): Try[T] = {
    val r = parser.parse(tokenizer, path)
    validators.flatMap(_.apply(r, path)).headOption.map(Failure(_)).getOrElse(r)
  }

  override def default = parser.default
}