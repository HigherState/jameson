package org.higherstate.jameson.parsers

object NoArgFound {
  def apply[T](parser:Parser[T]) = parser match {
    case p:HasDefault[T] => p.default
    case _               => NoArgFound
  }
}
