package org.higherstate.jameson.exceptions

import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.Path

case class ArrayIndexOutOfBoundsException(parser:Parser[Any], index:Int, path:Path) extends PathException