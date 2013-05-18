package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ArgumentsNotFoundException(parser:Parser[Any], keys:List[String], path:Path) extends PathException
