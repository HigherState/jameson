name := "Jameson"

organization := "org.highState"

version := "1.1.5"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.1",
  "org.scala-lang" % "scala-reflect" % "2.11.1",
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.2.3",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)