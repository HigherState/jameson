name := "Jameson"

organization := "org.highState"

version := "1.1.6"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.5",
  "org.scala-lang" % "scala-reflect" % "2.11.5",
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.4.4",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)