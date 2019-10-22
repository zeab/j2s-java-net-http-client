package zeab.j2sjavanet.httpclient.models

trait HttpResponse {
  val responseStatusCode: Int
  val responseHeaders: Map[String, String]
  val requestUrl: String
  val requestMethod: String
  val requestBody: String
  val requestHeaders: Map[String, String]
  val requestMetadata: Map[String, String]
  val durationInMs: Long
}
