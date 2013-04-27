package com.higherstate.jameson

trait Parent

case class Child1(tInt:Int) extends Parent
case class Child2(tBool:Boolean) extends Parent
case class Child3(t:Int, tBool:Boolean) extends Parent
case class NestedChild(child1:Child1, child2:Child2) extends Parent
case class MapChild(map:Map[String,Any])
case class ListChild(list:List[Any])
case class ListCharChild(list:List[Char])