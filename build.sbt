
//Imports
import Settings._
import Dependencies._

//Sbt Log Level
logLevel := Level.Info

//Add all the command alias's
CommandAlias.allPublishAlias

lazy val j2sjavanethttpclient = (project in file("."))
  .settings(rootSettings: _*)
  .settings(libraryDependencies ++= rootDependencies)
  .enablePlugins(SonaType)
