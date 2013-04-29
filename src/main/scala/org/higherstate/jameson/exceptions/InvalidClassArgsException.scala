package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class InvalidClassArgsException(path:Path) extends PathException
