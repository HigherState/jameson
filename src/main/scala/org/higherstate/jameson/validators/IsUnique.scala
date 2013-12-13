package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.exceptions.InvalidValueException

object IsUnique extends Validator  {

  def apply(value:Any, path:Path) = value match {
    case n:Seq[_] => {
      if (n.toSet.size != n.size) Some(InvalidValueException(this, "Elements in sequence are not unique", n, path))
      else None
    }
    case value    => Some(InvalidValueException(this, "Value does not have a elements", value, path))
  }

  def schema = Map("uniqueItems" -> true)
}
