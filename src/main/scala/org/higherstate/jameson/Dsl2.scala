package org.higherstate.jameson

import org.higherstate.jameson.validators._
import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import org.higherstate.jameson.tokenizers.Tokenizer

object Dsl2 {

  case class OrKeys(keys:Set[String]) extends AnyVal {
    def |(key:String) = OrKeys(keys + key)
    def ->(key:String)(implicit registry:Registry) = UnknownKeySelector(keys, Some(key), registry.defaultUnknownParser)
  }

  implicit class ImplicitString(val self:String) extends AnyVal {
    def |(key:String) = OrKeys(Set(self,key))
    def ->(key:String)(implicit registry:Registry) = UnknownKeySelector(Set(self), None, registry.defaultUnknownParser)
    def ->[T](parser:Parser[T])(implicit registry:Registry) = ParserKeySelector(Set(self), None, parser, false)

    def is(required:IsRequired)(implicit registry:Registry) = ParserKeySelector(Set(self), None, registry.defaultUnknownParser, true)
  }

//  implicit class ImplicitOrKeysString(val self:(OrKeys,String)) extends AnyVal {
//    def ->[T](parser:Parser[T])(implicit registry:Registry) = ParserKeySelector(self._1.keys, Some(self._2), parser, false)
//    def is(required:IsRequired)(implicit registry:Registry) = ParserKeySelector(self._1.keys, Some(self._2), registry.defaultUnknownParser, true)
//  }


  trait KeySelectorExt[+T] extends Any with KeySelector[String, T] {
    def map[V](func:T => V) = ParserKeySelector(keys, replaceKey, PipeParser(parser, func), isRequired)
  }

  trait IsRequired
  case object required extends IsRequired

  trait IsValidator
  case object email extends IsValidator
  case object nonempty extends IsValidator
  case object empty extends IsValidator
  case object excludekeys extends IsValidator

  trait AddValidator[U, T] {
    def parser:Parser[U]
    def maxlength(length:Int) = add(MaxLength(length))
    def minlength(length:Int) = add(MinLength(length))
    def regex(regex:String) = add(RegEx(regex.r))
    def >=(compare:Number) = add(GreaterThanEquals(compare))
    def >(compare:Number) = add(GreaterThan(compare))
    def <=(compare:Number) = add(LessThanEquals(compare))
    def <(compare:Number) = add(LessThan(compare))

    def is(validator:IsValidator):T = validator match {
      case `email`        => add(IsEmail)
      case `nonempty`     => add(MinLength(1))
      case `empty`        => add(MaxLength(0))
      case `excludekeys`  => newParser(swapOutOpenMap(parser))
    }

    private def swapOutOpenMap[T](parser:Parser[T]):Parser[T] = parser match {
      case OpenMapParser(s,df)  => DropMapParser(s).asInstanceOf[Parser[T]]
      case ValidatorParser(p,v) => ValidatorParser(swapOutOpenMap(p), v)
      case ParserWrapper(p)     => ParserWrapper(swapOutOpenMap(p))
      case _                    => throw new Exception("Can only use validation excludekeys on a map parser")
    }

    private def add(validator:Validator) = parser match {
      case ValidatorParser(p, v) => newParser(ValidatorParser(p, validator :: v))
      case parser                => newParser(ValidatorParser(parser, List(validator)))
    }
    protected def newParser(parser:Parser[U]):T

  }

  trait RequirableAddValidator[U, T] extends AddValidator[U, T] {
    def is(required:IsRequired) = asRequired
    protected def asRequired:T
  }

  case class UnknownKeySelector[T](keys:Set[String], replaceKey:Option[String], parser:Parser[T]) extends KeySelectorExt[T] with RequirableAddValidator[T, ParserKeySelector[T]] {
    def ->[T](parser:Parser[T])(implicit registry:Registry) = ParserKeySelector(keys, replaceKey, parser, isRequired)
    protected def newParser(parser:Parser[T]) = ParserKeySelector(keys, replaceKey, parser, isRequired)
    protected def asRequired = ParserKeySelector(keys, replaceKey, parser, true)
    def isRequired = false
  }


  case class ParserKeySelector[T](keys:Set[String], replaceKey:Option[String], parser:Parser[T], isRequired:Boolean) extends KeySelectorExt[T] with RequirableAddValidator[T, ParserKeySelector[T]] {
    protected def newParser(parser:Parser[T]) = ParserKeySelector(keys, replaceKey, parser, isRequired)
    protected def asRequired = ParserKeySelector(keys, replaceKey, parser, true)
  }

  case class ParserWrapper[T](parser:Parser[T]) extends Parser[T] with AddValidator[T, ParserWrapper[T]] {

    protected def newParser(parser:Parser[T]) = ParserWrapper(parser)
    def map[U](func:T => U) = PipeParser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }

  case class Tuple2Wrapper[T1,T2](parser:Parser[(T1,T2)]) extends Parser[(T1, T2)] {
    def map[U](func:(T1,T2) => U) = Pipe2Parser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }
  case class Tuple3Wrapper[T1,T2,T3](parser:Parser[(T1,T2,T3)]) extends Parser[(T1, T2, T3)] {
    def map[U](func:(T1,T2,T3) => U) = Pipe3Parser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }
  case class Tuple4Wrapper[T1,T2,T3,T4](parser:Parser[(T1,T2,T3,T4)]) extends Parser[(T1, T2, T3, T4)] {
    def map[U](func:(T1,T2,T3,T4) => U) = Pipe4Parser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }
  case class Tuple5Wrapper[T1,T2,T3,T4,T5](parser:Parser[(T1,T2,T3,T4,T5)]) extends Parser[(T1, T2, T3, T4, T5)] {
    def map[U](func:(T1,T2,T3,T4,T5) => U) = Pipe5Parser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }

  object as {
    def apply[T <: Any](implicit registry:Registry, typeTag:TypeTag[T]) = ParserWrapper(default[T])
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = ParserWrapper(ClassParser[T](selectors.toList, registry))
    def apply[U <: List[T],T](p:Parser[T]) = ParserWrapper(ListParser(p))
    def apply[T1,T2](p1:Parser[T1], p2:Parser[T2]) =
      Tuple2Wrapper(Tuple2ListParser(p1,p2))
    def apply[T1,T2](s1:KeySelector[String, T1], s2:KeySelector[String, T2]) =
      Tuple2Wrapper(Tuple2MapParser(s1, s2))
    def apply[T1,T2](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2]) =
      Tuple2Wrapper(Tuple2ListParser(default[T1], default[T2]))
    def apply[T1,T2,T3](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3]) =
      Tuple3Wrapper(Tuple3ListParser(p1, p2, p3))
    def apply[T1,T2,T3](s1:KeySelector[String, T1], s2:KeySelector[String, T2], s3:KeySelector[String, T3]) =
      Tuple3Wrapper(Tuple3MapParser(s1, s2, s3))
    def apply[T1,T2,T3](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3]) =
      Tuple3Wrapper(Tuple3ListParser(default[T1], default[T2], default[T3]))
    def apply[T1,T2,T3,T4](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4]) =
      Tuple4Wrapper(Tuple4ListParser(p1, p2, p3, p4))
    def apply[T1,T2,T3,T4](s1:KeySelector[String, T1], s2:KeySelector[String, T2], s3:KeySelector[String, T3], s4:KeySelector[String, T4]) =
      Tuple4Wrapper(Tuple4MapParser(s1, s2, s3, s4))
    def apply[T1,T2,T3,T4](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3], t4:TypeTag[T4]) =
      Tuple4Wrapper(Tuple4ListParser(default[T1], default[T2], default[T3], default[T4]))
    def apply[T1,T2,T3,T4,T5](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4], p5:Parser[T5]) =
      Tuple5Wrapper(Tuple5ListParser(p1 ,p2, p3, p4, p5))
    def apply[T1,T2,T3,T4,T5](s1:KeySelector[String, T1], s2:KeySelector[String, T2], s3:KeySelector[String, T3], s4:KeySelector[String, T4], s5:KeySelector[String, T5]) =
      Tuple5Wrapper(Tuple5MapParser(s1, s2, s3, s4, s5))
    def apply[T1,T2,T3,T4,T5](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3], t4:TypeTag[T4], t5:TypeTag[T5]) =
      Tuple5Wrapper(Tuple5ListParser(default[T1], default[T2], default[T3], default[T4], default[T5]))
  }

  object asEither {
    def apply[T, U](implicit registry:Registry, typeTagT:TypeTag[T], typeTagU:TypeTag[U]) =
      ParserWrapper(EitherParser(default[T], default[T]))
    def apply[T, U](left:Parser[T], right:Parser[U]) =
      ParserWrapper(EitherParser(left, right))
  }

  object tryAs {
    def apply[T](parsers:Parser[T]*) = TryParser(parsers)
    def apply[T1,T2](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2]) =
      TryParser(Seq(default[T1], default[T2]))
    def apply[T1,T2,T3](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3]) =
      TryParser(Seq(default[T1], default[T2], default[T3]))
    def apply[T1,T2,T3,T4](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3], t4:TypeTag[T4]) =
      TryParser(Seq(default[T1], default[T2], default[T3], default[T4]))
    def apply[T1,T2,T3,T4,T5](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3], t4:TypeTag[T4], t5:TypeTag[T5]) =
      TryParser(Seq(default[T1], default[T2], default[T3], default[T4], default[T5]))
  }

  object getAs {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(OptionParser(default[T]))
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(OptionParser(ClassParser[T](selectors.toList, registry)))
    def apply[T](parser:Parser[T]) =
      ParserWrapper(OptionParser(parser))
  }

  object getAsOrElse {
    def apply[T <: Any](orElse:T)(implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(OrElseParser(default[T], orElse))
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(orElse:T)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OrElseParser(ClassParser[T](selectors.toList, registry), orElse))
    def apply[T](parser:Parser[T], orElse:T) =
      ParserWrapper(OrElseParser(parser, orElse))
  }

  object asList {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) = ParserWrapper(ListParser(default[T]))
    def apply[T <: Any](parser:Parser[T]) = ParserWrapper(ListParser(parser))
  }

  object asMap {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(MapParser(default[T]))
    def apply(selectors:KeySelector[String, _]*)(implicit registry:Registry) =
      ParserWrapper(OpenMapParser(selectors.flatMap(s => s.keys.map((_, s))).toMap, registry.defaultUnknownParser))
  }

  private def default[T](implicit registry:Registry, t:TypeTag[T]) = registry.get[T].getOrElse(ClassParser[T](Nil, registry))
}
