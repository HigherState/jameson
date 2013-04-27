package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class InvalidTokenException(message:String, path:Path) extends Exception(message)