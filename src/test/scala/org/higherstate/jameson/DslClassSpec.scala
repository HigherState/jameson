package org.higherstate.jameson

import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class DslClassSpec extends WordSpec with MustMatchers  {

  "Simple class parsing" should {
    "parse simple class with value" in {
      as[Child1].parse("""{"tInt":3}""") mustEqual (Success(Child1(3)))
    }
    "parse simple class with value and extra values" in {
      as[Child2].parse("""{"tInt":3,"tBool":false}""") mustEqual (Success(Child2(false)))
    }
    "parse class with nested class" in {
      as[NestedChild].parse("""{"child1":{"tInt":3},"child2":{"tBool":false}}""") mustEqual (Success(NestedChild(Child1(3), Child2(false))))
    }
    "parse class with nested map" in {
      as[MapChild]("map" -> asMap("value" -> as[Any]) is excludekeys).parse("""{"map":{"value":3,"value2":false}}""") mustEqual (Success(MapChild(Map("value" -> 3))))
    }
    "parse class with key remapping" in {
      as[Child1]("Int" -> "tInt" -> as[Int]).parse("""{"Int":3}""") mustEqual (Success(Child1(3)))
    }
    "parse class with default" in {
      as[Child3]("tInt" -> getAsOrElse[Int](5)).parse("""{"tBool":false}""") mustEqual (Success(Child3(5, false)))
    }
    "parse class with multiple possible key values" in {
      as[Child1](("a"|"b") -> "tInt" -> as[Int]).parse("""{"b":3}""") mustEqual (Success(Child1(3)))
    }
    "parse class with an internal function piping" in {
      as[Child3]("int" -> "tInt" -> as [Int] map (_ * 2), "tBool" -> getAsOrElse(false)).parse("""{"int":12}""")  mustEqual (Success(Child3(24, false)))
    }
    "ignore any unrequired key value pairs" in {
      as[Child3].parse("""{"tMap":{"key1":"value1","key2":[1,2,3,4]},"tBool":false,"tList":[1,2,[3,4,5],{"k":"v"},4,5], "tInt":3}""") mustEqual Success(Child3(3, false))
    }
  }

  "Conditional class parsing" should {
    "Handle matching on string values" in {
      matchAs("type", "int" -> as[Child1]("value" -> "tInt" -> as[Int]), "bool" -> as[Child2]("value" -> "tBool" -> as[Boolean])).parse("""{"type":"bool","value":false}""") mustEqual (Success(Child2(false)))
    }

//    "Handle matching on the class name" in {
//      matchAs("class", as[Child1]("value" -> "tInt" -> as[Int]), as[Child2]("value" -> "tBool" -> as[Boolean]), as[Child3]("value1" -> "tBool" -> as[Boolean], "value2" -> "tInt" -> as[Int]))
//      .parse("""{"class":"Child3", "value1":true,"value2":4}""") mustEqual(Success(Child3(4, true)))
//    }

    "Handle matching with a partial function" in {
      matchAs[String, Collections]("type"){
        case "map" => as[MapChild]
        case "list" => as[ListChild]
      }.parse("""{"type":"list","list":[1,2,3,4]}""") mustEqual (Success(ListChild(List(1,2,3,4))))
    }
  }

  "Either class parsing" should {
    "Handle a correct left parse" in {
      asEither(as[Child1], as[Child2]).parse("""{"tInt":3}""") mustEqual (Success(Left(Child1(3))))
    }
    "Handle a correct right parse" in {
      asEither(as[Child1], as[Child2]).parse("""{"tBool":true}""") mustEqual (Success(Right(Child2(true))))
    }
  }

  "parse as object" should {
    val classParser = matchAs("animalType", "dog" -> as[Canine], "cat" -> as[Feline])
    val pet:Try[Object] = classParser("""{"animalType":"dog","name":"rufus","age":3}""")
    pet mustEqual Success(Canine("dog", "rufus", 3))
  }
}


class Pet(animalType:String, name:String, age:Int)

case class Canine(animalType:String, name:String, age:Int)
case class Feline(animalType:String, name:String, age:Int)

