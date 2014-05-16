package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class KeyNotFoundFailure(parser:Parser[Any], key:String, path:Path) extends PathFailure {
  def message:String =
    s"Expected key '$key' not found.\n\rPath: $path"
}