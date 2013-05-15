package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{DoubleToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.InvalidTokenException

trait DoubleExtractor[T] extends Extractor[Double,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case DoubleToken(value) => apply(value, path)
    case token              => Failure(InvalidTokenException(this, "Expected Boolean token", token, path))
  }
}
