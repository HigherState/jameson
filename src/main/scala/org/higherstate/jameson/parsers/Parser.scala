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

  def parse(map:java.util.Map[String, Any]):Try[U] = parse(JavaMapTokenizer(map), Path)
  def parse(list:java.util.List[Any]):Try[U] = parse(JavaMapTokenizer(list), Path)
}