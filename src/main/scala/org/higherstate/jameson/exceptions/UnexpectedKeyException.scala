package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class UnexpectedKeyException(key:String, path:Path) extends PathException
