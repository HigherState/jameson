package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.StringExtractor
import util.{Success, Try}
import org.higherstate.jameson.{Registry, Path}

case class StringParser() extends StringExtractor[String] {
  protected def parse(value: String, path: Path)(implicit registry:Registry): Try[String] = Success(value)
}
