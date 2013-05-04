package org.higherstate.jameson.parsers

import util.Try
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path

trait Parser[+U] {
  def parse(tokenizer:Tokenizer, path:Path):Try[U]

  def parse(jsonString:String):Try[U] = {
    parse(JacksonTokenizer(jsonString), Path)
  }

  def parse(map:java.util.Map[String, Any]):Try[U] = parse(JavaMapTokenizer(map), Path)
  def parse(list:java.util.List[Any]):Try[U] = parse(JavaMapTokenizer(list), Path)
}