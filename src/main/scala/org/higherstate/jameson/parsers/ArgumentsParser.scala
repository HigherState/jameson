package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.{NoPath, Path}
import org.higherstate.jameson.failures._
import org.higherstate.jameson.tokenizers.KeyToken
import scala.collection.mutable.ListBuffer

trait ObjectArgumentsParser[+U] extends Parser[U] {

  protected def arguments:Map[String, (Parser[_], Int)]
  protected def template:Array[Any]
  protected def groups:List[(Int, Parser[_], Set[String])]

  protected def getArgs(tokenizer:Tokenizer, path:Path): Valid[Array[Any]] =
    tokenizer.head match {
      case ObjectStartToken =>
        val buffers = groups.map( b => ObjectBuffer(b._2) -> b._1)
        val keyBuffers = buffers.zip(groups).flatMap(t => t._2._3.map(_ -> t._1._1)).toMap
        val validArgs = buildArgs(tokenizer.moveNext(), path, keyBuffers)
        if (buffers.nonEmpty) buffers.foldLeft(validArgs) { case (_validArgs, (buffer, index)) =>
          _validArgs.combine(buffer.parse(path)){ (args, value) =>
            args(index) = value
            args
          }
        }
        validArgs.flatMap { args =>
          if (!args.contains(NoArgFound)) Success(args)
          else Failure(ArgumentsNotFoundFailure(this, args.zipWithIndex.filter(_._1 == NoArgFound).flatMap(p => arguments.find(a => a._2._2 == p._2).map(_._1)).toList, path))
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Expected object start token", token, path))
    }
  //TODO: args not found in grouped object not coming through as correct error

  private def buildArgs(tokenizer:Tokenizer, path:Path, buffers:Map[String, ObjectBuffer]): Valid[Array[Any]] =
    tokenizer.head match {
      case KeyToken(key) =>
        arguments.get(key).map{ case (parser, index) =>
          parser.parse(tokenizer.moveNext(), path + key)
            .combine(buildArgs(tokenizer.moveNext(), path, buffers)){(value, args) =>
              args(index) = value
              args
            }
         }
          .orElse(buffers.get(key).map(b => buildArgs(b.add(tokenizer), path, buffers)))
          .getOrElse(buildArgs(tokenizer.dropNext(), path, buffers))
      case ObjectEndToken =>
        Success(template.clone())
      case token =>
        Failure(UnexpectedTokenFailure("Expected key or Object end token", token, path))
    }
}

trait ListArgumentParser[U] extends Parser[U] {
  protected def parsers:List[Parser[_]]

  protected def getArgs(tokenizer:Tokenizer, path:Path) = tokenizer.head match {
    case ArrayStartToken =>
      buildArgs(parsers, 0, tokenizer.moveNext(), path)
    case token =>
      Failure(InvalidTokenFailure(this, "Expected array start token", token, path))
  }

  private def buildArgs(p:List[Parser[_]], index:Int, tokenizer:Tokenizer, path:Path):Valid[List[Any]] =
    (tokenizer.head, p) match {
      case (ArrayEndToken, Nil) =>
        Success(Nil)
      case (ArrayEndToken, head :: tail) if head.hasDefault =>
        buildArgs(tail, index, tokenizer, path).map(head.default.get :: _)
      case (ArrayEndToken, _) =>
        Failure(InvalidTokenFailure(this, "Insufficient arguments for tuple", ArrayEndToken, path))
      case (token, Nil) =>
        Failure(UnexpectedTokenFailure("Expected array end token", token, path + index))
      case (_, head :: tail) =>
        head.parse(tokenizer, path + index).combine(buildArgs(tail, index + 1, tokenizer.moveNext(), path))(_ :: _)
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

  def parse(path:Path):Valid[Any] = {
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
    case ArrayStartToken  =>
      buffer += tokenizer.head
      var t = tokenizer.moveNext()
      while(t.head != ArrayEndToken) {
        t = bufferNested(tokenizer)
      }
      buffer += tokenizer.head
      t.moveNext()
    case ObjectStartToken =>
      buffer += tokenizer.head
      var t = tokenizer.moveNext()
      while(t.head != ObjectEndToken) {
        buffer += tokenizer.head
        t = bufferNested(tokenizer.moveNext())
      }
      buffer += tokenizer.head
      t.moveNext()
    case ArrayEndToken =>
      FailedTokenizer(BadToken(UnexpectedTokenFailure("Unexpected token", ArrayEndToken, NoPath)))
    case ObjectEndToken =>
      FailedTokenizer(BadToken(UnexpectedTokenFailure("Unexpected token", ObjectEndToken, NoPath)))
    case token            =>
      buffer += token
      tokenizer.moveNext()
  }
}
