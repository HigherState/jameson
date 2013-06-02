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

```
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
val differenceParser = asTuple[Int,Int] map (_ - _)
```


## Dsl parser/validators

#### Open map parser  \#*  
This will parse a json object and will map any key:value pair, whether it has been 
explicit validated against or not.  Parsing will return a Try[Map[String,Any]] object

```scala
val parser = #*() //This will simply map all object content to a Map[String,Any]
val parser = #*("a" -> AsInt) //This will validate that the key 'a' maps to an Integer
val parser = #*("a" -> AsString, "b" -> AsDouble) //This will validate that the key 'a' maps to a String and the key 'b' maps to a double 
val parser = #*("a" ->> AsBool) //This will validate that the key 'a' maps to a Boolean and that 'a' is required
val parser = #*("a" -> #*("b" -> AsFloat) //This will validate that the key 'a' maps to a map which if it has the key 'b' will map to a float 
```

####Closed map parser \#!
This will parse a json object and will map only those key:value pairs which have been explicitly validated.
If any other key value pairs are found, validation will fail.  Parsing will return a Try[Map[String,Any]] object

```scala
val parser = #!("a" -> AsInt) //This will validate that the key 'a' maps to an Integer and there are no other keys, a is not required
val parser = #!("a" ->> AsBool) //This will validate that the key 'a' maps to a Boolean, there are no other keys and that 'a' is required
```

####Drop map parser \#^
This will parse a json object and will map only those key:value pairs which have been explicitly validated.
If any other key value pairs are found, they will be ignored.  Parsing will return a Try[Map[String,Any]] object

```scala
val parser = #^("a" -> AsInt) //This will validate that the key 'a' maps to an Integer, a is not required
```

####List parser ||
This will parse a json array to a Try[List[?]].  The type of the return List will match the type of any parser argument, 
or will be Any if no parser argument is provided

```scala
val parser = ||() //This will validate that the  json is a list
val parser = ||(AsString) //This will validate the json is a list of strings 
val parser = ||(#*) //This will validate the json is a list of (open) Maps
```

####TraversableOnce parser ¦¦
This will parse a json array to a TraversableOnce[Try[?]].  The type of the return TraversableOnce Try values will match the type of any parser argument, 
or will be Any if no parser argument is provided.  If there is a Failure value in the TraversableOnce, there will be no
more elements.  The TraversableOnce parser should be a top level parser only.

```scala
val parser = ¦¦() //This will validate that the  json is a list
val parser = ¦¦(AsString) //This will validate the json is a list of strings 
val parser = ¦¦(||) //This will validate the json is a list of List[Any]
```


####Case class parser \>>
This will parse an json object into a specified case class using the default constructor.  Keys can be validated against to
resolve any type erasure, and keys renamed to match the correct parameter name.  Parser will also resolve mapping for any
unspecified case classes in the constructor parameters.  Parser will try and match the correct parser to the constructor parameter if not specified.

```scala
val parser >>[MyClass] // this will parse json object to MyClass
val parser >>[MyClass]("key" -> "count" -> AsInt) // this will map from key 'key' to parameter name 'count' as an Int
val parser >>[MyClass]("default" -> ?("defaultValue")) //this will use the value "defaultValue" for the argument "default" if value not found
val parser >>[MyClass]("list" -> ||(AsFloat)) //this will resolve the argument list as a list of floats.
```

####Option and OrElse parser ?
This will parse to a Some(value) if the value is not null, otherwise it will parse to None.  If a default value is specified
then this will either return a the value or the default value if null was found.  
*When used in a Map parser, if the key is not found and the validator is required (->>) then it will substitute a None, or default value.*
*When used in a Class parser, if the key is not found, then it will substitute a None, or default value*

```scala
val parser = ? // no validation
val parser = ?("empty") // no validation, if null, replaces with "empty"
val parser = ?(AsInt) // validate is null or Int
val parser = ?(AsDouble, 1.5) // validates is null or double
```

####Try parser ??
This will attempt to parse json into a series of ordered parsers.  The first to succeed returns the successfully parsed result.
If none succeed, a failure is returned.
*Currently having only two parsers is supported

```scala
val parser = ??(>>[MyClass1], >>[MyClass2]) // validates a MyClass1 object if possible, otherwise a MyClass2
```

####Either parser ><
This will parse to either the left provided parser or the right provided parser, returning a Left(value) or Right(value) object.
The parser will try the left parser first, and if it fails, will try the right parser.  This causes localized buffering of
the tokens.

```scala
val parser = ><(AsInt, AsDouble) // validates as either an int or a double
val parser = ><(#*("key" ->> AsInt), #*("key" -> AsString)) // validates as either a map with a required key int value, or a map which may have key string value
val parser = ><(>>[MyClass1], >>[MyClass2]) // validates as either a mapping into MyClass1 or MyClass2
```

####Matching parser /
This will parse a json object to a choice of possible parsers depending on a match with an extracted key,value pair. A partial function
can also be applied to the match value.  Finding the matching key value pair causes localized buffering of the tokens.  A default value 
can be provided if a key value pair is not found.

```scala
val parser = /("mapType","open" -> #*(), "closed" -> #*("key" -> AsString)) // selects parser based on "mapType" values "open" or "closed"
val parser = /("type", "c1" -> >>[MyClass1], "c2" -> >>[MyClass2]) // selects parser based on "type" values "c1" or "c2"
val parser = /("type", >>[MyClass1], >>[MyClass2]) // selects class parser based on "type" matching against the name of the class, "MyClass1" or "MyClass2"
val parser = /("type", "MyClass1", >>[MyClass1], >>[MyClass2])// if "type" is not found will match MyClass1
val parser = /[String,MySubClass]("type"){
    case "MyClass1"|"Class1" => >>[MyClass1]
    case _                   => >>[MyClass2]
}

```

####Piping to a function parser |\>
This will take the result of the previous parser and pipe it into a function with a corresponding argument.  If the previous parser is a tuple parser, you can pipe it 
into a function with corresponding arguments.

```scala
val parser = AsInt |> (_ + 6) // Adds 6 to the result of the parser
val parser = T(AsInt, AsDouble) |> (_ * _) //multiples both values in parsed array together
val parser = T("a1" -> AsInt, "a2" -> AsBool, "a3" -> ?(AsString)) |> f(Int, Bool, Option[String]) //pipe parsed object values into a function
```

[HigherState]: http://higher-state.blogspot.com
[Jackson]: http://jackson.codehaus.org/
[Scala]: http://www.scala-lang.org/
