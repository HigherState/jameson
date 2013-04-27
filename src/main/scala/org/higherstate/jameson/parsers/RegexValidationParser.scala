package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.StringExtractor
import util.{Success, Failure, Try}
import util.matching.Regex
import org.higherstate.jameson.exceptions.InvalidTokenException
import org.higherstate.jameson.{Registry, Path}

case class RegexValidationParser(regex:Regex, message:String = "Unexpected string format") extends StringExtractor[String] {
   protected def parse(value: String, path: Path)(implicit registry:Registry): Try[String] =
     if (!regex.pattern.matcher(value).matches()) Failure(InvalidTokenException(message, path))
     else Success(value)
 }
