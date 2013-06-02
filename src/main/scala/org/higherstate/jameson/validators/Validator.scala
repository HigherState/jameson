package org.higherstate.jameson.validators

import org.higherstate.jameson.Path

trait Validator {
  def apply(value:Any, path:Path):Option[Throwable]
}
