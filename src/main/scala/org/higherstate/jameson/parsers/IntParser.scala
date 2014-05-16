package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.LongRangeExtractor
import org.higherstate.jameson.failures._

case object IntParser extends LongRangeExtractor[Int] {
  val greaterThan = Some(Int.MinValue.toLong)
  val greaterThanExclusive = false
  val lessThan = Some(Int.MaxValue.toLong)
  val lessThanExclusive = false

  def apply(value:Long, path:Path) = Success(value.toInt)

  def schema = Map("type" -> "integer", "minimum" -> Int.MinValue, "maximum" -> Int.MaxValue)
}