package org.higherstate.jameson.parsers

import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import util.{Failure, Try}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path, Parser}

case class AnyParser() extends Parser[Any] {
  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[Any] = (jsonParser.getCurrentToken match {
    case JsonToken.VALUE_STRING                       => registry.defaultTextParser.apply(jsonParser, path)
    case JsonToken.START_OBJECT                       => registry.defaultObjectParser.apply(jsonParser, path)
    case JsonToken.START_ARRAY                        => registry.defaultArrayParser.apply(jsonParser, path)
    case JsonToken.VALUE_NUMBER_INT                   => registry.defaultLongParser.apply(jsonParser, path)
    case JsonToken.VALUE_NUMBER_FLOAT                 => registry.defaultDoubleParser.apply(jsonParser, path)
    case JsonToken.VALUE_NULL                         => registry.defaultNullParser.apply(jsonParser, path)
    case JsonToken.VALUE_TRUE | JsonToken.VALUE_FALSE => registry.defaultBooleanParser.apply(jsonParser, path)
    case token                                        => Failure(UnexpectedTokenException("Unexpected token", path))
  })

}
