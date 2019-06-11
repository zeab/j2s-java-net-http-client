package zeab.j2sjavanethttpclient.seed.httpclientmessages

import zeab.j2sjavanethttpclient.seed.HttpHeaders.uniqueNameKey

trait HttpClientFormatting {

  def httpClientErrorFormat(httpClientError: HttpClientError): String =
    s"""-----Error-----
       |* Unique Name:${httpClientError.requestMetaData.getOrElse(uniqueNameKey, "No Unique Name")}
       |* Timestamp: ${httpClientError.requestTimestamp}
       |* Error:
       |${httpClientError.errorMsg}
       |* ErrorExtended:
       |${httpClientError.errorDetailedMsg}
       |* Status Code:
       |${httpClientError.responseStatusCode}
       |* Response Time (ms): ${httpClientError.responseTime}ms
       |--Request Data--
       |* Url: ${httpClientError.requestUrl}
       |* Method: ${httpClientError.requestMethod}
       |* Body:
       |${if (httpClientError.requestBody == "") "Blank" else httpClientError.requestBody}
       |* Headers:
       |${mapFormat(httpClientError.requestHeaders)}
       |* MetaData:
       |${mapFormat(httpClientError.requestMetaData)}"""
      .stripMargin

  def httpClientResponseFormat(httpClientResponse: HttpClientResponse): String =
    s"""-----Response-----
       |* Unique Name: ${httpClientResponse.requestMetaData.getOrElse(uniqueNameKey, "No Unique Name")}
       |* Timestamp: ${httpClientResponse.requestTimestamp}
       |* Response Status Code: ${httpClientResponse.responseStatusCode}
       |* Response Body:
       |${httpClientResponse.responseBody}
       |* Response Headers:
       |${mapFormat(httpClientResponse.responseHeaders)}
       |* Response Time (ms): ${httpClientResponse.responseTimeInMs}
       |--Request Data--
       |* Url: ${httpClientResponse.requestUrl}
       |* Method: ${httpClientResponse.requestMethod}
       |* Body:
       |${if (httpClientResponse.requestBody == "") "Blank" else httpClientResponse.requestBody}
       |* Headers:
       |${mapFormat(httpClientResponse.requestHeaders)}
       |* MetaData:
       |${mapFormat(httpClientResponse.requestMetaData)}"""
      .stripMargin

  def mapFormat(m: Map[String, String]): String =
    if (m.isEmpty) "Blank"
    else
      m.toList.map { item =>
        val (key, value) = item
        s"  $key -> $value"
      }.mkString("  ", "\n  ", "")

}
