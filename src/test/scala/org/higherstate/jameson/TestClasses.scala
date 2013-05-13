package org.higherstate.jameson

trait Parent
trait Collections

case class Child1(tInt:Int) extends Parent
case class Child2(tBool:Boolean) extends Parent
case class Child3(tInt:Int, tBool:Boolean) extends Parent
case class NestedChild(child1:Child1, child2:Child2) extends Parent
case class MapChild(map:Map[String,Any]) extends Collections
case class ListChild(list:List[Any]) extends Collections
case class ListCharChild(list:List[Char]) extends Collections