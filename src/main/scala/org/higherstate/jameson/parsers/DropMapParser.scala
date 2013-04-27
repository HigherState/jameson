package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.extractors.{KeyValuePairsExtractor}
import util.{Failure, Success, Try}
import com.fasterxml.jackson.core.JsonParser
import collection.mutable
import org.higherstate.jameson.exceptions.KeyNotFoundException
import org.higherstate.jameson.{Selector, Registry, Path}

case class DropMapParser(selectors:Map[String, Selector[String,_]]) extends KeyValuePairsExtractor[Map[String, Any]] {
  private lazy val requiredKeys = selectors.filter(_._2.isRequired).map(_._2.key)

  protected def parse(value: TraversableOnce[Try[(String, JsonParser)]], path: Path)(implicit registry:Registry): Try[Map[String,Any]] = {
    val hold = if (requiredKeys.nonEmpty) Some(new mutable.HashSet[String]()) else None
    val map = value.flatMap(t => t.map { case (key, parser) =>
      selectors.get(key).map { selector =>
        if (selector.isRequired) hold.map(_ += key)
        selector.parser(parser, path + key).map(selector.toKey -> _)
          .failureMap(f => return f.asInstanceOf[Failure[Map[String,Any]]]).get
      }}
      .failureMap(f => return f.asInstanceOf[Failure[Map[String,Any]]]).get
    ).toMap

    requiredKeys.find(!hold.get.contains(_)).map(k => Failure(KeyNotFoundException(k, path))).getOrElse(Success(map))
  }
}