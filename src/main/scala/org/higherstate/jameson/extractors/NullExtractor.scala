package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{NullToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._

trait NullExtractor[T] extends Extractor[Null,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case NullToken => apply(null, path)
    case token     => Failure(InvalidTokenFailure(this, "Expected Null token", token, path))
  }
}
