package zeab.j2sjavanet.httpclient.models

case class HttpResponseRaw(
                            responseStatusCode: Int,
                            responseBody: String,
                            responseHeaders: Map[String, String],
                            requestUrl: String,
                            requestMethod: String,
                            requestBody: String,
                            requestHeaders: Map[String, String],
                            requestMetadata: Map[String, String],
                            durationInMs: Long
                          ) extends HttpResponse
