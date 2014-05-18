package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{LongToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._

trait LongExtractor[T] extends Extractor[Long,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) =>
      apply(value, path)
    case token            =>
      Failure(InvalidTokenFailure(this, "Expected Long token", token, path))
  }
}

trait LongRangeExtractor[T] extends Extractor[Long, T] {
  def greaterThan:Option[Long]
  def greaterThanExclusive:Boolean
  def lessThan:Option[Long]
  def lessThanExclusive:Boolean

  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case LongToken(value) =>
      greaterThan.flatMap { min =>
        if (greaterThanExclusive && min >= value) Some(InvalidValueFailure(this, s"Expected number to be greater than $min", value, path))
        else if (min > value) Some(InvalidValueFailure(this, s"Expected number to be greater than or equal to $min", value, path))
        else None
      }.orElse(lessThan.flatMap { max =>
        if (lessThanExclusive && max <= value) Some(InvalidValueFailure(this, s"Expected number to be less than $max", value, path))
        else if (max < value) Some(InvalidValueFailure(this, s"Expected number to be less than or equal to $max", value, path))
        else None
      }).fold(apply(value, path))(Failure(_))
    case token            =>
      Failure(InvalidTokenFailure(this, "Expected Long token", token, path))
  }
}
