package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class ConditionalKeyNotFoundException(key:String, path:Path) extends PathException

case class ConditionalKeyMatchNotFoundException(value:String, path:Path) extends PathException