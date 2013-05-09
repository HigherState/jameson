package org.higherstate.jameson.parsers

import scala.reflect.runtime._
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try
import reflect.runtime.universe._


//sealed trait FunctionParser[F,T] extends Parser[T] {
//  def typeTag:TypeTag[F]
//
//  println(currentMirror.reflect(func).symbol.asType.typeSignature)
//
//  def parse(tokenizer: Tokenizer, path: Path): Try[T] = ???
//}


//case class FunctionParser2Arg[T1, T2, U](func: (T1, T2) => U) extends FunctionParser[U] {
//
//}
