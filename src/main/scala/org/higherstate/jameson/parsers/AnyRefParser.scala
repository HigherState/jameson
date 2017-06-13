package org.higherstate.jameson.parsers

import reflect.{ClassTag, classTag}
import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._

case class AnyRefParser[T]()(implicit typeTag:ClassTag[T]) extends Parser[T] {
  def parse(tokenizer:Tokenizer, path: Path) = tokenizer.head match {
    case AnyRefToken(value) if classTag[T].runtimeClass.isAssignableFrom(value.getClass) =>
      Success(value.asInstanceOf[T])
    case token =>
      Failure(InvalidTokenFailure(this, "Expected any ref token", token, path))
  }

  def schema = Map.empty
}

