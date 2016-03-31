name := "Jameson"

organization := "org.higherstate"

version := "1.1.8"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps", "-language:reflectiveCalls",
  "-unchecked",
  "-Xfatal-warnings",
  "-Yinline-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture"
)

javacOptions ++= Seq("-target", "1.8", "-source", "1.8", "-Xlint:deprecation")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.8",
  "org.scala-lang" % "scala-reflect" % "2.11.8",
  "org.scalaz" %% "scalaz-core" % "7.1.7",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.7.3",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "joda-time" % "joda-time" % "2.9.2",
  "org.joda" % "joda-convert" % "1.8"
)