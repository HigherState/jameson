package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.failures.ValidationFailure

sealed trait Token extends Any
sealed trait ValueToken extends Any with Token {
  def value:Any
}
sealed trait StructuralToken extends Any with Token

case object ObjectStartToken extends Token with StructuralToken
case object ObjectEndToken extends Token with StructuralToken
case object ArrayStartToken extends Token with StructuralToken
case object ArrayEndToken extends Token with StructuralToken
case object StartToken extends Token with StructuralToken
case object EndToken extends Token with StructuralToken

case class KeyToken(value:String) extends AnyVal with Token with StructuralToken

case class BadToken(value:ValidationFailure) extends AnyVal with Token with StructuralToken

case class UnknownToken(value:Any) extends Token with StructuralToken

case class LongToken(value:Long) extends AnyVal with ValueToken
case class DoubleToken(value:Double) extends AnyVal with ValueToken
case class StringToken(value:String) extends AnyVal with ValueToken
case class BooleanToken(value:Boolean) extends AnyVal with ValueToken
case object NullToken extends ValueToken {
  def value = null
}

case class AnyRefToken(value:AnyRef) extends ValueToken


object ValueToken {
  def unapply(t:Token) = t match {
    case t:ValueToken => Some(t)
    case _ => None
  }
}



