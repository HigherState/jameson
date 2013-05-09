package org.higherstate.jameson

import org.specs2.mutable.Specification
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._
import org.higherstate.jameson.parsers.FunctionParser1Arg

class DslFunctionSpec extends Specification {

  "temp" should {
    "temp" in {
      val f = FunctionParser1Arg((s:String) => s.toString)
    }
  }
}
