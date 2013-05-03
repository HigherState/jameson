package org.higherstate.jameson.tokenizers

object -: {
  def unapply(tokenizer:Tokenizer) = Some(tokenizer.head -> tokenizer.tail)

}

trait Tokenizer {
  def head: Token
  def tail: Tokenizer
  def -:(head:Token):Tokenizer = TokenizerInstance(head, this)
}

private case class TokenizerInstance(head:Token, tail:Tokenizer) extends Tokenizer

object End extends Tokenizer {
  def head = EndToken
  def tail = End
}
