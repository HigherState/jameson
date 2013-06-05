package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{NullToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.InvalidTokenException

trait NullExtractor[T] extends Extractor[Null,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case NullToken => apply(null, path)
    case token     => Failure(InvalidTokenException(this, "Expected Null token", token, path))
  }
}
