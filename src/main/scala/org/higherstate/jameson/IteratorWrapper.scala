package org.higherstate.jameson

import util.{Try,Failure, Success}

case class IteratorWrapper[T](hasNextFunc:() => Try[Boolean], nextFunc:() => Try[T]) extends Iterator[Try[T]] {

  private var failure:Option[Failure[T]] = None
  def hasNext() = failure.isEmpty && {
    hasNextFunc() match {
      case f:Failure[Boolean] => failure = Some(Failure(f.exception)); true
      case Success(b)         => b
    }
  }
  def next() = failure.getOrElse(nextFunc())
}