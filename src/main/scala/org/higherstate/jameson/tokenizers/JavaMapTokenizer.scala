package org.higherstate.jameson.tokenizers

import org.higherstate.jameson.exceptions.UnexpectedValueException
import org.higherstate.jameson.NoPath
import org.higherstate.jameson.tokenizers.JavaTokenizer._

object JavaTokenizer {
  type jMap = java.util.Map[String, Any]
  type jEntry = java.util.Map.Entry[String, Any]
  type jList = java.lang.Iterable[Any]
  type jIterator[T] = java.util.Iterator[T]

  def apply(map:jMap):Tokenizer = JavaObjectTokenizer(map.entrySet().iterator())
  def apply(list:jList):Tokenizer = JavaArrayTokenizer(list.iterator())

  def tokenizeValue(any:Any):Either[Token, Tokenizer] = any match {
    case i:Int        => Left(LongToken(i))
    case l:Long       => Left(LongToken(l))
    case s:String     => Left(StringToken(s))
    case b:Boolean    => Left(BooleanToken(b))
    case m:jMap       => Right(JavaObjectTokenizer(m.entrySet().iterator()))
    case a:jList      => Right(JavaArrayTokenizer(a.iterator()))
    case f:Float      => Left(DoubleToken(f))
    case d:Double     => Left(DoubleToken(d))
    case null         => Left(NullToken)
    case c:Char       => Left(StringToken(c.toString))
    case b:Byte       => Left(LongToken(b))
    case s:Short      => Left(LongToken(s))
    case a:AnyRef     => Left(AnyRefToken(a))
    case t            => Left(BadToken(UnexpectedValueException("Not a json value", t, NoPath)))
  }
}

case class JavaObjectTokenizer(values:jIterator[jEntry]) extends Tokenizer {

  private var currentPair:Option[jEntry] = None
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
            _head = KeyToken(currentPair.get.getKey)
            isKey = false
          }
          this
        }
        else {
          isKey = true
          tokenizeValue(currentPair.get.getValue).left.map { value =>
            _head = value
          }.right.map { value =>
            child = Some(value)
            _head = value.head
          }
          this
        }
      }
}

case class JavaArrayTokenizer(values:jIterator[Any]) extends Tokenizer {
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
          tokenizeValue(values.next).left.map { value =>
            _head = value
          }.right.map { value =>
            child = Some(value)
            _head = value.head
          }
          this
        }
      }
}
