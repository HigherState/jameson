package org.higherstate.jameson

import org.scalatest.FunSuite
import org.higherstate.jameson.Dsl._

class NumericParsingTests extends FunSuite {

  test("Double range parsing") {
    println(AsDouble(>=(10.0), <(20.0)).parse("5").isFailure)
    println(AsDouble(>=(10.0), <=(20.0)).parse("10").isSuccess)
    println(AsDouble(>=(10.0), <(20.0)).parse("15").isSuccess)
    println(AsDouble(>=(10.0), <(20.0)).parse("20").isFailure)
    println(AsDouble(>=(10.0), <(20.0)).parse("25").isFailure)
  }

  test("Long range parsing") {
    println(AsLong(>=(10L), <(20L)).parse("5").isFailure)
    println(AsLong(>=(10L), <(20L)).parse("10").isSuccess)
    println(AsLong(>=(10L), <(20L)).parse("15").isSuccess)
    println(AsLong(>=(10L), <(20L)).parse("20").isFailure)
    println(AsLong(>=(10L), <(20L)).parse("25").isFailure)
  }
}
