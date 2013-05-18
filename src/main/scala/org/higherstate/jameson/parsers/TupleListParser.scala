package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try

case class Tuple2ListParser[T1,T2](p1:Parser[T1], p2:Parser[T2]) extends ListArgumentParser[(T1, T2)] {
  protected lazy val parsers = List(p1, p2)

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2)] = getArgs(tokenizer, path).map { x => (x: @unchecked) match {
    case (_1:T1 @unchecked) :: (_2:T2 @unchecked) :: Nil  => (_1, _2)
  }}
}

case class Tuple3ListParser[T1, T2, T3](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3]) extends ListArgumentParser[(T1, T2, T3)] {
  protected lazy val parsers = List(p1, p2, p3)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3)] = getArgs(tokenizer, path).map { x => (x: @unchecked) match {
    case (_1:T1 @unchecked) :: (_2:T2 @unchecked) :: (_3:T3 @unchecked) :: Nil  => (_1, _2, _3)
  }}
}

case class Tuple4ListParser[T1, T2, T3, T4](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4]) extends ListArgumentParser[(T1, T2, T3, T4)] {
  protected lazy val parsers = List(p1, p2, p3, p4)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4)] = getArgs(tokenizer, path).map { x => (x: @unchecked) match {
    case (_1:T1 @unchecked) :: (_2:T2 @unchecked) :: (_3:T3 @unchecked) :: (_4:T4 @unchecked) :: Nil  => (_1, _2, _3, _4)
  }}
}

case class Tuple5ListParser[T1, T2, T3, T4, T5](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4], p5:Parser[T5]) extends ListArgumentParser[(T1, T2, T3, T4, T5)] {
  protected lazy val parsers = List(p1, p2, p3, p4, p5)
  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4, T5)] = getArgs(tokenizer, path).map { x => (x: @unchecked) match {
    case (_1:T1 @unchecked) :: (_2:T2 @unchecked) :: (_3:T3 @unchecked) :: (_4:T4 @unchecked) :: (_5:T5 @unchecked) :: Nil  => (_1, _2, _3, _4, _5)
  }}
}
