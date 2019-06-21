package zeab.j2sjavanethttpclient.newhttpclient

case class HttpResponse[A](
                                       requestUrl:String = "",
                                       requestMethod:String = "",
                                       requestBody:String = "",
                                       requestHeaders:Map[String, String] = Map.empty,
                                       requestMetaData:Map[String, String] = Map.empty,
                                       responseCode:Int = 1,
                                       responseBody: A = "",
                                       responseHeaders:Map[String, String] = Map.empty
                                     )
