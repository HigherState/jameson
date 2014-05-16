package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ArgumentsNotFoundFailure(parser:Parser[Any], keys:List[String], path:Path) extends PathFailure {
  def message:String =
    s"Required object arguments not found: ${keys.mkString(",")}.\n\rPath: $path"
}
