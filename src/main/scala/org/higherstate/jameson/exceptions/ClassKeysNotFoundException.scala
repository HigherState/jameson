package org.higherstate.jameson.exceptions

import reflect.runtime.universe._
import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class ClassKeysNotFoundException(parser:Parser[Any], classType:TypeSymbol, keys:List[String], path:Path) extends PathException
