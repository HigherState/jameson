package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.BooleanExtractor
import util.{Success, Try}
import org.higherstate.jameson.{Registry, Path}

case class BooleanParser() extends BooleanExtractor[Boolean] {
  def parse(value:Boolean, path: Path)(implicit registry:Registry) = Success(value)
}

