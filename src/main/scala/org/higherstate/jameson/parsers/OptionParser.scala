package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case class OptionParser[T](parser:Parser[T]) extends Parser[Option[T]] {

  def parse(tokenizer:Tokenizer, path:Path) =
    tokenizer.head match {
      case NullToken | EndToken =>
        Success(None)
      case _ =>
        parser.parse(tokenizer, path).map(Some(_))
    }

  override def default = Some(None)

  def schema = parser.schema + ("defaultValue" -> null)
}

