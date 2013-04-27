package org.higherstate.jameson.parsers

import util.Try
import com.fasterxml.jackson.core.JsonParser
import org.higherstate.jameson.extractors.Extractor
import org.higherstate.jameson.{Registry, Path, Parser}

//Only supports eithers on single values otherwise need to buffer, to add.
case class EitherParser[T, U](leftParser:Extractor[_, T], rightParser:Extractor[_, U]) extends Parser[Either[T,U]] {

  def apply(jsonParser:JsonParser, path:Path)(implicit registry:Registry): Try[Either[T,U]] = {
    val left = leftParser(jsonParser, path)
    if (left.isSuccess) left.map(Left(_))
    else rightParser(jsonParser, path).map(Right(_))
  }
}
