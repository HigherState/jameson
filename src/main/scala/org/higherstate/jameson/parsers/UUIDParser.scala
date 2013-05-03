package org.higherstate.jameson.parsers

import util.{Success, Failure}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, InvalidTokenException}
import java.util.UUID
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._

case object UUIDParser extends Parser[UUID] {
  private val uuidRegex = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case StringToken(value) =>
      if (!uuidRegex.pattern.matcher(value).matches()) Failure(InvalidTokenException("String is not a Universally Unique Identifier", path))
      else Success(UUID.fromString(value))
    case token              => Failure(UnexpectedTokenException("Expected String token", token, path))
  }
 }
