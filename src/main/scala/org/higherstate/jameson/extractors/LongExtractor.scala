package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{LongToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.InvalidTokenException

trait LongExtractor[T] extends Extractor[Long,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) => apply(value, path)
    case token              => Failure(InvalidTokenException(this, "Expected Boolean token", token, path))
  }
}
