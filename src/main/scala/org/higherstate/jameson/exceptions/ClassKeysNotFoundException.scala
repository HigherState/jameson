package org.higherstate.jameson.exceptions

import reflect.runtime.universe._
import org.higherstate.jameson.Path

case class ClassKeysNotFoundException(classType:TypeSymbol, keys:List[String], path:Path) extends PathException
