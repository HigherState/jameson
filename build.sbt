name := "Jameson"

organization := "org.highState"

version := "1.0"

scalaVersion := "2.10.1"

// For Jackson snapshots
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.1",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.2.1-SNAPSHOT",
  "org.specs2" %% "specs2" % "1.14" % "test"
)