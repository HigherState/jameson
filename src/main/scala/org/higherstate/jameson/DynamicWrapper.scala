package org.higherstate.jameson

import scala.language.dynamics

case class DynamicWrapper[+T](map:Map[String, T]) extends AnyVal with Dynamic {
  def selectDynamic(name: String) = map(name)
}
