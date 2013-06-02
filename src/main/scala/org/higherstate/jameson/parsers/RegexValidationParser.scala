package org.higherstate.jameson.parsers

import util.{Success, Failure}
import util.matching.Regex
import org.higherstate.jameson.exceptions.{InvalidValueException, InvalidTokenException}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case class RegexValidationParser(regex:Regex, message:String = "Unexpected string format") extends Parser[String] {

  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) =>
      if (regex.pattern.matcher(value).matches()) Success(value)
      else Failure(InvalidValueException(this, message, tokenizer.head, path))
    case token              => Failure(InvalidTokenException(this, "Expected String token", token, path))
  }
}
