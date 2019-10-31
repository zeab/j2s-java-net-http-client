package zeab.j2sjavanet.httpclient.models

case class HttpException(
                          error: String,
                          cause: String,
                          responseStatusCode: Int,
                          responseBody: String,
                          responseHeaders: Map[String, String],
                          requestUrl: String,
                          requestMethod: String,
                          rawRequestBody: String,
                          requestHeaders: Map[String, String],
                          requestMetadata: Map[String, String],
                          durationInMs: Long
                        ) extends Throwable
