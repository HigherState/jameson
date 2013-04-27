package org.higherstate.jameson.parsers

import org.higherstate.jameson.extractors.ValuesExtractor
import util.{Failure, Success, Try}
import com.fasterxml.jackson.core.JsonParser
import org.higherstate.jameson.{Registry, Path, Parser}

case class NestedListParser[T](itemParser:Parser[T]) extends ValuesExtractor[List[T]] {

  protected def parse(value: TraversableOnce[Try[JsonParser]], path: Path)(implicit registry:Registry): Try[List[T]] = {
    var i = -1
    Success(value.map {
      case Success(parser) => itemParser(parser, path + {i+=1; i} ) match {
        case Success(v)   => v
        case f:Failure[_] => return f.asInstanceOf[Failure[List[T]]]
      }
      case f:Failure[_]    => return f.asInstanceOf[Failure[List[T]]]
    } toList)
  }
}
