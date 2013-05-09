package org.higherstate.jameson.extractors

import org.higherstate.jameson.parsers.Parser
import scala.util.Try
import org.higherstate.jameson.Path

trait Extractor[U,T] extends Parser[T] {

  def apply(value:U, path:Path):Try[T]
}
