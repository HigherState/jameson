package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class RegexValidationException(parser:Parser[Any], message:String, path:Path) extends PathException {
  override def getMessage:String =
    s"$message\n\rPath: $path"
}
