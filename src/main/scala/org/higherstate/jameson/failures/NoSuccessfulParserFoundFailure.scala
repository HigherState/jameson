package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class NoSuccessfulParserFoundFailure(parser:Parser[Any], path:Path) extends PathFailure {
  override def message:String =
    s"Failed to parse.\n\rPath: $path"
}
