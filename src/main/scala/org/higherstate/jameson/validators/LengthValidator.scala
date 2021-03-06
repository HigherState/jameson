package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.{ValidationFailure, InvalidValueFailure}


trait LengthValidator extends Validator {

  def apply(value:Any, path:Path) = value match {
    case n:Seq[_] =>
      validate(n.length, path)
    case n:String =>
      validate(n.length, path)
    case v =>
      Some(InvalidValueFailure(this, "Value does not have a length", v, path))
  }

  protected def validate(value:Int, path:Path):Option[ValidationFailure]
}

case class MinLength(compare:Int) extends LengthValidator {
  protected def validate(value:Int, path:Path):Option[ValidationFailure] =
    if (value < compare) Some(InvalidValueFailure(this, s"Expected value length to be greater than $compare", value, path))
    else None

  def schema = Map("minItems" -> compare)
}

case class MaxLength(compare:Int) extends LengthValidator {
  protected def validate(value:Int, path:Path):Option[ValidationFailure] =
    if (value > compare) Some(InvalidValueFailure(this, s"Expected value length to be less than $compare", value, path))
    else None

  def schema = Map("maxItems" -> compare)
}

