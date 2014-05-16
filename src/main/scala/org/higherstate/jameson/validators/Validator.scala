package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.ValidationFailure

trait Validator {
  def apply(value:Any, path:Path):Option[ValidationFailure]

  def schema:Map[String, Any]
}
