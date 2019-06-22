package zeab.j2sjavanethttpclient.httpclient

//Imports
import zeab.aenea.XmlSerialize
import zeab.j2sjavanethttpclient.seed.HttpHeaders._
import zeab.j2sjavanethttpclient.seed.HttpContentTypes._
//Circe
import io.circe.syntax._
import io.circe.Encoder

trait Serialization {

  def serialization[ReqBody](body:ReqBody, headers:Map[String,String])
                            (implicit encoder: Encoder[ReqBody]): Either[Throwable, String] ={
    //Change the body from a case class into the desired content type
      headers.find(_._1 == contentType) match {
        case Some(contentTypeHeader) =>
          val (_, contentType) = contentTypeHeader
          contentType match {
            case ct if ct == applicationJson =>
              //TODO ... what do i actually need to do here if it fails...
              Right(body.asJson.noSpaces)
            case ct if ct == applicationXml =>
              XmlSerialize.xmlSerialize[String](body) match {
                case Right(xml) => Right(xml)
                case Left(ex) => Left(ex)
              }
            case _ => Right(body.toString)
          }
        case None => Right(body.toString)
      }
  }

}
