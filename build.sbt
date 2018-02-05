val akkaVersion = "2.5.8"

name := """live-coin-prices"""
organization := "org.ozencem"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies += "org.webjars" %% "webjars-play" % "2.6.3"
libraryDependencies += "org.webjars" % "bootstrap" % "4.0.0"
libraryDependencies += "org.webjars" % "jquery" % "3.3.1"
libraryDependencies += "org.webjars" % "flot" % "0.8.3"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "org.ozencem.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "org.ozencem.binders._"
