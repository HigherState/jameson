package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.LongRangeExtractor
import org.higherstate.jameson.failures.Success

case object ByteParser extends LongRangeExtractor[Byte] {
  val greaterThan = Some(Byte.MinValue.toLong)
  val greaterThanExclusive = false
  val lessThan = Some(Byte.MaxValue.toLong)
  val lessThanExclusive = false

  def apply(value:Long, path:Path) = Success(value.toByte)

  def schema = Map("type" -> "integer", "minimum" -> Byte.MinValue, "maximum" -> Byte.MaxValue)
}
