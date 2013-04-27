package org.higherstate.jameson.parsers

import com.fasterxml.jackson.core._
import scala.util.{Failure, Try}
import org.higherstate.jameson.extractors.{KeyValuePairsExtractor, Extractor}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, ConditionalKeyMatchNotFoundException}
import org.higherstate.jameson.{Registry, Path, Parser, JsonParserBuffer}

case class MatchParser[T, U, V](identifierKey:String, excludeKeyValue:Boolean, parser:Extractor[U, V], matchParsers:Map[V, KeyValuePairsExtractor[T]]) extends Parser[T]{

  def apply(jsonParser: JsonParser, path: Path)(implicit registry: Registry): Try[T] =
    if (jsonParser.getCurrentToken != JsonToken.START_OBJECT) Failure(UnexpectedTokenException("Expected an object", path))
    else {
      val buffer = new JsonParserBuffer[U, V](jsonParser, identifierKey, excludeKeyValue, parser, path)
      buffer.getConditionalKeyValue.flatMap { key =>
        matchParsers.get(key).map(_(buffer,path))
          .getOrElse(Failure(ConditionalKeyMatchNotFoundException(key.toString, path)))
      }
    }
}