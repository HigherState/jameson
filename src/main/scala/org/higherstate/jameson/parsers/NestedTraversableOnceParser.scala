package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.extractors.{ValuesExtractor}
import util.{Failure, Success, Try}
import com.fasterxml.jackson.core.JsonParser
import org.higherstate.jameson.{Registry, Path, Parser}

case class NestedTraversableOnceParser[T](parser:Parser[T]) extends ValuesExtractor[TraversableOnce[Try[T]]] {

   protected def parse(value: TraversableOnce[Try[JsonParser]], path: Path)(implicit registry:Registry): Try[TraversableOnce[Try[T]]] = {
     var i = -1
     Success(value.toIterator.map {
       case Success(jp)   => parser(jp, path + {i+=1; i})
       case f:Failure[_]  => f.asInstanceOf[Failure[T]]
     }.takeWhileInclusive(_.isSuccess))
   }
 }
