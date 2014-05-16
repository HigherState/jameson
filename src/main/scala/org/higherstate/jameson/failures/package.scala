package org.higherstate.jameson

import scalaz.{NonEmptyList, ValidationNel}

package object failures {
  type Valid[T] = ValidationNel[ValidationFailure, T]

  type Failure[T] = scalaz.Failure[ValidationFailure, T]

  type Success[T] = scalaz.Success[ValidationFailure, T]

  object Success {
    def unapply[T](v:Valid[T]) =
      v.toOption

    def apply[T](value:T):Valid[T] =
      scalaz.Success(value)
  }

  object Failure {
    def unapply[T](v:Valid[T]) =
      v.swap.toOption

    def apply[T](failure:ValidationFailure):Valid[T] =
      scalaz.Failure(NonEmptyList(failure))

    def apply[T](failures:NonEmptyList[ValidationFailure]):Valid[T] =
      scalaz.Failure(failures)
  }
}
