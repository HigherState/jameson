package org.higherstate.jameson.tokenizers

import org.specs2.mutable.Specification
import scala.util._
import com.fasterxml.jackson.core.JsonFactory

class JacksonTokenizerSpec extends Specification{

  "JacksonTokenizer" should {
    "breakdown valid json map into an iterable" in {
      val json = """{"one":1, "null":null, "true":true,"two":2.0,"text":"text","array":[1,2,3],"map":{"value":false}}"""
      val jsonFactory = new JsonFactory()
      val jp = jsonFactory.createJsonParser(json)
      val r = JacksonTokenizer(jp).toList
      r.head mustEqual (Success(ObjectStartToken))
      r.last mustEqual (Success(ObjectEndToken))
    }
  }
}
