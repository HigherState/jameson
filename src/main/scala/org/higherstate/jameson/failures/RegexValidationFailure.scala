package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class RegexValidationFailure(parser:Parser[Any], _message:String, path:Path) extends PathFailure {
  def message:String =
    s"${_message}\n\rPath: $path"
}
