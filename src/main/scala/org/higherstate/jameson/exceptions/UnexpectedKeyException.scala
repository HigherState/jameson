package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class UnexpectedKeyException(parser:Parser[Any], key:String, path:Path) extends PathException
