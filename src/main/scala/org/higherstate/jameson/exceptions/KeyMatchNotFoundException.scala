package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class KeyMatchNotFoundException(parser:Parser[Any], path:Path) extends PathException {
  override def getMessage:String =
    s"No key found to match against an object.\n\rPath: $path"
}