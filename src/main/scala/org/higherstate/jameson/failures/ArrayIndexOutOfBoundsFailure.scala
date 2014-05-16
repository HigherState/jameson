package org.higherstate.jameson.failures

import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.Path

case class ArrayIndexOutOfBoundsFailure(parser:Parser[Any], index:Int, path:Path) extends PathFailure {
  def message:String =
    s"Array index [$index] is out of bounds.\n\rPath: $path"
}