package org.higherstate.jameson.parsers

import util.Try
import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Tokenizer

case class EitherParser[T, U](leftParser:Parser[T], rightParser:Parser[U]) extends Parser[Either[T,U]] {

  def parse(tokenizer:Tokenizer, path:Path): Try[(Either[T,U], Tokenizer)] = {
    leftParser.parse(tokenizer, path).map(_.mapLeft(Left(_)))
    .orElse(rightParser.parse(tokenizer, path).map(_.mapLeft(Right(_))))
  }
}
