package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ConditionalKeyNotFoundFailure(parser:Parser[Any], key:String, path:Path) extends PathFailure {
  def message:String =
    s"Conditional key '$key' not found in object.\n\rPath: $path"
}

case class ConditionalKeyMatchNotFoundFailure(parser:Parser[Any], value:Any, path:Path) extends PathFailure {
  def message:String =
    s"No match found on conditional key value '$value' value.\n\rPath: $path"
}