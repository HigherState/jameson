package org.higherstate.jameson

import org.scalatest.WordSpec
import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import org.higherstate.jameson.failures._
import org.scalatest.matchers.MustMatchers

class DslStreamParserSpec extends WordSpec with MustMatchers {

  "Stream Parser" should {
    "succeed if is a list" in {
      asStream[Any].parse("""[1,false,"text",[1,2,3],{}]""").map(s => s.toList mustEqual (List(Success(1), Success(false), Success("text"), Success(List(1,2,3)), Success(Map.empty))))
    }
    "Fail only failure element is reached" in {
      asStream(as[Boolean]).parse("""[true, true, 3, false]""").map { s =>
        val r = s.toList
        r(2).isFailure mustEqual true
        r must have size (3)
      }

    }
  }
}
