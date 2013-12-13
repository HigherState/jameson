package org.higherstate.jameson.exceptions

import org.higherstate.jameson.Path

trait PathException extends Throwable {
  def path:Path
}
