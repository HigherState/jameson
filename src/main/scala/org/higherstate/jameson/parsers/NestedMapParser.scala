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

  def parse(tokenizer:Tokenizer, path: Path): Try[Map[String,Any]] = tokenizer.head match {
    case ObjectStartToken =>
     if (requiredKeys.nonEmpty || defaultKeys.nonEmpty) toMap(tokenizer.moveNext(), path, Set.empty) flatMap { case (map, hold) =>
       requiredKeys.find(!hold.contains(_)).map(k => Failure(KeyNotFoundException(k, path))).getOrElse{
         Success(defaultKeys.filter(s => !hold.contains(s.key)).foldLeft(map)((m, s) => m + (s.toKey -> s.parser.asInstanceOf[HasDefault[_]].default)))
       }
     }
     else toMap(tokenizer.moveNext(), path)
    case token           => Failure(UnexpectedTokenException("Expected object start token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path:Path, hold:Set[String]):Try[(Map[String, Any], Set[String])]
  protected def toMap(tokenizer:Tokenizer, path:Path): Try[Map[String, Any]]
}

case class OpenMapParser(selectors:Map[String, Selector[String,_]], defaultParser:Parser[Any]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Try[(Map[String, Any], Set[String])] = tokenizer.head match {
    case ObjectEndToken => Success((Map.empty[String, Any], hold))
    case KeyToken(key)  => {
      val (parser, toKey) = selectors.get(key).mapOrElse(s => s.parser -> s.toKey, defaultParser -> key)
      parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
        toMap(tokenizer.moveNext(), path, hold + key).map { case (m, hold) => (m + (toKey -> r), hold) }
      }
    }
    case token          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path: Path):Try[Map[String, Any]] = tokenizer.head match {
    case ObjectEndToken => Success(Map.empty[String, Any])
    case KeyToken(key)  => {
      val (parser, toKey) = selectors.get(key).mapOrElse(s => s.parser -> s.toKey, defaultParser -> key)
      parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
        toMap(tokenizer.moveNext(), path).map(_ + (toKey -> r))
      }
    }
    case token          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}

case class DropMapParser(selectors:Map[String, Selector[String,_]]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Try[(Map[String, Any], Set[String])] = tokenizer.head match {
    case ObjectEndToken => Success((Map.empty[String, Any], hold))
    case KeyToken(key)  => selectors.get(key).map(p => p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
      toMap(tokenizer.moveNext(), path, hold + key).map { case (m, hold) => (m + (p.toKey -> r), hold) }
    }).getOrElse(toMap(tokenizer.dropNext(), path, hold))
    case token          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path: Path):Try[Map[String, Any]] = tokenizer.head match {
    case ObjectEndToken => Success(Map.empty[String, Any])
    case KeyToken(key)  => selectors.get(key).map(p => p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
      toMap(tokenizer.moveNext(), path).map(_ + (p.toKey -> r))
    }).getOrElse(toMap(tokenizer.dropNext(), path))
    case token          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}

case class CloseMapParser(selectors:Map[String, Selector[String,_]]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Try[(Map[String, Any], Set[String])] = tokenizer.head match {
    case ObjectEndToken => Success((Map.empty[String, Any], hold))
    case KeyToken(key)  => selectors.get(key).map(p => p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
      toMap(tokenizer.moveNext(), path, hold + key).map { case (m, hold) => (m + (p.toKey -> r), hold) }
    }).getOrElse(Failure(UnexpectedKeyException(key, path)))
    case token          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path: Path):Try[Map[String, Any]] = tokenizer.head match {
    case ObjectEndToken => Success(Map.empty[String, Any])
    case KeyToken(key)  => selectors.get(key).map(p => p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
      toMap(tokenizer.moveNext(), path).map(_ + (p.toKey -> r))
    }).getOrElse(Failure(UnexpectedKeyException(key, path)))
    case token          => Failure(UnexpectedTokenException("Expected object end token or key token", token, path))
  }
}
