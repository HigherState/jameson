package org.higherstate.jameson.failures

import org.higherstate.jameson.Path

trait PathFailure extends ValidationFailure {
  def path:Path
}
