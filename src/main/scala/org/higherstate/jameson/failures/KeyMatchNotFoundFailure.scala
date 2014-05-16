package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class KeyMatchNotFoundFailure(parser:Parser[Any], path:Path) extends PathFailure {
  def message:String =
    s"No key found to match against an object.\n\rPath: $path"
}