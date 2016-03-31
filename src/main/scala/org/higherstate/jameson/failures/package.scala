package org.higherstate.jameson

import scalaz.{\/, NonEmptyList}

package object failures {
  type Valid[T] = NonEmptyList[ValidationFailure] \/ T

  type Failure[T] = scalaz.Failure[ValidationFailure]

  type Success[T] = scalaz.Success[T]

  object Success {
    def unapply[T](v:Valid[T]) =
      v.toOption

    def apply[T](value:T):Valid[T] =
      scalaz.\/-(value)
  }

  object Failure {
    def unapply[T](v:Valid[T]) =
      v.swap.toOption

    def apply[T](failure:ValidationFailure):Valid[T] =
      scalaz.-\/(NonEmptyList(failure))

    def apply[T](failures:NonEmptyList[ValidationFailure]):Valid[T] =
      scalaz.-\/(failures)
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
          case (scalaz.-\/(x), scalaz.-\/(y)) =>
            Failure(x.append(y))
          case (scalaz.-\/(x), _) =>
            Failure(x)
          case (_, scalaz.-\/(y)) =>
            Failure(y)
        }
    }
  }
}
