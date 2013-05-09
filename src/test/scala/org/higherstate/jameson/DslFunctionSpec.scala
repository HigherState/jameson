package org.higherstate.jameson

import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._
import org.higherstate.jameson.parsers._
import reflect.runtime.universe._
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class DslFunctionSpec extends WordSpec with MustMatchers  {

  def testFunc(name1:String):String = name1
  "temp" should {
    "temp" in {
      val f = FunctionParser1Arg[String, String](testFunc(_))
      //println(f.apply())
    }
  }

  case class FunctionParser1Arg[T, U](func:(T => U))(implicit typeTag:TypeTag[T => U]) {

    //println(reflect.runtime.universe.)
    //val tmp = implicitly[T => U]

    def apply(t:T): Success[U] = ???
  }
}
