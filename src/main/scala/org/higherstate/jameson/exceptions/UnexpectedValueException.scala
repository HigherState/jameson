package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class UnexpectedValueException(message:String, unexpected:Any, path:Path) extends PathException