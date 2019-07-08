package zeab.j2sjavanethttpclient.httpclient

//Import
import zeab.aenea.XmlDeserialize
import zeab.httpseed.HttpContentTypes._
import zeab.httpseed.HttpHeaders._
//Circe
import io.circe.Decoder
import io.circe.parser.decode
//Scala
import scala.reflect.runtime.universe._

trait Deserialization {

  def deserialization[RespBody](responseHeaders: Map[String, String], rawResponseBody: String)(implicit decoder: Decoder[RespBody], typeTag: TypeTag[RespBody]): Either[Throwable, RespBody] = {
    //Find the type we need to decode into
    val decodeType: String = responseHeaders.find{ case (key, _) => key == contentType } match {
      case Some(contentTypeHeader) =>
        val (_, contentType) = contentTypeHeader
        contentType
      case None => ""
    }

    //Actually attempt to convert the string representation into an actual case class
    decodeType match {
      case respHeader if respHeader.contains(applicationXml) =>
        XmlDeserialize.xmlDeserialize[RespBody](rawResponseBody) match {
          case Right(bdy) => Right(bdy)
          case Left(ex) => Left(ex)
        }
      case respHeader if respHeader == applicationJson =>
        decode[RespBody](rawResponseBody) match {
          case Right(bdy) => Right(bdy)
          case Left(ex) => Left(new Exception(ex.toString))
        }
      case _ => Left(new Exception("Unsupported decode type"))
    }
  }
}
