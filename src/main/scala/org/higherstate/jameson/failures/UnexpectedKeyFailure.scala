package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class UnexpectedKeyFailure(parser:Parser[Any], key:String, path:Path) extends PathFailure {
  def message:String =
    s"Unexpected key '$key'.\n\rPath: $path"
}
