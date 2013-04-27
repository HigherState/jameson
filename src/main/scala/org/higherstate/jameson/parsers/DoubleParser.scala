package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.NumericExtractor
import util.{Success, Try}
import org.higherstate.jameson.{Registry, Path}

case class DoubleParser() extends NumericExtractor[Double] {
  def parse(value:Double, path: Path)(implicit registry:Registry) = Success(value)
}

