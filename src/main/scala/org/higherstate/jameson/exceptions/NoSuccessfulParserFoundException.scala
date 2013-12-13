package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class NoSuccessfulParserFoundException(parser:Parser[Any], path:Path) extends PathException {
  override def getMessage:String =
    s"Failed to parse.\n\rPath: $path"
}
