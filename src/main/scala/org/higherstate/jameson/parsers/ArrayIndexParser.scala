package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._
import org.higherstate.jameson.tokenizers.BadToken
import scala.annotation.tailrec

/**
 * Parse the value at the specified index.
 * @param index
 * @param parser
 * @tparam T
 */
case class ArrayIndexParser[T](index:Int, parser:Parser[T]) extends Parser[T] {

  //All failures at the level would be interrupt
  def parse(tokenizer: Tokenizer, path: Path): Valid[T] =
    tokenizer.head match {
      case ArrayStartToken  =>
        iterate(tokenizer.moveNext(), path, index)
      case token            =>
        Failure(InvalidTokenFailure(this, "Expected object start token", token, path))
    }

  @tailrec
  private def iterate(tokenizer:Tokenizer, path: Path, index:Int):Valid[T] =
    tokenizer.head match {
      case ArrayEndToken =>
        parser.default.fold[Valid[T]](Failure(ArrayIndexOutOfBoundsFailure(parser, this.index, path)))(Success(_))
      case token if index == 0 =>
        parser.parse(tokenizer, path + (this.index - index)).flatMap(value => iterateRemaining(tokenizer.moveNext(), path, value))
      case ObjectEndToken =>
        Failure(UnexpectedTokenFailure("Unexpected token.", ObjectEndToken, path))
      case token:BadToken =>
        Failure(UnexpectedTokenFailure("Unexpected token.", token, path))
      case EndToken =>
        Failure(UnexpectedTokenFailure("Unexpected token.", EndToken, path))
      case _   =>
        iterate(tokenizer.drop(), path, index - 1)
    }

  @tailrec
  private def iterateRemaining(tokenizer:Tokenizer, path:Path, value:T):Valid[T] =
    tokenizer.head match {
      case ArrayEndToken =>
        Success(value)
      case EndToken =>
        Failure(UnexpectedTokenFailure("Unexpected token.", EndToken, path))
      case token:BadToken =>
        Failure(UnexpectedTokenFailure("Unexpected token.", token, path))
      case ObjectEndToken =>
        Failure(UnexpectedTokenFailure("Unexpected token.", ObjectEndToken, path))
      case token =>
        iterateRemaining(tokenizer.drop(), path, value)
    }

  override def default:Option[T] = parser.default

  def schema = Map("type" -> "array", "items" -> (0 until index).foldLeft(List(parser.schema))((a, i) =>  Map.empty[String, Any] :: a))
}
