package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers.Token
import org.higherstate.jameson.parsers.Parser

case class UnexpectedTokenException(message:String, unexpected:Token, path:Path) extends PathException
