package zeab.experimental

case class HttpResponse[RespBody](
                                         responseCode: Int,
                                         responseHeaders: Map[String, String],
                                         responseBody: Either[Throwable, RespBody],
                                         rawResponseBody: String,
                                         duration: Long,
                                         requestUrl: String,
                                         requestMethod: String,
                                         requestBody: String,
                                         requestHeaders: Map[String, String],
                                         requestMetaData: Map[String, String]
                                       )
