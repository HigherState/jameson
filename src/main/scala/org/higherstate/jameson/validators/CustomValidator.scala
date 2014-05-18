package org.higherstate.jameson.validators

import org.higherstate.jameson.Path
import org.higherstate.jameson.failures.{CustomValidationFailure, ValidationFailure}
import reflect.runtime.universe.TypeTag

//case class CustomValidator[T](condition:T => Boolean, failure:T => Any)(implicit typeTag:TypeTag[T]) extends Validator {
//
//
//  def apply(value:Any, path:Path):Option[ValidationFailure] =
//    if (value.isInstanceOf[T] && !condition(value.asInstanceOf[T]))
//      failure(value.asInstanceOf[T]) match {
//        case v:ValidationFailure => Some(v)
//        case a => Some(CustomValidationFailure(a))
//      }
//    else None
//
//
//  def schema: Map[String, Any] = ???
//}
//
//case class CustomPartialFunctionValidation[T,Any](func:PartialFunction[T,Any]) extends Validator {
//
//  def apply(value:Any, path:Path):Option[ValidationFailure] =
//    if (value.isInstanceOf[T]) func.lift.apply(value.asInstanceOf[T]).collect {
//      case v:ValidationFailure =>
//        v
//      case a =>
//        CustomValidationFailure(a)
//    }
//    else None
//
//  def schema: Map[String, Any] = ???
//}
