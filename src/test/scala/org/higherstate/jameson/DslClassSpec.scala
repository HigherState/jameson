package org.higherstate.jameson

import org.specs2.mutable.Specification
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._

class DslClassSpec extends Specification {

  "Simple class parsing" should {
    "parse simple class with value" in {
      val parser = >>[Child1]
      parser("""{"tInt":3}""") mustEqual (Success(Child1(3)))
    }
    "parse simple class with value and extra values" in {
      val parser2 = >>[Child2]
      parser2("""{"tInt":3,"tBool":false}""") mustEqual (Success(Child2(false)))
    }
    "fail if constructor key not found" in {

    }
    "map constructor key" in {

    }
  }
}
