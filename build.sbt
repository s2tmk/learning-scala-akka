name := "Hello World"

version := "1.0"

scalaVersion := "2.13.4"

lazy val akkaVersion = "2.6.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
)