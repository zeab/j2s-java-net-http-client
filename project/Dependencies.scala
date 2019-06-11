//Imports
import sbt._

object Dependencies {

  //List of Versions
  val V = new {
    val scalaTest                   = "3.0.5"
  }

  //List of Dependencies
  val D = new {
    //Test
    val scalaTest                   = "org.scalatest" %% "scalatest" % V.scalaTest
  }

  val rootDependencies: Seq[ModuleID] = Seq(
    D.scalaTest
  )

}
