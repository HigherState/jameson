package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ConditionalKeyNotFoundException(parser:Parser[Any], key:String, path:Path) extends PathException

case class ConditionalKeyMatchNotFoundException(parser:Parser[Any], value:String, path:Path) extends PathException