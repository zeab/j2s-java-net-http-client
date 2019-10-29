package zeab.j2sjavanet.httpclient.models

case class HttpResponseDecoded[RespBody](
                                          responseStatusCode: Int,
                                          responseBody: RespBody,
                                          responseHeaders: Map[String, String],
                                          requestUrl: String,
                                          requestMethod: String,
                                          requestBody: String,
                                          requestHeaders: Map[String, String],
                                          requestMetadata: Map[String, String],
                                          durationInMs: Long
                                        ) extends HttpResponse
