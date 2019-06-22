package zeab.j2sjavanethttpclient.httpclient.models

case class HttpResponse[RespBody](
                                   timestamp: String,
                                   responseCode: Int,
                                   responseHeaders: Map[String, String],
                                   responseBody: Either[Throwable, RespBody],
                                   rawResponseBody: String,
                                   durationInMs: Long,
                                   requestUrl: String,
                                   requestMethod: String,
                                   requestBody: String,
                                   requestHeaders: Map[String, String],
                                   requestMetaData: Map[String, String]
                                 )
