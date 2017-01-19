name := "phantom-cassandra"

version := "1.0"

lazy val scalaV = "2.11.8"
lazy val akkaV = "2.4.16"
lazy val akkaHttpV = "10.0.1"
lazy val upickleV = "0.4.4"
lazy val scalaTestV = "3.0.0"
lazy val phantomV = "2.0.0"

def commonSettings = Seq(
  scalaVersion :=  scalaV
)

resolvers ++= Seq(
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Twitter Repository" at "http://maven.twttr.com",
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("websudos", "oss-releases")
)

lazy val root =
  Project("root", file("."))
    .aggregate(frontend, backend)

lazy val backend = Project("backend", file("backend"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.outworkers" %% "phantom-dsl" % phantomV,
      "com.outworkers" %% "phantom-connectors" % phantomV,
      "com.outworkers" %% "phantom-streams" % phantomV,
      "com.outworkers" %% "phantom-jdk8" % phantomV,

      "org.scalatest" %% "scalatest" % scalaTestV,
      "org.scalatest" %% "scalatest" % scalaTestV % "test",
      "org.json4s" %% "json4s-native" % "3.5.0",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % Test,
      "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
      "com.outworkers" %% "util-testing" % "0.26.4" % Test,

      "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpV,

      "com.lihaoyi" %% "upickle" % upickleV

    ),
    resourceGenerators in Compile += Def.task {
      val f1 = (fastOptJS in Compile in frontend).value
      val f2 = (packageScalaJSLauncher in Compile in frontend).value
      Seq(f1.data, f2.data)
    }.taskValue,
    watchSources ++= (watchSources in frontend).value
  )
  .dependsOn(sharedJvm)

// Scala-Js frontend
lazy val frontend =
  Project("frontend", file("frontend"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings: _*)
    .settings(
      persistLauncher in Compile := true,
      persistLauncher in Test := false,
      testFrameworks += new TestFramework("utest.runner.Framework"),
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.1",
        "com.lihaoyi" %%% "upickle" % upickleV,
        "com.lihaoyi" %%% "utest" % "0.4.4" % "test"
      )
    )
    .dependsOn(sharedJs)

lazy val shared =
  (crossProject.crossType(CrossType.Pure) in file ("shared"))
    .settings(
      scalaVersion := scalaV
    )

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js