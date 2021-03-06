package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.DoubleRangeExtractor
import org.higherstate.jameson.failures.Success

case object FloatParser extends DoubleRangeExtractor[Float] {
  val greaterThan = Some(Float.MinValue.toDouble)
  val greaterThanExclusive = false
  val lessThan = Some(Float.MaxValue.toDouble)
  val lessThanExclusive = false

  def apply(value:Double, path:Path) = Success(value.toFloat)

  def schema = Map("type" -> "number", "minimum" -> Float.MinValue, "maximum" -> Float.MaxValue)
}