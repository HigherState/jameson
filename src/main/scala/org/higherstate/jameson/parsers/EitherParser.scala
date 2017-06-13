package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.failures.Valid

case class EitherParser[T, U](leftParser:Parser[T], rightParser:Parser[U]) extends Parser[Either[T,U]] {

  def parse(tokenizer:Tokenizer, path:Path): Valid[Either[T,U]] = {
    val buffer = tokenizer.getBuffer
    leftParser.parse(buffer.getTokenizer, path).map(Left(_)) match {
      case Left(_) => rightParser.parse(buffer.getTokenizer, path).map(Right(_))
      case r => r
    }
  }

  def schema = Map("oneOf" -> List(leftParser.schema, rightParser.schema))
}
