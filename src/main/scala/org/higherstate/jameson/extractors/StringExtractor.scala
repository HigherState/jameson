package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{StringToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.UnexpectedTokenException

trait StringExtractor[T] extends Extractor[String, T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) => apply(value, path)
    case token              => Failure(UnexpectedTokenException("Expected String token", token, path))
  }
}
