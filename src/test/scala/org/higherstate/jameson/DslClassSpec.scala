package org.higherstate.jameson

import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import org.higherstate.jameson.failures._
import org.scalatest.{MustMatchers, WordSpec}

class DslClassSpec extends WordSpec with MustMatchers  {

  "Simple class parsing" should {
    "parse simple class with value" in {
      as[Child3]("tInt" -> ?(as [Int], 5)).parse("""{"tBool":false}""") mustEqual (Success(Child3(5, false)))
      as[Child1](("a"|"b") -> "tInt" -> as [Int]).parse("""{"b":3}""") mustEqual (Success(Child1(3)))
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
    "handle just key remapping" in {
      as[Child3]("int" -> "tInt").parse("""{"int":256, "tBool":false}""") mustEqual (Success(Child3(256, false)))
      as[Child3](("int"|"INT"|"tInt") -> "tInt").parse("""{"int":256, "tBool":false}""") mustEqual (Success(Child3(256, false)))
    }
  }

  "Conditional class parsing" should {
    "Handle matching on string values" in {
      matchAs("type", "int" -> as[Child1]("value" -> "tInt" -> as[Int]), "bool" -> as[Child2]("value" -> "tBool" -> as[Boolean])).parse("""{"type":"bool","value":false}""") mustEqual (Success(Child2(false)))
    }

    "Handle matching on the class name" in {
      matchAs("class", as[Child1]("value" -> "tInt" -> as[Int]), as[Child2]("value" -> "tBool" -> as[Boolean]), as[Child3]("value1" -> "tBool" -> as[Boolean], "value2" -> "tInt" -> as[Int]))
      .parse("""{"class":"Child3", "value1":true,"value2":4}""") mustEqual(Success(Child3(4, true)))
    }

    "Handle matching on the class name with map" in {
      matchAs("class", as[Child1]("value" -> "tInt" -> as[Int]) map (_.toString), as[Child2]("value" -> "tBool" -> as[Boolean]) map (_.toString), as[Child3]("value1" -> "tBool" -> as[Boolean], "value2" -> "tInt" -> as[Int]) map (_.toString))
        .parse("""{"class":"Child3", "value1":true,"value2":4}""") mustEqual(Success("Child3(4,true)"))
    }


    "Handle matching with a partial function" in {
      matchAs[String, Collections]("type"){
        case "map"  => as[MapChild]
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
    "convert json string" in {
      val classParser = matchAs("animalType", "dog" -> as[Canine], "cat" -> as[Feline])
      val pet: Valid[Object] = classParser("""{"animalType":"dog","name":"rufus","age":3}""")
      pet mustEqual Success(Canine("dog", "rufus", 3))
    }
  }

  "parse with extracting key groups" should {
    "Handle extracting 2 keys" in {
      val p = as[PartialNestedChild](("tInt"&"tBool") -> "child3" -> as[Child3])
      p.parse("""{"tInt":3,"tBool":false,"tFloat":3.4,"tString":"test"}""") mustEqual (Success(PartialNestedChild(3.4F, Child3(3,false), "test")))
    }
    "Handle double extracting 2 keys" in {
      val p = as[DoubleNestedChild](
        ("tInt"&"tBool") -> "child3" -> as[Child3],
        "tString" -> as [String] map (_ + "2"),
        ("tInt2"&"tBool2") -> "child32" -> as[Child3](
          "tInt2" -> "tInt" -> as [Int],
          "tBool2" -> "tBool" -> as [Boolean]
        )
      )
      p.parse("""{"tInt":3,"tInt2":6, "tBool":false, "tBool2":true, "tFloat":3.4,"tString":"test"}""") mustEqual (Success(DoubleNestedChild(3.4F, Child3(3,false), Child3(6, true), "test2")))
    }
  }

  "Parse with numeric validation" should {
    "handle successes" in {
      as[Child1]("tInt" -> as [Int] > 3 <= 1000).parse("""{"tInt":55}""") mustEqual (Success(Child1(55)))
      as[ChildDouble]("tDouble" -> as [Double] > -30).parse("""{"tDouble":-25}""") mustEqual (Success(ChildDouble(-25.0)))
      as[ChildByte]("tByte" -> as [Byte] <= 4).parser("""{"tByte":4}""") mustEqual (Success(ChildByte(4)))
    }
    "handle failures" in {
      as[Child1]("tInt" -> as [Int] > 3 <= 1000).parse("""{"tInt":3}""").isLeft mustEqual (true)
      as[Child1]("tInt" -> as [Int] > 3 <= 1000).parse("""{"tInt":1025}""").isLeft mustEqual (true)
      as[ChildDouble]("tDouble" -> as [Double] > -30).parse("""{"tDouble":-35}""").isLeft mustEqual (true)
    }
  }

  "parse with override" should {
    "ignore set value" in {
      val p = as[Canine]("age" -> to(13))
      p.parse("""{"animalType":"dog","name":"bob","age":12}""")  mustEqual Success(Canine("dog", "bob", 13))
      p.parse("""{"animalType":"dog","name":"bob"}""")  mustEqual Success(Canine("dog", "bob", 13))
    }

  }
}


class Pet(animalType:String, name:String, age:Int)

case class Canine(animalType:String, name:String, age:Int)
case class Feline(animalType:String, name:String, age:Int)

