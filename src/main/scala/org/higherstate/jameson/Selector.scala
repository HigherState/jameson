package org.higherstate.jameson

import org.higherstate.jameson.parsers.Parser

trait Selector[U, +T] extends Any {
  def keys:Set[U]
  def replaceKey:Option[U]
  def toKey = replaceKey.getOrElse(keys.head)
  def parser:Parser[T]
  def isRequired:Boolean
}

