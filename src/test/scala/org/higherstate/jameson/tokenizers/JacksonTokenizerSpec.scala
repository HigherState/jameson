package org.higherstate.jameson.tokenizers

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class JacksonTokenizerSpec extends WordSpec with MustMatchers {

  "Jackson tokenizer should" should {
    "Begin with first token" in {
      JacksonTokenizer("3").head mustEqual(LongToken(3))
      JacksonTokenizer("""{"key":"value"""").head mustEqual(ObjectStartToken)
    }
    "process key value tokens correctly" in {
      val jt = JacksonTokenizer("{\"key\":\"value\",\"key2\":[2,3,{\"key3\":{\"key4\":false}}]}")
      jt.head mustEqual(ObjectStartToken)
      jt.moveNext().head mustEqual(KeyToken("key"))
      jt.moveNext().head mustEqual(StringToken("value"))
      jt.moveNext().head mustEqual(KeyToken("key2"))
      jt.moveNext().head mustEqual(ArrayStartToken)
      jt.moveNext().head mustEqual(LongToken(2))
      jt.moveNext().head mustEqual(LongToken(3))
      jt.moveNext().head mustEqual(ObjectStartToken)
      jt.moveNext().head mustEqual(KeyToken("key3"))
      jt.moveNext().head mustEqual(ObjectStartToken)
      jt.moveNext().head mustEqual(KeyToken("key4"))
      jt.moveNext().head mustEqual(BooleanToken(false))
      jt.moveNext().head mustEqual(ObjectEndToken)
      jt.moveNext().head mustEqual(ObjectEndToken)
      jt.moveNext().head mustEqual(ArrayEndToken)
      jt.moveNext().head mustEqual(ObjectEndToken)
      jt.moveNext().head mustEqual(EndToken)
    }

    "support drop next" in {
      val jt = JacksonTokenizer("{\"key\":\"value\",\"key2\":{\"key3\":false}, \"key4\":4, \"key5\":[1,2,{\"key6\":3}]}")
      jt.head mustEqual(ObjectStartToken)
      jt.moveNext().head mustEqual(KeyToken("key"))
      jt.moveNext().head mustEqual(StringToken("value"))
      jt.moveNext().head mustEqual(KeyToken("key2"))
      jt.dropNext().head mustEqual(KeyToken("key4"))
      jt.moveNext().head mustEqual(LongToken(4))
      jt.moveNext().head mustEqual(KeyToken("key5"))
      jt.dropNext()
      jt.head mustEqual(ObjectEndToken)
      jt.moveNext().head mustEqual(EndToken)
    }
  }
}
