package org.higherstate.jameson

import org.higherstate.jameson.parsers.Parser

trait Selector[U, +T] extends Any {
  def keys:Set[U]
  def parser:Parser[T]
  def isGroup:Boolean
}

trait KeySelector[U, +T] extends Any with Selector[U, T] {
  def replaceKey:Option[U]
  def toKey = replaceKey.getOrElse(keys.head)
  def isRequired:Boolean
  def isParserSpecified:Boolean
}

