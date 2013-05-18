package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import scala.util.Try
import org.higherstate.jameson.exceptions.ArgumentsNotFoundException
import org.higherstate.jameson.tokenizers.KeyToken
import scala.util.Success
import scala.util.Failure
import org.higherstate.jameson.exceptions.InvalidTokenException

trait ObjectArgumentsParser[+U] extends Parser[U] {
  protected def arguments:Map[String, (Parser[_], Int)]
  protected def template:Array[Any]

  protected def getArgs(tokenizer:Tokenizer, path:Path): Try[Array[Any]] = tokenizer.head match {
    case ObjectStartToken => buildArgs(tokenizer.moveNext(), path).flatMap { args =>
      if (!args.exists(_ == NoArgFound)) Success(args)
      else Failure(ArgumentsNotFoundException(this, args.zipWithIndex.filter(_._1 == NoArgFound).flatMap(p => arguments.find(a => a._2._2 == p._2).map(_._1)).toList, path))
    }
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }

  private def buildArgs(tokenizer:Tokenizer, path:Path): Try[Array[Any]] =
    tokenizer.head match {
      case KeyToken(key)  => arguments.get(key).map(p => p._1.parse(tokenizer.moveNext(), path + key).flatMap { r =>
        buildArgs(tokenizer.moveNext, path).map { args =>
          args(p._2) = r
          args
        }
      }).getOrElse(buildArgs(tokenizer.dropNext(), path))
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
    case (ArrayEndToken, Nil)                             => Success(Nil)
    case (ArrayEndToken, (head:HasDefault[_]) :: tail)  => buildArgs(tail, index, tokenizer, path).map(head.default :: _)
    case (ArrayEndToken, _)                               => Failure(InvalidTokenException(this, "Insufficient arguments for tuple", ArrayEndToken, path))
    case (token, Nil)                                     => Failure(InvalidTokenException(this, "Expected array end token", token, path + index))
    case (_, head :: tail)                                => head.parse(tokenizer, path + index).flatMap(h => buildArgs(tail, index + 1, tokenizer.moveNext(), path).map(t => h :: t))
  }
}

object NoArgFound {
  def apply[T](parser:Parser[T]) = parser match {
    case p:HasDefault[T] => p.default
    case _               => NoArgFound
  }
}
