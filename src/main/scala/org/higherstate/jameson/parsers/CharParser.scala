package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.StringExtractor
import util.{Failure, Try, Success}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path}

case class CharParser() extends StringExtractor[Char] {
   protected def parse(value: String, path: Path)(implicit registry:Registry): Try[Char] =
     if (value.size != 1) Failure(UnexpectedTokenException("Expected a single character value", path))
     else Success(value.head)
 }
