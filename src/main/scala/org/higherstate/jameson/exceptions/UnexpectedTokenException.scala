package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class UnexpectedTokenException(message:String, path:Path) extends Exception(message)
