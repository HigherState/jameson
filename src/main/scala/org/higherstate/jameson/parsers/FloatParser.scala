package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.NumericExtractor
import util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path}

case class FloatParser() extends NumericExtractor[Float] {
   protected def parse(value: Double, path: Path)(implicit registry:Registry): Try[Float] =
     Try(value.toFloat).flatMap { r =>
       if (r != value) Failure(UnexpectedTokenException("Expected a float value", path))
       else Success(r)
     }
 }
