package org.higherstate.jameson.tokenizers

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import scala.collection.mutable.ListBuffer

import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import org.higherstate.jameson._

class BufferedTokenizerSpec extends WordSpec with MustMatchers {

  "Jackson tokenizer with JavaMapTokenizer" should {
    "return to point when buffering starts" in {
      val map = JMap("number" -> 123, "entries" -> JList(JMap("one" -> 1), JMap("two" -> 2)))
      val tokenizer = JavaMapTokenizer(map)
      val bufferedTokenizer = tokenizer.toBufferingTokenizer()
      val lb = new ListBuffer[Token]()
      val b = (1 to 5).foldLeft(bufferedTokenizer){(t, _) => lb += t.head;t.moveNext()}

      val bd = b.toBufferedTokenizer()
      val lb2 = new ListBuffer[Token]()
      (1 to 5).foldLeft(bd.asInstanceOf[Tokenizer]){(t, _) => lb2 += t.head;t.moveNext()}
      lb.result mustEqual lb2.result
    }
  }

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
case class QueryObject(groupby:List[Parent])