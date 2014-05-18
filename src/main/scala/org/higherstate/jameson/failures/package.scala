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

  implicit class ValidWrapper[T](val valid:Valid[T]) extends AnyVal {

    def isInterrupt =
      valid match {
        case Failure(x) if x.last.isInstanceOf[TokenStreamInterrupt] => true
        case _ => false
      }

    def combine[U,W](value: => Valid[U])(f:(T,U) => W):Valid[W] = {
      if (isInterrupt) valid.asInstanceOf[Valid[W]]
      else
        (valid, value) match {
          case (Success(x), Success(y)) =>
            Success(f(x, y))
          case (scalaz.Failure(x), scalaz.Failure(y)) =>
            Failure(x.append(y))
          case (scalaz.Failure(x), _) =>
            Failure(x)
          case (_, scalaz.Failure(y)) =>
            Failure(y)
        }
    }
  }
}
