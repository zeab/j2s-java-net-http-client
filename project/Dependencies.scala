//Imports
import sbt._

object Dependencies {

  //List of Versions
  val V = new {
    val aenea                       = "2.0.0"
    val circe                       = "0.11.1"
    val scalaTest                   = "3.0.5"
    val httpSeed                    = "1.0.+"
  }

  //List of Dependencies
  val D = new {
    //HttpSeed
    val httpSeed                    = "com.github.zeab" %% "httpseed" % V.httpSeed
    //Json
    val circeCore                   = "io.circe" %% "circe-parser" % V.circe
    val circeParser                 = "io.circe" %% "circe-generic" % V.circe
    //Xml
    val aenea                       = "com.github.zeab" %% "aenea" % V.aenea
    //Test
    val scalaTest                   = "org.scalatest" %% "scalatest" % V.scalaTest
  }

  val coreDependencies: Seq[ModuleID] = Seq(
    D.scalaTest,
    D.httpSeed
  )

  val fullDependencies: Seq[ModuleID] = Seq(
    D.circeCore,
    D.circeParser,
    D.aenea
  )

  val jsonSupportDependencies: Seq[ModuleID] = Seq(
    D.circeCore,
    D.circeParser
  )

  val xmlSupportDependencies: Seq[ModuleID] = Seq(
    D.aenea
  )

}
