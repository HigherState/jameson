package org.higherstate.jameson.tokenizers

trait Buffer {
  def getTokenizer:Tokenizer
}

private case class BaseBuffer(tokenizer:Tokenizer) extends Buffer {
  private val bufferHead = BufferToken(tokenizer.head)
  private var last = bufferHead

  def moveNext() {
    tokenizer.moveNext()
    last = tokenizer.head :: last
  }

  def getTokenizer = BufferingTokenizer(this, bufferHead)
}

private case class ExtendedBuffer(baseBuffer:BaseBuffer, bufferHead:BufferToken) extends Buffer {
  def getTokenizer = BufferingTokenizer(baseBuffer, bufferHead)
}

private case class BufferingTokenizer(baseBuffer:BaseBuffer, var bufferToken:BufferToken) extends Tokenizer {

  def head = bufferToken.token
  def moveNext() = {
    bufferToken = bufferToken.next.getOrElse {
      baseBuffer.moveNext()
      bufferToken.next.get
    }
    this
  }
  override def getBuffer() = BaseBuffer(this)
}

private case class BufferToken(token:Token) {
  var next:Option[BufferToken] = None

  def :: (token:Token) = {
    val c = BufferToken(token)
    next = Some(c)
    c
  }
}
