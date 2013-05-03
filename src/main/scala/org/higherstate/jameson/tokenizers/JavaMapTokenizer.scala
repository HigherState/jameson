package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.exceptions.UnexpectedValueException
import org.higherstate.jameson.NoPath
import scala.collection.mutable.ListBuffer

object JavaMapTokenizer {

  def apply(map:java.util.Map[String, Any]):Tokenizer = JavaMapTokenizerInstance(Left(map))
  def apply(array:Array[Any]):Tokenizer = JavaMapTokenizerInstance(Right(array))

}

case class JavaMapTokenizerInstance(value:Either[java.util.Map[String, Any], Array[Any]]) extends Tokenizer {

  type jMap = java.util.Map[String, Any]
  var tokens = value match {
    case Left(map) => tokenizeMap(map)
    case Right(array) => tokenizeArray(array)
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

  private def tokenizeArray(array:Array[Any]):List[Token] = ArrayStartToken :: array.toList.flatMap(tokenizeValue(_)) ::: List(ArrayEndToken)

  private def tokenizeValue(any:Any):List[Token] = any match {
    case i:Int        => List(LongToken(i))
    case l:Long       => List(LongToken(l))
    case s:String     => List(StringToken(s))
    case b:Boolean    => List(BooleanToken(b))
    case m:jMap       => tokenizeMap(m)
    case a:Array[Any] => tokenizeArray(a)
    case f:Float      => List(DoubleToken(f))
    case d:Double     => List(DoubleToken(d))
    case null         => List(NullToken)
    case c:Char       => List(StringToken(c.toString))
    case b:Byte       => List(LongToken(b))
    case s:Short      => List(LongToken(s))
    case t            => List(BadToken(UnexpectedValueException("Not a json value", t, NoPath)))
  }

  def moveNext() = {
    tokens = tokens.tail
    this
  }

  def head = if (tokens.nonEmpty) tokens.head else EndToken

}
