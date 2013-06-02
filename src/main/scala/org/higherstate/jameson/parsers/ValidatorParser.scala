package org.higherstate.jameson.parsers

import org.higherstate.jameson.validators.Validator
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import util.{Success, Failure, Try}

case class ValidatorParser[+T](parser:Parser[T], validators:List[Validator]) extends Parser[T] {
  def parse(tokenizer: Tokenizer, path: Path): Try[T] =
    parser.parse(tokenizer, path).flatMap { value =>
      validators.flatMap(_.apply(value, path)).headOption.map(Failure(_)).getOrElse(Success(value))
    }

  override def default = parser.default
}