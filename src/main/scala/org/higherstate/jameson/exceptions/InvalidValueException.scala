package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser

case class InvalidValueException(source:Any, message:String, unexpected:Any, path:Path) extends PathException {
  override def getMessage:String =
    s"$message.\n\rValue: $unexpected\n\rPath: $path"
}