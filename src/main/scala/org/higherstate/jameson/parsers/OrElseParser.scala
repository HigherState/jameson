package org.higherstate.jameson.parsers

import util.{Success, Try}
import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import org.higherstate.jameson.{Registry, Path, Parser}

case class OrElseParser[T](parser:Parser[T], orElse:T) extends Parser[T] {

  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[T] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NULL         => Success(orElse)
    case _                            => parser(jsonParser, path)
  }
}
