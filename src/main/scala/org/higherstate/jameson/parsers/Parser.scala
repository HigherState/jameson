package org.higherstate.jameson.parsers

import util.Try
import com.fasterxml.jackson.core.JsonFactory
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.{Registry, Path}

trait Parser[+U] {
  def parse(tokenizer:Tokenizer, path:Path):Try[(U, Tokenizer)]

  def apply(jsonString:String)(implicit registry:Registry):Try[U] = {
    val jsonFactory = new JsonFactory()
    val jp = jsonFactory.createJsonParser(jsonString)
    jp.nextToken
    parse(JacksonTokenizer(StartToken, jp), Path).map(_._1)
  }
}