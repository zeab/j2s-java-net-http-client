package zeab.experimental

case class HttpError(
                            requestUrl: String,
                            requestMethod: String,
                            requestBody: String,
                            requestHeaders: Map[String, String],
                            requestMetaData: Map[String, String],
                            error: String,
                            duration: Long
                          )