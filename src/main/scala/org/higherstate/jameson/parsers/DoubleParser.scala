package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.Success
import org.higherstate.jameson.extractors.DoubleExtractor

case object DoubleParser extends DoubleExtractor[Double] {
  def apply(value:Double, path:Path) = Success(value)

  def schema = Map("type" -> "number", "minimum" -> Double.MinValue, "maximum" -> Double.MaxValue)
}

