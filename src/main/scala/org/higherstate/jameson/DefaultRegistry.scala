package org.higherstate.jameson

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import parsers._

import reflect.runtime.universe._
import java.util.UUID


object DefaultRegistry {
  implicit val registry:Registry = new DefaultRegistryValues {}
  implicit val dateTimeFormatter:Option[DateTimeFormatter] = None
  implicit val dateTimeZone:ZoneOffset = ZoneOffset.UTC
}

trait DefaultRegistryValues extends Registry {

  def defaultUnknownParser:Parser[_] = AnyParser(this)
  def defaultTextParser:Parser[_] = StringParser
  def defaultLongParser:Parser[_] = LongParser
  def defaultDoubleParser:Parser[_] = DoubleParser
  def defaultBooleanParser:Parser[_] = BooleanParser
  def defaultObjectParser:Parser[_] = MapParser(defaultUnknownParser)
  def defaultArrayParser:Parser[_] = ListParser(defaultUnknownParser)
  def defaultNullParser:Parser[_] = NullParser
  def defaultAnyRefParser:Parser[_] = AnyRefParser[AnyRef]

  def classParsers:Map[TypeSymbol, Parser[_]] = Map(
    ts[Any] -> AnyParser(this),
    ts[AnyVal] -> AnyValParser(this),
    ts[Null] -> NullParser,
    ts[Boolean] -> BooleanParser,
    ts[Char] -> CharParser,
    ts[String] -> StringParser,
    ts[Byte] -> ByteParser,
    ts[Short] -> ShortParser,
    ts[Int] -> IntParser,
    ts[Long] -> LongParser,
    ts[Float] -> FloatParser,
    ts[Double] -> DoubleParser,
    ts[Option[_]] -> OptionParser(defaultUnknownParser),
    ts[Map[String, Any]] -> MapParser(defaultUnknownParser),
    ts[List[Any]] -> ListParser(defaultUnknownParser),
    ts[UUID] -> UUIDParser
  )

  private def ts[T:TypeTag] = typeOf[T].typeSymbol.asType
}
