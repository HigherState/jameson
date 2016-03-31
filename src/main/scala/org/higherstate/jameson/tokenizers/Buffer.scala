package org.higherstate.jameson.tokenizers

trait Buffer {
  def getTokenizer:Tokenizer
}

private case class BaseBuffer(tokenizer:Tokenizer) extends Buffer {
  private val bufferHead = BufferToken(tokenizer.head)
  private var last = bufferHead

  def moveNext():Unit = {
    tokenizer.moveNext()
    last = tokenizer.head :: last
  }

  def getTokenizer = BaseBufferingTokenizer(this, bufferHead)
}

private case class NestedBuffer(tokenizer:BufferingTokenizer, bufferHead:BufferToken) extends Buffer {
  def getTokenizer = {
    tokenizer.reset(bufferHead)
    NestedBufferingTokenizer(tokenizer)
  }
}

private trait BufferingTokenizer extends Tokenizer {
  def reset(_bufferToken:BufferToken):Unit
  def bufferToken:BufferToken
}

private case class NestedBufferingTokenizer(tokenizer:BufferingTokenizer) extends BufferingTokenizer {

  def head = tokenizer.head
  def moveNext() = {
    tokenizer.moveNext()
    this
  }
  override def getBuffer = NestedBuffer(this, bufferToken)

  def reset(_bufferToken:BufferToken):Unit = {
    tokenizer.reset(_bufferToken)
  }

  def bufferToken = tokenizer.bufferToken
}

private case class BaseBufferingTokenizer(baseBuffer:BaseBuffer, var bufferToken:BufferToken) extends BufferingTokenizer {

  def head = bufferToken.token
  def moveNext() = {
    bufferToken = bufferToken.next.getOrElse {
      baseBuffer.moveNext()
      bufferToken.next.get
    }
    this
  }
  override def getBuffer = NestedBuffer(this, bufferToken)

  def reset(_bufferToken:BufferToken):Unit = {
    bufferToken = _bufferToken
  }
}

private case class BufferToken(token:Token) {
  var next:Option[BufferToken] = None

  def :: (token:Token) = {
    val c = BufferToken(token)
    next = Some(c)
    c
  }
}
