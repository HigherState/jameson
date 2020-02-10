package org.higherstate.jameson.parsers
import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter

import org.higherstate.jameson.tokenizers.{StringToken, Tokenizer}
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._

import scala.util.Try

case class DateTimeParser()(implicit val dateTimeFormatter:Option[DateTimeFormatter], val dateTimeZone:ZoneOffset) extends Parser[ZonedDateTime]{
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) =>
      Try{
        dateTimeFormatter.fold(ZonedDateTime.parse(value))(ZonedDateTime.parse(value,_)).`with`(dateTimeZone)
      }.toOption
        .fold[Valid[ZonedDateTime]](Failure(InvalidValueFailure(this, "Not a valid date time string", value, path)))(Success(_))
    case token  =>
      Failure(InvalidTokenFailure(this, "Expected String token", token, path))
  }

  def schema = ???
}