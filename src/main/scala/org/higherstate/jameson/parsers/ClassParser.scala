package org.higherstate.jameson.parsers

import reflect.runtime.universe._
import util.{Failure, Try}
import reflect.runtime._
import org.higherstate.jameson.exceptions.InvalidClassArgsException
import org.higherstate.jameson.{KeySelector, Registry, Path}
import org.higherstate.jameson.tokenizers._
import scala.collection.mutable.ListBuffer

case class ClassParser[+T:TypeTag](selectors:List[KeySelector[String,_]], registry:Registry) extends ObjectArgumentsParser[T] {

  def getClassName = typeOf[T].typeSymbol.asType.name.toString

  private val constr = typeOf[T].typeSymbol.typeSignature.members.filter(_.isMethod).map(_.asMethod).filter(_.isConstructor).head
  protected lazy val (arguments, groups, template) = getArgumentsAndGroups()

  def parse(tokenizer:Tokenizer, path: Path): Try[T] =
    getArgs(tokenizer, path).flatMap { args =>
      Try(currentMirror.reflectClass(typeOf[T].typeSymbol.asClass).reflectConstructor(constr).apply(args:_*).asInstanceOf[T]).orElse(Failure(InvalidClassArgsException(this, args, path)))
    }

  private def getArgumentsAndGroups() = {
    val argsBuffer = new ListBuffer[(String, (Parser[_], Int))]()
    val groupBuffer = new ListBuffer[(Int, Parser[_], Set[String])]()
    val templateBuffer = new ListBuffer[Any]()
    constr.typeSignature match {
      case MethodType(params, _ ) => params.zipWithIndex.foreach{ case (p, i) =>
        val name = p.asTerm.name.toString
        selectors.find(_.toKey == name).map { s =>
          templateBuffer += NoArgFound(s.parser)
          if (s.isGroup) groupBuffer += ((i, s.parser, s.keys))
          else argsBuffer ++= s.keys.map(_ -> (s.parser, i))
        }.getOrElse {
          val parser = registry.get(p.typeSignature.typeSymbol.asType).getOrElse(EmbeddedClassParser(p.typeSignature.typeSymbol.asType, registry))
          argsBuffer += name -> (parser, i)
          templateBuffer += NoArgFound(parser)
        }
      }
      case _:NullaryMethodType    => Map.empty
    }
    (argsBuffer.toMap, groupBuffer.result, templateBuffer.toArray)
  }
}

case class EmbeddedClassParser(typeSymbol:TypeSymbol, registry:Registry) extends ObjectArgumentsParser[Any] {

  private val constr = typeSymbol.typeSignature.members.filter(_.isMethod).map(_.asMethod).filter(_.isConstructor).head
  protected lazy val arguments:Map[String, (Parser[_], Int)] = constr.typeSignature match {
    case MethodType(params, _ ) => params.zipWithIndex.map {
      case (p, i) => p.asTerm.name.toString -> (registry.get(p.typeSignature.typeSymbol.asType).getOrElse(EmbeddedClassParser(p.typeSignature.typeSymbol.asType, registry)) -> i)
    }.toMap
    case _:NullaryMethodType    => Map.empty
  }
  protected lazy val template:Array[Any] = arguments.map(_._2).toList.sortBy(_._2).map(p => NoArgFound(p._1)).toArray
  protected val groups = Nil

  def parse(tokenizer:Tokenizer, path: Path): Try[Any] =
    getArgs(tokenizer, path).flatMap { args =>
      Try(currentMirror.reflectClass(typeSymbol.asClass).reflectConstructor(constr).apply(args:_*)).orElse(Failure(InvalidClassArgsException(this, args, path)))
    }
}