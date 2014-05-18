package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.failures.UnexpectedTokenFailure
import org.higherstate.jameson.NoPath

trait Tokenizer {
  def head:Token
  def moveNext():Tokenizer
  def getBuffer:Buffer = BaseBuffer(this)

  def dropNext():Tokenizer = this.moveNext().drop()

  def drop():Tokenizer = this.head match {
    case (token:BadToken) => this
    case ArrayStartToken  =>
      this.moveNext()
      while(head != ArrayEndToken) {
        if (head.isInstanceOf[BadToken] || head == EndToken) return this
        drop()
      }
      this.moveNext()

    case ObjectStartToken =>
      this.moveNext()
      while(head != ObjectEndToken) {
        if (head.isInstanceOf[BadToken] || head == EndToken) return this
        moveNext().drop()
      }
      this.moveNext()

    case ArrayEndToken =>
      FailedTokenizer(BadToken(UnexpectedTokenFailure("Unexpected token", ArrayEndToken, NoPath)))
    case ObjectEndToken =>
      FailedTokenizer(BadToken(UnexpectedTokenFailure("Unexpected token", ObjectEndToken, NoPath)))
    case token =>
      this.moveNext()
  }

  //leaves arrayEnd or object end token as head
  def dropObjectOrArray():Tokenizer =
    this.head match {
      case (token:BadToken) => this
      case ArrayStartToken =>
        this.moveNext()
        while(head != ArrayEndToken) {
          if (head.isInstanceOf[BadToken] || head == EndToken) return this
          drop()
        }
        this
      case ObjectStartToken =>
        this.moveNext()
        while(head != ObjectEndToken) {
          if (head.isInstanceOf[BadToken] || head == EndToken) return this
          moveNext().drop()
        }
        this
      case ArrayEndToken =>
        FailedTokenizer(BadToken(UnexpectedTokenFailure("Unexpected token", ArrayEndToken, NoPath)))
      case ObjectEndToken =>
        FailedTokenizer(BadToken(UnexpectedTokenFailure("Unexpected token", ObjectEndToken, NoPath)))
      case token =>
        this
    }
}

case class FailedTokenizer(head:BadToken) extends Tokenizer {
  def moveNext() = this
}

object EndTokenizer extends Tokenizer {
  def head = EndToken
  def moveNext() = this
}
