package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.ValuesExtractor
import util.{Failure, Success, Try}
import com.fasterxml.jackson.core.JsonParser
import org.higherstate.jameson.{Registry, Path}

case object ListParser extends ListParserLike
trait ListParserLike extends ValuesExtractor[List[Any]] {

  protected def parse(value: TraversableOnce[Try[JsonParser]], path: Path)(implicit registry:Registry): Try[List[Any]] = {
    var i = -1
    Success(value.map {
      case Success(parser) => registry.defaultUnknownParser(parser, path + {i+=1; i}) match {
        case Success(v)   => v
        case f:Failure[_] => return f.asInstanceOf[Failure[List[Any]]]
      }
      case f:Failure[_]    => return f.asInstanceOf[Failure[List[Any]]]
    } toList)
  }
}
