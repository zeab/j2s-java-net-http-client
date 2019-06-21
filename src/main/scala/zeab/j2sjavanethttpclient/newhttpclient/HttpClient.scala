package zeab.j2sjavanethttpclient.newhttpclient

//Imports
import java.net.{HttpURLConnection, URL}
import java.nio.charset.CodingErrorAction

import zeab.j2sjavanethttpclient.seed.HttpMethods.get
import zeab.aenea.{XmlDeserialize, XmlSerialize}

import scala.io.Codec
import scala.io.Source.fromInputStream
import scala.util.{Failure, Success, Try}
//Circe
import io.circe.Encoder
import io.circe.syntax._
import io.circe.Decoder
import io.circe.parser.decode

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

trait HttpClient{

  def invokeHttp[RequestBody, SuccessfulResponseBody, FailedResponseBody: String](
                  url: String,
                  method: String = get,
                  body: RequestBody = "",
                  headers: Map[String, String] = Map.empty,
                  metaData: Map[String, String] = Map.empty
                )(implicit encoder: Encoder[RequestBody], decoder: Decoder[SuccessfulResponseBody], typeTag: TypeTag[SuccessfulResponseBody]): Either[Throwable, HttpResponse[B]] ={
    Try(new URL(url).openConnection) match {
      case Success(openConnection) =>
        openConnection match {
          case connection: HttpURLConnection =>
            implicit val openConn: HttpURLConnection = connection
            //Connection Settings
            setConnectionSettings(metaData)
            //Set Method
            setMethod(method)
            //Set Headers
            setHeaders(headers)
            //Set Body
            val serializeBody: Either[Throwable, String] =
              headers.find(_._1 == "Content-Type") match {
                case Some(contentTypeHeader) =>
                  val (_, contentType) = contentTypeHeader
                  contentType match {
                    case "application/json" => Right(body.asJson.noSpaces)
                    case "application/xml" => XmlSerialize.xmlSerialize[String](body) match {
                      case Right(xml) => Right(xml)
                      case Left(ex) => Left(ex)
                    }
                    case _ => Right(body.toString)
                  }
                case None => Right(body.toString)
              }
            serializeBody match {
              case Right(requestBody) =>
                setBody(method, requestBody)
                openConn.connect()
                implicit val codec: Codec = Codec("UTF-8")
                codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
                codec.onMalformedInput(CodingErrorAction.REPLACE)
                val responseCode = openConn.getResponseCode
                val responseHeaders = removeNullFromHeaders
                val responseBody =
                  responseCode match {
                    case code if 200 until 299 contains code =>
                      val raw: String = fromInputStream(openConn.getInputStream).mkString
                      val theThing =
                        responseHeaders.find(_._1 == "Content-Type") match {
                          case Some(contentTypeHeader) =>
                            val (_, contentType) = contentTypeHeader
                            contentType match {
                              case "application/json" => decode[B](raw).right.get
                              case "application/xml" => XmlDeserialize.xmlDeserialize[B](raw).right.get
                              case _ => raw.toString.asInstanceOf[B]
                            }
                          case None => raw.toString.asInstanceOf[B]
                        }
                      openConn.getInputStream.close()
                      theThing
                    case _ =>
                      val raw: String = fromInputStream(openConn.getErrorStream).mkString
                      val theThing =
                        responseHeaders.find(_._1 == "Content-Type") match {
                          case Some(contentTypeHeader) =>
                            val (_, contentType) = contentTypeHeader
                            contentType match {
                              case "application/json" => decode[B](raw).right.get
                              case "application/xml; charset=UTF-8" => XmlDeserialize.xmlDeserialize[B](raw).right.get
                              case _ => raw.toString.asInstanceOf[B]
                            }
                          case None => raw.toString.asInstanceOf[B]
                        }
                      openConn.getErrorStream.close()
                      theThing
                   }
                val fullResponse =
                  HttpResponse(url, method, requestBody, headers, metaData, responseCode, responseBody, responseHeaders)
                Right(fullResponse)
              case Left(ex) => Left(ex)
            }
          case _ => Left(new Exception("its not a http url connection... for some reason... never seen this happen"))
        }
      case Failure(ex) => Left(ex)
    }
  }

  def authorization: Map[String, String] = Map.empty

  def setConnectionSettings(metaData:Map[String, String] = Map.empty)(implicit connection: HttpURLConnection): Unit ={
    //TODO update this so the meta data is actually pulled correctly
    connection.setConnectTimeout(10000)
    connection.setReadTimeout(10000)
    connection.setRequestProperty("User-Agent", "j2sjavanethttpclient")
  }

  def setMethod(method:String)(implicit connection: HttpURLConnection): Unit =
    connection.setRequestMethod(method)

  def setHeaders(headers:Map[String, String])(implicit connection: HttpURLConnection): Unit ={
    headers.foreach { header =>
      val (headerKey, headerValue): (String, String) = header
      connection.setRequestProperty(headerKey, headerValue)
    }
  }

  def setBody(method:String, body: String)(implicit connection: HttpURLConnection): Unit ={
    if (method != get){
      connection.setDoOutput(true)
      if (body == "") connection.setFixedLengthStreamingMode(0)
      else connection.getOutputStream.write(body.getBytes("UTF-8"))
      connection.getOutputStream.close()
    }
  }

  //Format the response headers so they are easy to consume
  private def removeNullFromHeaders(implicit connection: HttpURLConnection): Map[String, String] = {
    //Map the java collection into scala
    connection.getHeaderFields.asScala.mapValues(_.asScala.toList).mapValues(_.toList).toMap.map { headers =>
      val (headerKey, headerValues) = headers
      //replaces nulls with strings of null so we don't blow up later
      val hk = if (headerKey == null) "null" else headerKey
      hk -> headerValues.mkString(" ")
    }
  }

}

object HttpClient extends HttpClient
