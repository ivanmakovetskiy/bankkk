ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

val akkaVersion = "2.6.18"
val akkaHttpVersion = "10.2.7"
val circeVersion = "0.14.1"
val AkkaHttpJsonVersion = "1.39.2"
lazy val slickVersion = "3.3.3"
lazy val postgresVersion = "42.3.1"

lazy val common = ProjectRef(base = file("../common"), id = "common")

lazy val operation = (project in file("."))
  .dependsOn(common)
  .settings(
    name := "operation",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpJsonVersion,

      "ch.qos.logback"     % "logback-classic"       % "1.2.3"
    )
  )
