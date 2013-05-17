package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.tokenizers.{ObjectStartToken, ObjectEndToken, KeyToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.{Failure, Success, Try}
import org.higherstate.jameson.exceptions.{TupleKeysNotFoundException, InvalidTokenException}

sealed trait TupleMapParser[U] extends Parser[U] {
  protected def arguments:Map[String, (Parser[_], Int)]
  protected def template:Array[Any]

  protected def getArgs(tokenizer:Tokenizer, path:Path): Try[Array[Any]] = tokenizer.head match {
    case ObjectStartToken => buildArgs(tokenizer.moveNext(), path).flatMap { args =>
      if (!args.exists(_ == NoArgFound)) Success(args)
      else Failure(TupleKeysNotFoundException(this, args.zipWithIndex.filter(_._1 == NoArgFound).flatMap(p => arguments.find(a => a._2._2 == p._2).map(_._1)).toList, path))
    }
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }

  private def buildArgs(tokenizer:Tokenizer, path:Path): Try[Array[Any]] =
    tokenizer.head match {
      case KeyToken(key)  => arguments.get(key).map(p => p._1.parse(tokenizer.moveNext(), path + key).flatMap { r =>
        buildArgs(tokenizer.moveNext, path).map { args =>
          args(p._2) = r
          args
        }
      }).getOrElse(buildArgs(tokenizer.dropNext(), path))
      case ObjectEndToken => Success(template.clone)
      case token          => Failure(InvalidTokenException(this, "Expected key or Object end token", token, path))
    }
}

case class Tuple2MapParser[T1, T2](s1:(String, Parser[T1]), s2:(String, Parser[T2])) extends TupleMapParser[(T1, T2)]  {
  protected def arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1))
  protected def template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked) => (_1, _2)
  }}
}

case class Tuple3MapParser[T1, T2, T3](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3])) extends TupleMapParser[(T1, T2, T3)]  {
  protected def arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1), s3.mapRight(_ -> 2))
  protected def template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2), NoArgFound(s3._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked) => (_1, _2, _3)
  }}
}

case class Tuple4MapParser[T1, T2, T3, T4](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3]), s4:(String, Parser[T4])) extends TupleMapParser[(T1, T2, T3, T4)]  {
  protected def arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1), s3.mapRight(_ -> 2), s4.mapRight(_ -> 3))
  protected def template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2), NoArgFound(s3._2), NoArgFound(s4._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked) => (_1, _2, _3, _4)
  }}
}

case class Tuple5MapParser[T1, T2, T3, T4, T5](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3]), s4:(String, Parser[T4]), s5:(String, Parser[T5])) extends TupleMapParser[(T1, T2, T3, T4, T5)]  {
  protected def arguments: Map[String, (Parser[_], Int)] = Map(s1.mapRight(_ -> 0), s2.mapRight(_ -> 1), s3.mapRight(_ -> 2), s4.mapRight(_ -> 3), s5.mapRight(_ -> 4))
  protected def template: Array[Any] = Array(NoArgFound(s1._2), NoArgFound(s2._2), NoArgFound(s3._2), NoArgFound(s4._2), NoArgFound(s5._2))

  def parse(tokenizer: Tokenizer, path: Path): Try[(T1, T2, T3, T4, T5)] = getArgs(tokenizer, path).map{ x => (x: @unchecked) match {
    case Array(_1:T1 @unchecked, _2:T2 @unchecked, _3:T3 @unchecked, _4:T4 @unchecked, _5:T5 @unchecked) => (_1, _2, _3, _4, _5)
  }}
}

