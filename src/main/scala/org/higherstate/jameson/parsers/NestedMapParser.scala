package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.tokenizers._
import util.{Failure, Success, Try}
import org.higherstate.jameson.exceptions.{UnexpectedTokenException, UnexpectedKeyException, KeyNotFoundException}
import org.higherstate.jameson.{Selector, Path}

sealed trait NestedMapParser extends Parser[Map[String, Any]] {
  def selectors:Map[String, Selector[String,_]]
  protected lazy val requiredKeys = selectors.filter(s => s._2.isRequired && !s._2.parser.isInstanceOf[HasDefault[_]]).map(_._2.key)
  protected lazy val defaultKeys = selectors.filter(s => s._2.isRequired && s._2.parser.isInstanceOf[HasDefault[_]]).map(_._2)

  protected def parse(tokenizer:Tokenizer, path: Path): Try[(Map[String,Any], Tokenizer)] = tokenizer match {
    case ObjectStartToken -: tail =>
     if (requiredKeys.nonEmpty || defaultKeys.nonEmpty) toMap(tail, path, Set.empty) flatMap { case (map, tokenizer, hold) =>
       requiredKeys.find(!hold.contains(_)).map(k => Failure(KeyNotFoundException(k, path))).getOrElse{
         Success(defaultKeys.filter(s => !hold.contains(s.key)).foldLeft(map)((m, s) => m + (s.toKey -> s.parser.asInstanceOf[HasDefault[_]].default)) -> tokenizer)
       }
     }
     else toMap(tokenizer, path)
    case token -: tail            => Failure(UnexpectedTokenException("Expected object start token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path:Path, hold:Set[String]):Try[(Map[String, Any], Tokenizer, Set[String])]
  protected def toMap(tokenizer:Tokenizer, path:Path): Try[(Map[String, Any], Tokenizer)]
}

case class OpenMapParser(selectors:Map[String, Selector[String,_]], defaultParser:Parser[Any]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Try[(Map[String, Any], Tokenizer, Set[String])] = tokenizer match {
    case ObjectEndToken -: tail => Success((Map.empty[String, Any], tail, hold))
    case KeyToken(key) -: tail  => selectors.get(key).mapOrElse(_.parser, defaultParser).parse(tail, path + key).flatMap {
        case (r, tokenizer) => toMap(tokenizer, path, hold + key).map { case (m, tokenizer, hold) => (m + (key -> r), tokenizer, hold) }
      }
    case token -: tail          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path: Path):Try[(Map[String, Any], Tokenizer)] = tokenizer match {
    case ObjectEndToken -: tail => Success((Map.empty[String, Any], tail))
    case KeyToken(key) -: tail  => selectors.get(key).mapOrElse(_.parser, defaultParser).parse(tail, path + key).flatMap {
      case (r, tokenizer) => toMap(tokenizer, path).map(_.mapLeft(_ + (key -> r)))
    }
    case token -: tail          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}

case class DropMapParser(selectors:Map[String, Selector[String,_]]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Try[(Map[String, Any], Tokenizer, Set[String])] = tokenizer match {
    case ObjectEndToken -: tail => Success((Map.empty[String, Any], tail, hold))
    case KeyToken(key) -: tail  => selectors.get(key).map(_.parser.parse(tail, path + key).flatMap {
      case (r, tokenizer) => toMap(tokenizer, path, hold + key).map { case (m, tokenizer, hold) => (m + (key -> r), tokenizer, hold) }
    }).getOrElse(toMap(tokenizer, path, hold))
    case token -: tail          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path: Path):Try[(Map[String, Any], Tokenizer)] = tokenizer match {
    case ObjectEndToken -: tail => Success((Map.empty[String, Any], tail))
    case KeyToken(key) -: tail  => selectors.get(key).map(_.parser.parse(tail, path + key).flatMap {
      case (r, tokenizer) => toMap(tokenizer, path).map(_.mapLeft(_ + (key -> r)))
    }).getOrElse(toMap(tokenizer, path))
    case token -: tail          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}

case class CloseMapParser(selectors:Map[String, Selector[String,_]]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Try[(Map[String, Any], Tokenizer, Set[String])] = tokenizer match {
    case ObjectEndToken -: tail => Success((Map.empty[String, Any], tail, hold))
    case KeyToken(key) -: tail  => selectors.get(key).map(_.parser.parse(tail, path + key).flatMap {
      case (r, tokenizer) => toMap(tokenizer, path, hold + key).map { case (m, tokenizer, hold) => (m + (key -> r), tokenizer, hold) }
    }).getOrElse(Failure(UnexpectedKeyException(key, path)))
    case token -: tail          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path: Path):Try[(Map[String, Any], Tokenizer)] = tokenizer match {
    case ObjectEndToken -: tail => Success((Map.empty[String, Any], tail))
    case KeyToken(key) -: tail  => selectors.get(key).map(_.parser.parse(tail, path + key).flatMap {
      case (r, tokenizer) => toMap(tokenizer, path).map(_.mapLeft(_ + (key -> r)))
    }).getOrElse(Failure(UnexpectedKeyException(key, path)))
    case token -: tail          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}
