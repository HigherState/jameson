package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.extractors.{KeyValuePairsExtractor}
import util.{Failure, Success, Try}
import com.fasterxml.jackson.core.JsonParser
import collection.mutable
import org.higherstate.jameson.exceptions.KeyNotFoundException
import org.higherstate.jameson.{Selector, Registry, Path}

case class OpenMapParser(selectors:Map[String, Selector[String,_]]) extends KeyValuePairsExtractor[Map[String, Any]] {
  private lazy val requiredKeys = selectors.filter(s => s._2.isRequired && !s._2.parser.isInstanceOf[HasDefault[_]]).map(_._2.key)
  private lazy val defaultKeys = selectors.filter(s => s._2.isRequired && s._2.parser.isInstanceOf[HasDefault[_]]).map(_._2)

  protected def parse(value: TraversableOnce[Try[(String, JsonParser)]], path: Path)(implicit registry:Registry): Try[Map[String,Any]]  = {
    val hold = if (requiredKeys.nonEmpty) Some(new mutable.HashSet[String]()) else None
    val map = value.map(_.flatMap { case (key, parser) =>
      selectors.get(key).map { selector =>
        if (selector.isRequired) hold.map(_ += key)
        selector.parser(parser, path + key).map(t => selector.toKey -> t)
      }
      .getOrElse(registry.defaultUnknownParser(parser, path + key).map(key -> _))
    }.failureMap(f => return f.asInstanceOf[Failure[Map[String,Any]]])
     .get
    ).toMap
    requiredKeys.find(!hold.get.contains(_)).map(k => Failure(KeyNotFoundException(k, path))).getOrElse{
      Success(defaultKeys.filter(s => !hold.get.contains(s.key)).foldLeft(map)((m, s) => m + (s.toKey -> s.parser.asInstanceOf[HasDefault[_]].default)))
    }
  }
}
