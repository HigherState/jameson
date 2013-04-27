package org.higherstate.jameson

import reflect.runtime.universe._

trait Registry {

  protected def classParsers:Map[TypeSymbol, Parser[_]]

  def defaultUnknownParser:Parser[_]
  def defaultTextParser:Parser[_]
  def defaultLongParser:Parser[_]
  def defaultDoubleParser:Parser[_]
  def defaultBooleanParser:Parser[_]
  def defaultObjectParser:Parser[_]
  def defaultArrayParser:Parser[_]
  def defaultNullParser:Parser[_]

  def apply[T:TypeTag]:Parser[T] = classParsers(typeOf[T].typeSymbol.asType).asInstanceOf[Parser[T]]
  def apply(typeSymbol:TypeSymbol):Parser[_] = classParsers(typeSymbol)

  def get[T:TypeTag]:Option[Parser[T]] = classParsers.get(typeOf[T].typeSymbol.asType).map(_.asInstanceOf[Parser[T]])
  def get(typeSymbol:TypeSymbol):Option[Parser[_]] = classParsers.get(typeSymbol)


}
