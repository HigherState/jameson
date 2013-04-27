package org.higherstate.jameson.extractors

import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import util.{Success, Failure, Try}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path, Parser, IteratorWrapper}

sealed trait Extractor[T,U] extends Parser[U] {

  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry):Try[U] = extract(jsonParser, path).flatMap(parse(_, path))

  protected def extract(jsonParser:JsonParser, path:Path):Try[T]

  protected def parse(value:T, path:Path)(implicit registry:Registry):Try[U]
}

sealed trait TraversableOnceExtractor[T, U] extends Parser[U] {

  def apply(jsonParser: JsonParser, path: Path)(implicit registry: Registry): Try[U] = parse(extract(jsonParser, path), path)

  protected def extract(jsonParser:JsonParser, path:Path):TraversableOnce[Try[T]]

  protected def parse(value:TraversableOnce[Try[T]], path:Path)(implicit registry:Registry):Try[U]
}

trait ValueExtractor[T] extends Extractor[Any, T] {

  def extract(jsonParser:JsonParser, path:Path):Try[Any] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NUMBER_FLOAT  | JsonToken.VALUE_NUMBER_INT => Success(jsonParser.getDoubleValue)
    case JsonToken.VALUE_NULL                                       => Success(null)
    case JsonToken.VALUE_STRING                                     => Success(jsonParser.getText)
    case JsonToken.VALUE_TRUE                                       => Success(true)
    case JsonToken.VALUE_FALSE                                      => Success(false)
    case _                                                          => Failure(UnexpectedTokenException("Expected true or false value", path))
  }
}

trait BooleanExtractor[T] extends Extractor[Boolean, T] {

  def extract(jsonParser:JsonParser, path:Path):Try[Boolean] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_TRUE         => Success(true)
    case JsonToken.VALUE_FALSE        => Success(false)
    case _                            => Failure(UnexpectedTokenException("Expected true or false value", path))
  }
}

trait StringExtractor[T] extends Extractor[String, T] {

  def extract(jsonParser:JsonParser, path:Path):Try[String] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_STRING       => Success(jsonParser.getText)
    case _                            => Failure(UnexpectedTokenException("Expected a text value", path))
  }
}

trait NumericExtractor[T] extends Extractor[Double, T] {

  def extract(jsonParser:JsonParser, path:Path):Try[Double] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NUMBER_FLOAT  | JsonToken.VALUE_NUMBER_INT => Success(jsonParser.getDoubleValue)
    case _                                                          => Failure(UnexpectedTokenException("Expected a numeric value", path))
  }
}

trait NullExtractor[T] extends Extractor[Null, T] {

  def extract(jsonParser:JsonParser, path:Path):Try[Null] = jsonParser.getCurrentToken match {
    case JsonToken.VALUE_NULL         => Success(null)
    case _                            => Failure(UnexpectedTokenException("Expected a null value", path))
  }
}

trait KeyValuePairsExtractor[T] extends TraversableOnceExtractor[(String, JsonParser), T] {

  def extract(jsonParser:JsonParser, path:Path):TraversableOnce[Try[(String,JsonParser)]] =
    if (jsonParser.getCurrentToken != JsonToken.START_OBJECT) Some(Failure(UnexpectedTokenException("Expected an object", path)))
    else IteratorWrapper(
      () => Try(jsonParser.nextToken != JsonToken.END_OBJECT),
      () => {
        val key = jsonParser.getCurrentName
        Try(jsonParser.nextToken).map { t => (key, jsonParser) }
      }
    )
}

trait ValuesExtractor[T] extends TraversableOnceExtractor[JsonParser, T] {

  def extract(jsonParser:JsonParser, path:Path):TraversableOnce[Try[JsonParser]] =
    if (jsonParser.getCurrentToken != JsonToken.START_ARRAY) Some(Failure(UnexpectedTokenException("Expected an array", path)))
    else IteratorWrapper(
      () => Try(jsonParser.nextToken != JsonToken.END_ARRAY),
      () => Success(jsonParser)
    )
}



