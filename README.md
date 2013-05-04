#Overview

###[HigherState][]

Jameson builds on [Jackson][] to provide a dsl for [Scala][] which ties validation with 
deserialization.  Jameson supports deserializing into native Scala Map and Lists as
well as deserializing a stream into a TraversableOnce, it further supports Option and Either types.  
Jameson will can deserialize into case classes and nested case classes. It is fully design to support 
custom extension.

Jameson parse returns a scala.util.Try object.  It will stop parsing on the first failure.

Jameson's DSL supports validation on any nested value as well as conditional deserialization 
and validation depending on key value matches. Jameson also supports key substitution.

# Usage

To use Jameson simply include a reference to the Dsl to create your own parser validators.

```scala
import org.higherState.jameson.Dsl._
    
val mapParser = #*("Age" -> ?(AsInt), "Email" -> r("""\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b"""), "Name" -> "First Name" -> AsString)    
val map:Success(Map[String:Any]) = mapParser("""{"Age":3,"Email":"test@jameson.com","Name":"John"}""")
    
//case class Canine(age:Int, name:String) extends Pet
val classParser = /("type", "dog" -> >>[Canine], "cat" -> >>[Feline])
val pet:Success(Pet) = classParser("""{"type":"dog","name":"rufus","age":3}""")
```

you can then define your own parser validation

##Key validator pairs

####Jameson supports parser validators against key value pairs, these are of the form  

"key" -> validator  			-if the key is not required  
"key" ->> validator 			-if the key is required  
"key" -> "newKey" -> validator	-if the key is not required and maps with a new key  
"key" -> "newKey" ->> validator	-if the key is required and maps with a new key  

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
\#* \#! \#^	-these validate and parse to Map[String,Any]  
||			-validates and parses to a List  
¦¦			-validates and parses to a TraversableOnce  
\>>         -validates against a class   
?			-validates and parses to Some(value) or None if null is found  
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
This will parse a json object to a choice of possible parsers depending on a match with an extracted key,value pair. Finding the
matching key value pair causes localized buffering of the tokens.  A default value can be provided if a key value pair is not found

```scala
val parser = /("mapType","open" -> #*(), "closed" -> #*("key" -> AsString)) // selects parser based on "mapType" values "open" or "closed"
val parser = /("type", "c1" -> >>[MyClass1], "c2" -> >>[MyClass2]) // selects parser based on "type" values "c1" or "c2"
val parser = /("type", >>[MyClass1], >>[MyClass2]) // selects class parser based on "type" matching against the name of the class, "MyClass1" or "MyClass2"
val parser = /("type", "MyClass1", >>[MyClass1], >>[MyClass2])// if "type" is not found will match MyClass1
```

####Function parser |\>
This will take a parser result and pipe into a function.

```scala
val parser = |>(AsInt, (i:Int) => i + 3) // validates an int and adds 3
val parser = |>(||(AsString), (l:List[Any]) => l.mkString(",")) // validates a list and maps in to a comma separated string  
```

[HigherState]: http://higher-state.blogspot.com
[Jackson]: http://jackson.codehaus.org/
[Scala]: http://www.scala-lang.org/
