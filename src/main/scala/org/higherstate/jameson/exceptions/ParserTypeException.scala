package org.higherstate.jameson.exceptions

case class ParserTypeException(found:String, expected:String) extends Exception()