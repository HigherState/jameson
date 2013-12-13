package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.{NoPath, Path}
import scala.util.Try
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, ArgumentsNotFoundException, InvalidTokenException}
import org.higherstate.jameson.tokenizers.KeyToken
import scala.util.Success
import scala.util.Failure
import scala.collection.mutable.ListBuffer

trait ObjectArgumentsParser[+U] extends Parser[U] {

  protected def arguments:Map[String, (Parser[_], Int)]
  protected def template:Array[Any]
  protected def groups:List[(Int, Parser[_], Set[String])]

  protected def getArgs(tokenizer:Tokenizer, path:Path): Try[Array[Any]] = tokenizer.head match {
    case ObjectStartToken => {
      val buffers = groups.map( b => ObjectBuffer(b._2) -> b._1)
      val keyBuffers = buffers.zip(groups).flatMap(t => t._2._3.map(_ -> t._1._1)).toMap
      val args = buildArgs(tokenizer.moveNext(), path, keyBuffers)
      if (buffers.nonEmpty) buffers.foldLeft(args) { case (args, (buffer, index)) =>
        args.flatMap(a => buffer.parse(path).map { v =>
          a(index) = v
          a
        })
      }
      args.flatMap { args =>
        if (!args.exists(_ == NoArgFound)) Success(args)
        else Failure(ArgumentsNotFoundException(this, args.zipWithIndex.filter(_._1 == NoArgFound).flatMap(p => arguments.find(a => a._2._2 == p._2).map(_._1)).toList, path))
      }
    }
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }
  //TODO: args not found in grouped object not coming through as correct error

  private def buildArgs(tokenizer:Tokenizer, path:Path, buffers:Map[String, ObjectBuffer]): Try[Array[Any]] =
    tokenizer.head match {
      case KeyToken(key) =>
        arguments.get(key)
          .map(p => p._1.parse(tokenizer.moveNext(), path + key).flatMap { r =>
          buildArgs(tokenizer.moveNext, path, buffers).map { args =>
            args(p._2) = r
            args
          }
        })
          .orElse(buffers.get(key).map(b => buildArgs(b.add(tokenizer), path, buffers)))
          .getOrElse(buildArgs(tokenizer.dropNext(), path, buffers))
      case ObjectEndToken => Success(template.clone)
      case token          => Failure(InvalidTokenException(this, "Expected key or Object end token", token, path))
    }
}

trait ListArgumentParser[U] extends Parser[U] {
  protected def parsers:List[Parser[_]]

  protected def getArgs(tokenizer:Tokenizer, path:Path) = tokenizer.head match {
    case ArrayStartToken => buildArgs(parsers, 0, tokenizer.moveNext(), path)
    case token           => Failure(InvalidTokenException(this, "Expected array start token", token, path))
  }

  private def buildArgs(p:List[Parser[_]], index:Int, tokenizer:Tokenizer, path:Path):Try[List[Any]] = (tokenizer.head, p) match {
    case (ArrayEndToken, Nil)                              => Success(Nil)
    case (ArrayEndToken, head :: tail) if head.hasDefault  => buildArgs(tail, index, tokenizer, path).map(head.default.get :: _)
    case (ArrayEndToken, _)                                => Failure(InvalidTokenException(this, "Insufficient arguments for tuple", ArrayEndToken, path))
    case (token, Nil)                                      => Failure(InvalidTokenException(this, "Expected array end token", token, path + index))
    case (_, head :: tail)                                 => head.parse(tokenizer, path + index).flatMap(h => buildArgs(tail, index + 1, tokenizer.moveNext(), path).map(t => h :: t))
  }

  def schema =
    Map("type" -> "list", "items" -> parsers.map(_.schema), "additionalItems" -> false)
}

object NoArgFound {
  def apply[T](parser:Parser[T]) = parser.default.getOrElse(NoArgFound)
}

case class ObjectBuffer(parser:Parser[_]) extends Tokenizer {
  val buffer = new ListBuffer[Token]
  buffer += ObjectStartToken
  var tokens:List[Token] = Nil
  def add(tokenizer:Tokenizer) = {
    buffer += tokenizer.head
    bufferNested(tokenizer.moveNext())
  }

  def parse(path:Path):Try[Any] = {
    buffer += ObjectEndToken
    tokens = buffer.result()
    parser.parse(this, path)
  }

  def head: Token = tokens.head

  def moveNext(): Tokenizer = {
    tokens = tokens.tail
    this
  }

  private def bufferNested(tokenizer:Tokenizer):Tokenizer = tokenizer.head match {
    case (token:BadToken) => tokenizer
    case ArrayStartToken  => {
      buffer += tokenizer.head
      var t = tokenizer.moveNext()
      while(t.head != ArrayEndToken) {
        t = bufferNested(tokenizer)
      }
      buffer += tokenizer.head
      t.moveNext()
    }
    case ObjectStartToken => {
      buffer += tokenizer.head
      var t = tokenizer.moveNext()
      while(t.head != ObjectEndToken) {
        buffer += tokenizer.head
        t = bufferNested(tokenizer.moveNext())
      }
      buffer += tokenizer.head
      t.moveNext()
    }
    case ArrayEndToken    => FailedTokenizer(BadToken(UnexpectedTokenException("Unexpected token", ArrayEndToken, NoPath)))
    case ObjectEndToken   => FailedTokenizer(BadToken(UnexpectedTokenException("Unexpected token", ObjectEndToken, NoPath)))
    case token            => {
      buffer += token
      tokenizer.moveNext()
    }
  }
}
