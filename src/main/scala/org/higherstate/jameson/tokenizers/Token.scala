package org.higherstate.jameson.tokenizers

sealed trait Token extends Any
sealed trait ValueToken extends Any with Token {
  def value:Any
}

case object ObjectStartToken extends Token
case object ObjectEndToken extends Token
case object ArrayStartToken extends Token
case object ArrayEndToken extends Token
case object StartToken extends Token
case object EndToken extends Token

case class KeyToken(value:String) extends AnyVal with Token

case class BadToken(value:Throwable) extends AnyVal with Token

case class LongToken(value:Long) extends AnyVal with ValueToken
case class DoubleToken(value:Double) extends AnyVal with ValueToken
case class StringToken(value:String) extends AnyVal with ValueToken
case class BooleanToken(value:Boolean) extends AnyVal with ValueToken
case object NullToken extends ValueToken {
  def value = null
}

case class AnyRefToken(value:AnyRef) extends ValueToken


