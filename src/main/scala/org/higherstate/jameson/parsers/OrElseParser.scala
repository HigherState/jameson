package org.higherstate.jameson.parsers

import org.higherstate.jameson.Path
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures.Success

case class OrElseParser[T](parser:Parser[T], _default:T) extends Parser[T] {

  def parse(tokenizer:Tokenizer, path:Path) =
    tokenizer.head match {
      case NullToken | EndToken =>
        Success(_default)
      case _ =>
        parser.parse(tokenizer, path)
    }

  override def default = Some(_default)

  def schema = parser.schema + ("defaultValue" -> (_default match {
    case number:Number => number
    case string:String => string
    case char:Char => char
    case bool:Boolean => bool
    case s:Seq[_] => "array"
    case _        => "object"
  }))
}
