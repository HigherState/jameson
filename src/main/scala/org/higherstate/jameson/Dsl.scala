package org.higherstate.jameson

import org.higherstate.jameson.validators._
import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import org.higherstate.jameson.tokenizers.Tokenizer
import scala.reflect.ClassTag
import scala.util.matching.Regex

object Dsl {

  case class OrKeys(keys:Set[String]) {
    def |(key:String) = OrKeys(keys + key)
    def ->(key:String)(implicit registry:Registry) = UnspecifiedKeySelector(keys, Some(key), registry.defaultUnknownParser)
    def ->[T](parser:Parser[T]) = ParserSelector(keys, parser, false, false)
  }

  case class AndKeys(keys:Set[String]) {
    def &(key:String) = AndKeys(keys + key)
    def ->(key:String) = AndKeysTo(keys, key)
    def ->[T](parser:Parser[T]) = ParserSelector(keys, parser, false, true)
  }
  case class AndKeysTo(keys:Set[String], to:String) {
    def ->[T](parser:Parser[T]) = ParserKeySelector(keys, Some(to), parser, false, true)
  }

  implicit class ImplicitString(val self:String) extends AnyVal {
    def |(key:String) = OrKeys(Set(self,key))
    def &(key:String) = AndKeys(Set(self,key))
    def ->(key:String)(implicit registry:Registry) = UnspecifiedKeySelector(Set(self), Some(key), registry.defaultUnknownParser)
    def ->[T](parser:Parser[T])(implicit registry:Registry) = ParserKeySelector(Set(self), None, parser, false, false)
    def ->[T](any:Any) = (self, any)
    def is(required:IsRequired)(implicit registry:Registry) = ParserKeySelector(Set(self), None, registry.defaultUnknownParser, true, false)
    def |>[V](func:Any => V)(implicit registry:Registry) = ParserKeySelector(Set(self), None, PipeParser(registry.defaultUnknownParser, func), false, false)
  }

  trait KeySelectorExt[+T] extends Any with KeySelector[String, T] {
    def map[V](func:T => V) = ParserKeySelector(keys, replaceKey, PipeParser(parser, func), isRequired, isGroup)
    def |>[V](func:T => V) = ParserKeySelector(keys, replaceKey, PipeParser(parser, func), isRequired, isGroup)
  }

  trait IsRequired
  case object required extends IsRequired

  trait ToFlatten
  case object flatten extends ToFlatten

  trait IsValidator
  case object email extends IsValidator
  case object nonempty extends IsValidator
  case object empty extends IsValidator
  case object excludekeys extends IsValidator

  trait AddValidator[U, +T] {
    def parser:Parser[U]
    def maxlength(length:Int) = add(MaxLength(length))
    def minlength(length:Int) = add(MinLength(length))
    def regex(regex:String) = add(RegEx(regex.r))
    def regex(regex:Regex) = add(RegEx(regex))
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

  case class UnspecifiedKeySelector[T](keys:Set[String], replaceKey:Option[String], parser:Parser[T]) extends KeySelectorExt[T] with RequirableAddValidator[T, ParserKeySelector[T]] {
    def ->[T](parser:Parser[T])(implicit registry:Registry) = ParserKeySelector(keys, replaceKey, parser, isRequired, false)
    protected def newParser(parser:Parser[T]) = ParserKeySelector(keys, replaceKey, parser, isRequired, false)
    protected def asRequired = ParserKeySelector(keys, replaceKey, parser, true, false)
    def isRequired = false
    def isGroup = false
    def isParserSpecified = false
  }

  case class ParserKeySelector[T](keys:Set[String], replaceKey:Option[String], parser:Parser[T], isRequired:Boolean, isGroup:Boolean) extends KeySelectorExt[T] with RequirableAddValidator[T, ParserKeySelector[T]] {
    protected def newParser(parser:Parser[T]) = ParserKeySelector(keys, replaceKey, parser, isRequired, isGroup)
    protected def asRequired = ParserKeySelector(keys, replaceKey, parser, true, isGroup)
    def isParserSpecified = true
  }

  case class ParserSelector[T](keys:Set[String], parser:Parser[T], isRequired:Boolean, isGroup:Boolean) extends Selector[String, T] with RequirableAddValidator[T, ParserSelector[T]] {
    protected def newParser(parser:Parser[T]) = ParserSelector(keys, parser, isRequired, isGroup)
    protected def asRequired = ParserSelector(keys, parser, true, isGroup)

    def map[V](func:T => V) = ParserSelector(keys, PipeParser(parser, func), isRequired, isGroup)
    def |>[V](func:T => V) = ParserSelector(keys, PipeParser(parser, func), isRequired, isGroup)
    def isParserSpecified = true
  }

  case class ParserWrapper[T](parser:Parser[T]) extends Parser[T] with AddValidator[T, ParserWrapper[T]] {

    protected def newParser(parser:Parser[T]) = ParserWrapper(parser)

    def map[U](func:T => U) = PipeParser(parser, func)
    def |>[U](func:T => U) = PipeParser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)

    override def default = parser.default
  }

//  implicit case class FlattenWrapper[U,T <: TraversableOnce[U], V <: TraversableOnce[T]](self:Parser[V]) extends AnyVal {
//    def to(flatten:ToFlatten) = FlattenParser(self)
//  }

  case class Tuple2Wrapper[T1,T2](parser:Parser[(T1,T2)]) extends Parser[(T1, T2)] {
    def map[U](func:(T1,T2) => U) = Pipe2Parser(parser, func)
    def map[V](func:((T1, T2)) => V) = PipeParser(parser, func)
    def |>[U](func:(T1,T2) => U) = Pipe2Parser(parser, func)
    def |>[V](func:((T1, T2)) => V) = PipeParser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }
  case class Tuple3Wrapper[T1,T2,T3](parser:Parser[(T1,T2,T3)]) extends Parser[(T1, T2, T3)] {
    def map[U](func:(T1,T2,T3) => U) = Pipe3Parser(parser, func)
    def map[V](func:((T1, T2, T3)) => V) = PipeParser(parser, func)
    def |>[U](func:(T1,T2,T3) => U) = Pipe3Parser(parser, func)
    def |>[V](func:((T1, T2, T3)) => V) = PipeParser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }
  case class Tuple4Wrapper[T1,T2,T3,T4](parser:Parser[(T1,T2,T3,T4)]) extends Parser[(T1, T2, T3, T4)] {
    def map[U](func:(T1,T2,T3,T4) => U) = Pipe4Parser(parser, func)
    def map[V](func:((T1, T2, T3, T4)) => V) = PipeParser(parser, func)
    def |>[U](func:(T1,T2,T3,T4) => U) = Pipe4Parser(parser, func)
    def |>[V](func:((T1, T2, T3, T4)) => V) = PipeParser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }
  case class Tuple5Wrapper[T1,T2,T3,T4,T5](parser:Parser[(T1,T2,T3,T4,T5)]) extends Parser[(T1, T2, T3, T4, T5)] {
    def map[U](func:(T1,T2,T3,T4,T5) => U) = Pipe5Parser(parser, func)
    def map[V](func:((T1, T2, T3, T4, T5)) => V) = PipeParser(parser, func)
    def |>[U](func:(T1,T2,T3,T4,T5) => U) = Pipe5Parser(parser, func)
    def |>[V](func:((T1, T2, T3, T4, T5)) => V) = PipeParser(parser, func)
    def parse(tokenizer:Tokenizer, path:Path) = parser.parse(tokenizer, path)
  }

  object as {
    def apply[T <: Any](implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(default[T])
    def apply[T <: AnyRef](selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(ClassParser[T](selectors.toList, registry))
    def apply[T <: AnyRef](selectorsList:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(ClassParser[T](selectorsList.toList ++ selectors.toList, registry))
    def apply[T <: AnyRef](selectorsList1:Seq[KeySelectorExt[_]], selectorsList2:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(ClassParser[T](selectorsList1.toList ++ selectorsList2.toList ++ selectors.toList, registry))
    def apply[T <: AnyRef](selectorsList1:Seq[KeySelectorExt[_]], selectorsList2:Seq[KeySelectorExt[_]], selectorsList3:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(ClassParser[T](selectorsList1.toList ++ selectorsList2.toList ++ selectorsList3.toList ++ selectors.toList, registry))
    def apply[U <: List[T],T](p:Parser[T]) = ParserWrapper(ListParser(p))
  }
  object asTuple {
    def apply[T1,T2](p1:Parser[T1], p2:Parser[T2]) =
      Tuple2Wrapper(Tuple2ListParser(p1,p2))
    def apply[T1,T2](s1:Selector[String, T1], s2:Selector[String, T2]) =
      Tuple2Wrapper(Tuple2MapParser(s1, s2))
    def apply[T1,T2](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2]) =
      Tuple2Wrapper(Tuple2ListParser(default[T1], default[T2]))
    def apply[T1,T2,T3](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3]) =
      Tuple3Wrapper(Tuple3ListParser(p1, p2, p3))
    def apply[T1,T2,T3](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3]) =
      Tuple3Wrapper(Tuple3MapParser(s1, s2, s3))
    def apply[T1,T2,T3](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3]) =
      Tuple3Wrapper(Tuple3ListParser(default[T1], default[T2], default[T3]))
    def apply[T1,T2,T3,T4](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4]) =
      Tuple4Wrapper(Tuple4ListParser(p1, p2, p3, p4))
    def apply[T1,T2,T3,T4](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4]) =
      Tuple4Wrapper(Tuple4MapParser(s1, s2, s3, s4))
    def apply[T1,T2,T3,T4](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3], t4:TypeTag[T4]) =
      Tuple4Wrapper(Tuple4ListParser(default[T1], default[T2], default[T3], default[T4]))
    def apply[T1,T2,T3,T4,T5](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4], p5:Parser[T5]) =
      Tuple5Wrapper(Tuple5ListParser(p1 ,p2, p3, p4, p5))
    def apply[T1,T2,T3,T4,T5](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4], s5:Selector[String, T5]) =
      Tuple5Wrapper(Tuple5MapParser(s1, s2, s3, s4, s5))
    def apply[T1,T2,T3,T4,T5](implicit registry:Registry, t1:TypeTag[T1], t2:TypeTag[T2], t3:TypeTag[T3], t4:TypeTag[T4], t5:TypeTag[T5]) =
      Tuple5Wrapper(Tuple5ListParser(default[T1], default[T2], default[T3], default[T4], default[T5]))
  }

  object asEither {
    def apply[T, U](implicit registry:Registry, typeTagT:TypeTag[T], typeTagU:TypeTag[U]) =
      ParserWrapper(EitherParser(default[T], default[U]))
    def apply[T, U](left:Parser[T], right:Parser[U]) =
      ParserWrapper(EitherParser(left, right))
  }

  sealed trait OptionMethods {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(OptionParser(default[T]))
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(OptionParser(ClassParser[T](selectors.toList, registry)))
    def apply[T <: AnyRef](selectorsList:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OptionParser(ClassParser[T](selectorsList.toList ++ selectors.toList, registry)))
    def apply[T <: AnyRef](selectorsList1:Seq[KeySelectorExt[_]], selectorsList2:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OptionParser(ClassParser[T](selectorsList1.toList ++ selectorsList2.toList ++ selectors.toList, registry)))
    def apply[T <: AnyRef](selectorsList1:Seq[KeySelectorExt[_]], selectorsList2:Seq[KeySelectorExt[_]], selectorsList3:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OptionParser(ClassParser[T](selectorsList1.toList ++ selectorsList2.toList ++ selectorsList3.toList ++ selectors.toList, registry)))


    def apply[T](parser:Parser[T]) =
      ParserWrapper(OptionParser(parser))
  }
  sealed trait getAsOrElseMethods {
    def apply[T <: Any](orElse:T)(implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(OrElseParser(default[T], orElse))
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(orElse:T)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OrElseParser(ClassParser[T](selectors.toList, registry), orElse))
    def apply[T <: AnyRef](selectorsList:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(orElse:T)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OrElseParser(ClassParser[T](selectorsList.toList ++ selectors.toList, registry), orElse))
    def apply[T <: AnyRef](selectorsList1:Seq[KeySelectorExt[_]], selectorsList2:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(orElse:T)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OrElseParser(ClassParser[T](selectorsList1.toList ++ selectorsList2.toList ++ selectors.toList, registry), orElse))
    def apply[T <: AnyRef](selectorsList1:Seq[KeySelectorExt[_]], selectorsList2:Seq[KeySelectorExt[_]], selectorsList3:Seq[KeySelectorExt[_]], selectors:KeySelectorExt[_]*)(orElse:T)(implicit registry:Registry, typeTag:TypeTag[T]) =
      ParserWrapper(OrElseParser(ClassParser[T](selectorsList1.toList ++ selectorsList2.toList ++ selectorsList3.toList ++ selectors.toList, registry), orElse))
    def apply[T](parser:Parser[T], orElse:T) =
      ParserWrapper(OrElseParser(parser, orElse))
  }

  object getAs extends OptionMethods
  object asOption extends OptionMethods
  object getAsOrElse extends getAsOrElseMethods

  object ? extends OptionMethods with getAsOrElseMethods

  object asList {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) = ParserWrapper(ListParser(default[T]))
    def apply[T <: Any](parser:Parser[T]) = ParserWrapper(ListParser(parser))
  }

  object asStream {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) = ParserWrapper(TraversableOnceParser(default[T]))
    def apply[T <: Any](parser:Parser[T]) = ParserWrapper(TraversableOnceParser(parser))
  }

  object asMap {
    def apply[T <: Any](implicit registry:Registry, t:TypeTag[T]) =
      ParserWrapper(MapParser(default[T]))
    def apply(selectors:KeySelector[String, _]*)(implicit registry:Registry) =
      ParserWrapper(OpenMapParser(selectors.flatMap(s => s.keys.map((_, s))).toMap, registry.defaultUnknownParser))
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

  object matchAs {
    def apply[T, U](key:String, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      MatchParser(key, registry[T], None, selectors.flatMap(p => p.keys.map(_ -> p.parser)).toMap)
    def apply[T, U](key:String, default:T, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
      MatchParser(key, registry[T], Some(default), selectors.flatMap(p => p.keys.map(_ -> p.parser)).toMap)

    def apply[T <: Any](key:String, classes:Parser[T]*)(implicit registry:Registry) =
      MatchParser(key, registry[String], None, classes.map(p => (getClassName(p), p)).toMap)
    def apply[T <: Any](key:String, default:String, classes:Parser[T]*)(implicit registry:Registry) =
      MatchParser(key, registry[String], Some(default), classes.map(p => (getClassName(p), p)).toMap)

    def apply[T](matches:Selector[String, T]*) =
      KeyMatcher(matches.flatMap(s => s.keys.map((_, s.parser))))

    def apply[T, U](key:String)(func:PartialFunction[T, Parser[U]])(implicit registry:Registry, typeTag:TypeTag[T]) =
      PartialParser(key, registry[T], None, func)
    def apply[T, U](key:String, default:T)(func:PartialFunction[T, Parser[U]])(implicit registry:Registry, typeTag:TypeTag[T]) =
      PartialParser(key, registry[T], Some(default), func)
  }


  object path {
    def /(key:String) = ObjectPathHolder(key, None)
    def /(index:Int) = ArrayIndexHolder(index, None)
  }

  sealed trait PathHolder {
    def ->[T](parser:Parser[T]):Parser[T]
    def /(key:String) = ObjectPathHolder(key, Some(this))
    def /(index:Int) = ArrayIndexHolder(index, Some(this))
  }

  case class ObjectPathHolder(pathKey:String, holder:Option[PathHolder]) extends PathHolder {
    def ->[T](parser:Parser[T]):Parser[T] =
      holder.map(_.->(ObjectPathParser(pathKey, parser))).getOrElse(ObjectPathParser(pathKey, parser))
  }

  case class ArrayIndexHolder(index:Int, holder:Option[PathHolder]) extends PathHolder {
    def ->[T](parser:Parser[T]):Parser[T] =
      holder.map(_.->(ArrayIndexParser(index, parser))).getOrElse(ArrayIndexParser(index, parser))
  }

  object nestedAs {
    def apply[T](implicit classTag:ClassTag[T]) = AnyRefParser[T]
  }

  private def default[T](implicit registry:Registry, t:TypeTag[T]) = registry.get[T].getOrElse(ClassParser[T](Nil, registry))


  private def getClassName[T](parser:Parser[T]):String = parser match {
    case c:ClassParser[_]     => c.getClassName
    case p:ParserWrapper[_]   => getClassName(p.parser)
    case p:PipeParser[_, _]   => getClassName(p.parser)
    case v:ValidatorParser[_] => getClassName(v.parser)
    case _                    => throw new Exception("Only class parser can be matched with out a key value")
  }
}
