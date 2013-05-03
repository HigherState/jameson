package org.higherstate.jameson.parsers

import util.{Failure, Success, Try}
import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.UnexpectedTokenException

case class ListParser[T](parser:Parser[T]) extends Parser[List[T]] {

  def parse(tokenizer:Tokenizer, path: Path): Try[(List[T], Tokenizer)] = tokenizer match {
    case ArrayStartToken -: tail => append(tail, path, 0)
    case token  -: tail          => Failure(UnexpectedTokenException("Expected array start token", token, path))
  }

  protected def append(tokenizer:Tokenizer, path:Path, index:Int):Try[(List[T], Tokenizer)] = tokenizer match {
    case ArrayEndToken -: tail => Success(Nil -> tail)
    case tokenizer             => parser.parse(tokenizer, path + index).flatMap {
      case (result, tail) => append(tail, path, index + 1).map(_.mapLeft(result :: _))
    }
  }
}

