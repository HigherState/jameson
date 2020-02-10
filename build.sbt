name := "Jameson"

organization := "org.higherstate"

version := "1.3.3"

scalaVersion := "2.12.10"

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
  "org.scala-lang" % "scala-compiler" % "2.12.10",
  "org.scala-lang" % "scala-reflect" % "2.12.10",
  "org.typelevel" %% "cats-core"               % "1.6.1",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.10.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)