package org.higherstate.jameson

import util.Try
import com.fasterxml.jackson.core.{JsonFactory, JsonParser}

trait Parser[+U] {
  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry):Try[U]

  def apply(jsonString:String)(implicit registry:Registry):Try[U] = {
    val jsonFactory = new JsonFactory()
    val jp = jsonFactory.createJsonParser(jsonString)
    jp.nextToken
    apply(jp, Path)
  }
}