package org.higherstate.jameson.parsers

import reflect.runtime.universe._
import com.fasterxml.jackson.core.JsonParser
import util.{Failure, Success, Try}
import org.higherstate.jameson.extractors.KeyValuePairsExtractor
import reflect.runtime._
import org.higherstate.jameson.exceptions.{ClassKeysNotFoundException, InvalidClassArgsException}
import org.higherstate.jameson.{Selector, Registry, Path, Parser}

case class ClassParser[T:TypeTag](selectors:List[Selector[String,_]], registry:Registry) extends KeyValuePairsExtractor[T] {

  def getClassName = typeOf[T].typeSymbol.asType.name.toString
  private val constr = typeOf[T].typeSymbol.typeSignature.members.filter(_.isMethod).map(_.asMethod).filter(_.isConstructor).head
  private val arguments:Map[String, (Parser[_], Int)] = constr.typeSignature match {
    case MethodType(params, _ ) => params.zipWithIndex.map { case (p, i) =>
      val name = p.asTerm.name.toString
      selectors.find(_.toKey == name).map(s => s.key -> (s.parser, i))
      .getOrElse(name -> (registry.get(p.typeSignature.typeSymbol.asType).getOrElse(EmbeddedClassParser(p.typeSignature.typeSymbol.asType, registry)), i))
    }.toMap
    case _:NullaryMethodType    => Map.empty
  }

  protected def parse(value: TraversableOnce[Try[(String, JsonParser)]], path: Path)(implicit registry: Registry): Try[T] = {
    val args = new Array[Any](arguments.size)
    var found:List[Int] = Nil
    value.foreach {
      case Success((key, parser)) => arguments.get(key).map(p => p._1(parser,path) match {
        case Success(v)   => {
          found = (p._2 :: found)
          args(p._2) = v
        }
        case f:Failure[_] => return f.asInstanceOf[Failure[T]]
      })
      case f:Failure[_]           => return f.asInstanceOf[Failure[T]]
    }
    if (found.size < args.size) Failure(ClassKeysNotFoundException(typeOf[T].typeSymbol.asType, arguments.filter(i => !found.contains(i._2._2)).map(_._1).toList, path))
    else Try(currentMirror.reflectClass(typeOf[T].typeSymbol.asClass).reflectConstructor(constr).apply(args:_*).asInstanceOf[T]).orElse(Failure(InvalidClassArgsException(path)))
  }
}

case class EmbeddedClassParser(typeSymbol:TypeSymbol, registry:Registry) extends KeyValuePairsExtractor[AnyRef] {

  val constr = typeSymbol.typeSignature.members.filter(_.isMethod).map(_.asMethod).filter(_.isConstructor).head
  val arguments:Map[String, (Parser[_], Int)] = constr.typeSignature match {
    case MethodType(params, _ ) => params.zipWithIndex.map {
      case (p, i) => p.asTerm.name.toString -> (registry.get(p.typeSignature.typeSymbol.asType).getOrElse(EmbeddedClassParser(p.typeSignature.typeSymbol.asType, registry)) -> i)
    }.toMap
    case _:NullaryMethodType    => Map.empty
  }

  protected def parse(value: TraversableOnce[Try[(String, JsonParser)]], path: Path)(implicit registry: Registry): Try[AnyRef] = {
    val args = new Array[Any](arguments.size)
    var found:List[Int] = Nil
    value.foreach {
      case Success((key, parser)) => arguments.get(key).map(p => p._1(parser,path) match {
        case Success(v)   => {
          found = (p._2 :: found)
          args(p._2) = v
        }
        case f:Failure[_] => return f.asInstanceOf[Failure[AnyRef]]
      })
      case f:Failure[_]           => return f.asInstanceOf[Failure[AnyRef]]
    }

    if (found.size < args.size) Failure(ClassKeysNotFoundException(typeSymbol, arguments.filter(i => !found.contains(i._2._2)).map(_._1).toList, path))
    else Try(currentMirror.reflectClass(typeSymbol.asClass).reflectConstructor(constr).apply(args:_*).asInstanceOf[AnyRef]).orElse(Failure(InvalidClassArgsException(path)))
  }
}