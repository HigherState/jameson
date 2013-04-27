package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.NumericExtractor
import util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path}

case class IntParser() extends NumericExtractor[Int] {
  protected def parse(value: Double, path: Path)(implicit registry:Registry): Try[Int] =
    Try(value.toInt).flatMap { r =>
      if (r != value) Failure(UnexpectedTokenException("Expected an integer value", path))
      else Success(r)
    }
}
