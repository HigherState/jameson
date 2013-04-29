#Overview

Jameson builds on [Jackson][] to provide a dsl for [Scala][] which ties validation with 
deserialization.  Jameson supports deserializing into native Scala Map and Lists as
well as deserializing a stream into a TraversableOnce, it further supports Option and Either types.  
Jameson will can deserialize into case classes and nested case classes. It is fully design to support 
custom extension.

Jameson's DSL supports validation on any nested value as well as conditional deserialization 
and validation depending on key value matches. Jameson also supports key substitution.

# Usage

To use Jameson simply include a reference to the Dsl

```scala
    import org.higherState.jameson.Dsl._
    
    val mapParser = #*("Age" -> ?(AsInt), "Email" -> r("""\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b"""), "Name" -> "First Name" -> AsString)    
    val map:Map[String:Any] = mapParser("""{"Age":3,"Email":"test@jameson.com","Name":"John"}""")
    
    val classParser = /("type", "dog" -> >>[Canine], "cat" -> >>[Feline]")
    val pet:Pet = classParser("""{"type":"dog","name":"rufus","age":3}""")
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
>>          -validates against a class
\#* \#! \#^	-these validate and parse to Map[String,Any]  
||			-validates and parses to a List  
¦¦			-validates and parses to a TraversableOnce  
?			-validates and parses to Some(value) or None if null is found  
\><			-validates and parses to Either  
/			-validates against a matched parser
r           -validates against a regex


## Dsl parser/validators

#### Open map parser  \#*  
This will parse a json object and will map any key:value pair, whether it has been 
explicit validated against or not.

```scala
val parser = #* //This will simply map all object content to a Map[String,Any]
val parser = #*("a" -> AsInt) //This will validate that the key 'a' maps to an Integer
val parser = #*("a" -> AsString, "b" -> AsDouble) //This will validate that the key 'a' maps to a String and the key 'b' maps to a double 
val parser = #*("a" ->> AsBool) //This will validate that the key 'a' maps to a Boolean and that 'a' is required
val parser = #*("a" -> #*("b" -> AsFloat) //This will validate that the key 'a' maps to a map which if it has the key 'b' will map to a float 
```

####Closed map parser \#!
This will parse a json object and will map only those key:value pairs which have been explicitly validated.
If any other key value pairs are found, validation will fail.

```scala
val parser = #!("a" -> AsInt) //This will validate that the key 'a' maps to an Integer and there are no other keys, a is not required
val parser = #!("a" ->> AsBool) //This will validate that the key 'a' maps to a Boolean, there are no other keys and that 'a' is required
```

####Drop map parser \#*
This will parse a json object and will map only those key:value pairs which have been explicitly validated.
If any other key value pairs are found, they will be ignored.

```scala
val parser = #^("a" -> AsInt) //This will validate that the key 'a' maps to an Integer, a is not required
```

####List parser ||
This will parse a json array to a List.

```scala
val parser = || //This will validate that the  json is a list
val parser = ||(AsString) //This will validate the json is a list of strings 
val parser = ||(#*) //This will validate the json is a list of (open) Maps
```


[Jackson]: http://jackson.codehaus.org/
[Scala]: http://www.scala-lang.org/
