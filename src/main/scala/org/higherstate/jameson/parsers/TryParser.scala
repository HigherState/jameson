package org.higherstate.jameson.parsers

import scala.util.{Failure, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.exceptions.NoSuccessfulParserFoundException

case class TryParser[+T](parsers:Seq[Parser[T]]) extends Parser[T] {

  def parse(tokenizer:Tokenizer, path:Path): Try[T] = {
    val buffer = tokenizer.getBuffer()
    parsers.toIterator.map(_.parse(buffer.getTokenizer, path)).find(_.isSuccess).getOrElse(Failure(NoSuccessfulParserFoundException(this, path)))
  }

  def schema = Map("oneOf" -> parsers.map(_.schema))
}
