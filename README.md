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
    
val mapParser = #*("Age" -> ?(AsInt), "Email" -> r("""\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b"""), "Name" -> "First Name" -> AsString)    
val map:Success[Map[String:Any]] = mapParser.parse("""{"Age":3,"Email":"test@jameson.com","Name":"John"}""")
    
val tupleParser = T(AsInt, AsString, AsString)
val tuple:Success[(Int, String, String)] = tupleParser.parse("""[123, "value1", "value2"]""")

//case class Canine(age:Int, name:String) extends Pet
val classParser = /("type", "dog" -> >>[Canine], "cat" -> >>[Feline])
val pet:Success[Pet] = classParser.parse("""{"type":"dog","name":"rufus","age":3}""")
```

you can then define your own parser validation

##Key validator pairs

####Jameson supports parser validators against key value pairs, these are of the form  

"key" -> parser/validator  			-if the key is not required  
"key" ->> parser/validator 			-if the key is required  
"key" -> "newKey" -> parser/validator	-if the key is not required and maps with a new key  
"key" -> "newKey" ->> parser/validator	-if the key is required and maps with a new key  
("key1"|"key2"|"key3") -> "newKey" -> parser/validator  -if there are different possible keys, must provide a new key  
"key" |> (Any) => T - pipe value into a function with a single any parameter  
"key" |>> (Any) => T - pipe value into a function with a single any parameter, the key is required  
"key" -> parser/validator[T] |> (T) => U - pipe value of parser/validator into a function with a single parameter of the same type as the parser output.  

####The following validators are supported out of the box.  
  
AsBool      -validates and parses to Boolean  
AsByte      -validates and parses to Byte  
AsShort     -validates and parses to Short  
AsInt		-validates and parses to Integer  
AsLong		-validates and parses to Long  
AsFloat		-validates and parses to Float  
AsDouble	-validates and parses to Double  
AsChar		-validates and parser to Char  
AsString	-validates and parses to String  
AsNull		-validates and parses to null  
AsAnyRef    -validates and parses embedded objects  
AsDateTime  -validates and parsers to a joda.DateTime  
\#* \#! \#^	-these validate and parse to Map[String,Any]  
||			-validates and parses to a List  
¦¦			-validates and parses to a TraversableOnce  
T           -validates and parses to a Tuple  
\>>         -validates against a class   
?			-validates and parses to Some(value) or None if null is found  
??			-validates and parses against the first success parser in the list  
\><			-validates and parses to Either  
/			-validates against a key value pair matched parser  
|\>         -validates and parses into a function  
r           -validates against a regex  


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

####Tuple parser T
This will parse a json array or object into a tuple of the type and length defined by the provided parser arguments. The tuple parser supports 
parsers with default values to allow for cases where the length of the json list maybe shorter than the number of tuple arguments.

```scala
val parser = T(AsInt, AsBool) //This will validate a json list of 2 elements
val parser = T(AsInt, ?(AsBool), ?(AsString)) //This will validate a json list of 1, 2, or 3 elements
val parser = T("int" -> AsInt, "bool" -> AsBool) //This will validate an object with keys 'int' and 'bool'
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
val parser = AsInt |\> (_ + 6) // Adds 6 to the result of the parser
val parser = T(AsInt, AsDouble) |\> (_ * _) //multiples both values in parsed array together
val parser = T("a1" -> AsInt, "a2" -> AsBool, "a3" -> ?(AsString)) |> f(Int, Bool, Option[String]) //pipe parsed object values into a function
```

[HigherState]: http://higher-state.blogspot.com
[Jackson]: http://jackson.codehaus.org/
[Scala]: http://www.scala-lang.org/
