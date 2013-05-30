package org.higherstate.jameson

import org.scalatest.WordSpec
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._
import java.util
import org.scalatest.matchers.MustMatchers

class DslValuesSpec extends WordSpec with MustMatchers {

  "Open Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map(("tInt", 3), "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "parse map without explicit key parsers" in {
      asMap[Any].parse(json) mustEqual (Success(map))
    }
    "not parse a json list" in {
      asMap[Any].parse("[1,2,3]").isFailure mustEqual true
    }
    "support explicit key parsers" in {
      asMap("tInt" -> as[Int], "tList" -> asList[Any]).parse(json) mustEqual Success(map)
    }
    "fail if explicit key parser not valid" in {
      asMap("tInt" -> as[Boolean]).parse(json).isFailure mustEqual true
    }
    "support required key parsers" in {
      asMap("tInt" -> as[Int], "tBool" -> as[Boolean] is required, "tList" -> asList[Any]).parse(json) mustEqual Success(map)
    }
    "fail if required key value not found" in {
      asMap("tFloat" -> as[Float] is required).parse(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      asMap("tFloat" -> as[Float]).parse(json).isSuccess mustEqual true
    }
    "fail on invalid json" in {
      asMap[Any].parse("""{"tInt3":3,}""").isFailure mustEqual true
    }
    "supports key renaming" in {
      asMap("tInt" -> "nInt" -> as[Int]).parse(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports key renaming with required" in {
      asMap("tBool" -> "nBool" -> as[Boolean] is required).parse(json) mustEqual Success(map - "tBool" + ("nBool" -> false))
    }
    "supports required option with key not found" in {
      asMap("tFloat" -> asOption[Float] is required).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required option with default value" in {
      asMap("tFloat" -> getAsOrElse[Float](3.5F) is required).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores option if not found and not required" in {
      asMap("tFloat" -> getAs[Float]).parse(json) mustEqual Success(map)
    }
    "supports | in key name" in {
      asMap(("tint"|"tInt"|"TInt") -> "tInt" -> as[Int]).parse(json) mustEqual Success(map)
    }
    "fail if none in | for required key name" in {
      asMap(("a"|"b"|"c") -> "d" -> as[Int] is required).parse(json).isFailure mustEqual true
    }
  }


  "Drop Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map(("tInt",3), "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      (asMap("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> asMap[Any], "tList" -> asList[Any]) is excludekeys).parse(json) mustEqual Success(map)
    }
    "ignores any keys which are not explicit" in {
      (asMap("tInt" -> as[Int], "tMap" -> asMap[Any], "tList" -> asList [Any]) is excludekeys).parse(json) mustEqual Success(map - "tBool")
    }
    "not parse a json list" in {
      (asMap("tList" -> asList[Any]) is excludekeys).parse("[1,2,3]").isFailure mustEqual true
    }
    "fail if required key value not found" in {
      (asMap("tInt" -> as[Int], "tBool" -> as[Boolean], "tFloat" -> as[Float] is required) is excludekeys).parse(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      (asMap("tInt" -> as[Int], "tBool" -> as[Boolean], "tFloat" -> as[Float]) is excludekeys).parse(json).isSuccess mustEqual true
    }
    "supports key renaming" in {
      (asMap("tInt" -> "nInt" -> as[Int]) is excludekeys).parse(json) mustEqual Success(Map("nInt" -> 3))
    }
    "supports required option with key not found" in {
      (asMap("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> asMap[Any], "tList" -> asList [Any], "tFloat" -> getAs[Float] is required) is excludekeys).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required option with default value" in {
      (asMap("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> asMap[Any], "tList" -> asList [Any], "tFloat" -> getAsOrElse[Float](3.5F) is required) is excludekeys).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores option if not found and not required" in {
      (asMap("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> asMap[Any], "tList" -> asList [Any], "tFloat" -> asOption[Float]) is excludekeys).parse(json) mustEqual Success(map)
    }
  }

  "List Parser" should {
    "succeed if is a list" in {
      asList[Any].parse("""[1,false,"text",[1,2,3],{}]""") mustEqual Success(List(1, false, "text", List(1,2,3), Map.empty))
    }
    "Succeed if all elements match parser" in {
      asList[String].parse("""["one","two","three"]""") mustEqual Success(List("one", "two", "three"))
    }
    "Fail if an element doesnt match parser" in {
      asList(as[Boolean]).parse("""[true, true, 3, false]""").isFailure mustEqual true
    }
    "Succeeds if empty list" in {
      asList[Any].parse("[]") mustEqual Success(Nil)
    }
    "Fails if json is object" in {
      asList[Any].parse("""{"key":"value"}""").isFailure mustEqual true
    }
  }

  "Traversable List Parser" should {
    "succeed if is a list" in {
      asStream[Any].parse("""[1,false,"text",[1,2,3],{}]""").get.toList mustEqual (List(Success(1), Success(false), Success("text"), Success(List(1,2,3)), Success(Map.empty)))
    }
    "Fail only failure element is reached" in {
      val r = asStream(as[Boolean]).parse("""[true, true, 3, false]""").get.toList
      r(2).isFailure mustEqual true
      r must have size (3)
    }
  }

  "Either parser" should {
    "Succeed simple left" in {
      asEither[Int, String].parse("4") mustEqual(Success(Left(4)))
    }
    "Succeed simple right" in {
      asEither(as[Int], as[String]).parse("\"test\"") mustEqual(Success(Right("test")))
    }
    "Succeed class left" in {
      asEither(as[Child1], as[Child2]).parse("""{"tInt":3}""") mustEqual(Success(Left(Child1(3))))
    }
    "Succeed class right" in {
      asEither[Child1, Child2].parse("""{"tBool":true}""") mustEqual(Success(Right(Child2(true))))
    }
    "Succeed with nested eithers" in {
      val p = asEither(asEither(as[Int], as[Boolean]), asEither(as[String], as[Null]))
      p("3") mustEqual Success(Left(Left(3)))
      p("true") mustEqual Success(Left(Right(true)))
      p("\"text\"") mustEqual Success(Right(Left("text")))
      p("null") mustEqual Success(Right(Right(null)))
    }
    "Succeed with nested eithers and map parsers" in {
      val mp1 = asMap("one" -> as[Int], "two" -> as[Int], "three" -> as[Int])
      val mp2 = asMap("one" -> as[Int], "two" -> as[Int], "three" -> as[Boolean])
      val mp3 = asMap("one" -> as[Int], "two" -> as[Int], "three" -> as[String])
      val mp4 = asMap("one" -> as[Int], "two" -> as[Int], "three" -> as[Null])
      val p = asEither(asEither(mp1, mp2), asEither(mp3, mp4))
      val p2 = asEither(asEither(asEither(mp1, mp2), asEither(mp1, mp2)),asEither(asEither(mp1, mp2), asEither(mp3, mp4)))
      p("""{"one":1,"two":2,"three":3}""") mustEqual Success(Left(Left(Map("one" -> 1, "two" -> 2, "three" -> 3))))
      p("""{"one":1,"two":2,"three":true}""") mustEqual Success(Left(Right(Map("one" -> 1, "two" -> 2, "three" -> true))))
      p("""{"one":1,"two":2,"three":"text"}""") mustEqual Success(Right(Left(Map("one" -> 1, "two" -> 2, ("three", "text")))))
      p("""{"one":1,"two":2,"three":null}""") mustEqual Success(Right(Right(Map("one" -> 1, "two" -> 2, ("three", null)))))

      p2("""{"one":1,"two":2,"three":"text"}""") mustEqual Success(Right(Right(Left(Map("one" -> 1, "two" -> 2, ("three", "text"))))))
    }
  }

  "Object parser" should {
    "Succeed with correct type match" in {
      val m = new util.HashMap[String,Any]()
      m.put("key", Child1(3))
      asMap("key" -> nestedAs[Child1]).parse(m) mustEqual(Success(Map(("key", Child1(3)))))
    }
  }

  "function parser" should {
    "Succeed with a key into function" in {
      def a(a:Any) = a.toString
      asMap("key" |> a).parse("""{"key":3}""") mustEqual(Success(Map(("key", "3"))))
    }
    "Succeed with a remap" in {
      def a(a:Any) = a.toString
      asMap("key" -> "newKey" map a).parse("""{"key":3}""") mustEqual(Success(Map(("newKey", "3"))))
    }
    "Succeed with a parser" in {
      asList(as[Int] map (_.toString)).parse("""[1,2]""") mustEqual(Success(List("1","2")))
    }
    "Succeed with a key and a parser" in {
      def a(a:Int) = a.toString
      asMap("key" -> as[Int] map a).parse("""{"key":3}""") mustEqual(Success(Map(("key","3"))))
    }
    "Succeed with a key, remap and a parser" in {
      asMap("key" -> "newKey" -> as[Int] map (_ + 4)).parse("""{"key":3}""") mustEqual(Success(Map("newKey" -> 7)))
    }
    "Succeed with a list tuple function pipe" in {
      (asTuple[Int, Long] map (_ + _)).parse("""[1,2]""") mustEqual Success(3)
    }
    "Succeed with a map tuple function pipe" in {
      (asTuple("int" -> as[Int], "long" -> as[Long]) map (_ + _)).parse("""{"long":3,"int":7}""") mustEqual Success(10)
    }
    "Succeed with a map tuple function which requires a single tuple value" in {
      (asTuple("int" -> as[Int], "long" -> as[Long]) map ((t:(Int, Long)) => t._1 + t._2)).parse("""{"long":3,"int":7}""") mustEqual Success(10)
    }
  }

  "conditional parsing" should {
    "Succeed with partial functions" in {
      val p2 = matchAs[String, Map[String,Any]]("type"){
        case "t1"         =>  asMap("value" -> as[Boolean])
        case "t2" | "t3"  =>  asMap("value" -> as[Int], "type" -> as[AnyVal])
      }
      val r = p2.parse("""{"type":"t2","value":3}""")
      r mustEqual(Success(Map("value" -> 3, "type" -> "t2")))
    }
    "Succeed with default value on partial function" in {
      val p2 = matchAs[String, Map[String,Any]]("type", "t1"){
        case "t1"         =>  asMap("value" -> as[Boolean])
        case "t2" | "t3"  =>  asMap("value" -> as[Int], "type" -> as[AnyVal])
      }
      val r = p2.parse("""{"value":false}""")
      r mustEqual(Success(Map("value" -> false)))
    }
    "Succeed with value match" in {
      matchAs("type", "t1" -> asMap("value" -> as[Boolean]), "t2" -> asMap("value" -> as[Int], "type" -> as[AnyVal]))
        .parse("""{"type":"t2","value":3}""") mustEqual (Success(Map("value" -> 3, "type" -> "t2")))
    }
    "Success on default value with value match" in {
      matchAs("type", "t1", "t1" -> asMap("value" -> as[Boolean]), "t2" -> asMap("value" -> as[Int], "type" -> as[AnyVal]))
        .parse("""{"value":true}""") mustEqual (Success(Map("value" -> true)))
    }
  }

  "tuple list parsing" should {
    "Succeed with simple 2 tuple" in {
      asTuple[Int, String].parse("[567,\"test\"]") mustEqual Success((567,"test"))
    }
    "Succeed with nested 2 tuple" in {
      asTuple(asMap[Any], asList[Any]).parse("""[{"key":"value"},[1,2,3,4]]""") mustEqual Success(Map(("key","value")), List(1,2,3,4))
    }
    "Succeed with simple 3 tuple" in {
      asTuple[Int, String, Boolean].parse("[567,\"test\", true]") mustEqual Success((567,"test", true))
    }
    "Succeed with default tail" in {
      asTuple(as[Int], getAs[String]).parse("[567]") mustEqual Success((567, None))
    }
    "Succeed with null value" in {
      asTuple(getAsOrElse[Int](3), getAs[String]).parse("[null, \"test\"]") mustEqual Success((3, Some("test")))
    }
    "Succeed with extended defaults" in {
      asTuple(as[Int], getAs[Boolean], getAs[String]).parse("[123]") mustEqual Success((123, None, None))
    }
  }

  "tuple map parser" should {
    "Succeed with a simple 2 tuple" in {
      asTuple("int" -> as[Int], "string" -> as[String]).parse("""{"string":"s","int":1}""") mustEqual Success((1,"s"))
    }
    "Succeed with a simple 2 tuple with extra key pairs" in {
      asTuple("int" -> as[Int], "string" -> as[String]).parse("""{"bool":true, "string":"s","list":[1,2,3,4],"int":1}""") mustEqual Success((1,"s"))
    }
    "Succeed with a simple 2 tuple with default value" in {
      asTuple("int" -> getAs[Int], "string" -> as[String]).parse("""{"string":"s"}""") mustEqual Success((None, "s"))
    }
    "Succeed with simple 3 tuple" in {
      asTuple("int" -> as[Int], "string" -> as[String], "bool" -> as[Boolean]).parse("""{"bool":false, "string":"s","int":1}""") mustEqual Success((1,"s", false))
    }
    "Succeed with a piped value" in {
      asTuple("int" -> as[Int] map (_ + 5), "string" -> as[String]).parse("""{"string":"s","int":1}""") mustEqual Success((6,"s"))
    }
    "Succeed with a range of keys" in {
      asTuple(("int"|"INT") -> as[Int], "string" -> as[String]).parse("""{"string":"s","INT":1}""") mustEqual Success((1,"s"))
    }
  }

  "try match parser" should {
    "Succeed simple left" in {
      tryAs(as[Int], as[String]).parse("4") mustEqual Success(4)
    }
    "Succeed simple right" in {
      tryAs(as[Int], as[String]).parse("\"test\"") mustEqual Success("test")
    }
    "Succeed class left" in {
      tryAs(as[Child1], as[Child2]).parse("""{"tInt":3}""") mustEqual Success(Child1(3))
    }
    "Succeed class right" in {
      tryAs(as[Child1], as[Child2]).parse("""{"tBool":true}""") mustEqual Success(Child2(true))
    }
    "Succeed third class" in {
      tryAs(as[Child1], as[Child2], as[Int]).parse("3") mustEqual Success(3)
    }
  }

  "nested parser " should {
    "handle nested buffering parser" in {
      val parser = asTuple(as[Int], asEither(asMap("one" -> as[Int], "three" -> as[Boolean]), asMap("one" -> as[Int], "three" -> as[Int])), matchAs("type", "int" -> asMap("value" -> as[Int]), "either" -> asMap("value" -> asEither(asMap[Any], as[String])) is excludekeys), asMap("value" -> as[Any]))
      parser("""[2, {"one":1, "two":2, "three":3},{"value":"text","type":"either"},{"value":false}]""") mustEqual Success((2, Right(Map("one" -> 1, "two" -> 2, "three" -> 3)), Map("value" -> Right("text")), Map("value" -> false)))
    }
  }

  "Key match parser " should {
    "handle key finding in order" in {
      matchAs("tInt" -> as[Child1], "tBool" -> as[Child2]).parse("""{"tFloat":12.3,"tInt":123,"tBool":false}""") mustEqual Success(Child1(123))
    }
    "handle key finding out of order" in {
      matchAs("tBool" -> as[Child2], "tInt" -> as[Child1]).parse("""{"tFloat":12.3,"tInt":123,"tBool":false}""") mustEqual Success(Child2(false))
    }
  }
}
