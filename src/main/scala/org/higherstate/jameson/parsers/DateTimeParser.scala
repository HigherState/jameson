package org.higherstate.jameson.parsers

import org.joda.time.{DateTimeZone, DateTime}
import org.higherstate.jameson.tokenizers.{StringToken, Tokenizer}
import org.higherstate.jameson.Path
import scala.util.{Try, Failure}
import org.higherstate.jameson.exceptions.{InvalidValueException, InvalidTokenException}
import org.joda.time.format.DateTimeFormatter

case class DateTimeParser(implicit val dateTimeFormatter:Option[DateTimeFormatter], val dateTimeZone:DateTimeZone) extends Parser[DateTime]{
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) => Try{
      dateTimeFormatter.map(DateTime.parse(value,_)).getOrElse(DateTime.parse(value)).withZone(dateTimeZone)
    }.recoverWith{ case ex:Throwable => Failure(InvalidValueException(this, "Not a valid date time string", value, path))}
    case token              => Failure(InvalidTokenException(this, "Expected String token", token, path))
  }
}
