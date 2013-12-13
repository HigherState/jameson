package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ConditionalKeyNotFoundException(parser:Parser[Any], key:String, path:Path) extends PathException {
  override def getMessage:String =
    s"Conditional key '$key' not found in object.\n\rPath: $path"
}

case class ConditionalKeyMatchNotFoundException(parser:Parser[Any], value:Any, path:Path) extends PathException  {
  override def getMessage:String =
    s"No match found on conditional key value '$value' value.\n\rPath: $path"
}