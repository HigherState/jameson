package org.higherstate.jameson.parsers

import util.Try
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Tokenizer

case class EitherParser[T, U](leftParser:Parser[T], rightParser:Parser[U]) extends Parser[Either[T,U]] {

  def parse(tokenizer:Tokenizer, path:Path): Try[Either[T,U]] = {
    val buffer = tokenizer.getBuffer()
    leftParser.parse(buffer.getTokenizer, path).map(Left(_))
    .orElse(rightParser.parse(buffer.getTokenizer, path).map(Right(_)))
  }
}
