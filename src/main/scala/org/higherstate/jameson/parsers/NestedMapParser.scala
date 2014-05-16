package org.higherstate.jameson.parsers

import org.higherstate.jameson.Extensions._
import org.higherstate.jameson.tokenizers._
import org.higherstate.jameson.failures._
import org.higherstate.jameson.{KeySelector, Path}

sealed trait NestedMapParser extends Parser[Map[String, Any]] {
  def selectors:Map[String, KeySelector[String,_]]

  protected lazy val requiredKeys = selectors.filter(s => s._2.isRequired && !s._2.parser.hasDefault).map(_._2.toKey)
  protected lazy val defaultKeys = selectors.filter(s => s._2.isRequired).flatMap(s => s._2.parser.default.map(d => s._2.toKey -> d))

  def parse(tokenizer:Tokenizer, path: Path): Valid[Map[String,Any]] = tokenizer.head match {
    case ObjectStartToken =>
     if (requiredKeys.nonEmpty || defaultKeys.nonEmpty)
       toMap(tokenizer.moveNext(), path, Set.empty) flatMap { case (map, hold) =>
         requiredKeys.find(!hold.contains(_))
           .fold(Success(map ++ defaultKeys.filter(p => !hold.contains(p._1))))(k => Failure(KeyNotFoundFailure(this, k, path)))
       }
     else toMap(tokenizer.moveNext(), path)
    case token =>
      Failure(InvalidTokenFailure(this, "Expected object start token", token, path))
  }

  protected def toMap(tokenizer:Tokenizer, path:Path, hold:Set[String]):Valid[(Map[String, Any], Set[String])]
  protected def toMap(tokenizer:Tokenizer, path:Path): Valid[Map[String, Any]]

  def schema:Map[String, Any] = {
    val m = Map("type" -> "object")
    val s = m ++ selectors.map(s => s._1 -> s._2.parser.schema)
    val r = selectors.filter(!_._2.isRequired)
    if (r.isEmpty) s
    else s + ("required" -> r.map(_._1))
  }
}

case class OpenMapParser(selectors:Map[String, KeySelector[String,_]], defaultParser:Parser[Any]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Valid[(Map[String, Any], Set[String])] =
    tokenizer.head match {
      case ObjectEndToken =>
        Success((Map.empty[String, Any], hold))
      case KeyToken(key)  =>
        val (parser, toKey) = selectors.get(key).mapOrElse(s => s.parser -> s.toKey, defaultParser -> key)
        parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
          toMap(tokenizer.moveNext(), path, hold + toKey).map { case (m, h) => (m + (toKey -> r), h) }
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Expected object end token or key token", token, path))
    }

  protected def toMap(tokenizer:Tokenizer, path: Path):Valid[Map[String, Any]] = tokenizer.head match {
    case ObjectEndToken =>
      Success(Map.empty[String, Any])
    case KeyToken(key)  =>
      val (parser, toKey) = selectors.get(key).mapOrElse(s => s.parser -> s.toKey, defaultParser -> key)
      parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
        toMap(tokenizer.moveNext(), path).map(_ + (toKey -> r))
      }
    case token =>
      Failure(InvalidTokenFailure(this, "Expected object end token or key token", token, path))
  }

  override def schema = super.schema + ("additionalProperties" -> true)
}

case class DropMapParser(selectors:Map[String, KeySelector[String,_]]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Valid[(Map[String, Any], Set[String])] =
    tokenizer.head match {
      case ObjectEndToken =>
        Success((Map.empty[String, Any], hold))
      case KeyToken(key)  =>
        selectors.get(key).fold(toMap(tokenizer.dropNext(), path, hold)) { p =>
          p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
            toMap(tokenizer.moveNext(), path, hold + p.toKey).map { case (m, h) => (m + (p.toKey -> r), h)}
          }
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Expected object end token or key token", token, path))
    }

  protected def toMap(tokenizer:Tokenizer, path: Path):Valid[Map[String, Any]] =
    tokenizer.head match {
      case ObjectEndToken =>
        Success(Map.empty[String, Any])
      case KeyToken(key)  =>
        selectors.get(key).fold(toMap(tokenizer.dropNext(), path)) { p =>
          p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
            toMap(tokenizer.moveNext(), path).map(_ + (p.toKey -> r))
          }
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Expected object end token or key token", token, path))
    }

  override def schema = super.schema + ("additionalProperties" -> true)
}

case class CloseMapParser(selectors:Map[String, KeySelector[String,_]]) extends NestedMapParser {

  protected def toMap(tokenizer:Tokenizer, path: Path, hold:Set[String]):Valid[(Map[String, Any], Set[String])] =
    tokenizer.head match {
      case ObjectEndToken =>
        Success((Map.empty[String, Any], hold))
      case KeyToken(key)  =>
        selectors.get(key).fold[Valid[(Map[String, Any], Set[String])]](Failure(UnexpectedKeyFailure(this, key, path))){p =>
          p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
            toMap(tokenizer.moveNext(), path, hold + p.toKey).map { case (m, h) => (m + (p.toKey -> r), h) }
          }
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Expected object end token or key token", token, path))
    }

  protected def toMap(tokenizer:Tokenizer, path: Path):Valid[Map[String, Any]] =
    tokenizer.head match {
      case ObjectEndToken =>
        Success(Map.empty[String, Any])
      case KeyToken(key)  =>
        selectors.get(key).fold[Valid[Map[String, Any]]](Failure(UnexpectedKeyFailure(this, key, path))) { p =>
          p.parser.parse(tokenizer.moveNext(), path + key).flatMap { r =>
            toMap(tokenizer.moveNext(), path).map(_ + (p.toKey -> r))
          }
        }
      case token =>
        Failure(InvalidTokenFailure(this, "Expected object end token or key token", token, path))
    }

  override def schema = super.schema + ("additionalProperties" -> false)
}
