package org.higherstate.jameson.tokenizers

import org.scalatest.{MustMatchers, WordSpec}
import JavaTokenizerSpec._

class JavaTokenizerSpec extends WordSpec with MustMatchers {

  "Jackson tokenizer should" should {
    "process key value tokens correctly" in {
      val jMap = JMap("key" -> "value", "key2" -> JList(2, 3, JMap("key3" -> JMap("key4" -> false)), 5))
      val jt = JavaTokenizer(jMap)
      jt.head mustEqual(ObjectStartToken)
      var ph = jt.moveNext()
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
      ph.head mustEqual(KeyToken("key"))
      ph = ph.moveNext()
      ph.head mustEqual(StringToken("value"))
      ph = ph.moveNext()
      ph.head mustEqual(ObjectEndToken)
      ph = ph.moveNext()
      ph.head mustEqual(EndToken)
    }

    "support drop next" in {
      //order not guaranteed
//      val jMap = JMap("key" -> "value", "key2" -> JMap("key3" -> false), "key4" -> 4, "key5" -> JList(1, 2, JMap("key6" -> 3)))
//      val jt = JavaTokenizer(jMap)
//      jt.head mustEqual(ObjectStartToken)
//      var ph = jt.moveNext()
//      ph.head mustEqual(KeyToken("key4"))
//      ph = ph.moveNext()
//      ph.head mustEqual(LongToken(4))
//      ph = ph.moveNext()
//      ph.head mustEqual(KeyToken("key5"))
//      ph = ph.dropNext()
//      ph.head mustEqual(KeyToken("key2"))
//      ph = ph.dropNext()
//      ph.head mustEqual(KeyToken("key"))
//      ph = ph.moveNext()
//      ph.head mustEqual(StringToken("value"))
//      ph = ph.moveNext()
//      ph.head mustEqual(ObjectEndToken)
//      ph.moveNext().head mustEqual(EndToken)
    }
  }
}

object JavaTokenizerSpec {
  def JMap(entries:(String,Any)*):java.util.Map[String, Any] = {
    val o = new java.util.HashMap[String, Any]()
    for((key, value) <- entries) {
      o.put(key, value)
    }
    o
  }

  def JList(entries:Any*):java.util.List[Any] = {
    val l = new java.util.LinkedList[Any]()
    for (e <- entries) l.add(e)
    l
  }
}
