package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.{Selector, Path}
import scala.util.Try

case class Tuple2MapParser[T1, T2](s1:Selector[String, T1], s2:Selector[String, T2]) extends ObjectArgumentsParser[(T1, T2)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = s1.keys.map(_ -> (s1.parser -> 0)).toMap ++ s2.keys.map(_ -> (s2.parser -> 1))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked) => (_1, _2)
  }}
}

case class Tuple3MapParser[T1, T2, T3](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3]) extends ObjectArgumentsParser[(T1, T2, T3)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = s1.keys.map(_ -> (s1.parser -> 0)).toMap ++ s2.keys.map(_ -> (s2.parser -> 1)) ++ s3.keys.map(_ -> (s3.parser -> 2))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser), NoArgFound(s3.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked) => (_1, _2, _3)
  }}
}

case class Tuple4MapParser[T1, T2, T3, T4](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4]) extends ObjectArgumentsParser[(T1, T2, T3, T4)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] = s1.keys.map(_ -> (s1.parser -> 0)).toMap ++ s2.keys.map(_ -> (s2.parser -> 1)) ++ s3.keys.map(_ -> (s3.parser -> 2)) ++ s4.keys.map(_ -> (s4.parser -> 3))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser), NoArgFound(s3.parser), NoArgFound(s4.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked) => (_1, _2, _3, _4)
  }}
}

case class Tuple5MapParser[T1, T2, T3, T4, T5](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4], s5:Selector[String, T5]) extends ObjectArgumentsParser[(T1, T2, T3, T4, T5)]  {
  protected lazy val arguments: Map[String, (Parser[_], Int)] =
    s1.keys.map(_ -> (s1.parser -> 0)).toMap ++ s2.keys.map(_ -> (s2.parser -> 1)) ++ s3.keys.map(_ -> (s3.parser -> 2)) ++ s4.keys.map(_ -> (s4.parser -> 3)) ++ s5.keys.map(_ -> (s5.parser -> 4))
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser), NoArgFound(s3.parser), NoArgFound(s4.parser), NoArgFound(s5.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4, T5)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked, _5:T5 @unchecked) => (_1, _2, _3, _4, _5)
  }}
}
