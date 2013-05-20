package org.higherstate.jameson.parsers

import util.Try
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import java.io.{InputStream, Reader}

trait Parser[+U] {

  def parse(tokenizer:Tokenizer, path:Path):Try[U]

  def parse(jsonString:String):Try[U] = parse(JacksonTokenizer(jsonString), Path)
  def parse(inputStream:InputStream):Try[U] = parse(JacksonTokenizer(inputStream), Path)
  def parse(reader:Reader):Try[U] = parse(JacksonTokenizer(reader), Path)

  def parse(map:java.util.Map[String, Any]):Try[U] = parse(JavaTokenizer(map), Path)
  def parse(list:java.lang.Iterable[Any]):Try[U] = parse(JavaTokenizer(list), Path)

  def apply(jsonString:String):Try[U] = parse(jsonString:String)
  def apply(inputStream:InputStream):Try[U] = parse(inputStream:InputStream)
  def apply(reader:Reader):Try[U] = parse(reader:Reader)

  def apply(map:java.util.Map[String, Any]):Try[U] = parse(map)
  def apply(list:java.lang.Iterable[Any]):Try[U] = parse(list)
}