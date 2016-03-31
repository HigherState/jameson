package org.higherstate.jameson.tokenizers

import org.scalatest.{MustMatchers, WordSpec}

class ScalaTokenizerSpec extends WordSpec with MustMatchers {

  "Jackson tokenizer should" should {
    "process key value tokens correctly" in {
      val map = Map("key" -> "value", "key2" -> List(2, 3, Map("key3" -> Map("key4" -> false)), 5))
      val jt = ScalaTokenizer(map)
      jt.head mustEqual(ObjectStartToken)
      var ph = jt.moveNext()
      ph.head mustEqual(KeyToken("key"))
      ph = ph.moveNext()
      ph.head mustEqual(StringToken("value"))
      ph = ph.moveNext()
      ph.head mustEqual(KeyToken("key2"))
      ph = ph.moveNext()
      ph.head mustEqual(ArrayStartToken)
      ph = ph.moveNext()
      ph.head mustEqual(LongToken(2))
      ph = ph.moveNext()
      ph.head mustEqual(LongToken(3))
      ph = ph.moveNext()
      ph.head mustEqual(ObjectStartToken)
      ph = ph.moveNext()
      ph.head mustEqual(KeyToken("key3"))
      ph = ph.moveNext()
      ph.head mustEqual(ObjectStartToken)
      ph = ph.moveNext()
      ph.head mustEqual(KeyToken("key4"))
      ph = ph.moveNext()
      ph.head mustEqual(BooleanToken(false))
      ph = ph.moveNext()
      ph.head mustEqual(ObjectEndToken)
      ph = ph.moveNext()
      ph.head mustEqual(ObjectEndToken)
      ph = ph.moveNext()
      ph.head mustEqual(LongToken(5))
      ph = ph.moveNext()
      ph.head mustEqual(ArrayEndToken)
      ph = ph.moveNext()
      ph.head mustEqual(ObjectEndToken)
      ph = ph.moveNext()
      ph.head mustEqual(EndToken)
    }
  }
}
