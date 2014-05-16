package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.LongRangeExtractor
import org.higherstate.jameson.failures.Success

case object ShortParser extends LongRangeExtractor[Short] {
  val greaterThan = Some(Short.MinValue.toLong)
  val greaterThanExclusive = false
  val lessThan = Some(Short.MaxValue.toLong)
  val lessThanExclusive = false

  def apply(value:Long, path:Path) = Success(value.toShort)

  def schema = Map("type" -> "integer", "minimum" -> Short.MinValue, "maximum" -> Short.MaxValue)
}