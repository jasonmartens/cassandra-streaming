name := "phantom-cassandra"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies ++= Seq(
  "com.outworkers" %% "phantom-dsl" % "2.0.0",
  "com.outworkers" %% "phantom-connectors" % "2.0.0",
  "com.outworkers" %% "phantom-streams" % "2.0.0",
  "com.outworkers" %% "phantom-jdk8" % "2.0.0",

  "org.scalatest" %% "scalatest" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.json4s"                   %% "json4s-native"                     % "3.5.0",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % Test,
  "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
  "com.outworkers" %% "util-testing" % "0.26.4" % Test

)