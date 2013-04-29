package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

case class RegexValidationException(message:String, path:Path) extends PathException
