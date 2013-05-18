package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.NoPath

trait Tokenizer {
  def head:Token
  def moveNext():Tokenizer
  def toBufferingTokenizer() = BufferingTokenizer(this)

  def dropNext():Tokenizer = this.moveNext.drop()

  private def drop():Tokenizer = this.head match {
    case (token:BadToken) => this
    case ArrayStartToken  => {
      this.moveNext()
      while(head != ArrayEndToken) drop()
      this.moveNext()
    }
    case ObjectStartToken => {
      this.moveNext()
      while(head != ObjectEndToken) moveNext().drop()
      this.moveNext()
    }
    case ArrayEndToken    => FailedTokenizer(BadToken(UnexpectedTokenException("Unexpected token", ArrayEndToken, NoPath)))
    case ObjectEndToken   => FailedTokenizer(BadToken(UnexpectedTokenException("Unexpected token", ObjectEndToken, NoPath)))
    case token            => this.moveNext
  }
}

case class FailedTokenizer(head:BadToken) extends Tokenizer {
  def moveNext() = this
}

case class BufferingTokenizer(tokenizer:Tokenizer) extends Tokenizer {
  private var buffer = List(tokenizer.head)
  private var subBufferingTokenizer:Option[BufferingTokenizer] = None
  def head = buffer.head
  def moveNext() = {
    buffer = tokenizer.moveNext.head :: buffer
    this
  }
  override def toBufferingTokenizer() =
    if (subBufferingTokenizer.nonEmpty) throw new Exception("Cannot buffer twice on the same buffered tokenizer")
    else {
      subBufferingTokenizer = Some(BufferingTokenizer(tokenizer))
      subBufferingTokenizer.get
    }

  def toBufferedTokenizer():BufferedTokenizer = subBufferingTokenizer match {
    case None     => BufferedTokenizer(buffer.reverse, tokenizer)
    //TODO must be a faster way here
    case Some(bt) => BufferedTokenizer(buffer.reverse ++ bt.toBufferedTokenizer().buffer.tail, tokenizer)
  }
}

case class BufferedTokenizer(var buffer:List[Token], tokenizer:Tokenizer) extends Tokenizer {
  //last element in buffer should be the same as first element in tokenizer
  def head = buffer.head
  def moveNext() = {
    buffer = buffer.tail
    if (buffer.isEmpty) tokenizer.moveNext
    else this
  }
}