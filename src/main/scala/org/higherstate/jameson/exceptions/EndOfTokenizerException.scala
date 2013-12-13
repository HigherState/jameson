package org.higherstate.jameson.exceptions

object EndOfTokenizerException extends Throwable {
  override def getMessage:String =
    s"Unexpected end of json content"
}