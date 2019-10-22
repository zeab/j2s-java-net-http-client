package zeab.j2sjavanet.httpclient

//Imports
import zeab.j2sjavanet.httpclient.models.{HttpError, HttpResponse, HttpResponseRaw}
//Java
import java.net.{HttpURLConnection, URL}
import java.nio.charset.CodingErrorAction
//Scala
import scala.collection.JavaConverters._
import scala.io.Source.fromInputStream
import scala.io.{BufferedSource, Codec}
import scala.util.{Failure, Success, Try}

trait HttpClient {

  val defaultConnectTimeoutInMs: String = "1000"
  val defaultReadTimeoutInMs: String = "7000"
  val defaultUserAgent: String = "j2sjavanethttpclient"

  def invokeHttpCore(
                      url: String,
                      method: String = "GET",
                      body: String = "",
                      headers: Map[String, String] = Map.empty,
                      metaData: Map[String, String] = Map.empty
                    ): Either[HttpError, HttpResponse] = {

    //Replace any invalid char's in the url's with the correct encoding
    val standardizedUrl: String =
      url
        .replace("{", "%7B")
        .replace("}", "%7D")
        .replace("|", "%7C")
        .replace("[", "%5B")
        .replace("]", "%5D")
        .replace("`", "%60")
        .replace("~", "%7E")

    //Make sure the method is upper case because if complains if its not
    val standardizedMethod: String = method.toUpperCase()

    //Connection Settings
    val connectTimeoutInMs: String = metaData.find(_._1.toLowerCase == "connecttimeoutinms").map(_._2).getOrElse(defaultConnectTimeoutInMs)
    val readTimeoutInMs: String = metaData.find(_._1.toLowerCase == "readtimeoutinms").map(_._2).getOrElse(defaultReadTimeoutInMs)
    val userAgent: String = metaData.find(_._1.toLowerCase == "useragent").map(_._2).getOrElse(defaultUserAgent)

    //Set the Charset to be used
    val charSet: String = "UTF-8"

    Try(new URL(standardizedUrl).openConnection) match {
      case Success(openConnection) =>
        openConnection match {
          case openConn: HttpURLConnection =>

            //Set Options
            openConn.setConnectTimeout(connectTimeoutInMs.toInt)
            openConn.setReadTimeout(readTimeoutInMs.toInt)
            openConn.setRequestProperty("User-Agent", userAgent)

            //Set Method
            openConn.setRequestMethod(standardizedMethod)

            //Set Headers
            val standardizedHeaders: Map[String, String] = authorization(standardizedUrl, standardizedMethod, body, headers, metaData)
            standardizedHeaders.foreach { header: (String, String) =>
              val (headerKey, headerValue): (String, String) = header
              openConn.setRequestProperty(headerKey, headerValue)
            }

            //Body
            if (standardizedMethod != "GET") {
              openConn.setDoOutput(true)
              if (body == "") Try(openConn.setFixedLengthStreamingMode(0))
              else Try(openConn.getOutputStream.write(body.getBytes(charSet)))
              Try(openConn.getOutputStream.close())
            }

            //Record when we start making the http connection
            val timestamp: Long = System.currentTimeMillis()

            //Open the connection <- basically not doing this makes gets take twice as long... no idea why
            Try(openConn.connect()) match {
              case Success(_) =>
                //Attempt to get the response code or catch the timeout exception
                val isConnectionComplete: Either[Throwable, (HttpURLConnection, Int)] =
                  Try(openConn.getResponseCode) match {
                    case Success(code) => Right((openConn, code))
                    case Failure(ex) => Left(ex)
                  }
                isConnectionComplete match {
                  case Right(resp) =>
                    val (_, responseStatusCode): (HttpURLConnection, Int) = resp
                    val responseHeaders: Map[String, String] = removeNullFromHeaders(openConn)

                    //Set the codec to be used for buffered source
                    //TODO this still doesn't feel 100% right to me but I have yet to figure out why... not the code it self but there had got to be a better way to pull the codec ... something connected to the response headers...???
                    implicit val codec: Codec = Codec(charSet)
                    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
                    codec.onMalformedInput(CodingErrorAction.REPLACE)

                    //Read the incoming buffer sources
                    val inputStream: Try[BufferedSource] = Try(fromInputStream(openConn.getInputStream))
                    val errorStream: Try[BufferedSource] = Try(fromInputStream(openConn.getErrorStream))

                    //The logic is if its a good then both will return success but if its bad then only 1 returns success
                    val responseBody: String =
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

                    //Return the response
                    Right(HttpResponseRaw(responseStatusCode, responseBody, responseHeaders, standardizedUrl, method, body, standardizedHeaders, metaData, System.currentTimeMillis() - timestamp))
                  case Left(ex) =>
                    Left(HttpError(ex.toString, "", standardizedUrl, standardizedMethod, body, standardizedHeaders, metaData, System.currentTimeMillis() - timestamp))
                }
              case Failure(ex) =>
                Left(HttpError(ex.toString, "", standardizedUrl, standardizedMethod, body, standardizedHeaders, metaData, System.currentTimeMillis() - timestamp))
            }
          case _ =>
            Left(HttpError("something happened that ive never seen before", "", standardizedUrl, standardizedMethod, body, headers, metaData, 0))
        }
      case Failure(ex) =>
        Left(HttpError(ex.toString, "", standardizedUrl, standardizedMethod, body, headers, metaData, 0))
    }
  }

  //Enable the authorization to be overridden when necessary
  def authorization(url: String, method: String, body: String, headers: Map[String, String], metaData: Map[String, String]): Map[String, String] = headers

  //Format the response headers so they are easy to consume
  private def removeNullFromHeaders(openConn: HttpURLConnection): Map[String, String] = {
    //Map the java collection into scala
    openConn.getHeaderFields.asScala
      .mapValues(_.asScala.toList).mapValues(_.toList)
      .toMap.map { headers: (String, List[String]) =>
      val (headerKey, headerValues): (String, List[String]) = headers
      //replaces nulls with strings of null so we don't blow up later
      val hk: String = if (headerKey == null) "null" else headerKey
      hk -> headerValues.mkString(" ")
    }
  }

}

object HttpClient extends HttpClient