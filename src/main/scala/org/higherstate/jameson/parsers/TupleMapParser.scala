package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try

case class Tuple2MapParser[T1, T2](s1:(String, Parser[T1]), s2:(String, Parser[T2])) extends ObjectArgumentsParser[(T1, T2)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked) => (_1, _2)
  }}
}

case class Tuple3MapParser[T1, T2, T3](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3])) extends ObjectArgumentsParser[(T1, T2, T3)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1), s3.mapRight(_ -> 2))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2), NoArgFound(s3._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked) => (_1, _2, _3)
  }}
}

case class Tuple4MapParser[T1, T2, T3, T4](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3]), s4:(String, Parser[T4])) extends ObjectArgumentsParser[(T1, T2, T3, T4)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1), s3.mapRight(_ -> 2), s4.mapRight(_ -> 3))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2), NoArgFound(s3._2), NoArgFound(s4._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked) => (_1, _2, _3, _4)
  }}
}

case class Tuple5MapParser[T1, T2, T3, T4, T5](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3]), s4:(String, Parser[T4]), s5:(String, Parser[T5])) extends ObjectArgumentsParser[(T1, T2, T3, T4, T5)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1), s3.mapRight(_ -> 2), s4.mapRight(_ -> 3), s5.mapRight(_ -> 4))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2), NoArgFound(s3._2), NoArgFound(s4._2), NoArgFound(s5._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4, T5)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked, _5:T5 @unchecked) => (_1, _2, _3, _4, _5)
  }}
}

