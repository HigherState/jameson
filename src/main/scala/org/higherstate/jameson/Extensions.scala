package org.higherstate.jameson

import scala.util.{Try, Failure, Success}

object Extensions {

  implicit class IteratorExt[T](val self:Iterator[T]) extends AnyVal {
    def takeWhileInclusive(predicate:(T) => Boolean) = {
      var done = false
      val p2 = (t: T) => !done && { if (!predicate(t)) done=true; true }
      self.takeWhile(p2)
    }
  }

  implicit class FailureExt[T](val self:Try[T]) extends AnyVal {
    def failureMap(function:Failure[T] => Failure[T]) = self match {
      case s:Success[T] => s
      case f:Failure[T] => function(f)
    }
  }
}
