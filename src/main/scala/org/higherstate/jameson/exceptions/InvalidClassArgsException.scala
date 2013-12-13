package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class InvalidClassArgsException(parser:Parser[Any], failedArgs:Array[Any], path:Path) extends PathException {
  override def getMessage:String =
    s"Invalid arguments for object: ${failedArgs.mkString(",")}.\n\rPath: $path"
}
