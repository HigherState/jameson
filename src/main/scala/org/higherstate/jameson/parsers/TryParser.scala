package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.failures._

case class TryParser[+T](parsers:Seq[Parser[T]]) extends Parser[T] {

  def parse(tokenizer:Tokenizer, path:Path) = {
    val buffer = tokenizer.getBuffer
    parsers.toIterator.map(_.parse(buffer.getTokenizer, path)).find(_.isSuccess).getOrElse(Failure(NoSuccessfulParserFoundFailure(this, path)))
  }

  def schema = Map("oneOf" -> parsers.map(_.schema))
}
