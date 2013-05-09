package org.higherstate.jameson

import org.specs2.mutable.Specification
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import scala.util._
import java.util

class DslValuesSpec extends Specification{

  "Open Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "parse map without explicit key parsers" in {
      #*().parse(json) mustEqual Success(map)
    }
    "not parse a json list" in {
      #*().parse("[1,2,3]").isFailure mustEqual true
    }
    "support explicit key parsers" in {
      #*("tInt" -> AsInt, "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "fail if explicit key parser not valid" in {
      #*("tInt" -> AsBool).parse(json).isFailure mustEqual true
    }
    "support required key parsers" in {
      #*("tInt" -> AsInt, "tBool" ->> AsBool, "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "fail if required key value not found" in {
      #*("tFloat" ->> AsFloat).parse(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      #*("tFloat" -> AsFloat).parse(json).isSuccess mustEqual true
    }
    "fail on invalid json" in {
      #*().parse("""{"tInt3":3,}""").isFailure mustEqual true
    }
    "supports key renaming" in {
      #*("tInt" -> "nInt" -> AsInt).parse(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports key renaming with required" in {
      #*("tBool" -> "nBool" ->> AsBool).parse(json) mustEqual Success(map - "tBool" + ("nBool" -> false))
    }
    "supports required ? with key not found" in {
      #*("tFloat" ->> ?(AsFloat)).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #*("tFloat" ->> ?(AsFloat, 3.5F)).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #*("tFloat" -> ?(AsFloat)).parse(json) mustEqual Success(map)
    }
    "supports | in key name" in {
      #*(("tint"|"tInt"|"TInt") -> "tInt" -> AsInt).parse(json) mustEqual Success(map)
    }
    "fail if none in | for required key name" in {
      #*(("a"|"b"|"c") -> "d" ->> AsInt).parse(json).isFailure mustEqual true
    }
  }

  "Closed Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "fails if a key is found without explicit mapping" in {
      #!("tInt" -> AsInt, "tMap" -> #*(), "tList" -> ||()).parse(json).isFailure mustEqual true
    }
    "not parse a json list" in {
      #!("tList" -> ||()).parse("[1,2,3]").isFailure mustEqual true
    }
    "fail if required key value not found" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> AsFloat).parse(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" -> AsFloat).parse(json).isSuccess mustEqual true
    }
    "supports key renaming" in {
      #!("tInt" -> "nInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" -> AsFloat).parse(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports required ? with key not found" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?(AsFloat)).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #*("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?(AsFloat, 3.5F)).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #*("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" -> ?(AsFloat)).parse(json) mustEqual Success(map)
    }
  }

  "Drop Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "ignores any keys which are not explicit" in {
      #^("tInt" -> AsInt, "tMap" -> #*(), "tList" -> ||()).parse(json) mustEqual Success(map - "tBool")
    }
    "not parse a json list" in {
      #^("tList" -> ||()).parse("[1,2,3]").isFailure mustEqual true
    }
    "fail if required key value not found" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tFloat" ->> AsFloat).parse(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tFloat" -> AsFloat).parse(json).isSuccess mustEqual true
    }
    "supports key renaming" in {
      #^("tInt" -> "nInt" -> AsInt).parse(json) mustEqual Success(Map("nInt" -> 3))
    }
    "supports required ? with key not found" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?(AsFloat)).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?(AsFloat, 3.5F)).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*(), "tList" -> ||(), "tFloat" -> ?(AsFloat)).parse(json) mustEqual Success(map)
    }
  }

  "List Parser" should {
    "succeed if is a list" in {
      ||().parse("""[1,false,"text",[1,2,3],{}]""") mustEqual Success(List(1, false, "text", List(1,2,3), Map.empty))
    }
    "Succeed if all elements match parser" in {
      ||(AsString).parse("""["one","two","three"]""") mustEqual Success(List("one", "two", "three"))
    }
    "Fail if an element doesnt match parser" in {
      ||(AsBool).parse("""[true, true, 3, false]""").isFailure mustEqual true
    }
    "Succeeds if empty list" in {
      ||().parse("[]") mustEqual Success(Nil)
    }
    "Fails if json is object" in {
      ||().parse("""{"key":"value"}""").isFailure mustEqual true
    }
  }

  "Traversable List Parser" should {
    "succeed if is a list" in {
      ¦¦().parse("""[1,false,"text",[1,2,3],{}]""").get.toList mustEqual (List(Success(1), Success(false), Success("text"), Success(List(1,2,3)), Success(Map.empty)))
    }
    "Fail only failure element is reached" in {
      val r = ¦¦(AsBool).parse("""[true, true, 3, false]""").get.toList
      r(2).isFailure mustEqual true
      r should have size (3)
    }
  }

  "Either parser" should {
    "Succeed simple left" in {
      ><(AsInt, AsString).parse("4") mustEqual(Success(Left(4)))
    }
    "Succeed simple right" in {
      ><(AsInt, AsString).parse("\"test\"") mustEqual(Success(Right("test")))
    }
    "Succeed class left" in {
      ><(>>[Child1], >>[Child2]).parse("""{"tInt":3}""") mustEqual(Success(Left(Child1(3))))
    }
    "Succeed class right" in {
      val p = ><(>>[Child1], >>[Child2])
      p.parse("""{"tBool":true}""") mustEqual(Success(Left(Child2(true))))
    }
  }

  "Object parser" should {
    "Succeed with correct type match" in {
      val m = new util.HashMap[String,Any]()
      m.put("key", Child1(3))
      #*("key" -> AsAnyRef[Child1]).parse(m) mustEqual(Success(Map("key" -> Child1(3))))
    }
  }

  "function parser" should {
    "Succeed with a key into function" in {
      def a(a:Any) = a.toString
      #*("key" |> a).parse("""{"key":3}""") mustEqual(Success(Map("key" -> "3")))
    }
    "Succeed with a remap" in {
      def a(a:Any) = a.toString
      #*("key" -> "newKey" |> a).parse("""{"key":3}""") mustEqual(Success(Map("newKey" -> "3")))
    }
    "Succeed with a parser" in {
      ||(AsInt |> (_.toString)).parse("""[1,2]""") mustEqual(Success(List("1","2")))
    }
    "Succeed with a key and a parser" in {
      def a(a:Int) = a.toString
      #*("key" -> AsInt |> a).parse("""{"key":3}""") mustEqual(Success(Map("key" -> "3")))
    }
    "Succeed with a key, remap and a parser" in {
      #*("key" -> "newKey" -> AsInt |> (_ + 4)).parse("""{"key":3}""") mustEqual(Success(Map("newKey" -> 7)))
    }
  }
}
