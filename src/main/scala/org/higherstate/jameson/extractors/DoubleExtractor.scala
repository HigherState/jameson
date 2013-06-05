package org.higherstate.jameson.extractors

import org.higherstate.jameson.tokenizers.{LongToken, DoubleToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.Failure
import org.higherstate.jameson.exceptions.{InvalidValueException, InvalidTokenException}

trait DoubleExtractor[T] extends Extractor[Double,  T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case DoubleToken(value) => apply(value, path)
    case LongToken(value)   => apply(value, path)
    case token              => Failure(InvalidTokenException(this, "Expected Double token", token, path))
  }
}

trait DoubleRangeExtractor[T] extends Extractor[Double, T] {
  def greaterThan:Option[Double]
  def greaterThanExclusive:Boolean
  def lessThan:Option[Double]
  def lessThanExclusive:Boolean

  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case DoubleToken(value) => applyRange(value, path)
    case LongToken(value)   => applyRange(value, path)
    case token              => Failure(InvalidTokenException(this, "Expected Double token", token, path))
  }

  private def applyRange(value:Double, path:Path) =
    greaterThan.flatMap { min =>
      if (greaterThanExclusive && min >= value) Some(InvalidValueException(this, s"Expected number to be greater than $min", value, path))
      else if (min > value) Some(InvalidValueException(this, s"Expected number to be greater than or equal to $min", value, path))
      else None
    }.orElse(lessThan.flatMap { max =>
      if (lessThanExclusive && max <= value) Some(InvalidValueException(this, s"Expected number to be less than $max", value, path))
      else if (max < value) Some(InvalidValueException(this, s"Expected number to be less than or equal to $max", value, path))
      else None
    }).map(Failure(_)).getOrElse(apply(value, path))
}
