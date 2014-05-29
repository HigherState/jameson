package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.{CustomValidationFailure, ValidationFailure}
import reflect.runtime.universe._
//
//case class CustomValidator[T](condition:T => Boolean, failure:T => Any)(implicit typeTag:TypeTag[T]) extends Validator {
//
//
//  def apply(value:T, path:Path):Option[ValidationFailure] =
//    if (!condition(value)) failure(value) match {
//
//    }
//    else None
//
//
//  def schema: Map[String, Any] = ???
//}
//
//case class CustomPartialFunctionValidation[T,Any](func:PartialFunction[T,Any]) extends Validator {
//
//  def apply(value:T, path:Path):Option[ValidationFailure] =
//    func.lift.apply(value.asInstanceOf[T]).map {
//      case v:ValidationFailure =>
//        v
//      case a =>
//        CustomValidationFailure(a).asInstanceOf[ValidationFailure]
//    }
//
//  def schema: Map[String, Any] = ???
//}
