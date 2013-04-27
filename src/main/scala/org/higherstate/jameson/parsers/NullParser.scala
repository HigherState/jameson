package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.NullExtractor
import util.{Success, Try}
import org.higherstate.jameson.{Registry, Path}

case class NullParser() extends NullExtractor[Null] {
  protected def parse(value: Null, path: Path)(implicit registry:Registry): Try[Null] = Success(value)
}
