package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.{Selector, Path}
import scala.util.Try
import scala.collection.mutable.ListBuffer

case class Tuple2MapParser[T1, T2](s1:Selector[String, T1], s2:Selector[String, T2]) extends ObjectArgumentsParser[(T1, T2)]  {

  protected val (arguments, groups) = TupleMapParser.getArgumentsAndGroup(s1, s2)
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked) => (_1, _2)
  }}

  def schema = Map(
    "type" -> "object",
    "properties" -> Map(
      s1.keys.head -> s1.parser.schema,
      s2.keys.head -> s2.parser.schema))
}

case class Tuple3MapParser[T1, T2, T3](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3]) extends ObjectArgumentsParser[(T1, T2, T3)]  {
  protected val (arguments, groups) = TupleMapParser.getArgumentsAndGroup(s1, s2, s3)
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser), NoArgFound(s3.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked) => (_1, _2, _3)
  }}

  def schema = Map(
    "type" -> "object",
    "properties" -> Map(
      s1.keys.head -> s1.parser.schema,
      s2.keys.head -> s2.parser.schema,
      s3.keys.head -> s3.parser.schema))
}

case class Tuple4MapParser[T1, T2, T3, T4](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4]) extends ObjectArgumentsParser[(T1, T2, T3, T4)]  {
  protected val (arguments, groups) = TupleMapParser.getArgumentsAndGroup(s1, s2, s3, s4)
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser), NoArgFound(s3.parser), NoArgFound(s4.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked) => (_1, _2, _3, _4)
  }}

  def schema = Map(
    "type" -> "object",
    "properties" -> Map(
      s1.keys.head -> s1.parser.schema,
      s2.keys.head -> s2.parser.schema,
      s3.keys.head -> s3.parser.schema,
      s4.keys.head -> s4.parser.schema))
}

case class Tuple5MapParser[T1, T2, T3, T4, T5](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4], s5:Selector[String, T5]) extends ObjectArgumentsParser[(T1, T2, T3, T4, T5)]  {
  protected val (arguments, groups) = TupleMapParser.getArgumentsAndGroup(s1, s2, s3, s4, s5)
  protected lazy val template: Array[Any] = Array(NoArgFound(s1.parser), NoArgFound(s2.parser), NoArgFound(s3.parser), NoArgFound(s4.parser), NoArgFound(s5.parser))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4, T5)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked, _5:T5 @unchecked) => (_1, _2, _3, _4, _5)
  }}

  def schema = Map(
    "type" -> "object",
    "properties" -> Map(
      s1.keys.head -> s1.parser.schema,
      s2.keys.head -> s2.parser.schema,
      s3.keys.head -> s3.parser.schema,
      s4.keys.head -> s4.parser.schema,
      s5.keys.head -> s5.parser.schema))
}

object TupleMapParser {
  def getArgumentsAndGroup(selectors:Selector[String, _]*) = {
    val argsBuffer = new ListBuffer[(String, (Parser[_], Int))]()
    val groupBuffer = new ListBuffer[(Int, Parser[_], Set[String])]()
    for ((selector, index) <- selectors.zipWithIndex) {
      if (selector.isGroup) groupBuffer += ((index, selector.parser, selector.keys))
      else argsBuffer ++= selector.keys.map(k => (k, (selector.parser, index)))
    }
    argsBuffer.toMap -> groupBuffer.result
  }

  def getSchema(selectors:Selector[String, _]*) = {
    val m = Map(
      "type" -> "object",
      "properties" -> selectors.map(s => s.keys.head -> s.parser.schema)
    )
    val r = selectors.filter(!_.parser.hasDefault)
    if (r.isEmpty) m
    else m + ("required" -> r.map(r => r.keys.head))

  }
}
