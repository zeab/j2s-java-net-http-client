//Imports
import sbt._

object Dependencies {

  //List of Versions
  val V = new {
    val aenea                       = "1.0.0"
    val circe                       = "0.11.1"
    val scalaTest                   = "3.0.5"
  }

  //List of Dependencies
  val D = new {
    //Xml
    val aenea                       = "com.github.zeab" %% "aenea" % V.aenea
    //Json
    val circeCore                   = "io.circe" %% "circe-parser" % V.circe
    val circeParser                 = "io.circe" %% "circe-generic" % V.circe
    //Test
    val scalaTest                   = "org.scalatest" %% "scalatest" % V.scalaTest
  }

  val rootDependencies: Seq[ModuleID] = Seq(
    D.aenea,
    D.circeCore,
    D.circeParser,
    D.scalaTest
  )

}
