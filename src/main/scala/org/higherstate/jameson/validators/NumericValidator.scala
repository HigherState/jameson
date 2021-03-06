package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.{ValidationFailure, InvalidValueFailure}

trait NumericValidator extends Validator {

  def apply(value:Any, path:Path) = value match {
    case n:Number =>
      validate(n, path)
    case v =>
      Some(InvalidValueFailure(this, "Value is non-numeric", value, path))
  }

  protected def validate(value:Number, path:Path):Option[ValidationFailure]
}

case class GreaterThan(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue <= compare.doubleValue) Some(InvalidValueFailure(this, s"Expected number to be greater than $compare", value, path))
    else None

  def schema = Map(
    "minimum" -> compare,
    "exclusiveMinimum" -> true
  )
}

case class LessThan(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue >= compare.doubleValue) Some(InvalidValueFailure(this, s"Expected number to be less than $compare", value, path))
    else None

  def schema = Map(
    "maximum" -> compare,
    "exclusiveMaximum" -> true
  )
}

case class GreaterThanEquals(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue < compare.doubleValue) Some(InvalidValueFailure(this, s"Expected number to be greater than or equal to $compare", value, path))
    else None

  def schema = Map(
    "minimum" -> compare
  )
}

case class LessThanEquals(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue > compare.doubleValue) Some(InvalidValueFailure(this, s"Expected number to be less than or equal to $compare", value, path))
    else None

  def schema = Map(
    "maximum" -> compare
  )
}
