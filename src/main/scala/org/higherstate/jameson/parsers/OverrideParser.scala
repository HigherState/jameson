package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.Success


case class OverrideParser[T](t:T) extends Parser[T] {
  def parse(tokenizer: Tokenizer, path: Path) = {
    tokenizer.dropObjectOrArray()
    Success(t)
  }

  def schema: Map[String, Any] = ???
  override def default = Some(t)
}
