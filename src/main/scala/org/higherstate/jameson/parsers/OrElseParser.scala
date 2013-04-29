package org.higherstate.jameson.parsers

import util.{Success, Try}
import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import org.higherstate.jameson.{Registry, Path, Parser}

case class OrElseParser[T](parser:Parser[T], default:T) extends Parser[T] with HasDefault[T] {

  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[T] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NULL         => Success(default)
    case _                            => parser(jsonParser, path)
  }
}

case class DefaultOrElseParser(default:Any) extends Parser[Any] with HasDefault[Any]  {
  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[Any] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NULL         => Success(default)
    case _                            => registry.defaultUnknownParser(jsonParser, path)
  }
}
