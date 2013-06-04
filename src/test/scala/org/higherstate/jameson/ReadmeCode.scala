package org.higherstate.jameson

import org.higherstate.jameson.DefaultRegistry._
import org.higherstate.jameson.Dsl._
import java.awt.Color
import org.joda.time.DateTime
import java.util.UUID

class ReadmeCode {

  case class Rectangle(height:Float, width:Float, location:(Float, Float), color:Color, fillColor:Option[Color])
  case class Circle(radius:Float, location:(Float, Float))
  def toColorFunc(color:Int) = new Color(color)

  val json = """
    {
        "shape":"Rectangle",
        "x":12.35,
        "y":45.6,
        "h":100,
        "w":150,
        "color":14321232,
        "fillColor":null
    }
             """

  val parser =
    matchAs("shape",
      as [Rectangle](
        "h" -> "height",
        "w" -> "width",
        "color" -> as [Int] map toColorFunc,
        "fillColor" -> getAs(as[Int] map toColorFunc),
        ("x"&"y") -> "location" -> asTuple("x" -> as [Float], "y" -> as [Float])),
      as [Circle] (
        ("x"&"y") -> "location" -> asTuple("x" -> as [Float], "y" -> as [Float])
      )
    )
  def func(any:Any) = 3

  // Basic key to parser map
  val selector = "key" -> parser

  //If we need to map to a new key, or class parameter
  val remapSelector = "key" -> "newKey" -> parser

  //If we have different possible keys to map to a new key or class parameter
  val orKeysselector = ("key1"|"key2"|"key3") -> "newKey" -> parser

  //If we we want to pipe parser results through a function
  val mapSelector = "key" -> parser map func
  val altMapSelector = "key" -> parser |> func

  //If we want to group keys and parse them to another object parser
  val groupKeysSelector = ("key1"&"key2") -> "newKey" -> parser

  //Boolean parser
  val boolParser = as [Boolean]

  //Numeric parsers with validation
  val doubleLessThanParser = as [Double] < 25
  val intRangeParser  = as [Int] > 25 <= 35

  //regex parsing
  val regexParser = as [String] regex "<TAG\b[^>]*>(.*?)</TAG>"

  //email parsing
  val emailParser = as [String] is email

  //string length
  val stringLengthParser = as [String] maxlength 3 minlength 12

  val charParser = as [Char]
  val byteParser = as [Byte]
  val longParser = as [Long]
  val floatParser = as [Float]
  val jodaDateTimeParser = as [DateTime]
  val uuidParser = as [UUID]

  //parsing a json array to tuple
  val tupleListParser = asTuple[String, Double, Boolean]

  //parsing json array with specific parsers
  val tupleListDefinedParsers = asTuple(as [String], as [Double] > 0, as [Boolean])

  //parsing json object to tuple
  val tupleMapParser = asTuple("double" -> as [Double], "int" -> as [Int])

  //parsing json object to tuple with complex selectors
//  val tupleMapParserOrKeys = asTuple(
//    ("double"|"Double") -> as [Double],
//    "int" -> as [Int] map func,
//    ("x"&"y") -> as [Point])

  val differenceParser = asTuple[Int,Int] map (_ - _)

  //parser will auto identify constructor arguments and map with json object keys
  case class SimpleClass(string:String, int:Int)
  val simpleParser = as [SimpleClass]
  simpleParser("""{
  "string":"text",
  "int":0
  }""")


  //can specify validation on an argument
  val validationParser = as [SimpleClass]("int" -> as [Int] > 0)
  validationParser("""{
  "string":"text",
  "int":10
  }""")


  //can remap key if argument name doesn't match
  val remapParser = as [SimpleClass]("text" -> "string")
  remapParser("""{
  "text":"text",
  "int":10
  }""")

  //parser will automatically result nested case classes
  case class NestedClass(simple:SimpleClass)
  val nestedParser = as [NestedClass]
  nestedParser("""{
  "simple": {
    "string":"text",
    "int":0
    }
  }""")


  val nestedValidationParser = as[NestedClass] (
    "SimpleClass" -> as [SimpleClass]("int" -> as [Int] > 0)
  )
  nestedValidationParser("""{
  "simple": {
    "string":"text",
    "int":10
    }
  }""")

  //can group keys to create nested classes
  val groupParser = as [NestedClass] (
    ("string"&"int") -> "simple" -> as [SimpleClass]
  )
  groupParser("""{
  "string":"text",
  "int":10,
  "bool":false
  }""")

  //can group keys to create nested classes
}
