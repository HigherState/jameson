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
    "inside a match" in {
      val parser = matchAs("pType", "one" -> listParser, "two" -> listParser)
      val json = """{"int":3,"parents":[{"type":"Child1", "tInt":3},{"tInt":3, "type":"Child1"}, {"tBool":false, "type":"Child2"}], "pType":"two"}"""
      val r = parser(json)
      r.isSuccess mustEqual (true)
    }
  }
}
