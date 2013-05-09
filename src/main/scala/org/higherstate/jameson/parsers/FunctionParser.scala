package org.higherstate.jameson.parsers

import scala.reflect.runtime._
import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try


sealed trait FunctionParser[T] extends Parser[T] {
  def func:Any

  def parse(tokenizer: Tokenizer, path: Path): Try[T] = ???
}

case class FunctionParser1Arg[T, U](func: T => U) extends FunctionParser[U] {

}
