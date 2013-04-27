package org.higherstate.jameson

import parsers._
import reflect.runtime.universe._
import java.util.UUID

object DefaultRegistry {
  implicit val registry:Registry = new DefaultRegistryValues {}
}

trait DefaultRegistryValues extends Registry {

  def defaultUnknownParser:Parser[_] = AnyParser()
  def defaultTextParser:Parser[_] = StringParser()
  def defaultLongParser:Parser[_] = LongParser()
  def defaultDoubleParser:Parser[_] = DoubleParser()
  def defaultBooleanParser:Parser[_] = BooleanParser()
  def defaultObjectParser:Parser[_] = MapParser
  def defaultArrayParser:Parser[_] = ListParser
  def defaultNullParser:Parser[_] = NullParser()

  def classParsers:Map[TypeSymbol, Parser[_]] = Map(
    ts[Any] -> AnyParser(),
    ts[Null] -> NullParser(),
    ts[Boolean] -> BooleanParser(),
    ts[Char] -> CharParser(),
    ts[String] -> StringParser(),
    ts[Byte] -> ByteParser(),
    ts[Short] -> ShortParser(),
    ts[Int] -> IntParser(),
    ts[Long] -> LongParser(),
    ts[Float] -> FloatParser(),
    ts[Double] -> DoubleParser(),
    ts[Option[_]] -> OptionParser(AnyParser()),
    ts[Map[String, Any]] -> MapParser,
    ts[List[Any]] -> ListParser,
    ts[UUID] -> UUIDParser()
  )

  private def ts[T:TypeTag] = typeOf[T].typeSymbol.asType
}
