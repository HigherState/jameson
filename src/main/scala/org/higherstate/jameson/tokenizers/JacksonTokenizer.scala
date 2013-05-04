package org.higherstate.jameson.tokenizers

import com.fasterxml.jackson.core.{JsonFactory, JsonToken, JsonParser}
import scala.util.Try
import java.io.{InputStream, Reader}

object JacksonTokenizer {
  def apply(jsonString:String):Tokenizer = {
    val jsonFactory = new JsonFactory()
    val jp = jsonFactory.createParser(jsonString)
    jp.nextToken
    JacksonTokenizerInstance(jp)
  }
  def apply(reader:Reader):Tokenizer = {
    val jsonFactory = new JsonFactory()
    val jp = jsonFactory.createParser(reader)
    jp.nextToken
    JacksonTokenizerInstance(jp)
  }
  def apply(inputStream:InputStream):Tokenizer = {
    val jsonFactory = new JsonFactory()
    val jp = jsonFactory.createParser(inputStream)
    jp.nextToken
    JacksonTokenizerInstance(jp)
  }

}
private case class JacksonTokenizerInstance(jsonParser:JsonParser) extends Tokenizer {

  private var keyState = 0
  def head = Try {
    if (jsonParser.getCurrentName != null && keyState <= 1 && jsonParser.getCurrentToken != JsonToken.END_OBJECT && jsonParser.getCurrentToken != JsonToken.END_ARRAY) {
      keyState = 1
      KeyToken(jsonParser.getCurrentName)
    }
    else jsonParser.getCurrentToken match {
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
      case JsonToken.VALUE_EMBEDDED_OBJECT  => AnyRefToken(jsonParser.getEmbeddedObject)
      case JsonToken.NOT_AVAILABLE          => ???
      case JsonToken.FIELD_NAME             => ???
      case null                             => EndToken
    }}.recover{ case e:Throwable => BadToken(e)}.get


  def moveNext() =
    if (keyState == 0) Try(jsonParser.nextValue).map(_ => this).recover{ case e:Throwable => FailedTokenizer(BadToken(e))}.get
    else if (keyState == 1) {
      keyState = 2
      this
    }
    else if (keyState == 2) {
      keyState = 0
      Try(jsonParser.nextValue).map(_ => this).recover{ case e:Throwable => FailedTokenizer(BadToken(e))}.get
    }
    else this
}
