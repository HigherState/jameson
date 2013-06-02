package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class KeyMatchNotFoundException(parser:Parser[Any], path:Path) extends PathException