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
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "parse map without explicit key parsers" in {
      #*().parse(json) mustEqual (Success(map))
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
      r must have size (3)
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
      p.parse("""{"tBool":true}""") mustEqual(Success(Right(Child2(true))))
    }
    "Succeed with nested eithers" in {
      val p = ><(><(AsInt, AsBool), ><(AsString, AsNull))
      p("3") mustEqual Success(Left(Left(3)))
      p("true") mustEqual Success(Left(Right(true)))
      p("\"text\"") mustEqual Success(Right(Left("text")))
      p("null") mustEqual Success(Right(Right(null)))
    }
    "Succeed with nested eithers and map parsers" in {
      val mp1 = #*("one" -> AsInt, "two" -> AsInt, "three" -> AsInt)
      val mp2 = #*("one" -> AsInt, "two" -> AsInt, "three" -> AsBool)
      val mp3 = #*("one" -> AsInt, "two" -> AsInt, "three" -> AsString)
      val mp4 = #*("one" -> AsInt, "two" -> AsInt, "three" -> AsNull)
      val p = ><(><(mp1, mp2), ><(mp3, mp4))
      val p2 = ><(><(><(mp1, mp2), ><(mp1, mp2)),><(><(mp1, mp2), ><(mp3, mp4)))
      p("""{"one":1,"two":2,"three":3}""") mustEqual Success(Left(Left(Map("one" -> 1, "two" -> 2, "three" -> 3))))
      p("""{"one":1,"two":2,"three":true}""") mustEqual Success(Left(Right(Map("one" -> 1, "two" -> 2, "three" -> true))))
      p("""{"one":1,"two":2,"three":"text"}""") mustEqual Success(Right(Left(Map("one" -> 1, "two" -> 2, "three" -> "text"))))
      p("""{"one":1,"two":2,"three":null}""") mustEqual Success(Right(Right(Map("one" -> 1, "two" -> 2, "three" -> null))))

      p2("""{"one":1,"two":2,"three":"text"}""") mustEqual Success(Right(Right(Left(Map("one" -> 1, "two" -> 2, "three" -> "text")))))
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
    "Succeed with a list tuple function pipe" in {
      (T(AsInt, AsLong) |> (_ + _)).parse("""[1,2]""") mustEqual Success(3)
    }
    "Succeed with a map tuple function pipe" in {
      (T("int" -> AsInt, "long" -> AsLong) |> (_ + _)).parse("""{"long":3,"int":7}""") mustEqual Success(10)
    }
    "Succeed with a map tuple function which requires a single tuple value" in {
      (T("int" -> AsInt, "long" -> AsLong) |> ((t:(Int, Long)) => t._1 + t._2)).parse("""{"long":3,"int":7}""") mustEqual Success(10)
    }
  }

  "conditional parsing" should {
    "Succeed with partial functions" in {
      val p2 = /[String, Map[String,Any]]("type"){
        case "t1"         =>  #*("value" -> AsBool)
        case "t2" | "t3"  =>  #!("value" -> AsInt, "type" -> AsAnyVal)
      }
      val r = p2.parse("""{"type":"t2","value":3}""")
      r mustEqual(Success(Map("value" -> 3, "type" -> "t2")))
    }
    "Succeed with default value on partial function" in {
      val p2 = /[String, Map[String,Any]]("type", "t1"){
        case "t1"         =>  #*("value" -> AsBool)
        case "t2" | "t3"  =>  #!("value" -> AsInt, "type" -> AsAnyVal)
      }
      val r = p2.parse("""{"value":false}""")
      r mustEqual(Success(Map("value" -> false)))
    }
    "Succeed with value match" in {
      /("type", "t1" -> #*("value" -> AsBool), "t2" -> #!("value" -> AsInt, "type" -> AsAnyVal))
        .parse("""{"type":"t2","value":3}""") mustEqual (Success(Map("value" -> 3, "type" -> "t2")))
    }
    "Success on default value with value match" in {
      /("type", "t1", "t1" -> #*("value" -> AsBool), "t2" -> #!("value" -> AsInt, "type" -> AsAnyVal))
        .parse("""{"value":true}""") mustEqual (Success(Map("value" -> true)))
    }
  }

  "tuple list parsing" should {
    "Succeed with simple 2 tuple" in {
      T(AsInt, AsString).parse("[567,\"test\"]") mustEqual Success((567,"test"))
    }
    "Succeed with nested 2 tuple" in {
      T(#*(),||()).parse("""[{"key":"value"},[1,2,3,4]]""") mustEqual Success(Map("key" -> "value"), List(1,2,3,4))
    }
    "Succeed with simple 3 tuple" in {
      T(AsInt, AsString, AsBool).parse("[567,\"test\", true]") mustEqual Success((567,"test", true))
    }
    "Succeed with default tail" in {
      T(AsInt, ?(AsString)).parse("[567]") mustEqual Success((567, None))
    }
    "Succeed with null value" in {
      T(?(AsInt,3), ?(AsString)).parse("[null, \"test\"]") mustEqual Success((3, Some("test")))
    }
    "Succeed with extended defaults" in {
      T(AsInt, ?(AsBool), ?(AsString)).parse("[123]") mustEqual Success((123, None, None))
    }
  }

  "tuple map parser" should {
    "Succeed with a simple 2 tuple" in {
      T("int" -> AsInt, "string" -> AsString).parse("""{"string":"s","int":1}""") mustEqual Success((1,"s"))
    }
    "Succeed with a simple 2 tuple with extra key pairs" in {
      T("int" -> AsInt, "string" -> AsString).parse("""{"bool":true, "string":"s","list":[1,2,3,4],"int":1}""") mustEqual Success((1,"s"))
    }
    "Succeed with a simple 2 tuple with default value" in {
      T("int" -> ?(AsInt), "string" -> AsString).parse("""{"string":"s"}""") mustEqual Success((None, "s"))
    }
    "Succeed with simple 3 tuple" in {
      T("int" -> AsInt, "string" -> AsString, "bool" -> AsBool).parse("""{"bool":false, "string":"s","int":1}""") mustEqual Success((1,"s", false))
    }
    "Succeed with a piped value" in {
      T("int" -> AsInt |> (_ + 5), "string" -> AsString).parse("""{"string":"s","int":1}""") mustEqual Success((6,"s"))
    }
    "Succeed with a range of keys" in {
      T(("int"|"INT") -> AsInt, "string" -> AsString).parse("""{"string":"s","INT":1}""") mustEqual Success((1,"s"))
    }
  }

  "try match parser" should {
    "Succeed simple left" in {
      ??(AsInt, AsString).parse("4") mustEqual Success(4)
    }
    "Succeed simple right" in {
      ??(AsInt, AsString).parse("\"test\"") mustEqual Success("test")
    }
    "Succeed class left" in {
      ??(>>[Child1], >>[Child2]).parse("""{"tInt":3}""") mustEqual Success(Child1(3))
    }
    "Succeed class right" in {
      ??(>>[Child1], >>[Child2]).parse("""{"tBool":true}""") mustEqual Success(Child2(true))
    }
  }

  "nested parser " should {
    "handle nested buffering parser" in {
      val parser = T(AsInt, ><(#*("one" -> AsInt, "three" -> AsBool), #*("one" -> AsInt, "three" -> AsInt)), /("type", "int" -> #^("value" -> AsInt), "either" -> #^("value" -> ><(#*(), AsString))), #!("value" -> AsAny))
      parser("""[2, {"one":1, "two":2, "three":3},{"value":"text","type":"either"},{"value":false}]""") mustEqual Success((2, Right(Map("one" -> 1, "two" -> 2, "three" -> 3)), Map("value" -> Right("text")), Map("value" -> false)))
    }
  }
}
