package org.higherstate.jameson

import org.scalatest.{MustMatchers, WordSpec}
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.SymbolicDsl._
import org.higherstate.jameson.failures._
import java.util

class SymbolicDslValuesSpec extends WordSpec with MustMatchers {

  "Open Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "parse map without explicit key parsers" in {
      #*().parse(json) mustEqual (Success(map))
    }
    "not parse a json list" in {
      #*().parse("[1,2,3]").isLeft mustEqual true
    }
    "support explicit key parsers" in {
      #*("tInt" -> as[Int], "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "fail if explicit key parser not valid" in {
      #*("tInt" -> as[Boolean]).parse(json).isLeft mustEqual true
    }
    "support required key parsers" in {
      #*("tInt" -> as[Int], "tBool" ->> as[Boolean], "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "fail if required key value not found" in {
      #*("tFloat" ->> as[Float]).parse(json).isLeft mustEqual true
    }
    "not fail if not required key value is not found" in {
      #*("tFloat" -> as[Float]).parse(json).isRight mustEqual true
    }
    "fail on invalid json" in {
      #*().parse("""{"tInt3":3,}""").isLeft mustEqual true
    }
    "supports key renaming" in {
      #*("tInt" -> "nInt" -> as[Int]).parse(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports key renaming with required" in {
      #*("tBool" -> "nBool" ->> as[Boolean]).parse(json) mustEqual Success(map - "tBool" + ("nBool" -> false))
    }
    "supports required ? with key not found" in {
      #*("tFloat" ->> ?[Float]).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #*("tFloat" ->> ?[Float](3.5F)).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #*("tFloat" -> ?[Float]).parse(json) mustEqual Success(map)
    }
    "supports | in key name" in {
      #*(("tint"|"tInt"|"TInt") -> "tInt" -> as[Int]).parse(json) mustEqual Success(map)
    }
    "fail if none in | for required key name" in {
      #*(("a"|"b"|"c") -> "d" ->> as[Int]).parse(json).isLeft mustEqual true
    }
  }

  "Closed Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      #!("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "fails if a key is found without explicit mapping" in {
      #!("tInt" -> as[Int], "tMap" -> #*(), "tList" -> ||()).parse(json).isLeft mustEqual true
    }
    "not parse a json list" in {
      #!("tList" -> ||()).parse("[1,2,3]").isLeft mustEqual true
    }
    "fail if required key value not found" in {
      #!("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> as[Float]).parse(json).isLeft mustEqual true
    }
    "not fail if not required key value is not found" in {
      #!("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" -> as[Float]).parse(json).isRight mustEqual true
    }
    "supports key renaming" in {
      #!("tInt" -> "nInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" -> as[Float]).parse(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports required ? with key not found" in {
      #!("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?[Float]).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #*("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?[Float](3.5F)).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #*("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" -> ?(as[Float])).parse(json) mustEqual Success(map)
    }
  }

  "Drop Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      #^("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||()).parse(json) mustEqual Success(map)
    }
    "ignores any keys which are not explicit" in {
      #^("tInt" -> as[Int], "tMap" -> #*(), "tList" -> ||()).parse(json) mustEqual Success(map - "tBool")
    }
    "not parse a json list" in {
      #^("tList" -> ||()).parse("[1,2,3]").isLeft mustEqual true
    }
    "fail if required key value not found" in {
      #^("tInt" -> as[Int], "tBool" -> as[Boolean], "tFloat" ->> as[Float]).parse(json).isLeft mustEqual true
    }
    "not fail if not required key value is not found" in {
      #^("tInt" -> as[Int], "tBool" -> as[Boolean], "tFloat" -> as[Float]).parse(json).isRight mustEqual true
    }
    "supports key renaming" in {
      #^("tInt" -> "nInt" -> as[Int]).parse(json) mustEqual Success(Map("nInt" -> 3))
    }
    "supports required ? with key not found" in {
      #^("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?[Float]).parse(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #^("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" ->> ?[Float](3.5F)).parse(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #^("tInt" -> as[Int], "tBool" -> as[Boolean], "tMap" -> #*(), "tList" -> ||(), "tFloat" -> ?[Float]).parse(json) mustEqual Success(map)
    }
  }

  "List Parser" should {
    "succeed if is a list" in {
      ||().parse("""[1,false,"text",[1,2,3],{}]""") mustEqual Success(List(1, false, "text", List(1,2,3), Map.empty))
    }
    "Succeed if all elements match parser" in {
      ||(as[String]).parse("""["one","two","three"]""") mustEqual Success(List("one", "two", "three"))
    }
    "Fail if an element doesnt match parser" in {
      ||(as[Boolean]).parse("""[true, true, 3, false]""").isLeft mustEqual true
    }
    "Succeeds if empty list" in {
      ||().parse("[]") mustEqual Success(Nil)
    }
    "Fails if json is object" in {
      ||().parse("""{"key":"value"}""").isLeft mustEqual true
    }
  }

  "Traversable List Parser" should {
    "succeed if is a list" in {
      ¦¦().parse("""[1,false,"text",[1,2,3],{}]""").foreach(_.toList mustEqual (List(Success(1), Success(false), Success("text"), Success(List(1,2,3)), Success(Map.empty))))
    }
//    "Fail only failure element is reached" in {
//      ¦¦(as[Boolean]).parse( """[true, true, 3, false]""").foreach{ l =>
//        val r = l.toList
//        r(2).isLeft mustEqual true
//        r must have size (3)
//      }
//    }
  }

  "Either parser" should {
    "Succeed simple left" in {
      ^(as[Int], as[String]).parse("4") mustEqual(Success(Left(4)))
    }
    "Succeed simple right" in {
      ^(as[Int], as[String]).parse("\"test\"") mustEqual(Success(Right("test")))
    }
    "Succeed class left" in {
      ^(as[Child1], as[Child2]).parse("""{"tInt":3}""") mustEqual(Success(Left(Child1(3))))
    }
    "Succeed class right" in {
      val p = ^(as[Child1], as[Child2])
      p.parse("""{"tBool":true}""") mustEqual(Success(Right(Child2(true))))
    }
    "Succeed with nested eithers" in {
      val p = ^(^(as[Int], as[Boolean]), ^(as[String], as[Null]))
      p("3") mustEqual Success(Left(Left(3)))
      p("true") mustEqual Success(Left(Right(true)))
      p("\"text\"") mustEqual Success(Right(Left("text")))
      p("null") mustEqual Success(Right(Right(null)))
    }
    "Succeed with nested eithers and map parsers" in {
      val mp1 = #*("one" -> as[Int], "two" -> as[Int], "three" -> as[Int])
      val mp2 = #*("one" -> as[Int], "two" -> as[Int], "three" -> as[Boolean])
      val mp3 = #*("one" -> as[Int], "two" -> as[Int], "three" -> as[String])
      val mp4 = #*("one" -> as[Int], "two" -> as[Int], "three" -> as[Null])
      val p = ^(^(mp1, mp2), ^(mp3, mp4))
      val p2 = ^(^(^(mp1, mp2), ^(mp1, mp2)),^(^(mp1, mp2), ^(mp3, mp4)))
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
      ||(as[Int] |> (_.toString)).parse("""[1,2]""") mustEqual(Success(List("1","2")))
    }
    "Succeed with a key and a parser" in {
      def a(a:Int) = a.toString
      #*("key" -> as[Int] |> a).parse("""{"key":3}""") mustEqual(Success(Map("key" -> "3")))
    }
    "Succeed with a key, remap and a parser" in {
      #*("key" -> "newKey" -> as[Int] |> (_ + 4)).parse("""{"key":3}""") mustEqual(Success(Map("newKey" -> 7)))
    }
    "Succeed with a list tuple function pipe" in {
      (T(as[Int], as[Long]) |> (_ + _)).parse("""[1,2]""") mustEqual Success(3)
    }
    "Succeed with a map tuple function pipe" in {
      (T("int" -> as[Int], "long" -> as[Long]) |> (_ + _)).parse("""{"long":3,"int":7}""") mustEqual Success(10)
    }
    "Succeed with a map tuple function which requires a single tuple value" in {
      (T("int" -> as[Int], "long" -> as[Long]) |> ((t:(Int, Long)) => t._1 + t._2)).parse("""{"long":3,"int":7}""") mustEqual Success(10)
    }
  }

  "conditional parsing" should {
    "Succeed with partial functions" in {
      val p2 = /[String, Map[String,Any]]("type"){
        case "t1"         =>  #*("value" -> as[Boolean])
        case "t2" | "t3"  =>  #!("value" -> as[Int], "type" -> as[AnyVal])
      }
      val r = p2.parse("""{"type":"t2","value":3}""")
      r mustEqual(Success(Map("value" -> 3, "type" -> "t2")))
    }
    "Succeed with default value on partial function" in {
      val p2 = /[String, Map[String,Any]]("type", "t1"){
        case "t1"         =>  #*("value" -> as[Boolean])
        case "t2" | "t3"  =>  #!("value" -> as[Int], "type" -> as[AnyVal])
      }
      val r = p2.parse("""{"value":false}""")
      r mustEqual(Success(Map("value" -> false)))
    }
    "Succeed with value match" in {
      /("type", "t1" -> #*("value" -> as[Boolean]), "t2" -> #!("value" -> as[Int], "type" -> as[AnyVal]))
        .parse("""{"type":"t2","value":3}""") mustEqual (Success(Map("value" -> 3, "type" -> "t2")))
    }
    "Success on default value with value match" in {
      /("type", "t1", "t1" -> #*("value" -> as[Boolean]), "t2" -> #!("value" -> as[Int], "type" -> as[AnyVal]))
        .parse("""{"value":true}""") mustEqual (Success(Map("value" -> true)))
    }
  }

  "tuple list parsing" should {
    "Succeed with simple 2 tuple" in {
      T(as[Int], as[String]).parse("[567,\"test\"]") mustEqual Success((567,"test"))
    }
    "Succeed with nested 2 tuple" in {
      T(#*(),||()).parse("""[{"key":"value"},[1,2,3,4]]""") mustEqual Success(Map("key" -> "value") -> List(1,2,3,4))
    }
    "Succeed with simple 3 tuple" in {
      T(as[Int], as[String], as[Boolean]).parse("[567,\"test\", true]") mustEqual Success((567,"test", true))
    }
    "Succeed with default tail" in {
      T(as[Int], ?[String]).parse("[567]") mustEqual Success((567, None))
    }
    "Succeed with null value" in {
      T(?[Int](3), ?[String]).parse("[null, \"test\"]") mustEqual Success((3, Some("test")))
    }
    "Succeed with extended defaults" in {
      T(as[Int], ?[Boolean], ?[String]).parse("[123]") mustEqual Success((123, None, None))
    }
  }

  "tuple map parser" should {
    "Succeed with a simple 2 tuple" in {
      T("int" -> as[Int], "string" -> as[String]).parse("""{"string":"s","int":1}""") mustEqual Success((1,"s"))
    }
    "Succeed with a simple 2 tuple with extra key pairs" in {
      T("int" -> as[Int], "string" -> as[String]).parse("""{"bool":true, "string":"s","list":[1,2,3,4],"int":1}""") mustEqual Success((1,"s"))
    }
    "Succeed with a simple 2 tuple with default value" in {
      T("int" -> ?[Int], "string" -> as[String]).parse("""{"string":"s"}""") mustEqual Success((None, "s"))
    }
    "Succeed with simple 3 tuple" in {
      T("int" -> as[Int], "string" -> as[String], "bool" -> as[Boolean]).parse("""{"bool":false, "string":"s","int":1}""") mustEqual Success((1,"s", false))
    }
    "Succeed with a piped value" in {
      T("int" -> as[Int] |> (_ + 5), "string" -> as[String]).parse("""{"string":"s","int":1}""") mustEqual Success((6,"s"))
    }
    "Succeed with a range of keys" in {
      T(("int"|"INT") -> as[Int], "string" -> as[String]).parse("""{"string":"s","INT":1}""") mustEqual Success((1,"s"))
    }
  }

  "try match parser" should {
    "Succeed simple left" in {
      ??(as[Int], as[String]).parse("4") mustEqual Success(4)
    }
    "Succeed simple right" in {
      ??(as[Int], as[String]).parse("\"test\"") mustEqual Success("test")
    }
    "Succeed class left" in {
      ??(as[Child1], as[Child2]).parse("""{"tInt":3}""") mustEqual Success(Child1(3))
    }
    "Succeed class right" in {
      ??(as[Child1], as[Child2]).parse("""{"tBool":true}""") mustEqual Success(Child2(true))
    }
    "Succeed third class" in {
      ??(as[Child1], as[Child2], as[Int]).parse("3") mustEqual Success(3)
    }
  }

  "nested parser " should {
    "handle nested buffering parser" in {
      val parser = T(as[Int], ^(#*("one" -> as[Int], "three" -> as[Boolean]), #*("one" -> as[Int], "three" -> as[Int])), /("type", "int" -> #^("value" -> as[Int]), "either" -> #^("value" -> ^(#*(), as[String]))), #!("value" -> as[Any]))
      parser("""[2, {"one":1, "two":2, "three":3},{"value":"text","type":"either"},{"value":false}]""") mustEqual Success((2, Right(Map("one" -> 1, "two" -> 2, "three" -> 3)), Map("value" -> Right("text")), Map("value" -> false)))
    }
  }

  "Key match parser " should {
    "handle key finding in order" in {
      /("tInt" -> as[Child1], "tBool" -> as[Child2]).parse("""{"tFloat":12.3,"tInt":123,"tBool":false}""") mustEqual Success(Child1(123))
    }
    "handle key finding out of order" in {
      /("tBool" -> as[Child2], "tInt" -> as[Child1]).parse("""{"tFloat":12.3,"tInt":123,"tBool":false}""") mustEqual Success(Child2(false))
    }
  }
}
