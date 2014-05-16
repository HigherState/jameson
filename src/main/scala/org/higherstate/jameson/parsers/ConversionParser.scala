package org.higherstate.jameson.parsers

import reflect.runtime.universe.{TypeTag, typeOf}
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._
import java.text.NumberFormat

case class ConversionParser[T](implicit typeTag:TypeTag[T]) extends Parser[T] {
  import ConversionParser._

  def parse(tokenizer: Tokenizer, path: Path): Valid[T] =
    parser(tokenizer.head, path).map(_.asInstanceOf[T])


  private val parser:Function[(Token, Path),Valid[_]] = {
    ts[T] match {
      case CharType => {
        case (ValueToken(v), path) =>
          val c = v.toString
          if (c.length == 1) Success(c.head)
          else Failure(InvalidValueFailure(this, "Unable to convert value to a char", c, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a char", t, path))
      }
      case StringType => {
        case (ValueToken(v), _) =>
          Success(v.toString)
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a string", t, path))
      }
      case BoolType => {
        case (BooleanToken(value),_) =>
          Success(value)
        case (LongToken(value), _) =>
          Success(value > 0)
        case (DoubleToken(value), _) =>
          Success(value > 0)
        case (StringToken(c),_) if isTrue.contains(c.toLowerCase) =>
          Success(true)
        case (StringToken(c),_) if isFalse.contains(c.toLowerCase) =>
          Success(false)
        case (StringToken(Numeric(value)),_) =>
          Success(value > 0)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a boolean", value, path))
        case (NullToken, _) =>
          Success(false)
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to bool", t, path))
      }
      case ByteType => {
        case (BooleanToken(true),_) =>
          Success(1.toByte)
        case (BooleanToken(false),_) =>
          Success(0.toByte)
        case (LongToken(value), _) if value.toDouble.isValidByte =>
          Success(value.toByte)
        case (LongToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a byte", value, path))
        case (DoubleToken(value), _) if value.isValidByte =>
          Success(value.toByte)
        case (DoubleToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a byte", value, path))
        case (StringToken(Numeric(value)), path)if value.isValidByte =>
          Success(value.toByte)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a byte", value, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a byte", t, path))
      }
      case ShortType => {
        case (BooleanToken(true),_) =>
          Success(1.toShort)
        case (BooleanToken(false),_) =>
          Success(0.toShort)
        case (LongToken(value), _) if value.toDouble.isValidShort =>
          Success(value.toShort)
        case (LongToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a short", value, path))
        case (DoubleToken(value), _) if value.isValidShort =>
          Success(value.toShort)
        case (DoubleToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a short", value, path))
        case (StringToken(Numeric(value)), path)if value.isValidShort =>
          Success(value.toShort)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a short", value, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a short", t, path))
      }
      case IntType => {
        case (BooleanToken(true),_) =>
          Success(1)
        case (BooleanToken(false),_) =>
          Success(0)
        case (LongToken(value), _) if value.toDouble.isValidInt =>
          Success(value.toInt)
        case (LongToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a int", value, path))
        case (DoubleToken(value), _) if value.isValidInt =>
          Success(value.toInt)
        case (DoubleToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a int", value, path))
        case (StringToken(Numeric(value)), path)if value.isValidInt =>
          Success(value.toInt)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a int", value, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a int", t, path))
      }
      case LongType => {
        case (BooleanToken(true),_) =>
          Success(1L)
        case (BooleanToken(false),_) =>
          Success(0L)
        case (LongToken(value), _) =>
          Success(value)
        case (DoubleToken(value), _) if value.isWhole() =>
          Success(value.toLong)
        case (DoubleToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a long", value, path))
        case (StringToken(Numeric(value)), path)if value.isWhole() =>
          Success(value.toLong)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a long", value, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a long", t, path))
      }
      case FloatType => {
        case (BooleanToken(true),_) =>
          Success(1F)
        case (BooleanToken(false),_) =>
          Success(0F)
        case (LongToken(value), _) if value <= Float.MaxValue && value >= Float.MinValue =>
          Success(value.toFloat)
        case (LongToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a float", value, path))
        case (DoubleToken(value), _) if value <= Float.MaxValue && value >= Float.MinValue =>
          Success(value.toFloat)
        case (DoubleToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a float", value, path))
        case (StringToken(Numeric(value)), path) if value <= Float.MaxValue && value >= Float.MinValue =>
          Success(value.toFloat)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a float", value, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a float", t, path))
      }
      case DoubleType => {
        case (BooleanToken(true),_) =>
          Success(1.0)
        case (BooleanToken(false),_) =>
          Success(0.0)
        case (LongToken(value), _) =>
          Success(value.toDouble)
        case (DoubleToken(value), _)  =>
          Success(value)
        case (StringToken(Numeric(value)), path) =>
          Success(value)
        case (StringToken(value), path) =>
          Failure(InvalidValueFailure(this, "Unable to convert value to a double", value, path))
        case (t, path) =>
          Failure(InvalidTokenFailure(this, "Unable to convert token to a double", t, path))
      }
      case t =>
        throw ConversionTypeNotSupportedException(t)
    }
  }


  def schema: Map[String, Any] = Map.empty
}

object ConversionParser {

  val CharType = ts[Char]
  val StringType = ts[String]
  val BoolType = ts[Boolean]
  val ByteType = ts[Byte]
  val ShortType = ts[Short]
  val IntType = ts[Int]
  val LongType = ts[Long]
  val FloatType = ts[Float]
  val DoubleType = ts[Double]

  private def ts[T:TypeTag] = typeOf[T].typeSymbol.asType.toString

  val isTrue = Set("true", "t", "yes", "y")
  val isFalse = Set("false", "f", "no", "n")



}

case class ConversionTypeNotSupportedException(_type:String) extends Throwable {
  override def getMessage: String =
    s"Conversion parser does not support type ${_type}"
}

object Numeric {
  private val numericRegex = "^[\\+-]{0,1}(\\d{1,3}[\\.', ](\\d{3}[\\.', ])*\\d{3}([\\.,]\\d*)?|\\d*([\\.,]\\d*)?)$".r
  private val numberFormat = NumberFormat.getInstance()

  def isNumeric(s:String) = !isNullorWhiteSpace(s) && numericRegex.pattern.matcher(s).matches() && s != "-" && s != "+" && s != "." && s != ","
  def isNullorWhiteSpace(s:String) : Boolean  = s == null || s.trim().isEmpty

  def unapply(s:String) =
    if (isNumeric(s)) Some(numberFormat.parse(s).doubleValue)
    else None
}
