package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.InvalidValueFailure

object IsUnique extends Validator  {

  def apply(value:Any, path:Path) =
    value match {
      case n:Seq[_] =>
        if (n.toSet.size != n.size) Some(InvalidValueFailure(this, "Elements in sequence are not unique", n, path))
        else None
      case v =>
        Some(InvalidValueFailure(this, "Value does not have a elements", v, path))
    }

  def schema = Map("uniqueItems" -> true)
}
