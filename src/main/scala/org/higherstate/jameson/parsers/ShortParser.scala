package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.NumericExtractor
import util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path}

case class ShortParser() extends NumericExtractor[Short] {
  protected def parse(value: Double, path: Path)(implicit registry:Registry): Try[Short] =
    Try(value.toShort).flatMap { r =>
      if (r != value) Failure(UnexpectedTokenException("Expected an integer value", path))
      else Success(r)
    }
}
