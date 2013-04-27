package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.KeyValuePairsExtractor
import com.fasterxml.jackson.core.JsonParser
import util.{Try, Success, Failure}
import org.higherstate.jameson.{Registry, Path}

case object MapParser extends MapParserLike
trait MapParserLike extends KeyValuePairsExtractor[Map[String,Any]] {

  protected def parse(value: TraversableOnce[Try[(String, JsonParser)]], path: Path)(implicit registry:Registry): Try[Map[String, Any]] = {
    Success(value.map {
      case Success((key, parser)) => {
        registry.defaultUnknownParser(parser, path + key) match {
          case Success(v) => (key, v)
          case f:Failure[_]  => return f.asInstanceOf[Failure[Map[String, Any]]]
        }
      }
      case f:Failure[_]           => return f.asInstanceOf[Failure[Map[String, Any]]]
    } toMap)
  }
}