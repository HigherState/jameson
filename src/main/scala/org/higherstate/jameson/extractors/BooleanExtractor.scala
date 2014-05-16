package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{BooleanToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._

trait BooleanExtractor[T] extends Extractor[Boolean,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case BooleanToken(value) =>
      apply(value, path)
    case token              =>
      Failure(InvalidTokenFailure(this, "Expected Boolean token", token, path))
  }
}
