package org.higherstate.jameson.parsers

import reflect.runtime.universe._
import util.{Failure, Success, Try}
import reflect.runtime._
import org.higherstate.jameson.exceptions.{InvalidTokenException, ClassKeysNotFoundException, InvalidClassArgsException}
import org.higherstate.jameson.{Selector, Registry, Path}
import org.higherstate.jameson.tokenizers._

case class ClassParser[+T:TypeTag](selectors:List[Selector[String,_]], registry:Registry) extends Parser[T] {

  def getClassName = typeOf[T].typeSymbol.asType.name.toString
  private val constr = typeOf[T].typeSymbol.typeSignature.members.filter(_.isMethod).map(_.asMethod).filter(_.isConstructor).head
  private val arguments:Map[String, (Parser[_], Int)] = constr.typeSignature match {
    case MethodType(params, _ ) => params.zipWithIndex.flatMap { case (p, i) =>
      val name = p.asTerm.name.toString
      selectors.find(_.toKey == name).map(s => s.keys.map(_ -> (s.parser, i)))
      .getOrElse(List(name -> (registry.get(p.typeSignature.typeSymbol.asType).getOrElse(EmbeddedClassParser(p.typeSignature.typeSymbol.asType, registry)), i)))
    }.toMap
    case _:NullaryMethodType    => Map.empty
  }
  private val noArgs = arguments.map(_._2._2).max + 1

  private val hasDefaults = selectors.exists(_.parser.isInstanceOf[HasDefault[_]])

  def parse(tokenizer:Tokenizer, path: Path): Try[T] = tokenizer.head match {
    case ObjectStartToken => {
      val args = new Array[Any](noArgs)
      buildArgs(tokenizer.moveNext(), args, path).flatMap { found =>
        val diff = noArgs - found.size
        if (diff > 0 && (!hasDefaults || (arguments collect { case (key, (p:HasDefault[_], i)) if !found.contains(i) => {args(i) = p.default; i}}).size < diff))
          Failure(ClassKeysNotFoundException(this, typeOf[T].typeSymbol.asType, arguments.filter(i => !found.contains(i._2._2)).map(_._1).toList, path))
        else Try(currentMirror.reflectClass(typeOf[T].typeSymbol.asClass).reflectConstructor(constr).apply(args:_*).asInstanceOf[T]).orElse(Failure(InvalidClassArgsException(this, path)))
      }
    }
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }

  private def buildArgs(tokenizer:Tokenizer, args:Array[Any], path:Path): Try[List[Int]] =
    tokenizer.head match {
    case KeyToken(key)  => arguments.get(key).map(p => p._1.parse(tokenizer.moveNext(), path + key).flatMap { r =>
      args(p._2) = r
      buildArgs(tokenizer.moveNext, args, path).map(p._2 :: _)
    }).getOrElse{
      registry.defaultUnknownParser.parse(tokenizer.moveNext(), path + key) //TODO, implement content skipper to move on tokenizer
      buildArgs(tokenizer.moveNext(), args, path)
    }
    case ObjectEndToken => Success(Nil)
    case token          => Failure(InvalidTokenException(this, "Expected key or Object end token", token, path))
  }
}

case class EmbeddedClassParser(typeSymbol:TypeSymbol, registry:Registry) extends Parser[Any] {

  val constr = typeSymbol.typeSignature.members.filter(_.isMethod).map(_.asMethod).filter(_.isConstructor).head
  val arguments:Map[String, (Parser[_], Int)] = constr.typeSignature match {
    case MethodType(params, _ ) => params.zipWithIndex.map {
      case (p, i) => p.asTerm.name.toString -> (registry.get(p.typeSignature.typeSymbol.asType).getOrElse(EmbeddedClassParser(p.typeSignature.typeSymbol.asType, registry)) -> i)
    }.toMap
    case _:NullaryMethodType    => Map.empty
  }

  def parse(tokenizer:Tokenizer, path: Path): Try[Any] = tokenizer.head match {
    case ObjectStartToken => {
      val args = new Array[Any](arguments.size)
      buildArgs(tokenizer.moveNext, args, path).flatMap { found =>
        if (found.size < args.size) Failure(ClassKeysNotFoundException(this, typeSymbol, arguments.filter(i => !found.contains(i._2._2)).map(_._1).toList, path))
        else Try(currentMirror.reflectClass(typeSymbol.asClass).reflectConstructor(constr).apply(args:_*)).orElse(Failure(InvalidClassArgsException(this, path)))
      }
    }
    case token            => Failure(InvalidTokenException(this, "Expected object start token", token, path))
  }

  def buildArgs(tokenizer:Tokenizer, args:Array[Any], path:Path): Try[List[Any]] = tokenizer.head match {
    case KeyToken(key)  => arguments.get(key).map(p => p._1.parse(tokenizer.moveNext(), path + key).flatMap { r =>
      args(p._2) = r
      buildArgs(tokenizer.moveNext(), args, path).map(p._2 :: _)
    }).getOrElse{
      registry.defaultUnknownParser.parse(tokenizer.moveNext(), path + key) //TODO, implement content skipper to move on tokenizer
      buildArgs(tokenizer.moveNext, args, path)
    }
    case ObjectEndToken => Success(Nil)
    case token          => Failure(InvalidTokenException(this, "Expected key or Object end token", token, path))
  }
}