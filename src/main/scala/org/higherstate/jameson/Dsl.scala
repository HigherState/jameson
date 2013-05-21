package org.higherstate.jameson

import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import scala.util.matching.Regex
import scala.reflect.ClassTag
import org.joda.time.format.DateTimeFormatter
import org.joda.time.DateTimeZone

object Dsl {

  case class OrKeys(keys:Set[String]) extends AnyVal {
    def |(key:String) = OrKeys(keys + key)
  }

  case class AndKeys(keys:Set[String]) extends AnyVal {
    def &(key:String) = AndKeys(keys + key)
  }

  case class KeysMapped(keys:Set[String], toKey:String)

  case class SelectorInstance[U,T](keys:Set[U], parser:Parser[T]) extends Selector[U,T] {
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func))
  }
  case class KeySelectorInstance[U,T](keys:Set[U], parser:Parser[T], replaceKey:Option[U], isRequired:Boolean) extends KeySelector[U, T] {
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  //case class SelectorInstance[U,T](keys:Set[U], parser:Parser[T])

  implicit class StringTupleExtension(val self:(String, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = KeySelectorInstance(Set(self._1), parser, Some(self._2), true)

    def |>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self._1), PipeParser(AsAny, func), Some(self._2), false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self._1), PipeParser(AsAny, func), Some(self._2), true)
  }

  implicit class StringSetTupleExtension(val self:(OrKeys, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = KeySelectorInstance(self._1.keys, parser, Some(self._2), true)

    def |>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(self._1.keys, PipeParser(AsAny, func), Some(self._2), false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(self._1.keys, PipeParser(AsAny, func), Some(self._2), true)
  }

  implicit class AnyExt[U](val self:U) extends AnyVal {
    def ->>[T](parser:Parser[T]) = KeySelectorInstance(Set(self), parser, None, true)
  }

  implicit class SelectorWrapper[T](val self:(OrKeys, Parser[T])) extends AnyVal with Selector[String, T] {
    def keys = self._1.keys
    def parser = self._2
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func))
  }

  implicit class UnrequiredMultiKeySelectorWithReplaceKey[T](val self:((OrKeys, String),Parser[T])) extends AnyVal with KeySelector[String, T] {
    def keys = self._1._1.keys.map(_.toString)
    def parser = self._2
    def isRequired = false
    def replaceKey = Some(self._1._2)
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class UnrequiredKeySelectorWithReplaceKey[T](val self:((String, String),Parser[T])) extends AnyVal with KeySelector[String, T] {
    def keys = Set(self._1._1)
    def parser = self._2
    def isRequired = false
    def replaceKey = Some(self._1._2)
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class UnrequiredKeySelector[U, T](val self:(U,Parser[T])) extends AnyVal with KeySelector[U, T] {
    def keys = Set(self._1)
    def parser = self._2
    def isRequired = false
    def replaceKey = None
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class StringPipe(val self:String) extends AnyVal {
    def |>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self), PipeParser(AsAny, func), None, false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self), PipeParser(AsAny, func), None, true)

    def |(key:String) = OrKeys(Set(self, key))
    def &(key:String) = AndKeys(Set(self, key))
  }

  implicit class ParserPipe[T](val self:Parser[T]) extends AnyVal {
    def |>[V](func:T => V) = PipeParser(self, func)
  }
  implicit class Parser2Pipe[T1, T2](val self:Parser[(T1, T2)]) extends AnyVal {
    def |>[V](func:(T1, T2) => V) = Pipe2Parser(self, func)
    def |>[V](func:((T1, T2)) => V) = PipeParser(self, func)
  }
  implicit class Parser3Pipe[T1, T2, T3](val self:Parser[(T1, T2, T3)]) extends AnyVal {
    def |>[V](func:(T1, T2, T3) => V) = Pipe3Parser(self, func)
    def |>[V](func:((T1, T2, T3)) => V) = PipeParser(self, func)
  }
  implicit class Parser4Pipe[T1, T2, T3, T4](val self:Parser[(T1, T2, T3, T4)]) extends AnyVal {
    def |>[V](func:(T1, T2, T3, T4) => V) = Pipe4Parser(self, func)
    def |>[V](func:((T1, T2, T3, T4)) => V) = PipeParser(self, func)
  }
  implicit class Parser5Pipe[T1, T2, T3, T4, T5](val self:Parser[(T1, T2, T3, T4, T5)]) extends AnyVal {
    def |>[V](func:(T1, T2, T3, T4, T5) => V) = Pipe5Parser(self, func)
    def |>[V](func:((T1, T2, T3, T4, T5)) => V) = PipeParser(self, func)
  }

  case class GreaterThan[T](value:T, exclusive:Boolean)
  case class LessThan[T](value:T, exclusive:Boolean)

  def >=[T](value:T) = GreaterThan(value, false)
  def >[T](value:T) = GreaterThan(value, true)

  def <=[T](value:T) = LessThan(value, false)
  def <[T](value:T) = LessThan(value, true)

  object || {
    def apply()(implicit registry:Registry) = ListParser[Any](registry.defaultUnknownParser)
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = ListParser(registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = ListParser[T](parser)
  }

  object #* {
    def apply()(implicit registry:Registry) = MapParser[Any](registry.defaultUnknownParser)
    def apply(selectors:KeySelector[String, _]*)(implicit registry:Registry) = OpenMapParser(selectors.flatMap(s => s.keys.map(_ -> s)).toMap, registry.defaultUnknownParser)
  }

  object ¦¦ {
    def apply()(implicit registry:Registry) = TraversableOnceParser[Any](registry.defaultUnknownParser)
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = TraversableOnceParser[T](registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = TraversableOnceParser(parser)
  }

  def #!(selector:KeySelector[String, _], selectors:KeySelector[String, _]*) =
    CloseMapParser((selectors :+ selector).flatMap(s => s.keys.map(_ -> s)).toMap)

  def #^(selector:KeySelector[String, _], selectors:KeySelector[String, _]*) =
    DropMapParser((selectors :+ selector).flatMap(s => s.keys.map(_ -> s)).toMap)

  object ? {
    def apply()(implicit registry:Registry) = OptionParser(registry.defaultUnknownParser)
    def apply(orElse:Any)(implicit registry:Registry) = OrElseParser(registry.defaultUnknownParser, orElse)
    def apply[U](parser:Parser[U]) = OptionParser(parser)
    def apply[U](parser:Parser[U], orElse:U) = OrElseParser(parser, orElse)
  }

  def ><[T,U](leftParser:Parser[T], rightParser:Parser[U]) = EitherParser(leftParser, rightParser)

  //maybe defaults should be extractors...
  def /[T, U](key:String, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
    MatchParser(key, registry[T], None, selectors.flatMap(p => p.keys.map(_ -> p.parser)).toMap)

  def /[U](key:String, classes:ClassParser[U]*)(implicit registry:Registry) =
    MatchParser(key, registry[String], None, classes.map(p => p.getClassName -> p).toMap)

  def /[T, U](key:String, default:T, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
    MatchParser(key, registry[T], Some(default), selectors.flatMap(p => p.keys.map(_ -> p.parser)).toMap)

  def /[U](key:String, default:String, classes:ClassParser[U]*)(implicit registry:Registry) =
    MatchParser(key, registry[String], Some(default), classes.map(p => p.getClassName -> p).toMap)

  def /[T, U](key:String)(func:PartialFunction[T, Parser[U]])(implicit registry:Registry, typeTag:TypeTag[T]) =
    PartialParser(key, registry[T], None, func)

  def /[T, U](key:String, default:T)(func:PartialFunction[T, Parser[U]])(implicit registry:Registry, typeTag:TypeTag[T]) =
    PartialParser(key, registry[T], Some(default), func)

  def /[T](matches:(String, Parser[T])*) = KeyMatcher(matches)

  def ??[U](leftParser:Parser[U], rightParser:Parser[U]) = TryParser(leftParser, rightParser)

  def >>[T <: AnyRef](implicit registry:Registry, typeTag:TypeTag[T]) = ClassParser[T](Nil, registry)

  def >>[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = {
    ClassParser[T](selectors.toList, registry)
  }

  def T[T1,T2](p1:Parser[T1], p2:Parser[T2]) = Tuple2ListParser(p1, p2)
  def T[T1,T2,T3](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3]) = Tuple3ListParser(p1, p2, p3)
  def T[T1,T2,T3,T4](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4]) = Tuple4ListParser(p1, p2, p3, p4)
  def T[T1,T2,T3,T4,T5](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4], p5:Parser[T5]) = Tuple5ListParser(p1, p2, p3, p4, p5)

  def T[T1,T2](s1:Selector[String, T1], s2:Selector[String, T2]) = Tuple2MapParser(s1, s2)
  def T[T1,T2,T3](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3]) = Tuple3MapParser(s1, s2, s3)
  def T[T1,T2,T3, T4](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4]) = Tuple4MapParser(s1, s2, s3, s4)
  def T[T1,T2,T3, T4, T5](s1:Selector[String, T1], s2:Selector[String, T2], s3:Selector[String, T3], s4:Selector[String, T4], s5:Selector[String, T5]) = Tuple5MapParser(s1, s2, s3, s4, s5)

  def r(regex:String) = RegexValidationParser(regex.r, "Invalid string format.")
  def r(regex:String, message:String) = RegexValidationParser(regex.r, message)
  def r(regex:Regex) = RegexValidationParser(regex, "Invalid string format.")
  def r(regex:Regex, message:String) = RegexValidationParser(regex, message)

  def AsAny(implicit registry:Registry) = AnyParser(registry)
  def AsAnyVal(implicit registry:Registry) = AnyValParser(registry)
  val AsBool = BooleanParser
  val AsByte = ByteParser
  val AsChar = CharParser

  val AsDouble = DoubleParser
  def AsDouble(greaterThan:GreaterThan[Double]) = DoubleRangeParser(Some(greaterThan.value), greaterThan.exclusive, None, false)
  def AsDouble(lessThan:LessThan[Double]) = DoubleRangeParser(None, false, Some(lessThan.value), lessThan.exclusive)
  def AsDouble(greaterThan:GreaterThan[Double], lessThan:LessThan[Double]) = DoubleRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)
  def AsDouble(lessThan:LessThan[Double], greaterThan:GreaterThan[Double]) = DoubleRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)

  val AsFloat = FloatParser
  def AsFloat(greaterThan:GreaterThan[Float]) = FloatRangeParser(Some(greaterThan.value), greaterThan.exclusive, None, false)
  def AsFloat(lessThan:LessThan[Float]) = FloatRangeParser(None, false, Some(lessThan.value), lessThan.exclusive)
  def AsFloat(greaterThan:GreaterThan[Float], lessThan:LessThan[Float]) = FloatRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)
  def AsFloat(lessThan:LessThan[Float], greaterThan:GreaterThan[Float]) = FloatRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)


  val AsInt = IntParser
  def AsInt(greaterThan:GreaterThan[Int]) = IntRangeParser(Some(greaterThan.value), greaterThan.exclusive, None, false)
  def AsInt(lessThan:LessThan[Int]) = IntRangeParser(None, false, Some(lessThan.value), lessThan.exclusive)
  def AsInt(greaterThan:GreaterThan[Int], lessThan:LessThan[Int]) = IntRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)
  def AsInt(lessThan:LessThan[Int], greaterThan:GreaterThan[Int]) = IntRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)

  val AsLong = LongParser
  def AsLong(greaterThan:GreaterThan[Long]) = DoubleRangeParser(Some(greaterThan.value), greaterThan.exclusive, None, false)
  def AsLong(lessThan:LessThan[Long]) = DoubleRangeParser(None, false, Some(lessThan.value), lessThan.exclusive)
  def AsLong(greaterThan:GreaterThan[Long], lessThan:LessThan[Long]) = LongRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)
  def AsLong(lessThan:LessThan[Long], greaterThan:GreaterThan[Long]) = LongRangeParser(Some(greaterThan.value), greaterThan.exclusive, Some(lessThan.value), lessThan.exclusive)

  val AsNull = NullParser
  val AsShort = ShortParser
  val AsString = StringParser
  val AsUUID = UUIDParser
  def AsMap(implicit registry:Registry) = MapParser[Any](registry.defaultUnknownParser)
  def AsDateTime(implicit dateTimeFormatter:Option[DateTimeFormatter], dateTimeZone:DateTimeZone):DateTimeParser = DateTimeParser()(dateTimeFormatter, dateTimeZone)

  def AsAnyRef[T](implicit classTag:ClassTag[T]) = AnyRefParser[T]
}
