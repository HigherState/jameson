package org.higherstate.jameson

import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import scala.util.matching.Regex
import scala.reflect.ClassTag
import org.joda.time.format.DateTimeFormatter
import org.joda.time.DateTimeZone



object Dsl {

  case class KeyHold(keys:Set[String]) extends AnyVal {
    def |(key:String) = KeyHold(keys + key)
  }

  case class KeysMapped(keys:Set[String], toKey:String)

  case class SelectorInstance[U,T](keys:Set[U], parser:Parser[T], replaceKey:Option[U], isRequired:Boolean) extends Selector[U, T] {
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class StringTupleExtension(val self:(String, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = SelectorInstance(Set(self._1), parser, Some(self._2), true)

    def |>[V](func:Any => V)(implicit registry:Registry) = SelectorInstance(Set(self._1), PipeParser(AsAny, func), Some(self._2), false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = SelectorInstance(Set(self._1), PipeParser(AsAny, func), Some(self._2), true)
  }

  implicit class StringSetTupleExtension(val self:(KeyHold, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = SelectorInstance(self._1.keys, parser, Some(self._2), true)

    def |>[V](func:Any => V)(implicit registry:Registry) = SelectorInstance(self._1.keys, PipeParser(AsAny, func), Some(self._2), false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = SelectorInstance(self._1.keys, PipeParser(AsAny, func), Some(self._2), true)
  }

  implicit class AnyExt[U](val self:U) extends AnyVal {
    def ->>[T](parser:Parser[T]) = SelectorInstance(Set(self), parser, None, true)
  }

  implicit class UnrequiredMultiKeySelectorWithReplaceKey[T](val self:((KeyHold, String),Parser[T])) extends AnyVal with Selector[String, T] {
    def keys = self._1._1.keys.map(_.toString)
    def parser = self._2
    def isRequired = false
    def replaceKey = Some(self._1._2)
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class UnrequiredSelectorWithReplaceKey[T](val self:((String, String),Parser[T])) extends AnyVal with Selector[String, T] {
    def keys = Set(self._1._1)
    def parser = self._2
    def isRequired = false
    def replaceKey = Some(self._1._2)
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class UnrequiredSelector[U, T](val self:(U,Parser[T])) extends AnyVal with Selector[U, T] {
    def keys = Set(self._1)
    def parser = self._2
    def isRequired = false
    def replaceKey = None
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class StringPipe(val self:String) extends AnyVal {
    def |>[V](func:Any => V)(implicit registry:Registry) = SelectorInstance(Set(self), PipeParser(AsAny, func), None, false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = SelectorInstance(Set(self), PipeParser(AsAny, func), None, true)

    def |(key:String) = KeyHold(Set(self, key))
  }

  implicit class ParserPipe[T](val self:Parser[T]) extends AnyVal {
    def |>[V](func:T => V) = PipeParser(self, func)
  }

  object || {
    def apply()(implicit registry:Registry) = ListParser[Any](registry.defaultUnknownParser)
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = ListParser(registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = ListParser[T](parser)
  }

  object #* {
    def apply()(implicit registry:Registry) = MapParser[Any](registry.defaultUnknownParser)
    def apply(selectors:Selector[String, _]*)(implicit registry:Registry) = OpenMapParser(selectors.flatMap(s => s.keys.map(_ -> s)).toMap, registry.defaultUnknownParser)
  }

  object ¦¦ {
    def apply()(implicit registry:Registry) = TraversableOnceParser[Any](registry.defaultUnknownParser)
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = TraversableOnceParser[T](registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = TraversableOnceParser(parser)
  }

  def #!(selector:Selector[String, _], selectors:Selector[String, _]*) =
    CloseMapParser((selectors :+ selector).flatMap(s => s.keys.map(_ -> s)).toMap)

  def #^(selector:Selector[String, _], selectors:Selector[String, _]*) =
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

  def >>[T <: AnyRef](implicit registry:Registry, typeTag:TypeTag[T]) = ClassParser[T](Nil, registry)

  def >>[T <: AnyRef](selectors:Selector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = {
    ClassParser[T](selectors.toList, registry)
  }

  def T[T1,T2](p1:Parser[T1], p2:Parser[T2]) = Tuple2ListParser(p1, p2)
  def T[T1,T2,T3](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3]) = Tuple3ListParser(p1, p2, p3)
  def T[T1,T2,T3,T4](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4]) = Tuple4ListParser(p1, p2, p3, p4)
  def T[T1,T2,T3,T4,T5](p1:Parser[T1], p2:Parser[T2], p3:Parser[T3], p4:Parser[T4], p5:Parser[T5]) = Tuple5ListParser(p1, p2, p3, p4, p5)

  def T[T1,T2](s1:(String, Parser[T1]), s2:(String, Parser[T2])) = Tuple2MapParser(s1, s2)
  def T[T1,T2,T3](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3])) = Tuple3MapParser(s1, s2, s3)
  def T[T1,T2,T3, T4](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3]), s4:(String, Parser[T4])) = Tuple4MapParser(s1, s2, s3, s4)
  def T[T1,T2,T3, T4, T5](s1:(String, Parser[T1]), s2:(String, Parser[T2]), s3:(String, Parser[T3]), s4:(String, Parser[T4]), s5:(String, Parser[T5])) = Tuple5MapParser(s1, s2, s3, s4, s5)

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
  val AsFloat = FloatParser
  val AsInt = IntParser
  val AsLong = LongParser
  val AsNull = NullParser
  val AsShort = ShortParser
  val AsString = StringParser
  val AsUUID = UUIDParser
  def AsDateTime(implicit dateTimeFormatter:Option[DateTimeFormatter], dateTimeZone:DateTimeZone):DateTimeParser = DateTimeParser()(dateTimeFormatter, dateTimeZone)

  def AsAnyRef[T](implicit classTag:ClassTag[T]) = AnyRefParser[T]
}
