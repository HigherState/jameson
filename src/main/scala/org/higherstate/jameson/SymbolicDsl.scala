package org.higherstate.jameson

import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import scala.util.matching.Regex
import scala.reflect.ClassTag
import org.joda.time.format.DateTimeFormatter
import org.joda.time.DateTimeZone
import org.higherstate.jameson.extractors.{LongRangeExtractor, DoubleRangeExtractor}

object SymbolicDsl {

  case class OrKeys(keys:Set[String]) extends AnyVal {
    def |(key:String) = OrKeys(keys + key)
  }

  case class AndKeys(keys:Set[String]) extends AnyVal {
    def &(key:String) = AndKeys(keys + key)
  }

  case class KeysMapped(keys:Set[String], toKey:String)

  case class SelectorInstance[U,T](keys:Set[U], parser:Parser[T]) extends Selector[U,T] {
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func))
    def isGroup = false
  }
  case class KeySelectorInstance[U,T](keys:Set[U], parser:Parser[T], replaceKey:Option[U], isRequired:Boolean) extends KeySelector[U, T] {
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
    def isGroup = false
    def isParserSpecified = false
  }

  //case class SelectorInstance[U,T](keys:Set[U], parser:Parser[T])

  implicit class StringTupleExtension(val self:(String, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = KeySelectorInstance(Set(self._1), parser, Some(self._2), true)

    def |>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self._1), PipeParser(registry.defaultUnknownParser, func), Some(self._2), false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self._1), PipeParser(registry.defaultUnknownParser, func), Some(self._2), true)
  }

  implicit class StringSetTupleExtension(val self:(OrKeys, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = KeySelectorInstance(self._1.keys, parser, Some(self._2), true)

    def |>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(self._1.keys, PipeParser(registry.defaultUnknownParser, func), Some(self._2), false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(self._1.keys, PipeParser(registry.defaultUnknownParser, func), Some(self._2), true)
  }

  implicit class AnyExt[U](val self:U) extends AnyVal {
    def ->>[T](parser:Parser[T]) = KeySelectorInstance(Set(self), parser, None, true)
  }

  implicit class SelectorWrapper[T](val self:(OrKeys, Parser[T])) extends AnyVal with Selector[String, T] {
    def keys = self._1.keys
    def parser = self._2
    def |>[V](func:T => V) = SelectorInstance(keys, PipeParser(parser, func))
    def isGroup = false
  }

  implicit class UnrequiredMultiKeySelectorWithReplaceKey[T](val self:((OrKeys, String),Parser[T])) extends AnyVal with KeySelector[String, T] {
    def keys = self._1._1.keys.map(_.toString)
    def parser = self._2
    def isRequired = false
    def isParserSpecified = false
    def isGroup = false
    def replaceKey = Some(self._1._2)
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class UnrequiredKeySelectorWithReplaceKey[T](val self:((String, String),Parser[T])) extends AnyVal with KeySelector[String, T] {
    def keys = Set(self._1._1)
    def parser = self._2
    def isRequired = false
    def isParserSpecified = false
    def isGroup = false
    def replaceKey = Some(self._1._2)
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class UnrequiredKeySelector[U, T](val self:(U,Parser[T])) extends AnyVal with KeySelector[U, T] {
    def keys = Set(self._1)
    def parser = self._2
    def isRequired = false
    def isParserSpecified = false
    def isGroup = false
    def replaceKey = None
    def |>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, isRequired)
    def |>>[V](func:T => V) = KeySelectorInstance(keys, PipeParser(parser, func), replaceKey, true)
  }

  implicit class StringPipe(val self:String) extends AnyVal {
    def |>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self), PipeParser(registry.defaultUnknownParser, func), None, false)
    def |>>[V](func:Any => V)(implicit registry:Registry) = KeySelectorInstance(Set(self), PipeParser(registry.defaultUnknownParser, func), None, true)

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
    def apply[T](parser:Parser[T]) = OptionParser(parser)
    def apply[T](parser:Parser[T], default:T) = OrElseParser(parser, default)

    def apply[T <: Any](implicit registry:Registry, typeTag:TypeTag[T]) = OptionParser(registry.get[T].getOrElse(ClassParser[T](Nil, registry)))
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = OptionParser(ClassParser[T](selectors.toList, registry))
    def apply[T <: Any](default:T)(implicit registry:Registry, typeTag:TypeTag[T]) = OrElseParser(registry.get[T].getOrElse(ClassParser[T](Nil, registry)), default)
    def apply[T <: AnyRef](selectors:KeySelector[String, _]*)(default:T)(implicit registry:Registry, typeTag:TypeTag[T]) = OrElseParser(ClassParser[T](selectors.toList, registry), default)
  }

  def ^[T,U](leftParser:Parser[T], rightParser:Parser[U]) = EitherParser(leftParser, rightParser)

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

  def ??[U](parsers:Parser[U]*) = TryParser(parsers)


  def as[T <: Any](implicit registry:Registry, typeTag:TypeTag[T]) = registry.get[T].getOrElse(ClassParser[T](Nil, registry))
  def as[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = ClassParser[T](selectors.toList, registry)

  def >>[T <: Any](implicit registry:Registry, typeTag:TypeTag[T]) = registry.get[T].getOrElse(ClassParser[T](Nil, registry))
  def >>[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = ClassParser[T](selectors.toList, registry)



  def getAs[T <: Any](implicit registry:Registry, typeTag:TypeTag[T]) = OptionParser(registry.get[T].getOrElse(ClassParser[T](Nil, registry)))

  def getAs[T <: AnyRef](selectors:KeySelector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = OptionParser(ClassParser[T](selectors.toList, registry))


  def getAsOrElse[T <: Any](default:T)(implicit registry:Registry, typeTag:TypeTag[T]) = OrElseParser(registry.get[T].getOrElse(ClassParser[T](Nil, registry)), default)

  def getAsOrElse[T <: AnyRef](selectors:KeySelector[String, _]*)(default:T)(implicit registry:Registry, typeTag:TypeTag[T]) = OrElseParser(ClassParser[T](selectors.toList, registry), default)


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

  def AsDateTime(implicit dateTimeFormatter:Option[DateTimeFormatter], dateTimeZone:DateTimeZone):DateTimeParser = DateTimeParser()(dateTimeFormatter, dateTimeZone)
  def AsAnyRef[T](implicit classTag:ClassTag[T]) = AnyRefParser[T]


  trait GreaterDouble extends DoubleRangeExtractor[Double] {
    def <(value:Double) = DoubleRangeParser(this.greaterThan, this.greaterThanExclusive, Some(value), true)
    def <=(value:Double) = DoubleRangeParser(this.greaterThan, this.greaterThanExclusive, Some(value), false)
  }

  trait LesserDouble extends DoubleRangeExtractor[Double] {
    def >(value:Double) = DoubleRangeParser(Some(value), true, this.lessThan, this.lessThanExclusive)
    def >=(value:Double) = DoubleRangeParser(Some(value), false, this.lessThan, this.lessThanExclusive)
  }

  def >(value:Double) = new DoubleRangeParser(Some(value), true, None, false) with GreaterDouble
  def >=(value:Double) = new DoubleRangeParser(Some(value), false, None, false) with GreaterDouble
  def <(value:Double) = new DoubleRangeParser(None, false, Some(value), true) with LesserDouble
  def <=(value:Double) = new DoubleRangeParser(None, false, Some(value), false) with LesserDouble

  trait GreaterFloat extends DoubleRangeExtractor[Float] {
    def <(value:Float) = FloatRangeParser(this.greaterThan.map(_.toFloat), this.greaterThanExclusive, Some(value), true)
    def <=(value:Float) = FloatRangeParser(this.greaterThan.map(_.toFloat), this.greaterThanExclusive, Some(value), false)
  }

  trait LesserFloat extends DoubleRangeExtractor[Float] {
    def >(value:Float) = FloatRangeParser(Some(value), true, this.lessThan.map(_.toFloat), this.lessThanExclusive)
    def >=(value:Float) = FloatRangeParser(Some(value), false, this.lessThan.map(_.toFloat), this.lessThanExclusive)
  }

  def >(value:Float) = new FloatRangeParser(Some(value), true, None, false) with GreaterFloat
  def >=(value:Float) = new FloatRangeParser(Some(value), false, None, false) with GreaterFloat
  def <(value:Float) = new FloatRangeParser(None, false, Some(value), true) with LesserFloat
  def <=(value:Float) = new FloatRangeParser(None, false, Some(value), false) with LesserFloat

  trait GreaterLong extends LongRangeExtractor[Long] {
    def <(value:Long) = LongRangeParser(this.greaterThan, this.greaterThanExclusive, Some(value), true)
    def <=(value:Long) = LongRangeParser(this.greaterThan, this.greaterThanExclusive, Some(value), false)
  }

  trait LesserLong extends LongRangeExtractor[Long] {
    def >(value:Long) = LongRangeParser(Some(value), true, this.lessThan, this.lessThanExclusive)
    def >=(value:Long) = LongRangeParser(Some(value), false, this.lessThan, this.lessThanExclusive)
  }

  def >(value:Long) = new LongRangeParser(Some(value), true, None, false) with GreaterLong
  def >=(value:Long) = new LongRangeParser(Some(value), false, None, false) with GreaterLong
  def <(value:Long) = new LongRangeParser(None, false, Some(value), true) with LesserLong
  def <=(value:Long) = new LongRangeParser(None, false, Some(value), false) with LesserLong

  trait GreaterInt extends LongRangeExtractor[Int] {
    def <(value:Int) = IntRangeParser(this.greaterThan.map(_.toInt), this.greaterThanExclusive, Some(value), true)
    def <=(value:Int) = IntRangeParser(this.greaterThan.map(_.toInt), this.greaterThanExclusive, Some(value), false)
  }

  trait LesserInt extends LongRangeExtractor[Int] {
    def >(value:Int) = IntRangeParser(Some(value), true, this.lessThan.map(_.toInt), this.lessThanExclusive)
    def >=(value:Int) = IntRangeParser(Some(value), false, this.lessThan.map(_.toInt), this.lessThanExclusive)
  }

  def >(value:Int) = new IntRangeParser(Some(value), true, None, false) with GreaterInt
  def >=(value:Int) = new IntRangeParser(Some(value), false, None, false) with GreaterInt
  def <(value:Int) = new IntRangeParser(None, false, Some(value), true) with LesserInt
  def <=(value:Int) = new IntRangeParser(None, false, Some(value), false) with LesserInt
}
