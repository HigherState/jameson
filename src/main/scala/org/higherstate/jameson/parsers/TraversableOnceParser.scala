package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.extractors.ValuesExtractor
import util.{Failure, Success, Try}
import com.fasterxml.jackson.core.JsonParser
import org.higherstate.jameson.{Registry, Path}

case object TraversableOnceParser extends TraversableOnceParserLike

trait TraversableOnceParserLike extends ValuesExtractor[TraversableOnce[Try[Any]]] {

  protected def parse(value: TraversableOnce[Try[JsonParser]], path: Path)(implicit registry:Registry): Try[TraversableOnce[Try[Any]]] = {
    var i = -1
    Success(value.toIterator.map {
      case Success(parser) => registry.defaultUnknownParser(parser, path + {i += 1; i})
      case f:Failure[_]    => f.asInstanceOf[Try[Any]]
    }.takeWhileInclusive(_.isSuccess))
  }
}
