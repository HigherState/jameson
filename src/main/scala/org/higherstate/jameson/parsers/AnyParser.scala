package org.higherstate.jameson.parsers

import org.higherstate.jameson.{Registry, Path}
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case class AnyParser(registry:Registry) extends Parser[Any] {
  def parse(tokenizer:Tokenizer, path:Path): Valid[Any] = tokenizer.head match {
    case s:StringToken =>
      registry.defaultTextParser.parse(tokenizer, path)
    case ObjectStartToken =>
      registry.defaultObjectParser.parse(tokenizer, path)
    case ArrayStartToken =>
      registry.defaultArrayParser.parse(tokenizer, path)
    case l:LongToken =>
      registry.defaultLongParser.parse(tokenizer, path)
    case d:DoubleToken =>
      registry.defaultDoubleParser.parse(tokenizer, path)
    case NullToken =>
      registry.defaultNullParser.parse(tokenizer, path)
    case b:BooleanToken =>
      registry.defaultBooleanParser.parse(tokenizer, path)
    case a:AnyRefToken =>
      registry.defaultAnyRefParser.parse(tokenizer, path)
    case token =>
      Failure(InvalidTokenFailure(this, "Unexpected token", token, path))
  }

  def schema = Map.empty
}
