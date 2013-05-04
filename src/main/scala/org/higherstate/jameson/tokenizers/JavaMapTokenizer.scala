package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.exceptions.UnexpectedValueException
import org.higherstate.jameson.NoPath
import scala.collection.mutable.ListBuffer

object JavaMapTokenizer {

  def apply(map:java.util.Map[String, Any]):Tokenizer = JavaMapTokenizerInstance(Left(map))
  def apply(list:java.util.List[Any]):Tokenizer = JavaMapTokenizerInstance(Right(list))

}

case class JavaMapTokenizerInstance(value:Either[java.util.Map[String, Any], java.util.List[Any]]) extends Tokenizer {
  type jMap = java.util.Map[String, Any]
  type jList = java.util.List[Any]

  var tokens = value match {
    case Left(map) => tokenizeMap(map)
    case Right(list) => tokenizeList(list)
  }

  private def tokenizeMap(map:jMap):List[Token] = {
    val l = new ListBuffer[Token]
    l += ObjectStartToken
    val i = map.entrySet().iterator()
    while (i.hasNext) {
      val p = i.next()
      l += KeyToken(p.getKey)
      l ++= tokenizeValue(p.getValue)
    }
    l += ObjectEndToken
    l.result()
  }

  private def tokenizeList(list:jList):List[Token] = {
    val l = new ListBuffer[Token]
    l += ArrayStartToken
    val i = list.iterator()
    while (i.hasNext) l ++= tokenizeValue(i.next())
    l += ArrayEndToken
    l.result
  }

  private def tokenizeValue(any:Any):List[Token] = any match {
    case i:Int        => List(LongToken(i))
    case l:Long       => List(LongToken(l))
    case s:String     => List(StringToken(s))
    case b:Boolean    => List(BooleanToken(b))
    case m:jMap       => tokenizeMap(m)
    case a:jList      => tokenizeList(a)
    case f:Float      => List(DoubleToken(f))
    case d:Double     => List(DoubleToken(d))
    case null         => List(NullToken)
    case c:Char       => List(StringToken(c.toString))
    case b:Byte       => List(LongToken(b))
    case s:Short      => List(LongToken(s))
    case a:AnyRef     => List(AnyRefToken(a))
    case t            => List(BadToken(UnexpectedValueException("Not a json value", t, NoPath)))
  }

  def moveNext() = {
    tokens = tokens.tail
    this
  }

  def head = if (tokens.nonEmpty) tokens.head else EndToken

}
