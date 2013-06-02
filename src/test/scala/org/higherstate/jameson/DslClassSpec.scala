package org.higherstate.jameson

import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class DslClassSpec extends WordSpec with MustMatchers  {

  "Simple class parsing" should {
    "parse simple class with value" in {
      >>[Child1].parse("""{"tInt":3}""") mustEqual (Success(Child1(3)))
      >>[Child3]("tInt" -> ?(AsInt, 5)).parse("""{"tBool":false}""") mustEqual (Success(Child3(5, false)))
      >>[Child1](("a"|"b") -> "tInt" -> AsInt).parse("""{"b":3}""") mustEqual (Success(Child1(3)))
    }
    "parse simple class with value and extra values" in {
      >>[Child2].parse("""{"tInt":3,"tBool":false}""") mustEqual (Success(Child2(false)))
    }
    "parse class with nested class" in {
      >>[NestedChild].parse("""{"child1":{"tInt":3},"child2":{"tBool":false}}""") mustEqual (Success(NestedChild(Child1(3), Child2(false))))
    }
    "parse class with nested map" in {
      >>[MapChild]("map" -> #^("value" -> AsAny)).parse("""{"map":{"value":3,"value2":false}}""") mustEqual (Success(MapChild(Map("value" -> 3))))
    }
    "parse class with key remaping" in {
      >>[Child1]("Int" -> "tInt" -> AsInt).parse("""{"Int":3}""") mustEqual (Success(Child1(3)))
    }
//    "fail if constructor key not found" in {
//
//    }
//    "map constructor key" in {
//
//    }
  }
}
