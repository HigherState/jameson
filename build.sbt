name := "Jameson"

organization := "org.highState"

version := "1.1.5"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.4",
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.2.1",
  "org.scalatest" %% "scalatest" % "2.0.M6-SNAP16" % "test",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2"
)