package org.higherstate.jameson

import org.specs2.mutable.Specification
  import org.higherstate.jameson.DefaultRegistry._
  import org.higherstate.jameson.Dsl._
  import scala.util._

  class DslValuesSpec extends Specification{

  "Open Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "parse map without explicit key parsers" in {
      #*(json) mustEqual Success(map)
    }
    "not parse a json list" in {
      #*("[1,2,3]").isFailure mustEqual true
    }
    "support explicit key parsers" in {
      #*("tInt" -> AsInt, "tList" -> ||)(json) mustEqual Success(map)
    }
    "fail if explicit key parser not valid" in {
      #*("tInt" -> AsBool)(json).isFailure mustEqual true
    }
    "support required key parsers" in {
      #*("tInt" -> AsInt, "tBool" ->> AsBool, "tList" -> ||)(json) mustEqual Success(map)
    }
    "fail if required key value not found" in {
      #*("tFloat" ->> AsFloat)(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      #*("tFloat" -> AsFloat)(json).isSuccess mustEqual true
    }
    "fail on invalid json" in {
      #*("""{"tInt3":3,}""").isFailure mustEqual true
    }
    "supports key renaming" in {
      #*("tInt" -> "nInt" -> AsInt)(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports key renaming with required" in {
      #*("tBool" -> "nBool" ->> AsBool)(json) mustEqual Success(map - "tBool" + ("nBool" -> false))
    }
    "supports required ? with key not found" in {
      #*("tFloat" ->> ?(AsFloat))(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #*("tFloat" ->> ?(AsFloat, 3.5F))(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #*("tFloat" -> ?(AsFloat))(json) mustEqual Success(map)
    }
  }

  "Closed Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||)(json) mustEqual Success(map)
    }
    "fails if a key is found without explicit mapping" in {
      #!("tInt" -> AsInt, "tMap" -> #*, "tList" -> ||)(json).isFailure mustEqual true
    }
    "not parse a json list" in {
      #!("tList" -> ||)("[1,2,3]").isFailure mustEqual true
    }
    "fail if required key value not found" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" ->> AsFloat)(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" -> AsFloat)(json).isSuccess mustEqual true
    }
    "supports key renaming" in {
      #!("tInt" -> "nInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" -> AsFloat)(json) mustEqual Success(map - "tInt" + ("nInt" -> 3))
    }
    "supports required ? with key not found" in {
      #!("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" ->> ?(AsFloat))(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #*("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" ->> ?(AsFloat, 3.5F))(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #*("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" -> ?(AsFloat))(json) mustEqual Success(map)
    }
  }

  "Drop Map Parser" should {
    val json = """{"tInt":3,"tBool":false,"tMap":{"t1":1},"tList":[1,2,3]}"""
    val map = Map("tInt" -> 3, "tBool" -> false, "tMap" -> Map("t1" -> 1), "tList" -> List(1,2,3))
    "succeeded if all keys have explicit parser" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||)(json) mustEqual Success(map)
    }
    "ignores any keys which are not explicit" in {
      #^("tInt" -> AsInt, "tMap" -> #*, "tList" -> ||)(json) mustEqual Success(map - "tBool")
    }
    "not parse a json list" in {
      #^("tList" -> ||)("[1,2,3]").isFailure mustEqual true
    }
    "fail if required key value not found" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tFloat" ->> AsFloat)(json).isFailure mustEqual true
    }
    "not fail if not required key value is not found" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tFloat" -> AsFloat)(json).isSuccess mustEqual true
    }
    "supports key renaming" in {
      #^("tInt" -> "nInt" -> AsInt)(json) mustEqual Success(Map("nInt" -> 3))
    }
    "supports required ? with key not found" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" ->> ?(AsFloat))(json) mustEqual Success(map + ("tFloat" -> None))
    }
    "supports required ? with default value" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" ->> ?(AsFloat, 3.5F))(json) mustEqual Success(map + ("tFloat" -> 3.5F))
    }
    "ignores ? if not found and not required" in {
      #^("tInt" -> AsInt, "tBool" -> AsBool, "tMap" -> #*, "tList" -> ||, "tFloat" -> ?(AsFloat))(json) mustEqual Success(map)
    }
  }

  "List Parser" should {
    "succeed if is a list" in {
      ||("""[1,false,"text",[1,2,3],{}]""") mustEqual Success(List(1, false, "text", List(1,2,3), Map.empty))
    }
    "Succeed if all elements match parser" in {
      val parser = ||(AsString)
      parser("""["one","two","three"]""") mustEqual Success(List("one", "two", "three"))
    }
    "Fail if an element doesnt match parser" in {
      val parser = ||(AsBool)
      parser(""""[true, true, 3, false]""").isFailure mustEqual true
    }
    "Succeeds if empty list" in {
      ||("[]") mustEqual Success(Nil)
    }
    "Fails if json is object" in {
      ||("""{"key":"value"}""").isFailure mustEqual true
    }
  }

  "Traversable List Parser" should {
    "succeed if is a list" in {
      ¦¦("""[1,false,"text",[1,2,3],{}]""").get.toList mustEqual (List(Success(1), Success(false), Success("text"), Success(List(1,2,3)), Success(Map.empty)))
    }
    "Fail only failure element is reached" in {
      val parser = ¦¦(AsBool)
      val r = parser("""[true, true, 3, false]""").get.toList
      r(2).isFailure mustEqual true
      r should have size (3)
    }
  }
}
