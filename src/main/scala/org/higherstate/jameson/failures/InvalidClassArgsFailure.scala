package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class InvalidClassArgsFailure(parser:Parser[Any], failedArgs:Array[Any], path:Path) extends PathFailure {
  def message:String =
    s"Invalid arguments for object: ${failedArgs.mkString(",")}.\n\rPath: $path"
}
