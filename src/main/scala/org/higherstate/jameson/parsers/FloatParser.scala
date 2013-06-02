package org.higherstate.jameson.parsers

import util.Success
import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.DoubleRangeExtractor

case object FloatParser extends DoubleRangeExtractor[Float] {
  val greaterThan = Some(Float.MinValue.toDouble)
  val greaterThanExclusive = false
  val lessThan = Some(Float.MaxValue.toDouble)
  val lessThanExclusive = false

  def apply(value:Double, path:Path) = Success(value.toFloat)
}

case class FloatRangeParser(greaterThanValue:Option[Float], greaterThanExclusive:Boolean, lessThanValue:Option[Float], lessThanExclusive:Boolean) extends DoubleRangeExtractor[Float] {
  val greaterThan = greaterThanValue.orElse(Some(Float.MinValue)).map(_.toDouble)
  val lessThan = lessThanValue.orElse(Some(Float.MaxValue)).map(_.toDouble)

  def apply(value:Double, path:Path) = Success(value.toFloat)
}