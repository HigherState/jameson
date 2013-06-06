package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try

case class LazyParser[T](getParser:() => Parser[T]) extends Parser[T] {
  private lazy val parser = getParser()

  def parse(tokenizer: Tokenizer, path: Path): Try[T] = parser.parse(tokenizer, path)
}
