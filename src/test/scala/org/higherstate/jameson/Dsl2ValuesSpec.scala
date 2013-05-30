package org.higherstate.jameson

import org.scalatest.{FunSuite}
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl2._

class Dsl2Tests extends FunSuite {

  test("blah") {
    val p = "key" -> as [Int] >= 3 < 4 minlength 4 is email map (_ + 3) is required

    val p3 = "first" -> "second"
    val p2 = asList(as [Int] > 3 <= 10) maxlength 10
    p2("[4,5,6]")
    //val t2 = "tuple" -> as [Int, Int] map ((a,b) => a + b) required
    val list = as[Int, Double, Double]
    val f = asList[Int] is nonempty
    //val m = asMap[Int]
    val t = as(as [Int] >= 3 < 10, getAs [Boolean])
    //val t2 = as[Int, Boolean, String] map (_ + _ + _)
    println(p)
    println(t)


  }


}
