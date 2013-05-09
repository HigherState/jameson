package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try

case class PipeParser[T,U](parser:Parser[T], func:T => U) extends Parser[U] {
  def parse(tokenizer: Tokenizer, path: Path): Try[U] = parser.parse(tokenizer, path).map(func)
}
