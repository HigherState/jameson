package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.tokenizers.Token

case class InvalidTokenFailure(parser:Parser[Any], _message:String, invalidToken:Token, path:Path) extends PathFailure {
  def message:String =
    s"${_message}.\n\rToken: $invalidToken\n\rPath: $path"
}