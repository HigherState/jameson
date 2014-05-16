package org.higherstate.jameson.parsers

import org.higherstate.jameson.validators.Validator
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._

case class ValidatorParser[+T](parser:Parser[T], validators:List[Validator]) extends Parser[T] {
  def parse(tokenizer: Tokenizer, path: Path): Valid[T] =
    parser.parse(tokenizer, path).flatMap { value =>
      validators
        .flatMap(_.apply(value, path))
        .headOption
        .fold(Success(value))(Failure(_))
    }

  override def default = parser.default

  def schema = parser.schema ++ validators.flatMap(_.schema)
}