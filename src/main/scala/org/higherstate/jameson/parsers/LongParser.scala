package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.NumericExtractor
import util.{Failure, Try, Success}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path}

case class LongParser() extends NumericExtractor[Long] {
  def parse(value:Double, path: Path)(implicit registry:Registry) = {
    val r = value.toLong
    if (r != value) Failure(UnexpectedTokenException("Expected a long value", path))
    else Success(r)
  }
}

