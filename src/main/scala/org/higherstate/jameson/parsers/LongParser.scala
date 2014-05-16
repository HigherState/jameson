package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.LongExtractor
import org.higherstate.jameson.failures.Success

case object LongParser extends LongExtractor[Long] {
  def apply(value:Long, path:Path) =
    Success(value)

  def schema = Map("type" -> "integer", "minimum" -> Long.MinValue, "maximum" -> Long.MaxValue)
}