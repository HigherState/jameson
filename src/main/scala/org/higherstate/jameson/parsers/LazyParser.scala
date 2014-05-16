package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.Valid

case class LazyParser[T](getParser:() => Parser[T]) extends Parser[T] {
  private lazy val parser:Parser[T] = getParser()

  def parse(tokenizer: Tokenizer, path: Path): Valid[T] =
    parser.parse(tokenizer, path)

  def schema = ???
}
