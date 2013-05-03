package org.higherstate.jameson

import org.specs2.mutable.Specification
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._

class DslClassSpec extends Specification {

  "Simple class parsing" should {
    "parse simple class with value" in {
      >>[Child1].parse("""{"tInt":3}""") mustEqual (Success(Child1(3)))
      >>[Child2].parse("""{"tInt":3,"tBool":false}""") mustEqual (Success(Child2(false)))
      >>[NestedChild].parse("""{"child1":{"tInt":3},"child2":{"tBool":false}}""") mustEqual (Success(NestedChild(Child1(3), Child2(false))))
      >>[MapChild]("map" -> #^("value" -> AsAny)).parse("""{"map":{"value":3,"value2":false}}""") mustEqual (Success(MapChild(Map("value" -> 3))))
      >>[Child1]("Int" -> "tInt" -> AsInt).parse("""{"Int":3}""") mustEqual (Success(Child1(3)))
      >>[Child3]("tInt" -> ?(AsInt, 5)).parse("""{"tBool":false}""") mustEqual (Success(Child3(5, false)))
    }
//    "parse simple class with value and extra values" in {
//
//    }
//    "fail if constructor key not found" in {
//
//    }
//    "map constructor key" in {
//
//    }
  }
}
