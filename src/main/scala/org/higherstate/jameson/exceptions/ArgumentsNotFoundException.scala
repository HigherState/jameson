package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ArgumentsNotFoundException(parser:Parser[Any], keys:List[String], path:Path) extends PathException {
  override def getMessage:String =
    s"Required object arguments not found: ${keys.mkString(",")}.\n\rPath: $path"
}
