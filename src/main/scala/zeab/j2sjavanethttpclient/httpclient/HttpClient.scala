package zeab.j2sjavanethttpclient.httpclient

//Imports
import zeab.j2sjavanethttpclient.seed.HttpMethods.get
import zeab.j2sjavanethttpclient.seed.HttpSeed
import zeab.j2sjavanethttpclient.seed.authorization.BearerAuthorization
import zeab.j2sjavanethttpclient.seed.httpclientmessages.{HttpClientError, HttpClientResponse}
//Java
import java.net.{HttpURLConnection, URL}
import java.nio.charset.CodingErrorAction
import java.text.SimpleDateFormat
import java.util.Calendar
//Scala
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Codec
import scala.io.Source.fromInputStream
import scala.util.{Failure, Success, Try}

trait HttpClient {

  def invokeAsyncHttpClientResponse(
                                     httpSeed: HttpSeed,
                                     isReturnBody: Boolean
                                   )
                                   (implicit executionContext: ExecutionContext): Future[Either[HttpClientError, HttpClientResponse]] =
    Future(invokeHttpClientResponse(httpSeed.url, httpSeed.method, httpSeed.body, httpSeed.headers, httpSeed.metaData, isReturnBody))

  def invokeHttpClientResponse(
                                url: String,
                                method: String = get,
                                body: String = "",
                                headers: Map[String, String] = Map.empty,
                                metaData: Map[String, String] = Map.empty,
                                isReturnBody: Boolean = true
                              ): Either[HttpClientError, HttpClientResponse] = {
    //TODO Hook these to read from the meta data and set if not found
    val connectionTimeoutInMs: Int = 1000
    val readTimeoutInMs: Int = 7000
    val userAgent: String = "j2sjavanethttpclient"
    //TODO Clean this up since idk if i really need it...
    val cleanUpHeaders: Map[String, String] = headers.filter(h => h._1.nonEmpty)
    //TODO Make the close connection configurable
    val combinedHeaders: Map[String, String] = (authorization(url, method, body, cleanUpHeaders, metaData) ++ cleanUpHeaders ++ Map("Connection" -> "close")).filter(h => h._1.nonEmpty)
    val callCreateTimeMark: Long = System.currentTimeMillis()
    val timestamp: String = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS").format(Calendar.getInstance().getTime)
    //TODO Add some safety around this where it wont blow up if you don't have a legit url...
    Try(new URL(url).openConnection) match {
      case Success(openConn) =>
        openConn match {
          case openConn: HttpURLConnection =>
            //TODO... i think this charSet needs to come from the headers... i think...
            val charSet: String = "UTF-8"
            //TODO Make all these configurable
            //Http Options
            openConn.setConnectTimeout(connectionTimeoutInMs)
            openConn.setReadTimeout(readTimeoutInMs)
            openConn.setRequestProperty("User-Agent", userAgent)
            //If were not getting a body don't bother opening the input stream
            //TODO Setting the DoInput as false seemed like a good idea but I tried and no I think you don't actually get the response code back at all... the todo is to figure that out
            //if (!isReturnBody) openConn.setDoInput(false)
            //Methods Default is GET by java.net
            openConn.setRequestMethod(method)
            //Headers
            combinedHeaders.foreach { header =>
              val (headerKey, headerValue): (String, String) = header
              openConn.setRequestProperty(headerKey, headerValue)
            }
            //Body
            val requestBody: Option[HttpClientError] =
              if (method != get) {
                openConn.setDoOutput(true)
                val attachBody: Try[Unit] =
                  if (body == "") Try(openConn.setFixedLengthStreamingMode(0))
                  else Try(openConn.getOutputStream.write(body.getBytes(charSet)))
                attachBody match {
                  case Success(_) => None
                  case Failure(exception) =>
                    val exceptionTimeMark: Long = System.currentTimeMillis()
                    exception.toString match {
                      case "java.net.SocketTimeoutException: connect timed out" => Some(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 10, exception.toString, "Unable to connect to the endpoint in a timely manner"))
                      case "java.net.ConnectException: Connection refused (Connection refused)" => Some(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 20, exception.toString, "Check to make sure the service exists"))
                      case "java.net.SocketTimeoutException: Read timed out" => Some(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 30, exception.toString, "Unable to get the body from the endpoint in a timely manner"))
                      case _ => Some(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 0, exception.toString, exception.getMessage))
                    }
                }
              }
              else None
            //CHeck the connection and move on
            requestBody match {
              case Some(ex) => Left(ex)
              case None =>
                //TODO Figure out why i thought i needed flush in the first place
                //openConn.getOutputStream.flush()
                if (method != get) openConn.getOutputStream.close()
                //Open the connection <- basically not doing this makes gets take twice as long... go figure
                Try(openConn.connect()) match {
                  case Success(_) =>
                    //TODO Clean up this entire section and remove the chevron pattern :)
                    //Get the response details
                    val t0: Long = System.currentTimeMillis()
                    Try(openConn.getResponseCode) match {
                      case Success(respCode) =>
                        val t1: Long = System.currentTimeMillis()
                        //TODO Make this configurable from metadata...? i think...?
                        val responseBody: String =
                          if (isReturnBody) {
                            //TODO Grab this from the response header... i think... i think thats right...
                            implicit val codec: Codec = Codec(charSet)
                            codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
                            codec.onMalformedInput(CodingErrorAction.REPLACE)
                            //Try the successful stream first and if we get an error try the error stream
                            Try(fromInputStream(openConn.getInputStream).mkString) match {
                              case Success(value) => value
                              case Failure(_) =>
                                Try(fromInputStream(openConn.getErrorStream).mkString) match {
                                  case Success(value) => value
                                  case Failure(ex) => ex.toString
                                }
                            }
                          }
                          else "Body not decoded on purpose"
                        //TODO Make the removal of the whitespaces a little more error checking where when i have text i enter a an extra space so they dont all get smashed together
                        val response: HttpClientResponse = HttpClientResponse(timestamp, url, method, body, combinedHeaders, metaData, respCode, (t1 - t0).toInt, responseBody.replaceAll("[\n\r]", " "), removeNullFromHeaders(openConn))
                        Try(openConn.getInputStream.close())
                        Right(response)
                      case Failure(ex) =>
                        Try(openConn.getErrorStream.close())
                        Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (System.currentTimeMillis() - t0).toInt, 4, ex.toString, ex.getMessage))
                    }
                  case Failure(exception) =>
                    val exceptionTimeMark: Long = System.currentTimeMillis()
                    exception.toString match {
                      case "java.net.SocketTimeoutException: connect timed out" => Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 10, exception.toString, "Unable to connect to the endpoint in a timely manner"))
                      case "java.net.ConnectException: Connection refused (Connection refused)" => Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 20, exception.toString, "Check to make sure the service exists"))
                      case "java.net.SocketTimeoutException: Read timed out" => Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 30, exception.toString, "Unable to get the body from the endpoint in a timely manner"))
                      case _ => Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (exceptionTimeMark - callCreateTimeMark).toInt, 0, exception.toString, exception.getMessage))
                    }
                }
            }
          case _ =>
            //Never seen this happen yet...
            Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (System.currentTimeMillis() - callCreateTimeMark).toInt, 1, "Error while attempting to get ConnectionObject"))
        }
      case Failure(ex) => Left(HttpClientError(timestamp, url, method, body, combinedHeaders, metaData, (System.currentTimeMillis() - callCreateTimeMark).toInt, 2, ex.toString, ex.getMessage))
    }
  }

  //This can be overridden with any authorization and if you wish to keep bearer auth just build that function into your new authorization
  def authorization(url: String, method: String, body: String, headers: Map[String, String], metaData: Map[String, String]): Map[String, String] =
    BearerAuthorization.bearerAuthorization(metaData)

  //Format the response headers so they are easy to consume
  private def removeNullFromHeaders(openConn: HttpURLConnection): Map[String, String] = {
    //Map the java collection into scala
    openConn.getHeaderFields.asScala.mapValues(_.asScala.toList).mapValues(_.toList).toMap.map { headers =>
      val (headerKey, headerValues) = headers
      //replaces nulls with strings of null so we don't blow up later
      val hk = if (headerKey == null) "null" else headerKey
      hk -> headerValues.mkString(" ")
    }
  }

  def invokeAsyncHttpClientResponse(
                                     url: String,
                                     method: String,
                                     body: String,
                                     headers: Map[String, String],
                                     metaData: Map[String, String],
                                     isReturnBody: Boolean = true
                                   )
                                   (implicit executionContext: ExecutionContext): Future[Either[HttpClientError, HttpClientResponse]] =
    Future(invokeHttpClientResponse(url, method, body, headers, metaData, isReturnBody))

  def invokeHttpClientResponse(
                                httpSeed: HttpSeed,
                                isReturnBody: Boolean
                              ): Either[HttpClientError, HttpClientResponse] =
    invokeHttpClientResponse(httpSeed.url, httpSeed.method, httpSeed.body, httpSeed.headers, httpSeed.metaData, isReturnBody)

}

object HttpClient extends HttpClient