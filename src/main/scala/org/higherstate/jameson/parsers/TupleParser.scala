package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.{ArrayEndToken, ArrayStartToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.InvalidTokenException

trait TupleParser[U] extends Parser[U] {
  protected def parsers:List[Parser[_]]

  protected def getArgs(tokenizer:Tokenizer, path:Path) = tokenizer.head match {
    case ArrayStartToken => buildArgs(parsers, 0, tokenizer.moveNext(), path)
    case token           => Failure(InvalidTokenException(this, "Expected array start token", token, path))
  }

  private def buildArgs(p:List[Parser[_]], index:Int, tokenizer:Tokenizer, path:Path):Try[List[Any]] = (tokenizer.head, p) match {
    case (ArrayEndToken, Nil)                             => Success(Nil)
    case (ArrayEndToken, (head:HasDefault[Any]) :: tail)  => buildArgs(tail, index, tokenizer, path).map(head.default :: _)
    case (ArrayEndToken, _)                               => Failure(InvalidTokenException(this, "Insufficient arguments for tuple", ArrayEndToken, path))
    case (token, Nil)                                     => Failure(InvalidTokenException(this, "Expected array end token", token, path + index))
    case (_, head :: tail)                                => head.parse(tokenizer, path + index).flatMap(h => buildArgs(tail, index + 1, tokenizer.moveNext(), path).map(t => h :: t))
  }

}

case class Tuple2ListParser[T1,T2](p1:Parser[T1], p2:Parser[T2]) extends TupleParser[(T1, T2)] {
  lazy val parsers = List(p1, p2)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2)] = getArgs(tokenizer, path).map {
    case _1 :: _2 :: Nil  => (_1.asInstanceOf[T1], _2.asInstanceOf[T2])
  }
}

case class Tuple3ListParser[T1, T2, T3](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3]) extends TupleParser[(T1, T2, T3)] {
  lazy val parsers = List(p1, p2, p3)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3)] = getArgs(tokenizer, path).map {
    case _1 :: _2 :: _3 :: Nil  => (_1.asInstanceOf[T1], _2.asInstanceOf[T2], _3.asInstanceOf[T3])
  }
}

case class Tuple4ListParser[T1, T2, T3, T4](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4]) extends TupleParser[(T1, T2, T3, T4)] {
  lazy val parsers = List(p1, p2, p3, p4)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4)] = getArgs(tokenizer, path).map {
    case _1 :: _2 :: _3 :: _4:: Nil  => (_1.asInstanceOf[T1], _2.asInstanceOf[T2], _3.asInstanceOf[T3], _4.asInstanceOf[T4])
  }
}

case class Tuple5ListParser[T1, T2, T3, T4, T5](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4], p5:Parser[T5]) extends TupleParser[(T1, T2, T3, T4, T5)] {
  lazy val parsers = List(p1, p2, p3, p4, p5)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4, T5)] = getArgs(tokenizer, path).map {
    case _1 :: _2 :: _3 :: _4:: _5 :: Nil  => (_1.asInstanceOf[T1], _2.asInstanceOf[T2], _3.asInstanceOf[T3], _4.asInstanceOf[T4], _5.asInstanceOf[T5])
  }
}
