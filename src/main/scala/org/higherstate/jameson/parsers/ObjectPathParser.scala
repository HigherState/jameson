package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import org.higherstate.jameson.failures._
import org.higherstate.jameson.tokenizers.KeyToken

case class ObjectPathParser[T](pathKey:String, parser:Parser[T]) extends Parser[T] {
  def parse(tokenizer: Tokenizer, path: Path): Valid[T] = tokenizer.head match {
    case ObjectStartToken =>
      findMatch(tokenizer.moveNext(), path)
    case token            =>
      Failure(InvalidTokenFailure(this, "Expected object start token", token, path))
  }

  private def findMatch(tokenizer:Tokenizer, path: Path):Valid[T] = tokenizer.head match {
    case KeyToken(key) if key == pathKey =>
      parser.parse(tokenizer.moveNext(), path + key).flatMap(value => dropRemainingKeys(tokenizer.moveNext(), path, value))
    case KeyToken(key) =>
      findMatch(tokenizer.dropNext(), path + key)
    case ObjectEndToken =>
      parser.default.fold[Valid[T]](Failure(KeyNotFoundFailure(this, pathKey, path)))(Success(_))
    case token =>
      Failure(InvalidTokenFailure(this, "Expected a key or object end token", token, path))
  }

  private def dropRemainingKeys(tokenizer:Tokenizer, path:Path, value:T):Valid[T] = tokenizer.head match {
    case KeyToken(key)  =>
      dropRemainingKeys(tokenizer.dropNext(), path, value)
    case ObjectEndToken =>
      Success(value)
    case token          =>
      Failure(InvalidTokenFailure(this, "Expected a key or object end token", token, path))
  }

  override def default:Option[T] = parser.default

  def schema = Map("type" -> "object", "properties" -> Map(pathKey -> parser.schema))
}

