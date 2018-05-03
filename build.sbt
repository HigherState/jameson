name := "Jameson"

organization := "org.higherstate"

version := "1.3.0"

scalaVersion := "2.12.5"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps", "-language:reflectiveCalls",
  "-unchecked",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture"
)

javacOptions ++= Seq("-target", "1.8", "-source", "1.8", "-Xlint:deprecation")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.12.5",
  "org.scala-lang" % "scala-reflect" % "2.12.5",
  "org.typelevel" %% "cats" % "0.9.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "joda-time" % "joda-time" % "2.9.9",
  "org.joda" % "joda-convert" % "2.0.1"
)