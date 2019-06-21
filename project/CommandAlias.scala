
//Imports
import sbt.{Def, addCommandAlias}

object CommandAlias {

  //List all Publish Alias here
  val allPublishAlias: Seq[Def.Setting[_]] = addCommandAlias(s"maven", ";test;publishSigned;sonatypeRelease")

}
