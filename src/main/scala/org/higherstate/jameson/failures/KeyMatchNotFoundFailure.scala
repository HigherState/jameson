package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class KeyMatchNotFoundFailure(parser:Parser[Any], path:Path, keys:Set[String]) extends PathFailure {
  def message:String =
    s"No key found to match against an object.  Key(s) found '${keys.mkString(",")}'\n\rPath: $path"
}