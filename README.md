#Overview

###[HigherState][]

Jameson builds on [Jackson][] to provide a DSL for [Scala][] which ties validation with 
deserialization.  Jameson supports deserializing into native Scala Map and Lists as
well as deserializing a stream into a TraversableOnce. Tuples, Option and Either types are also mapped, 
as well as the ability to pipe values into a function.  
Jameson can deserialize into case classes and nested case classes. It is fully designed to support 
custom extension.

Jameson parse returns a scala.util.Try object.  It will stop parsing on the first failure.

Jameson's DSL supports validation on any nested value as well as conditional deserialization 
and validation depending on key value matches. Jameson also supports key substitution.

# Usage

To use Jameson simply include a reference to the DSL to create your own parser validators.

```scala
import org.higherState.jameson.Dsl._
import org.higherstate.jameson.DefaultRegistry._
    
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
    }"""

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
```

you can then define your own parser validation

####Key validator pairs

Jameson supports parser validators against key value pairs these are used for mapping json objects onto class parser parameters,
tuple parameters or Maps.  These are of the form:  
  

```scala
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
```

####Parsing primitives, strings and dates

```scala

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

//Other supported parsers
val charParser = as [Char]
val byteParser = as [Byte]
val longParser = as [Long]
val floatParser = as [Float]
val jodaDateTimeParser = as [DateTime]
val uuidParser = as [UUID]
```

####Parsing case classes
```scala

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
  
//can select on different possible keys
val possibleParser = as [SimpleClass] (("int"|"number") -> "int")
possibleParser("""{
  "string":"text",
  "number":10
  }""")

//parser will automatically result nested case classes
case class NestedClass(simple:SimpleClass, bool:Boolean)
val nestedParser = as [NestedClass]
nestedParser("""{
  "simple": {
    "string":"text",
    "int":0
    },
  "bool":false
  }""")


val nestedValidationParser = as[NestedClass] (
  "SimpleClass" -> as [SimpleClass]("int" -> as [Int] > 0)
)
nestedValidationParser("""{
  "simple": {
    "string":"text",
    "int":10
    },
  "bool":false
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
```

####Option and defaults

For parsing as an option, both getAs, or asOption maybe used.  
To flatten the option with a default value, use getAsOrElse.

```scala
//parsing an optional value
val optionParser = getAs[Int]
optionParser("3")
res0:Try[Option[Int]] = Success(Some(3))
optionParser("null")
res1:Try[Option[Int]] = Success(None)
optionParser("")
res2:Try[Option[Int]] = Success(None)

//parsing a case class with Option parameter, can resolve type erasure
case class OptionClass(float:Float,int:Option[Int])
val optionClassParser = as [OptionClass] ("int" -> getAs [Int])
optionParser("""{"float":3.5,"int":7}""")
res0:Try[OptionClass] = Success(OptionClass(3.5, Some(7)))
optionParser("""{"float":3.5,"int":null}""")
res0:Try[OptionClass] = Success(OptionClass(3.5, None))
optionParser("""{"float":3.5}""")
res0:Try[OptionClass] = Success(OptionClass(3.5, None))

//parsing with a default value
val defaultParser = getAsOrElse[String]("not found")
defaultParser("\"result\"")
res0:Try[String] = Success("result")
optionParser("null")
res1:Try[String] = Success("not found")
optionParser("")
res2:Try[String] = Success("not found")

val defaultClassParser = getAsOrElse [SimpleClass](SimpleClass("empty", 0))

```
 
####Parsing tuples and mapping into functions with more than one argument
  
```scala
//parsing a json array to tuple
val tupleListParser = asTuple[String, Double, Boolean]

//parsing json array with specific parsers
val tupleListDefinedParsers = asTuple(as [String], as [Double] > 0, as [Boolean])

//parsing json object to tuple
val tupleMapParser = asTuple("double" -> as [Double], "int" -> as [Int])

//parsing json object to tuple with complex selectors
val tupleMapParserOrKeys = asTuple(
  ("double"|"Double") -> as [Double], 
  "int" -> as [Int] map func,
  ("x"&"y") -> as [Point]
)

//mapping tuple results into multi argument functions
val additionParser = asTuple[Int,Int] map (_ + _)
```

####Parsing list

Will parse a json array into a list object

```scala
//parse an array of strings
val stringsParser = asList [String]
stringsParser("""["one","two","three"]""")
res0:List[String] = List("one","two","three")

//parse with a specific parser
val floatsParser = asList (as [Float] > 0)
floatsParser("[1.4,1.1,2.76]")
res1:List[Float] = List(1.4,1.1,2.76)
```

####Paths

Can select a path through nested json objects and arrays, parsing on the result of a single key or array index respectively.  
If any part of the path is not found, a failure will be returned, unless the parser has a default value such as 
Option -> None, List -> Nil, Map -> Map.empty or a getAsOrElse default value.

```scala
//parse a value at the end of a path
val pathParser = path / "key" -> as [Double]
pathParser("""{"key":4.5}""")
res0:Double = 4.5

//parser a value at the end of a path through a nested json object
val nestedPathParser = path / "key1" / "key2" -> as List[Char] minlength 2
nestedPathParser("""{"key1":{"key2":['a','b','c']}}""")
res1:List[Char] = List('a','b','c')

//parse a default value where the path is not found
var defaultPathParser = path / "key" -> as Option[String]
defaultPathParser("""{"key2":"test"}""")
res2:Option[String] = None

//Extract object from array
val pathArrayParser = path / "list" / 1 -> as [SimpleClass]
pathArrayParser("""{"list":[{"string":"one","int":1},{"string":"two","int":2}]}""")
res3:Try[SimpleClass] = Success(SimpleClass("two",2))
```

####Recursive parser

If the json structure is recursive, the parser can be made to reference itself.  The parser must be lazy evaluated and explicitly 
typed.

```Scala
case class ParentContainer(parent:Option[ParentContainer])

//recursive parser across a single class
lazy val parser:Parser[ParentContainer] = as [ParentContainer]("parent" -> asOption(self (parser)))
parser.parse("""{
    "parent":{
        "parent":{
            "parent":{}
        }
    }
}""") 
res0:Try[ParentContainer] = Success(ParentContainer(Some(ParentContainer(Some(ParentContainer(Some(ParentContainer(None))))))))

case class RecursiveChild1(value:Int, child:RecursiveChild2)
case class RecursiveChild2(value:String, child:Option[RecursiveChild1])

//recursive parser across 2 case classes
lazy val parser:Parser[RecursiveChild2] =  as [RecursiveChild2] ("child" -> asOption [RecursiveChild1] ("child" -> self(parser)))
parser("""{
    "value":"one",
    "child":{
        "value":2,
        "child":{
            "value":"three"
        }
    }
}""") 
res1:Try[RecursiveChild2] = Success(RecursiveChild2("one", Some(RecursiveChild1(2, RecursiveChild2("three",None)))))
```

####Parsing stream 

Will parse a json array into a TraversableOnce collection object. Each element is in an individual try as it will not be
parsed until called.

```scala
//parse an array of objects
val streamParser = asStream [SimpleClass]
streamParser("""[{"string":"one","int":1},{"string":"two","int":2},{"string":"three","int":3}]""")
```

####Fold and Fold Left

Will parse array values into a folding operation.

```scala
//Sum values in an array
val sumParser = fold(0)((a, i) => a + i)
sumParser.parse("[1,2,3,4,5,6,7,8,9]")
res0:Try[Int] = Success(45)

//Calculate average
val averageParser = foldLeft[Int, (Int, Int)]((0,0))((a, i) => (a._1 + i, a._2 + 1)) map (a => a._1 / a._2)
averageParser.parse("[1,2,3,4,5,6,7,8,9]")
res1:Try[Int] = Success(5)
```

####Parsing either

Will try parsing left and if fails right.  If the right fails, this will be the exception passed backed.  The either parser
causes buffering of tokens.

```scala
//parsing an either
val eitherParser = asEither[Int, String]

//parsing an either with nested complex selectors
val classParser = asEither (as [SimpleClass], as [Nested Class])
```

####Matching parser

Will parse an json object matching on either a key value pair, or the existance of a key.  
When matching on existence of a key, if more than one match is found, precedence is given in order of the parsers.
Partial functions can be used as well.  The matching parser causes buffering of tokens.

```scala
case class Square(width:Int)
case class Rectangle(height:Int,width:Int)
case class Circle(radius:Int)

//match on a key value, no default provided
val shapeParser = matchAs("shape", "sq" -> as [Square], "rect" -> as [Rectangle], "circ" -> as [Circle])
shapeParser("""{"shape":"sq","width":10}""")
res0:Try[AnyRef] = Success(Square(10))

//match on class type name, Rectangle is default if no shape key found
val typeNameParser = matchAs("type", "Rectangle", as [Square], as [Rectangle], as [Circle])
typeNameParser("""{"radius":23,"type":"Circle"}""")
res1:Try[AnyRef] = Success(Circle(23))

//match on existence of key
val existsParser = matchAs("height" -> as [Rectangle], "width" -> as [Square], "radius" -> as [Circle])
existsParser("""{"width":100,"height":200}""")
res2:Try[AnyRef] = Success(Rectangle(200,100))

//partial function match
val partialParser = matchAs[String, AnyRef]("type"){
case "circ" | "Circle"    => as [Circle]
case "rect" | "Rectangle" => as [Rectangle]
case _                    => as [Square]
}
```

####Try parser

Will attempt to parse the json object in each successive parser until a successful parse occurs.  If all fails, the the failure
from the last parser will be returned.  The try parser causes buffering of tokens.

####Map parser

[HigherState]: http://higher-state.blogspot.com
[Jackson]: http://jackson.codehaus.org/
[Scala]: http://www.scala-lang.org/
