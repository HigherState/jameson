package org.higherstate.jameson

import org.scalatest.FunSuite
import org.higherstate.jameson.Dsl._

class NumericParsingTests extends FunSuite {

  test("Double range parsing") {
    println((>=(10.0) <(20.0))("5").isFailure)
    println((>=(10.0) <(20.0))("10").isSuccess)
    println((>=(10.0) <(20.0))("15").isSuccess)
    println((>=(10.0) <(20.0))("20").isFailure)
    println((>=(10.0) <(20.0))("25").isFailure)
    println(>(100.0)("100").isFailure)
    println(<=(50.0)("50").isSuccess)
    println((<(20.0) >=(10.0))("15").isSuccess)
  }

  test("Float range parsing") {
    println((>=(10.0F) <(20.0F))("5").isFailure)
    println((>=(10.0F) <(20.0F))("10").isSuccess)
    println((>=(10.0F) <(20.0F))("15").isSuccess)
    println((>=(10.0F) <(20.0F))("20").isFailure)
    println((>=(10.0F) <(20.0F))("25").isFailure)
    println(>(100.0F)("100").isFailure)
    println(<=(50.0F)("50").isSuccess)
    println((<(20.0F) >=(10.0F))("15").isSuccess)
  }

  test("Long range parsing") {
    println((>=(10L) <(20L))("5").isFailure)
    println((>=(10L) <(20L))("10").isSuccess)
    println((>=(10L) <(20L))("15").isSuccess)
    println((>=(10L) <(20L))("20").isFailure)
    println((>=(10L) <(20L))("25").isFailure)
    println(>(100L)("100").isFailure)
    println(<=(50L)("50").isSuccess)
    println((<(20L) >=(10L))("15").isSuccess)
  }

  test("Int range parsing") {
    println((>=(10) <(20))("5").isFailure)
    println((>=(10) <(20))("10").isSuccess)
    println((>=(10) <(20))("15").isSuccess)
    println((>=(10) <(20))("20").isFailure)
    println((>=(10) <(20))("25").isFailure)
    println(>(100)("100").isFailure)
    println(<=(50)("50").isSuccess)
    println((<(20) >=(10))("15").isSuccess)
  }
}
