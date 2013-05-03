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
    def failureMap(function:Throwable => Failure[T]) = self match {
      case s:Success[T] => s
      case Failure(t) => function(t)
    }
  }

  implicit class TupleExt[A,B](val a:(A,B)) extends AnyVal {
    def mapLeft[X](f: A => X) = (f(a._1), a._2)
    def mapRight[X](f: B => X) = (a._1, f(a._2))
  }

  implicit class OptionExt[T](val self: Option[T]) extends AnyVal {
    def mapOrElse[U](map:T => U,orElse:U) = self.map(map).getOrElse(orElse)
  }
}
