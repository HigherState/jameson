package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Token

case class UnexpectedTokenException(message:String, unexpected:Token, path:Path) extends PathException
