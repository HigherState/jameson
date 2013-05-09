package org.higherstate.jameson.parsers

import util.{Failure, Try}
import org.higherstate.jameson.exceptions.UnexpectedTokenException
import org.higherstate.jameson.{Registry, Path}
import org.higherstate.jameson.tokenizers._

case class AnyValParser(registry:Registry) extends Parser[Any] {
  def parse(tokenizer:Tokenizer, path:Path): Try[Any] = tokenizer.head match {
    case s:StringToken    => registry.defaultTextParser.parse(tokenizer, path)
    case l:LongToken      => registry.defaultLongParser.parse(tokenizer, path)
    case d:DoubleToken    => registry.defaultDoubleParser.parse(tokenizer, path)
    case NullToken        => registry.defaultNullParser.parse(tokenizer, path)
    case b:BooleanToken   => registry.defaultBooleanParser.parse(tokenizer, path)
    case token            => Failure(UnexpectedTokenException("Unexpected token", token, path))
  }
}
