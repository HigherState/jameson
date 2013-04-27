package org.higherstate.jameson

trait Path {
  def + (index:Int):Path
  def + (key:String):Path
  def toString():String
}
object NoPath extends Path {
  def + (index:Int):Path = NoPath
  def + (key:String):Path = NoPath
  override def toString() = "No path defined"
}
object Path extends Path {
  def + (index:Int):Path = PathItem(s"[$index]" :: Nil)
  def + (key:String):Path = PathItem(key :: Nil)
  override def toString() = "/"
}
private case class PathItem(elements:List[String]) extends Path {
  def + (index:Int):Path = PathItem(s"[$index]" :: elements)
  def + (key:String):Path = PathItem(key :: elements)
  override def toString() = elements.reverse.mkString("/","/","")
}
