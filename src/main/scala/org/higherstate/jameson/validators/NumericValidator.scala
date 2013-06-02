package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.exceptions.InvalidValueException

trait NumericValidator extends Validator {

  def apply(value:Any, path:Path) = value match {
    case n:Number => validate(n, path)
    case value    => Some(InvalidValueException(this, "Value is non-numeric", value, path))
  }

  protected def validate(value:Number, path:Path):Option[Throwable]
}

case class GreaterThan(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue <= value.doubleValue) Some(InvalidValueException(this, s"Expected number to be greater than $compare", value, path))
    else None
}

case class LessThan(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue >= value.doubleValue) Some(InvalidValueException(this, s"Expected number to be less than $compare", value, path))
    else None
}

case class GreaterThanEquals(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue < value.doubleValue) Some(InvalidValueException(this, s"Expected number to be greater than or equal to $compare", value, path))
    else None
}

case class LessThanEquals(compare:Number) extends NumericValidator {
  protected def validate(value:Number, path:Path) =
    if (value.doubleValue > value.doubleValue) Some(InvalidValueException(this, s"Expected number to be less than or equal to $compare", value, path))
    else None
}
