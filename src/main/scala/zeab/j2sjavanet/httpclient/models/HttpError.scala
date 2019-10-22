package zeab.j2sjavanet.httpclient.models

case class HttpError(
                      error: String,
                      extra: String,
                      requestUrl: String,
                      requestMethod: String,
                      requestBody: String,
                      requestHeaders: Map[String, String],
                      requestMetadata: Map[String, String],
                      durationInMs: Long
                    )
