package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.exceptions.InvalidValueException
import scala.util.matching.Regex

trait StringValidator extends Validator {

  def apply(value:Any, path:Path) = value match {
    case n:String => validate(n, path)
    case value    => Some(InvalidValueException(this, "Value is not text", value, path))
  }

  protected def validate(value:String, path:Path):Option[Throwable]
}

case class RegEx(regex:Regex) extends StringValidator {

  protected def validate(value:String, path:Path) =
    if (regex.pattern.matcher(value).matches()) None
    else Some(InvalidValueException(this, "Invalid string format", value, path))
}

case object IsEmail extends StringValidator {

  private val regex = """(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""".r

  protected def validate(value:String, path:Path) =
    if (regex.pattern.matcher(value).matches()) None
    else Some(InvalidValueException(this, "Value is not a valid email address", value, path))
}