package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class UnexpectedValueException(message:String, unexpected:Any, path:Path) extends PathException {
  override def getMessage:String =
    s"$message\n\rValue: $unexpected\n\rPath: $path"
}