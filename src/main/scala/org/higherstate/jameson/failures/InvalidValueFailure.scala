package org.higherstate.jameson.failures

import org.higherstate.jameson.Path

case class InvalidValueFailure(source:Any, _message:String, unexpected:Any, path:Path) extends PathFailure {
  def message:String =
    s"${_message}\n\rValue: $unexpected\n\rPath: $path"
}