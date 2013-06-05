package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{ValueToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.InvalidTokenException

trait ValueExtractor[T] extends Extractor[Any,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case t:ValueToken => apply(t, path)
    case token        => Failure(InvalidTokenException(this, "Expected String token", token, path))
  }
}
