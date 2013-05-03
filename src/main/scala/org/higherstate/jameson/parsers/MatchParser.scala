package org.higherstate.jameson.parsers

import scala.util.{Failure, Try}
import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.exceptions.ConditionalKeyNotFoundException
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.exceptions.ConditionalKeyMatchNotFoundException

//TODO: limit to map like parsers
case class MatchParser[T, U](identifierKey:String, identifierParser:Parser[U], matchParsers:Map[U, Parser[T]]) extends Parser[T]{

  def parse(tokenizer:Tokenizer, path: Path): Try[(T, Tokenizer)] =
    findMatch(tokenizer, path, Nil).flatMap {
      case (m, _) => matchParsers.get(m).map(_.parse(tokenizer, path)).getOrElse(Failure(ConditionalKeyMatchNotFoundException(m.toString, path)))
    }

  //Returns tokenizer with match key and value removed
  private def findMatch(tokenizer:Tokenizer, path: Path, nesting:List[Char]):Try[(U, Tokenizer)] = tokenizer match {
    case End                                                                 => Failure(ConditionalKeyNotFoundException(identifierKey, path))
    case ObjectEndToken -: tail if nesting.isEmpty                           => Failure(ConditionalKeyNotFoundException(identifierKey, path))
    case (token:BadToken) -: tail                                            => Failure(UnexpectedTokenException("Bad token found", token, path))
    case (KeyToken(key)) -: tail if nesting.isEmpty && key == identifierKey  => identifierParser.parse(tail, path + key)
    case ArrayStartToken -: tail                                             => findMatch(tail, path, '[' :: nesting).map(_.mapRight(ArrayStartToken -: _))
    case ObjectStartToken -: tail                                            => findMatch(tail, path, '{' :: nesting).map(_.mapRight(ObjectStartToken -: _))
    case ArrayEndToken -: tail                                               => nesting match {
      case '[' :: nTail => findMatch(tail, path, nTail).map(_.mapRight(ArrayEndToken -: _))
      case _            => Failure(UnexpectedTokenException("Unexpected token", ArrayEndToken, path))
    }
    case ObjectEndToken -: tail                                              => nesting match {
      case '{' :: nTail => findMatch(tail, path, nTail).map(_.mapRight(ObjectEndToken -: _))
      case _            => Failure(UnexpectedTokenException("Unexpected token", ObjectEndToken, path))
    }
    case token -: tail                                                       => findMatch(tail, path, nesting).map(_.mapRight(token -: _))
  }

}