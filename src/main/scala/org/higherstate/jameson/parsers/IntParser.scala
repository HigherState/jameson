package org.higherstate.jameson.parsers

import util.Success
import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.LongRangeExtractor

case object IntParser extends LongRangeExtractor[Int] {
  val greaterThan = Some(Int.MinValue.toLong)
  val greaterThanExclusive = false
  val lessThan = Some(Int.MaxValue.toLong)
  val lessThanExclusive = false

  def apply(value:Long, path:Path) = Success(value.toInt)
}

case class IntRangeParser(greaterThanValue:Option[Int], greaterThanExclusive:Boolean, lessThanValue:Option[Int], lessThanExclusive:Boolean) extends LongRangeExtractor[Int] {
  val greaterThan = greaterThanValue.orElse(Some(Int.MinValue)).map(_.toLong)
  val lessThan = lessThanValue.orElse(Some(Int.MaxValue)).map(_.toLong)

  def apply(value:Long, path:Path) = Success(value.toInt)
}