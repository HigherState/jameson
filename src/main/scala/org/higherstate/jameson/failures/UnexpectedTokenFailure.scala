package org.higherstate.jameson.failures

import org.higherstate.jameson.tokenizers.Token
import org.higherstate.jameson.Path

case class UnexpectedTokenFailure(_message:String, unexpected:Token, path:Path) extends PathFailure with TokenStreamInterrupt {
  def message:String =
    s"${_message}\n\rToken: $unexpected\n\rPath: $path"
}
