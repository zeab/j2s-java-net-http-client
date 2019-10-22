//package otherother.httpclient
//
//import java.net.{HttpURLConnection, URL}
//
//import io.circe.generic.AutoDerivation
//
//import scala.reflect.runtime.universe._
//import io.circe.{Decoder, Encoder}
//import other.MyClass
//import zeab.httpseed.HttpMethods.get
//import zeab.httpseed.{HttpError, HttpResponse, NoBody}
//import zeab.j2sjavanethttpclient.httpclient.{HttpClient, HttpClientHelpers, Serialization}
//import zeab.j2sjavanethttpclient.httpclient.HttpClientSettings.{connectTimeout, defaultConnectTimeoutInMs, defaultReadTimeoutInMs, defaultUserAgent, readTimeout, userAgent}
//
//import scala.concurrent.{ExecutionContext, Future}
//import scala.io.BufferedSource
//import scala.io.Source.fromInputStream
//import scala.util.{Failure, Success, Try}
//
//trait HttpClient extends HttpClientHelpers{
//
//  //TODO Add Http Seed Support back in...
//
//  def invokeAsyncHttp[ReqBody, RespBody](
//                                          url: String,
//                                          method: String = get,
//                                          body: ReqBody = "",
//                                          headers: Map[String, String] = Map.empty,
//                                          metaData: Map[String, String] = Map.empty
//                                        )(implicit executionContext: ExecutionContext, encoder: Encoder[ReqBody], decoder: Decoder[RespBody], typeTag: TypeTag[RespBody]): Future[Either[HttpError, HttpResponse[RespBody]]] =
//    Future(invokeHttp[ReqBody, RespBody](url, method, body, headers, metaData))
//
//  def invokeHttp[ReqBody, RespBody](
//                                     url: String,
//                                     method: String = get,
//                                     body: ReqBody = "",
//                                     headers: Map[String, String] = Map.empty,
//                                     metaData: Map[String, String] = Map.empty
//                                   )(implicit typeTag: TypeTag[RespBody]): Either[HttpError, HttpResponse[RespBody]] = {
//
//    //Make sure the method is upper case because if complains if its not
//    val standardizedMethod: String = method.toUpperCase()
//
//    //Grab a timestamp for logging purposes
//    val timestamp: String = getTimestamp(metaData)
//
//    //Clean up the headers and make sure none are empty
//    val stripEmptyHeaders: Map[String, String] = headers.filterNot{ case (header, _) => header == "" }
//
//    //Change the body from a case class into the desired content type
//    val possibleSerializedRequestBody: Either[Throwable, String] =
//      seri2[ReqBody](ooo[ReqBody], body) //serialization(body, stripEmptyHeaders)
//
//    possibleSerializedRequestBody match {
//      case Right(reqBody) =>
//
//        //This is where the actual request starts timing how long it takes to make this request
//        val requestSentTimestamp: Long = System.currentTimeMillis()
//
//        Try(new URL(url).openConnection) match {
//          case Success(openConnection) =>
//            openConnection match {
//              case openConn: HttpURLConnection =>
//
//                //Set Options
//                openConn.setConnectTimeout(metaData.getOrElse(connectTimeout, defaultConnectTimeoutInMs).toInt)
//                openConn.setReadTimeout(metaData.getOrElse(readTimeout, defaultReadTimeoutInMs).toInt)
//                openConn.setRequestProperty("User-Agent", metaData.getOrElse(userAgent, defaultUserAgent))
//
//                //Set Method
//                openConn.setRequestMethod(standardizedMethod)
//
//                //Headers
//                //Authorization is calculated here based on the keys in the meta data
//                val completedHeaders: Map[String, String] = authorization(url, standardizedMethod, reqBody, stripEmptyHeaders, metaData)
//                completedHeaders.foreach { header =>
//                  val (headerKey, headerValue): (String, String) = header
//                  openConn.setRequestProperty(headerKey, headerValue)
//                }
//
//                //Body
//                if (standardizedMethod != get) {
//                  openConn.setDoOutput(true)
//                  if (body == "") Try(openConn.setFixedLengthStreamingMode(0))
//                  else Try(openConn.getOutputStream.write(reqBody.getBytes(charSet)))
//                  Try(openConn.getOutputStream.close())
//                }
//
//                //Open the connection <- basically not doing this makes gets take twice as long... go figure
//                Try(openConn.connect()) match {
//                  case Success(_) =>
//
//                    //Start grabbing response data
//                    val responseCode: Int = openConn.getResponseCode
//                    val responseHeaders: Map[String, String] = removeNullFromHeaders(openConn)
//
//                    typeTag.tpe.typeSymbol.name.toString match {
//                      case "NoBody" =>
//                        //Grab the timestamp here
//                        val responseReceivedTimestamp: Long = System.currentTimeMillis()
//                        Right(HttpResponse(timestamp, responseCode, responseHeaders, Right(NoBody().asInstanceOf[RespBody]), "", responseReceivedTimestamp - requestSentTimestamp, url, standardizedMethod, reqBody, completedHeaders, metaData))
//                      case _ =>
//                        //Read the incoming buffer sources
//                        val inputStream: Try[BufferedSource] = Try(fromInputStream(openConn.getInputStream))
//                        val errorStream: Try[BufferedSource] = Try(fromInputStream(openConn.getErrorStream))
//
//                        //There is a super odd thing here where if its a good then both will return success but if its bad then only 1 returns success
//                        val rawResponseBody: String =
//                          (inputStream, errorStream) match {
//                            case (Success(stream), Success(_)) =>
//                              val respBody: String = stream.mkString
//                              stream.close()
//                              respBody
//                            case (Failure(_), Success(stream)) =>
//                              val respBody: String = stream.mkString
//                              stream.close()
//                              respBody
//                            case _ => "unable to read either the input stream or the error stream"
//                          }
//
//                        //Grab the timestamp here since at this point all were doing is decoding the response which we already have completed
//                        val responseReceivedTimestamp: Long = System.currentTimeMillis()
//
//                        if (typeTag.tpe.typeSymbol.name.toString == "String")
//                          Right(HttpResponse(timestamp, responseCode, responseHeaders, Right(rawResponseBody.asInstanceOf[RespBody]), rawResponseBody, responseReceivedTimestamp - requestSentTimestamp, url, standardizedMethod, reqBody, completedHeaders, metaData))
//                        else {
//                          //decode the body into a case class
//                          val decodedBody: Either[Throwable, RespBody] = //deserialization[RespBody](responseHeaders, rawResponseBody)
//                          Right("".asInstanceOf[RespBody])
//                          //Give the response back to the user
//                          Right(HttpResponse(timestamp, responseCode, responseHeaders, decodedBody, rawResponseBody, responseReceivedTimestamp - requestSentTimestamp, url, standardizedMethod, reqBody, completedHeaders, metaData))
//                        }
//                    }
//                  case Failure(ex) =>
//                    val exceptionReceivedTimestamp: Long = System.currentTimeMillis()
//                    Left(HttpError(timestamp, url, standardizedMethod, body.toString, completedHeaders, metaData, ex.toString, exceptionReceivedTimestamp - requestSentTimestamp))
//                }
//              case _ =>
//                val exceptionReceivedTimestamp: Long = System.currentTimeMillis()
//                Left(HttpError(timestamp, url, standardizedMethod, body.toString, stripEmptyHeaders, metaData, "Somehow we don't have an open HttpUrlConnection... never seen this happen ever...", exceptionReceivedTimestamp - requestSentTimestamp))
//            }
//          case Failure(ex) =>
//            val exceptionReceivedTimestamp: Long = System.currentTimeMillis()
//            Left(HttpError(timestamp, url, standardizedMethod, body.toString, stripEmptyHeaders, metaData, ex.toString, exceptionReceivedTimestamp - requestSentTimestamp))
//        }
//      case Left(ex) =>
//        Left(HttpError(timestamp, url, standardizedMethod, body.toString, stripEmptyHeaders, metaData, ex.toString, 0))
//    }
//  }
//
//  //TODO change this back to doing the bearer token or nothing if nothing is present
//  def authorization(url: String, method: String, body: String, headers: Map[String, String], metaData: Map[String, String]): Map[String, String] = headers
//
//  def seri2[A](f: A => Either[Throwable, String], obj: A): Either[Throwable, String] = f(obj)
//  def ooo[A]: A => Either[Throwable, String] = {a => Right(a.toString)}
//
//}
//
//object HttpClient extends HttpClient with Serialization{
//  import zeab.aenea.XmlSerializer._
//  import io.circe.generic.auto._
//
//  override def seri2[A](f: A => Either[Throwable, String], obj: A): Either[Throwable, String] = {
//
//    def y[E: Encoder] = serialization(obj, Map.empty)
//
//    def x(implicit encoder: Encoder[A]) = serialization(obj, Map.empty)
//    y
//    println()
//    f(obj)
//  }
//
////  override def ooo[A]: A => Either[Throwable, String] ={a =>
////    val eee = a.getClass.getName
////    println()
////    //lazy implicit val fooDecoder: Decoder[MyClass] = deriveDecoder[MyClass]
////    //lazy implicit val fooEncoder: Encoder[MyClass] = deriveEncoder[MyClass]
////    //implicit val fooEncoderaaa: Encoder[A] = deriveEncoder[A]
////    //fooEncoderaaa
////    //implicit def helloEncoder[A: Encoder]: Encoder[MyClass] = deriveEncoder
////    //val f = MyClass(4)
////    val s = serialization(a, Map.empty)
////    //ooo1[A]
////    //val yy = f.asXml
////
////   // yy
////    s
////  }
//
////  val g = MyClass(6)
////  val s = serialization(g, Map.empty)
////  def ooo1[A]: A => Either[Throwable, String] = {a =>
////    serialization[A](a, Map.empty)
////  }
//
////  def ooo1[A]: A => Either[Throwable, String] = {a =>
////    val jj = a
////    println()
////    val g = MyClass(6)
////    val s = serialization(a, Map.empty)
////    Right(a.toString)
////  }
//}