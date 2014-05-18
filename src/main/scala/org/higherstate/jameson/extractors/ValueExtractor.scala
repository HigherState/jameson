package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{ValueToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._

trait ValueExtractor[T] extends Extractor[Any,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case t:ValueToken =>
      apply(t, path)
    case token =>
      Failure(InvalidTokenFailure(this, "Expected value token", token, path))
  }
}
