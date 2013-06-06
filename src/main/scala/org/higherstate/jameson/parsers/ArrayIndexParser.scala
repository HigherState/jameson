package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, ArrayIndexOutOfBoundsException, InvalidTokenException}
import org.higherstate.jameson.tokenizers.BadToken

case class ArrayIndexParser[T](index:Int, parser:Parser[T]) extends Parser[T] {

  def parse(tokenizer: Tokenizer, path: Path): Try[T] = tokenizer.head match {
    case ArrayStartToken  => iterate(tokenizer.moveNext(), path, index)
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }

  private def iterate(tokenizer:Tokenizer, path: Path, index:Int):Try[T] = tokenizer.head match {
    case ArrayEndToken        => parser.default.map(Success(_)).getOrElse(Failure(ArrayIndexOutOfBoundsException(parser, this.index, path)))
    case token if index == 0  => parser.parse(tokenizer, path + (this.index - index)).flatMap(value => iterateRemaining(tokenizer.moveNext, path, value))
    case ObjectEndToken       => Failure(UnexpectedTokenException("Unexpected token.", ObjectEndToken, path))
    case token:BadToken       => Failure(UnexpectedTokenException("Unexpected token.", token, path))
    case EndToken             => Failure(UnexpectedTokenException("Unexpected token.", EndToken, path))
    case _                    => iterate(tokenizer.drop(), path, index - 1)
  }

  private def iterateRemaining(tokenizer:Tokenizer, path:Path, value:T):Try[T] = tokenizer.head match {
    case ArrayEndToken  => Success(value)
    case EndToken       => Failure(UnexpectedTokenException("Unexpected token.", EndToken, path))
    case token:BadToken => Failure(UnexpectedTokenException("Unexpected token.", token, path))
    case ObjectEndToken => Failure(UnexpectedTokenException("Unexpected token.", ObjectEndToken, path))
    case token          => iterateRemaining(tokenizer.drop(), path, value)
  }

  override def default:Option[T] = parser.default
}
