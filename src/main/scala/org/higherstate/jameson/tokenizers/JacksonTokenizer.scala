package org.higherstate.jameson.tokenizers

import com.fasterxml.jackson.core.{JsonToken, JsonParser}
import scala.util.{Try}

case class JacksonTokenizer(head:Token, jsonParser:JsonParser) extends Tokenizer {

  lazy val tail = if (jsonParser.nextToken() != null) copy(head = next()) else End
  private def next(): Token = {
    Try(jsonParser.getCurrentToken match {
        case JsonToken.FIELD_NAME             => KeyToken(jsonParser.getCurrentName)
        case JsonToken.VALUE_STRING           => StringToken(jsonParser.getText)
        case JsonToken.START_OBJECT           => ObjectStartToken
        case JsonToken.END_OBJECT             => ObjectEndToken
        case JsonToken.START_ARRAY            => ArrayStartToken
        case JsonToken.END_ARRAY              => ArrayEndToken
        case JsonToken.VALUE_NUMBER_INT       => LongToken(jsonParser.getLongValue)
        case JsonToken.VALUE_NUMBER_FLOAT     => DoubleToken(jsonParser.getDoubleValue)
        case JsonToken.VALUE_NULL             => NullToken
        case JsonToken.VALUE_TRUE             => BooleanToken(true)
        case JsonToken.VALUE_FALSE            => BooleanToken(false)
        case JsonToken.VALUE_EMBEDDED_OBJECT  => ???
        case JsonToken.NOT_AVAILABLE          => ???
      }).recover{ case e:Throwable => BadToken(e)}.get
  }
}
