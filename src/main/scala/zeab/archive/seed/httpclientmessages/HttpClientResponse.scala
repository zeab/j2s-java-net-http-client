//package zeab.archive.seed.httpclientmessages
//
//case class HttpClientResponse(
//                               requestTimestamp: String = "",
//                               requestUrl: String = "",
//                               requestMethod: String = "",
//                               requestBody: String = "",
//                               requestHeaders: Map[String, String] = Map.empty,
//                               requestMetaData: Map[String, String] = Map.empty,
//                               responseStatusCode: Int = 0,
//                               responseTimeInMs: Int = 0,
//                               responseBody: String = "",
//                               responseHeaders: Map[String, String] = Map.empty
//                             ) extends HttpClientFormatting {
//  override def toString: String = httpClientResponseFormat(this)
//}