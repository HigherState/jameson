package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.failures.UnexpectedValueFailure
import org.higherstate.jameson.NoPath
import org.higherstate.jameson.tokenizers.ScalaTokenizer._


object ScalaTokenizer {

  def apply(map:Map[String, Any]):Tokenizer =
    ScalaObjectTokenizer(map.toIterator)
  def apply(list:Seq[Any]):Tokenizer =
    ScalaArrayTokenizer(list.toIterator)

  def tokenizeValue(any:Any):Either[Token, Tokenizer] = any match {
    case i:Int        => Left(LongToken(i))
    case l:Long       => Left(LongToken(l))
    case s:String     => Left(StringToken(s))
    case b:Boolean    => Left(BooleanToken(b))
    case m:Map[String, Any]@unchecked =>
      Right(ScalaObjectTokenizer(m.toIterator))
    case a:Seq[Any] =>
      Right(ScalaArrayTokenizer(a.toIterator))
    case f:Float      => Left(DoubleToken(f))
    case d:Double     => Left(DoubleToken(d))
    case null         => Left(NullToken)
    case c:Char       => Left(StringToken(c.toString))
    case b:Byte       => Left(LongToken(b))
    case s:Short      => Left(LongToken(s))
    case a:AnyRef     => Left(AnyRefToken(a))
    case t            => Left(BadToken(UnexpectedValueFailure("Not a json value", t, NoPath)))
  }
}

case class ScalaObjectTokenizer(values:Iterator[(String, Any)]) extends Tokenizer {

  private var currentPair:Option[(String, Any)] = None
  private var _head:Token = ObjectStartToken
  private var isKey = true

  private var child:Option[Tokenizer] = None

  def head = _head

  def moveNext() =
    if (child.isEmpty && head == ObjectEndToken) {
      _head = EndToken
      EndTokenizer
    }
    else
      child.flatMap { t =>
        if (t.moveNext() == EndTokenizer) {
          child = None
          None
        } else {
          _head = t.head
          Some(this)
        }
      }
      .getOrElse {
        if (isKey) {
          if (!values.hasNext) _head = ObjectEndToken
          else {
            currentPair = Some(values.next())
            _head = KeyToken(currentPair.get._1)
            isKey = false
          }
          this
        }
        else {
          isKey = true
          tokenizeValue(currentPair.get._2).left.map { value =>
            _head = value
          }.right.map { value =>
            child = Some(value)
            _head = value.head
          }
          this
        }
      }
}

case class ScalaArrayTokenizer(values:Iterator[Any]) extends Tokenizer {
  private var _head:Token = ArrayStartToken
  def head = _head

  private var child:Option[Tokenizer] = None

  def moveNext() =
    if (child.isEmpty && head == ArrayEndToken) {
      _head = EndToken
      EndTokenizer
    }
    else
      child.flatMap { t =>
        if (t.moveNext() == EndTokenizer) {
          child = None
          None
        } else {
          _head = t.head
          Some(this)
        }
      }
      .getOrElse {
        if (!values.hasNext) {
          _head = ArrayEndToken
          this
        }
        else {
          tokenizeValue(values.next()).left.map { value =>
            _head = value
          }.right.map { value =>
            child = Some(value)
            _head = value.head
          }
          this
        }
      }
}
