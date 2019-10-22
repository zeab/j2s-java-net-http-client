
//Imports
import Common.seqBaseProjectTemplate
import Versions._
import sbt.Def

//Settings
object Settings {
  val coreSettings: Seq[Def.Setting[_]] = seqBaseProjectTemplate(coreVersion)
  val fullSettings: Seq[Def.Setting[_]] = seqBaseProjectTemplate(fullVersion)
  val jsonSupportSettings: Seq[Def.Setting[_]] = seqBaseProjectTemplate(jsonSupportVersion)
  val xmlSupportSettings: Seq[Def.Setting[_]] = seqBaseProjectTemplate(xmlSupportVersion)
}
