package org.higherstate.jameson.parsers

import util.Success
import org.higherstate.jameson.Path
import org.higherstate.jameson.extractors.{LongRangeExtractor, LongExtractor}

case object LongParser extends LongExtractor[Long] {
  def apply(value:Long, path:Path) = Success(value)
}

case class LongRangeParser(greaterThan:Option[Long], greaterThanExclusive:Boolean, lessThan:Option[Long], lessThanExclusive:Boolean) extends LongRangeExtractor[Long] {
  def apply(value:Long, path:Path) = Success(value)
}
