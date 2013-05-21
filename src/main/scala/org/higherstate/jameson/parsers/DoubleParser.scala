package org.higherstate.jameson.parsers

import scala.util.Success
import org.higherstate.jameson.Path

import org.higherstate.jameson.extractors.{DoubleRangeExtractor, DoubleExtractor}

case object DoubleParser extends DoubleExtractor[Double] {
  def apply(value:Double, path:Path) = Success(value)
}

case class DoubleRangeParser(greaterThan:Option[Double], greaterThanExclusive:Boolean, lessThan:Option[Double], lessThanExclusive:Boolean) extends DoubleRangeExtractor[Double] {
  def apply(value:Double, path:Path) = Success(value)
}



