package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers.Tokenizer
import org.higherstate.jameson.Path
import scala.util.Try

case class PipeParser[T, +U](parser:Parser[T], func:T => U) extends Parser[U] {
  def parse(tokenizer: Tokenizer, path: Path): Try[U] = parser.parse(tokenizer, path).map(func)

  override def default = parser.default.map(func)
}

case class Pipe2Parser[T1, T2,U](parser:Parser[(T1, T2)], func:(T1, T2) => U) extends Parser[U] {
  def parse(tokenizer: Tokenizer, path: Path): Try[U] = parser.parse(tokenizer, path).map(t => func(t._1, t._2))
}

case class Pipe3Parser[T1, T2, T3, U](parser:Parser[(T1, T2, T3)], func:(T1, T2, T3) => U) extends Parser[U] {
  def parse(tokenizer: Tokenizer, path: Path): Try[U] = parser.parse(tokenizer, path).map(t => func(t._1, t._2, t._3))
}

case class Pipe4Parser[T1, T2, T3, T4, U](parser:Parser[(T1, T2, T3, T4)], func:(T1, T2, T3, T4) => U) extends Parser[U] {
  def parse(tokenizer: Tokenizer, path: Path): Try[U] = parser.parse(tokenizer, path).map(t => func(t._1, t._2, t._3, t._4))
}

case class Pipe5Parser[T1, T2, T3, T4, T5, U](parser:Parser[(T1, T2, T3, T4, T5)], func:(T1, T2, T3, T4, T5) => U) extends Parser[U] {
  def parse(tokenizer: Tokenizer, path: Path): Try[U] = parser.parse(tokenizer, path).map(t => func(t._1, t._2, t._3, t._4, t._5))
}



