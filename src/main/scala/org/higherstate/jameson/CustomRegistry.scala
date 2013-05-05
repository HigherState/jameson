package org.higherstate.jameson

import reflect.runtime.universe._
import org.higherstate.jameson.parsers.Parser
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter

trait CustomRegistry extends Registry {

  implicit val registry:Registry = this
  implicit def dateTimeFormatter:Option[DateTimeFormatter] = _defaultDateTimeFormatter
  implicit def dateTimeZone:DateTimeZone = _defaultDateTimeZone

  private val defaults = new DefaultRegistryValues {}
  private var _defaultClassParser:Map[TypeSymbol, Parser[_]] = defaults.classParsers
  private var _defaultUnknownParser = defaults.defaultUnknownParser
  private var _defaultTextParser = defaults.defaultTextParser
  private var _defaultLongParser = defaults.defaultLongParser
  private var _defaultDoubleParser = defaults.defaultDoubleParser
  private var _defaultBooleanParser = defaults.defaultBooleanParser
  private var _defaultArrayParser = defaults.defaultArrayParser
  private var _defaultObjectParser = defaults.defaultObjectParser
  private var _defaultNullParser = defaults.defaultNullParser
  private var _defaultAnyRefParser = defaults.defaultAnyRefParser

  private var _defaultDateTimeZone = DefaultRegistry.dateTimeZone
  private var _defaultDateTimeFormatter = DefaultRegistry.dateTimeFormatter

  protected def overrideDefaultUnknownParser(parser:Parser[_]) { _defaultUnknownParser = parser }
  protected def overrideDefaultTextParser(parser:Parser[_]) { _defaultTextParser = parser }
  protected def overrideDefaultLongParser(parser:Parser[_]) { _defaultLongParser = parser }
  protected def overrideDefaultDoubleParser(parser:Parser[_]) { _defaultDoubleParser = parser}
  protected def overrideDefaultBooleanParser(parser:Parser[_]) { _defaultBooleanParser = parser}
  protected def overrideDefaultArrayParser(parser:Parser[_]) { _defaultArrayParser = parser}
  protected def overrideDefaultObjectParser(parser:Parser[_]) { _defaultObjectParser = parser}
  protected def overrideDefaultNullParser(parser:Parser[_]) { _defaultNullParser = parser}
  protected def overrideDefaultAnyRefParser(parser:Parser[_]) { _defaultAnyRefParser = parser}

  protected def overrideDefaultDateTimeZone(dateTimeZone:DateTimeZone) { _defaultDateTimeZone = dateTimeZone}
  protected def overrideDefaultDateTimeFormatter(dateTimeFormatter:DateTimeFormatter) { _defaultDateTimeFormatter = Some(dateTimeFormatter)}

  def defaultUnknownParser:Parser[_] = _defaultUnknownParser
  def defaultTextParser:Parser[_] = _defaultTextParser
  def defaultLongParser:Parser[_] = _defaultLongParser
  def defaultDoubleParser:Parser[_] = _defaultDoubleParser
  def defaultBooleanParser:Parser[_] = _defaultBooleanParser
  def defaultObjectParser:Parser[_] = _defaultObjectParser
  def defaultArrayParser:Parser[_] = _defaultArrayParser
  def defaultNullParser:Parser[_] = _defaultNullParser
  def defaultAnyRefParser:Parser[_] = _defaultAnyRefParser

  protected def bindParser[T:TypeTag](parser:Parser[_]) {
    _defaultClassParser += typeOf[T].typeSymbol.asType -> parser
  }

  lazy val classParsers:Map[TypeSymbol, Parser[_]] = _defaultClassParser
}
