package org.higherstate.jameson.failures

case class CustomValidationFailure(failure:Any) extends ValidationFailure {
  def message: String = failure.toString
}
