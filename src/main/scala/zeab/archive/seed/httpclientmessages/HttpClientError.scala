//package zeab.archive.seed.httpclientmessages
//
//case class HttpClientError(
//                            requestTimestamp: String = "",
//                            requestUrl: String = "",
//                            requestMethod: String = "",
//                            requestBody: String = "",
//                            requestHeaders: Map[String, String] = Map.empty,
//                            requestMetaData: Map[String, String] = Map.empty,
//                            responseTime: Int = 0,
//                            responseStatusCode: Int = 0,
//                            errorMsg: String = "",
//                            errorDetailedMsg: String = ""
//                          ) extends HttpClientFormatting {
//  override def toString: String = httpClientErrorFormat(this)
//}
