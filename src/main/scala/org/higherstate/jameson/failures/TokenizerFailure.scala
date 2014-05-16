package org.higherstate.jameson.failures

case class TokenizerFailure(exception:Throwable) extends ValidationFailure {
  def message = exception.getMessage
}