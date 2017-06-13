package org.higherstate.jameson.parsers

import org.joda.time.{DateTimeZone, DateTime}
import org.higherstate.jameson.tokenizers.{StringToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._
import org.joda.time.format.DateTimeFormatter
import scala.util.Try

case class DateTimeParser()(implicit val dateTimeFormatter:Option[DateTimeFormatter], val dateTimeZone:DateTimeZone) extends Parser[DateTime]{
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) =>
      Try{
        dateTimeFormatter.fold(DateTime.parse(value))(DateTime.parse(value,_)).withZone(dateTimeZone)
      }.toOption
       .fold[Valid[DateTime]](Failure(InvalidValueFailure(this, "Not a valid date time string", value, path)))(Success(_))
    case token  =>
      Failure(InvalidTokenFailure(this, "Expected String token", token, path))
  }

  def schema = ???
}
