package org.higherstate.jameson

import org.scalatest.WordSpec
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import org.scalatest.matchers.MustMatchers
import org.higherstate.jameson.parsers.Parser
import org.higherstate.jameson.failures.Success

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

  "self referencing json validation" should {
    "handle self reference in selector" in {
      lazy val parser:Parser[ParentContainer] = as [ParentContainer]("parent" -> asOption(self (parser)))
      parser.parse("""{"parent":{"parent":{"parent":{}}}}""") mustEqual Success(ParentContainer(Some(ParentContainer(Some(ParentContainer(Some(ParentContainer(None))))))))
    }

    "parse with no recursing in a two level recursion" in {
      lazy val parser:Parser[RecursiveChild1] = as [RecursiveChild1] ("child" -> as [RecursiveChild2] ("child" -> asOption(self(parser))))
      parser("""{"value":1,"child":{"value":"two"}}""") mustEqual Success(RecursiveChild1(1, RecursiveChild2("two", None)))
    }

    "parse with recursion in a two level recursion" in {
      lazy val parser:Parser[RecursiveChild2] =  as [RecursiveChild2] ("child" -> asOption [RecursiveChild1] ("child" -> self(parser)))
      parser("""{"value":"one","child":{"value":2,"child":{"value":"three"}}}""") mustEqual Success(RecursiveChild2("one", Some(RecursiveChild1(2, RecursiveChild2("three",None)))))
    }
  }

}
