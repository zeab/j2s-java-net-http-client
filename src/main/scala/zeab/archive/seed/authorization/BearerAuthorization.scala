//package zeab.archive.seed.authorization
//
//import zeab.archive.seed.{HttpHeaders, HttpMetaDataKeys}
//
//object BearerAuthorization {
//
//  def bearerAuthorization(metaData:Map[String, String]): Map[String, String] ={
//    metaData.find(_._1 == HttpMetaDataKeys.setBearerKey) match {
//      case Some(bearer) => HttpHeaders.bearerHeader(bearer._2)
//      case None => Map.empty
//    }
//  }
//
//}
