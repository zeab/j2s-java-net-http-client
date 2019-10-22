
//Imports
import Settings._
import Dependencies._
import ModuleNames._

//Sbt Log Level
logLevel := Level.Info

//Add all the command alias's
CommandAlias.allPublishAlias

lazy val core = (project in file(coreKey))
  .settings(coreSettings: _*)
  .settings(libraryDependencies ++= coreDependencies)
  .enablePlugins(SonaType)

lazy val full = (project in file(fullKey))
  .settings(fullSettings: _*)
  .dependsOn(core)
  .settings(libraryDependencies ++= fullDependencies)
  .enablePlugins(SonaType)
