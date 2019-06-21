package zeab.experimental

//Imports
import zeab.aenea.{XmlDeserialize, XmlSerialize}
//Java
import java.net.{HttpURLConnection, URL}
import java.nio.charset.CodingErrorAction
//Circe
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
//Scala
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source.fromInputStream
import scala.io.{BufferedSource, Codec}
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

//Notes
//Success Vs Failure... this is an indication weather the call was able to be made without an exception being thrown....
//it does not indicate anything about the actual http call it self... as in a 500 will be a successful response since its not an exception...

trait HttpClient {

  val defaultConnectTimeoutInMs: String = "1000"
  val defaultReadTimeoutInMs: String = "7000"
  val defaultUserAgent: String = "j2sjavanethttpclient"

  def invokeAsyncHttp[ReqBody, RespBody](
                                          url: String,
                                          method: String = "GET",
                                          body: ReqBody = "",
                                          headers: Map[String, String] = Map.empty,
                                          metaData: Map[String, String] = Map.empty
                                        )(implicit executionContext: ExecutionContext, encoder: Encoder[ReqBody], decoder: Decoder[RespBody], typeTag: TypeTag[RespBody]): Future[Either[HttpError, HttpResponse[RespBody]]] =
    Future(invokeHttp[ReqBody, RespBody](url, method, body, headers, metaData))

  def invokeHttp[ReqBody, RespBody](
                                     url: String,
                                     method: String = "GET",
                                     body: ReqBody = "",
                                     headers: Map[String, String] = Map.empty,
                                     metaData: Map[String, String] = Map.empty
                                   )(implicit encoder: Encoder[ReqBody], decoder: Decoder[RespBody], typeTag: TypeTag[RespBody]): Either[HttpError, HttpResponse[RespBody]] = {

    //Set the Charset to be used
    val charSet: String = "UTF-8"

    //Clean up the headers and make sure none are empty
    val stripEmptyHeaders: Map[String, String] = headers.filterNot(_._1 == "")

    //Change the body from a case class into the desired content type
    val possibleSerializedRequestBody: Either[Throwable, String] =
      stripEmptyHeaders.find(_._1 == "Content-Type") match {
        case Some(contentTypeHeader) =>
          val (_, contentType) = contentTypeHeader
          contentType match {
            case "application/json" =>
              //TODO ... what do i actually need to do here if it fails...
              Right(body.asJson.noSpaces)
            case "application/xml" =>
              XmlSerialize.xmlSerialize[String](body) match {
                case Right(xml) => Right(xml)
                case Left(ex) => Left(ex)
              }
            case _ => Right(body.toString)
          }
        case None => Right(body.toString)
      }

    possibleSerializedRequestBody match {
      case Right(reqBody) =>

        //This is where the actual request starts timing how long it takes to make this request
        val requestSentTimestamp: Long = System.currentTimeMillis()

        Try(new URL(url).openConnection) match {
          case Success(openConnection) =>
            openConnection match {
              case openConn: HttpURLConnection =>

                //Set Options
                openConn.setConnectTimeout(metaData.find(_._1 == "Connect-Timeout").getOrElse("" -> defaultConnectTimeoutInMs)._2.toInt)
                openConn.setReadTimeout(metaData.find(_._1 == "Read-Timeout").getOrElse("" -> defaultReadTimeoutInMs)._2.toInt)
                openConn.setRequestProperty("User-Agent", metaData.find(_._1 == "User-Agent").getOrElse("" -> defaultUserAgent)._2)

                //Set Method
                openConn.setRequestMethod(method)

                //Headers
                //Authorization is calculated here based on the keys in the meta data
                val completedHeaders: Map[String, String] = authorization(url, method, reqBody, stripEmptyHeaders, metaData)
                completedHeaders.foreach { header =>
                  val (headerKey, headerValue): (String, String) = header
                  openConn.setRequestProperty(headerKey, headerValue)
                }

                //Body
                if (method != "GET") {
                  openConn.setDoOutput(true)
                  if (body == "") Try(openConn.setFixedLengthStreamingMode(0))
                  else Try(openConn.getOutputStream.write(reqBody.getBytes(charSet)))
                  Try(openConn.getOutputStream.close())
                }

                //Open the connection <- basically not doing this makes gets take twice as long... go figure
                Try(openConn.connect()) match {
                  case Success(_) =>

                    //Start grabbing response data
                    val responseCode: Int = openConn.getResponseCode
                    val responseHeaders: Map[String, String] = removeNullFromHeaders(openConn)

                    typeTag.tpe.typeSymbol.name.toString match {
                      case "NoBody" =>
                        //Grab the timestamp here
                        val responseReceivedTimestamp: Long = System.currentTimeMillis()
                        Right(HttpResponse(responseCode, responseHeaders, Right(NoBody().asInstanceOf[RespBody]), "", responseReceivedTimestamp - requestSentTimestamp, url, method, reqBody, completedHeaders, metaData))
                      case _ =>
                        //Set the codec to be used for buffered source
                        //TODO this still doesn't feel 100% right to me but I have yet to figure out why... not the code it self but there had got to be a better way to pull the codec ... something connected to the response headers...???
                        implicit val codec: Codec = Codec(charSet)
                        codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
                        codec.onMalformedInput(CodingErrorAction.REPLACE)

                        //Read the incoming buffer sources
                        val inputStream: Try[BufferedSource] = Try(fromInputStream(openConn.getInputStream))
                        val errorStream: Try[BufferedSource] = Try(fromInputStream(openConn.getErrorStream))

                        //There is a super odd thing here where if its a good then both will return success but if its bad then only 1 returns success
                        val rawResponseBody: String =
                          (inputStream, errorStream) match {
                            case (Success(stream), Success(_)) =>
                              val respBody: String = stream.mkString
                              stream.close()
                              respBody
                            case (Failure(_), Success(stream)) =>
                              val respBody: String = stream.mkString
                              stream.close()
                              respBody
                            case _ => "unable to read either the input stream or the error stream"
                          }

                        //Grab the timestamp here since at this point all were doing is decoding the response which we already have completed
                        val responseReceivedTimestamp: Long = System.currentTimeMillis()

                        //Find the type we need to decode into
                        val decodeType: String = responseHeaders.find(_._1 == "Content-Type") match {
                          case Some(contentTypeHeader) =>
                            val (_, contentType) = contentTypeHeader
                            contentType
                          case None => ""
                        }

                        //Actually attempt to convert the string representation into an actual case class
                        //We don't bother decoding the body here if its a string because you get it in the raw body anyways...? sure for now is that the right answer...idk
                        val decodedBody: Either[Throwable, RespBody] =
                        decodeType match {
                          case respHeader if respHeader.contains("application/xml") =>
                            XmlDeserialize.xmlDeserialize[RespBody](rawResponseBody) match {
                              case Right(bdy) => Right(bdy)
                              case Left(ex) => Left(ex)
                            }
                          case "application/json" =>
                            decode[RespBody](rawResponseBody) match {
                              case Right(bdy) => Right(bdy)
                              case Left(ex) => Left(new Exception(ex.toString))
                            }
                          case _ => Left(new Exception("Unsupported decode type"))
                        }

                        //Give the response back to the user
                        Right(HttpResponse(responseCode, responseHeaders, decodedBody, rawResponseBody, responseReceivedTimestamp - requestSentTimestamp, url, method, reqBody, completedHeaders, metaData))
                    }
                  case Failure(ex) =>
                    val exceptionReceivedTimestamp: Long = System.currentTimeMillis()
                    Left(HttpError(url, method, body.toString, completedHeaders, metaData, ex.toString, exceptionReceivedTimestamp - requestSentTimestamp))
                }
              case _ =>
                val exceptionReceivedTimestamp: Long = System.currentTimeMillis()
                Left(HttpError(url, method, body.toString, stripEmptyHeaders, metaData, "Somehow we don't have an open HttpUrlConnection... never seen this happen ever...", exceptionReceivedTimestamp - requestSentTimestamp))
            }
          case Failure(ex) =>
            val exceptionReceivedTimestamp: Long = System.currentTimeMillis()
            Left(HttpError(url, method, body.toString, stripEmptyHeaders, metaData, ex.toString, exceptionReceivedTimestamp - requestSentTimestamp))
        }
      case Left(ex) =>
        Left(HttpError(url, method, body.toString, stripEmptyHeaders, metaData, ex.toString, 0))
    }
  }

  //TODO change this back to doing the bearer token or nothing if nothing is present
  def authorization(url: String, method: String, body: String, headers: Map[String, String], metaData: Map[String, String]): Map[String, String] = headers

  //Format the response headers so they are easy to consume
  private def removeNullFromHeaders(openConn: HttpURLConnection): Map[String, String] = {
    //Map the java collection into scala
    openConn.getHeaderFields.asScala.mapValues(_.asScala.toList).mapValues(_.toList).toMap.map { headers =>
      val (headerKey, headerValues): (String, List[String]) = headers
      //replaces nulls with strings of null so we don't blow up later
      val hk: String = if (headerKey == null) "null" else headerKey
      hk -> headerValues.mkString(" ")
    }
  }

}

object HttpClient extends HttpClient