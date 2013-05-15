package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.tokenizers.Token

case class InvalidTokenException(parser:Parser[Any], message:String, invalidToken:Token, path:Path) extends PathException