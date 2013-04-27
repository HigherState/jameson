package org.higherstate.jameson

trait Selector[U, +T] extends Any {
  def key:U
  def replaceKey:Option[U]
  def toKey = replaceKey.getOrElse(key)
  def parser:Parser[T]
  def isRequired:Boolean
}