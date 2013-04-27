package org.higherstate.jameson

import com.fasterxml.jackson.core._
import java.math.{BigInteger, BigDecimal}
import com.fasterxml.jackson.core.JsonParser.NumberType
import scala.util.{Failure, Try}
import collection.mutable.ListBuffer
import org.higherstate.jameson.extractors.Extractor
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, ConditionalKeyNotFoundException}

//Note keyParser needs to be a single value parser, could force as extractor
//Path is not accurate here
class JsonParserBuffer[T,U](jsonParser:JsonParser, conditionalKey:String, excludeConditionalKey:Boolean, keyParser:Extractor[T,U], path:Path)(implicit val registry:Registry) extends JsonParser {

  private var currentToken:JsonToken = jsonParser.getCurrentToken
  private var currentValue:String = jsonParser.getText
  private lazy val (conditionalKeyValue:Try[U], buffer) = getKeyValueAndBuffer()

  private def getKeyValueAndBuffer():(Try[U], Iterator[(JsonToken, String)]) = {
    var nesting:List[Char] = Nil
    val buffer = new ListBuffer[(JsonToken, String)]()

    while(true) {
      val continue = Try(jsonParser.nextToken != JsonToken.END_OBJECT || !nesting.isEmpty)
      if (continue.isFailure) return continue.asInstanceOf[Failure[U]] -> buffer.toIterator
      else if (!continue.get) return  Failure(ConditionalKeyNotFoundException(conditionalKey, path)) -> Iterator.empty
      else if (nesting.isEmpty && jsonParser.getCurrentToken == JsonToken.FIELD_NAME && jsonParser.getCurrentName == conditionalKey) {
        if (!excludeConditionalKey) buffer += (jsonParser.getCurrentToken -> jsonParser.getText)
        val keyValue = Try(jsonParser.nextValue).flatMap { next =>
          if (!excludeConditionalKey) buffer += (jsonParser.getCurrentToken -> jsonParser.getText)
          keyParser(jsonParser, path)
        }
        return (keyValue, buffer.toIterator)
      }
      else {
        jsonParser.getCurrentToken match {
          case JsonToken.START_ARRAY  => nesting = '[' :: nesting
          case JsonToken.START_OBJECT => nesting = '{' :: nesting
          case JsonToken.END_ARRAY    => nesting match {
            case '[' :: tail  => nesting = tail
            case _            => return Failure(UnexpectedTokenException("", path)) -> Iterator.empty
          }
          case JsonToken.END_OBJECT   => nesting match {
            case '{' :: tail  => nesting = tail
            case _            => return Failure(UnexpectedTokenException("", path)) -> Iterator.empty
          }
          case _                      => Unit
        }
        buffer += (jsonParser.getCurrentToken -> jsonParser.getText)
      }
    }
    //never reached
    Failure(ConditionalKeyNotFoundException(conditionalKey, path)) -> Iterator.empty
  }

  def getConditionalKeyValue:Try[U] = conditionalKeyValue

  def getCodec: ObjectCodec = jsonParser.getCodec

  def setCodec(p1: ObjectCodec) {}

  def version(): Version = jsonParser.version

  def close() { jsonParser.close}

  def nextToken(): JsonToken = {
    if (buffer.hasNext) {
      val pair = buffer.next
      currentToken = pair._1
      currentValue = pair._2
    }
    else {
      currentToken = jsonParser.nextToken()
      currentValue = jsonParser.getText
    }
    currentToken
  }

  def nextValue(): JsonToken = nextToken

  def skipChildren(): JsonParser = ???

  def isClosed: Boolean = jsonParser.isClosed

  def getCurrentToken: JsonToken = currentToken

  def hasCurrentToken: Boolean = ???

  def getCurrentName: String = currentValue

  def getParsingContext: JsonStreamContext = jsonParser.getParsingContext

  def getTokenLocation: JsonLocation = ???

  def getCurrentLocation: JsonLocation = ???

  def clearCurrentToken() {}

  def getLastClearedToken: JsonToken = ???

  def overrideCurrentName(p1: String) {}

  def getText: String = currentValue

  def getTextCharacters: Array[Char] = currentValue.toArray

  def getTextLength: Int = currentValue.length

  def getTextOffset: Int = ???

  def hasTextCharacters: Boolean = ???

  def getNumberValue: Number = ???

  def getNumberType: NumberType = ???

  def getIntValue: Int = currentValue.toInt

  def getLongValue: Long = currentValue.toLong

  def getBigIntegerValue: BigInteger = ???

  def getFloatValue: Float = currentValue.toFloat

  def getDoubleValue: Double = currentValue.toDouble

  def getDecimalValue: BigDecimal = ???

  def getEmbeddedObject: AnyRef = ???

  def getBinaryValue(p1: Base64Variant): Array[Byte] = ???

  def getValueAsString(p1: String): String = currentValue
}
