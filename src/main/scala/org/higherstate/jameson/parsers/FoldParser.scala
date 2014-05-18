package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.{ArrayEndToken, ArrayStartToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._
import scala.annotation.tailrec

case class FoldParser[T, U](parser:Parser[T], initial:U, func:(U,T) => U) extends Parser[U] {

  def parse(tokenizer:Tokenizer, path: Path): Valid[U] = tokenizer.head match {
    case ArrayStartToken =>
      foldLeft(tokenizer.moveNext(), path, 0, initial)
    case token =>
      Failure(InvalidTokenFailure(this, "Expected array start token", token, path))
  }

  @tailrec private def foldLeft(tokenizer:Tokenizer, path:Path, index:Int, accumulator:U):Valid[U] = tokenizer.head match {
    case ArrayEndToken =>
      Success(accumulator)
    case _             =>
      parser.parse(tokenizer, path + index).map(func(accumulator, _)) match {
        case Failure(f) =>  //collect any other failures
          ListParser(parser).parse(tokenizer.moveNext(), path) match {
            case Failure(f2) =>
              Failure(f.append(f2))
            case _ =>
              Failure(f)
          }
        case Success(value) =>
          foldLeft(tokenizer.moveNext(), path, index + 1, value)
      }
  }

  override def default:Option[U] = Some(initial)

  def schema = Map("type" -> "array", "items" -> parser.schema)
}
