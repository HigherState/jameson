package org.higherstate.jameson.extractors

import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.Valid

trait Extractor[U,+T] extends Parser[T] {

  def apply(value:U, path:Path):Valid[T]
}
