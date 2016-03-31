package org.higherstate.jameson.tokenizers

import org.scalatest.{MustMatchers, WordSpec}
import scala.collection.mutable.ListBuffer
import org.higherstate.jameson._
import JavaTokenizerSpec._

class BufferedTokenizerSpec extends WordSpec with MustMatchers {

  "Jackson tokenizer with JavaMapTokenizer" should {
    "return to point when buffering starts" in {
      val map = JMap("number" -> 123, "entries" -> JList(JMap("one" -> 1), JMap("two" -> 2)))
      val tokenizer = JavaTokenizer(map)
      val buffer = tokenizer.getBuffer
      val lb = new ListBuffer[Token]()
      val b = (1 to 5).foldLeft(buffer.getTokenizer){(t, _) => lb += t.head;t.moveNext()}

      val bd = buffer.getTokenizer
      val lb2 = new ListBuffer[Token]()
      (1 to 5).foldLeft(bd){(t, _) => lb2 += t.head;t.moveNext()}
      lb.result mustEqual lb2.result
    }
  }
}
case class QueryObject(groupby:List[Parent])