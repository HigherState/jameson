package org.higherstate.jameson

import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import scala.util.matching.Regex

object Dsl {

  case class RequiredSelector[U,T](key:U, parser:Parser[T], replaceKey:Option[U]) extends Selector[U, T] {
    def isRequired = true
  }

  implicit class StringTupleExtenstion(val self:(String, String)) extends AnyVal {
    def ->>[T](parser:Parser[T]) = RequiredSelector(self._1, parser, Some(self._2))
  }
  implicit class AnyExt[U](val self:U) extends AnyVal {
    def ->>[T](parser:Parser[T]) = RequiredSelector(self, parser, None)
  }


  implicit class UnrequiredSelectorWithReplaceKey[T](val self:((String, String),Parser[T])) extends AnyVal with Selector[String, T] {
    def key = self._1._1
    def parser = self._2
    def isRequired = false
    def replaceKey = Some(self._1._2)
  }
  implicit class UnrequiredSelector[U, T](val self:(U,Parser[T])) extends AnyVal with Selector[U, T] {
    def key = self._1
    def parser = self._2
    def isRequired = false
    def replaceKey = None
  }

  object || {
    def apply()(implicit registry:Registry) = ListParser[Any](registry.defaultUnknownParser)
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = ListParser(registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = ListParser[T](parser)
  }

  object #* {
    def apply()(implicit registry:Registry) = MapParser[Any](registry.defaultUnknownParser)
    def apply(selectors:Selector[String, _]*)(implicit registry:Registry) = OpenMapParser(selectors.map(s => s.key -> s).toMap, registry.defaultUnknownParser)
  }

  object ¦¦ {
    def apply()(implicit registry:Registry) = TraversableOnceParser[Any](registry.defaultUnknownParser)
    def apply[T](implicit registry:Registry, typeTag:TypeTag[T]) = TraversableOnceParser[T](registry[T])
    def apply[T](parser:Parser[T])(implicit registry:Registry, typeTag:TypeTag[T]) = TraversableOnceParser(parser)
  }

  def #!(selector:Selector[String, _], selectors:Selector[String, _]*) =
    CloseMapParser((selectors :+ selector).map(s => s.key -> s).toMap)

  def #^(selector:Selector[String, _], selectors:Selector[String, _]*) =
    DropMapParser((selectors :+ selector).map(s => s.key -> s).toMap)

  object ? {
    def apply()(implicit registry:Registry) = OptionParser(registry.defaultUnknownParser)
    def apply(orElse:Any)(implicit registry:Registry) = OrElseParser(registry.defaultUnknownParser, orElse)
    def apply[U](parser:Parser[U]) = OptionParser(parser)
    def apply[U](parser:Parser[U], orElse:U) = OrElseParser(parser, orElse)
  }

  def ><[T,U](leftParser:Parser[T], rightParser:Parser[U]) = EitherParser(leftParser, rightParser)

  //maybe defaults should be extractors...
  def /[T, U](key:String, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
    MatchParser(key, registry[T], None, selectors.map(p => p.key -> p.parser).toMap)

  def /[U](key:String, classes:ClassParser[U]*)(implicit registry:Registry) =
    MatchParser(key, registry[String], None, classes.map(p => p.getClassName -> p).toMap)

  def /[T, U](key:String, default:T, selectors:Selector[T, U]*)(implicit registry:Registry, typeTag:TypeTag[T]) =
    MatchParser(key, registry[T], Some(default), selectors.map(p => p.key -> p.parser).toMap)

  def /[U](key:String, default:String, classes:ClassParser[U]*)(implicit registry:Registry) =
    MatchParser(key, registry[String], Some(default), classes.map(p => p.getClassName -> p).toMap)

  def >>[T <: AnyRef](implicit registry:Registry, typeTag:TypeTag[T]) = ClassParser[T](Nil, registry)

  def >>[T <: AnyRef](selectors:Selector[String, _]*)(implicit registry:Registry, typeTag:TypeTag[T]) = {
    ClassParser[T](selectors.toList, registry)
  }

  def |>[T,U](parser:Parser[T], func:T => U) = FunctionParser(parser, func)

  def r(regex:String) = RegexValidationParser(regex.r, "Invalid string format.")
  def r(regex:String, message:String) = RegexValidationParser(regex.r, message)
  def r(regex:Regex) = RegexValidationParser(regex, "Invalid string format.")
  def r(regex:Regex, message:String) = RegexValidationParser(regex, message)

  def AsAny(implicit registry:Registry) = AnyParser(registry)
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
}
