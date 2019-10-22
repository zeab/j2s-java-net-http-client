//package zeab.j2sjavanethttpclient.httpclient
//
////Imports
//import zeab.aenea.XmlSerializer._
//import zeab.httpseed.HttpContentTypes._
//import zeab.httpseed.HttpHeaders._
////Circe
//import io.circe.Encoder
//import io.circe.syntax._
//
//trait Serialization {
//
//  def serialization[ReqBody](body: ReqBody, headers: Map[String, String])
//                            (implicit encoder: Encoder[ReqBody]): Either[Throwable, String] = {
//    //TODO Add a value where it will change the header to the right header if its a blank
//    if (body.toString == "") Right("")
//    else
//      headers.find{ case (key, _) => key == contentType } match {
//        case Some(contentTypeHeader) =>
//          val (_, contentType): (String, String) = contentTypeHeader
//          contentType match {
//            case ct if ct == applicationJson =>
//              //TODO ... what do i actually need to do here if it fails...
//              Right(body.asJson.noSpaces)
//            case ct if ct.contains(applicationXml) => body.asXml
//            case _ => Right(body.toString)
//          }
//        case None => Right(body.toString)
//      }
//  }
//
//}
