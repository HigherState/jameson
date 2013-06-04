package org.higherstate.jameson

import org.scalatest.WordSpec
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import org.scalatest.matchers.MustMatchers

class NestedStructuresSpec extends WordSpec with MustMatchers {

  "list with matches" should {
    val listParser = as [ListParents]("parents" -> asList(matchAs("type", as [Child1], as [Child2])))
    "handle multiple elements" in {
      val json = """{"int":3,"parents":[{"type":"Child1", "tInt":3},{"tInt":3, "type":"Child1"}, {"tBool":false, "type":"Child2"}]}"""
      val r = listParser(json)
      r.isSuccess mustEqual (true)
    }

    val matchParser = matchAs("pType", "one" -> listParser, "two" -> listParser)
    "inside a match" in {
      val json = """{"int":3,"parents":[{"type":"Child1", "tInt":3},{"tInt":3, "type":"Child1"}, {"tBool":false, "type":"Child2"}], "pType":"two"}"""
      val r = matchParser(json)
      r.isSuccess mustEqual (true)
    }

    val doubleMatchParser = matchAs("dType", "one" -> matchParser, "two" -> matchParser)
    "with a double match" in {
      val json = """{"int":3,"dType":"one","parents":[{"type":"Child1", "tInt":3},{"tInt":3, "type":"Child1"},{"tBool":false, "type":"Child2"}], "pType":"two"}"""
      val r = doubleMatchParser(json)
      r.isSuccess mustEqual (true)
    }
  }
}
