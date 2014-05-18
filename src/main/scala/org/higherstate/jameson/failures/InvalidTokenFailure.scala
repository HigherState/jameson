package org.higherstate.jameson.failures

import org.higherstate.jameson.Path
import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.tokenizers.{StructuralToken, ValueToken, Token}

object InvalidTokenFailure {

  def apply(parser:Parser[Any], _message:String, invalidToken:Token, path:Path) =
    invalidToken match {
      case token:ValueToken =>
        InvalidValueTokenFailure(parser, _message, token, path)
      case token:StructuralToken =>
        UnexpectedTokenFailure(_message, token, path)
    }
}
case class InvalidValueTokenFailure(parser:Parser[Any], _message:String, invalidToken:ValueToken, path:Path) extends PathFailure {
  def message:String =
    s"${_message}.\n\rToken: $invalidToken\n\rPath: $path"
}
