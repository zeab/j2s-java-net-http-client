//Imports
import sbt._

object Dependencies {

  //List of Versions
  val V = new {
    val aenea                       = "1.0.0"
    val circe                       = "0.9.3"
    val scalaTest                   = "3.0.5"
  }

  //List of Dependencies
  val D = new {
    //Json
    val circeCore                   = "io.circe" %% "circe-parser" % V.circe
    //Xml
    val aenea                       = "com.github.zeab" %% "aenea" % V.aenea
    //Test
    val scalaTest                   = "org.scalatest" %% "scalatest" % V.scalaTest
  }

  val rootDependencies: Seq[ModuleID] = Seq(
    D.scalaTest,
    D.aenea,
    D.circeCore
  )

}
