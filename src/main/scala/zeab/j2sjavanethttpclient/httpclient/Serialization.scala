package zeab.j2sjavanethttpclient.httpclient

//Imports
import zeab.aenea.XmlSerialize
import zeab.httpseed.HttpContentTypes._
import zeab.httpseed.HttpHeaders._
//Circe
import io.circe.Encoder
import io.circe.syntax._

trait Serialization {

  def serialization[ReqBody](body: ReqBody, headers: Map[String, String])
                            (implicit encoder: Encoder[ReqBody]): Either[Throwable, String] = {
    //Change the body from a case class into the desired content type
    headers.find{ case (key, _) => key == contentType } match {
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
