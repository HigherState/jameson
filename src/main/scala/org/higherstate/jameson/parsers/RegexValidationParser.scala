package org.higherstate.jameson.parsers

import util.{Success, Failure}
import util.matching.Regex
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, InvalidTokenException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case class RegexValidationParser(regex:Regex, message:String = "Unexpected string format") extends Parser[String] {

  def parse(tokenizer:Tokenizer, path: Path) = tokenizer match {
    case StringToken(value) -: tail =>
      if (regex.pattern.matcher(value).matches()) Success(value -> tail)
      else Failure(InvalidTokenException(message, path))
    case token -: tail               => Failure(UnexpectedTokenException("Expected String token", token, path))
  }
}
