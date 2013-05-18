package org.higherstate.jameson.parsers

import scala.util.{Failure, Try}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.exceptions.NoSuccessfulParserFoundException

//TODO: Extend buffer functionality to allow many parsers
case class TryParser[+T](leftParser:Parser[T], rightParser:Parser[T]) extends Parser[T] {

  def parse(tokenizer:Tokenizer, path:Path): Try[T] = {
    val bufferingTokenizer = tokenizer.toBufferingTokenizer()
    leftParser.parse(bufferingTokenizer, path)
    .orElse(rightParser.parse(bufferingTokenizer.toBufferedTokenizer(), path)).orElse(Failure(NoSuccessfulParserFoundException(this, path)))
  }
}
