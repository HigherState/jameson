package org.higherstate.jameson

import cats.data.NonEmptyList


package object failures {
  type Valid[+T] = NonEmptyList[ValidationFailure] Either T

  type Failure[+T] = Right[ValidationFailure, T]

  type Success[+T] = Left[ValidationFailure, T]

  object Success {
    def unapply[T](v:Valid[T]) =
      v.toOption

    def apply[T](value:T):Valid[T] =
      Right(value)
  }

  object Failure {
    def unapply[T](v:Valid[T]) =
      v.swap.toOption

    def apply[T](failure:ValidationFailure):Valid[T] =
      Left(NonEmptyList(failure, Nil))

    def apply[T](failures:NonEmptyList[ValidationFailure]):Valid[T] =
      Left(failures)
  }


  implicit class ValidWrapper[T](val valid:Valid[T]) extends AnyVal {

    def isInterrupt =
      valid match {
        case Failure(x) if x.toList.last.isInstanceOf[TokenStreamInterrupt] => true
        case _ => false
      }

    def combine[U,W](value: => Valid[U])(f:(T,U) => W):Valid[W] = {
      if (isInterrupt) valid.asInstanceOf[Valid[W]]
      else
        (valid, value) match {
          case (Success(x), Success(y)) =>
            Success(f(x, y))
          case (Left(x), Left(y)) =>
            Failure(x ++ y.toList)
          case (Left(x), _) =>
            Failure(x)
          case (_, Left(y)) =>
            Failure(y)
        }
    }
  }
}
