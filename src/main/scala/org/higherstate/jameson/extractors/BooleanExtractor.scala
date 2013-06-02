package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{BooleanToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.InvalidTokenException

trait BooleanExtractor[T] extends Extractor[Boolean,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case BooleanToken(value) => apply(value, path)
    case token              => Failure(InvalidTokenException(this, "Expected Boolean token", token, path))
  }
}
