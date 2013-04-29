package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class KeyNotFoundException(key:String, path:Path) extends PathException