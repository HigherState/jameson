package org.higherstate.jameson.parsers

import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.Path
import java.io.{InputStream, Reader}
import org.higherstate.jameson.failures.Valid

trait Parser[+U] {

  def parse(tokenizer:Tokenizer, path:Path):Valid[U]

  def parse(jsonString:String):Valid[U] =
    parse(JacksonTokenizer(jsonString), Path)
  def parse(inputStream:InputStream):Valid[U] =
    parse(JacksonTokenizer(inputStream), Path)
  def parse(reader:Reader):Valid[U] =
    parse(JacksonTokenizer(reader), Path)

  def parse(map:java.util.Map[String, Any]):Valid[U] =
    parse(JavaTokenizer(map), Path)
  def parse(list:java.lang.Iterable[Any]):Valid[U] =
    parse(JavaTokenizer(list), Path)

  def parse(map:Map[String,Any]):Valid[U] =
    parse(ScalaTokenizer(map), Path)
  def parse(list:Seq[Any]):Valid[U] =
    parse(ScalaTokenizer(list), Path)

  def apply(jsonString:String):Valid[U] =
    parse(jsonString:String)
  def apply(inputStream:InputStream):Valid[U] =
    parse(inputStream:InputStream)
  def apply(reader:Reader):Valid[U] =
    parse(reader:Reader)

  def apply(map:java.util.Map[String, Any]):Valid[U] =
    parse(map)
  def apply(list:java.lang.Iterable[Any]):Valid[U] =
    parse(list)

  def default:Option[U] =
    None
  def hasDefault =
    default.nonEmpty

  def schema:Map[String, Any]
}