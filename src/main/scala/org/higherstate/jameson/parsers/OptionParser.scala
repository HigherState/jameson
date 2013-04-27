package org.higherstate.jameson.parsers

import util.{Success, Try}
import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import org.higherstate.jameson.{Registry, Path, Parser}

case class OptionParser[T](parser:Parser[T]) extends Parser[Option[T]] {

  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[Option[T]] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NULL         => Success(None)
    case _                            => parser(jsonParser, path).map(Some(_))
  }

}

case class DefaultOptionParser() extends Parser[Option[Any]] {
  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[Option[Any]] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NULL         => Success(None)
    case _                            => registry.defaultUnknownParser(jsonParser, path).map(Some(_))
  }
}
