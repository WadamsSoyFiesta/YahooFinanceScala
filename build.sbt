lazy val commonSettings = Seq(
  version := "0.1",
  organization := "openquant",
  scalaVersion := "2.11.8",
  scalacOptions := Seq(
    "-target:jvm-1.8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-encoding", "utf8",
    "-Xlint"
  ),
  resolvers ++= Seq(Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.bintrayRepo("megamsys", "scala"),
    "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
  )
)

lazy val commonDependencies = Seq(
  "org.slf4j" % "jcl-over-slf4j" % "1.7.7",
  "commons-logging" % "commons-logging" % "99-empty",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "com.iheart" %% "ficus" % "1.2.3",
  "com.typesafe.akka" %% "akka-stream" % "2.4.3",
  //"com.typesafe.akka" %% "akka-http-core" % "2.4-SNAPSHOT",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.3",
  "com.github.tototoshi" %% "scala-csv" % "1.+",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

lazy val testDependencies = Seq(
  "org.specs2" %% "specs2" % "3.+" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.3" % "test"
)

lazy val yahoofinancescala = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= commonDependencies)
  .settings(libraryDependencies ++= testDependencies)

