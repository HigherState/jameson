package org.higherstate.jameson

trait Parent
trait Collections

case class Child1(tInt:Int) extends Parent
case class Child2(tBool:Boolean) extends Parent
case class Child3(tInt:Int, tBool:Boolean) extends Parent
case class PartialNestedChild(tFloat:Float, child3:Child3, tString:String)
case class DoubleNestedChild(tFloat:Float, child3:Child3, child32:Child3, tString:String)
case class NestedChild(child1:Child1, child2:Child2) extends Parent
case class ParentContainer(parent:Option[ParentContainer])
case class MapChild(map:Map[String,Any]) extends Collections
case class ListChild(list:List[Any]) extends Collections
case class ListCharChild(list:List[Char]) extends Collections
case class ListParents(int:Int, parents:List[Parent])
case class RecursiveChild1(value:Int, child:RecursiveChild2)
case class RecursiveChild2(value:String, child:Option[RecursiveChild1])

case class ChildDouble(tDouble:Double)
case class ChildByte(tByte:Byte)