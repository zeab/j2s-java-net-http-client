//package zeab.archive.seed
//
//trait HttpHeaders {
//
//  val uniqueNameKey: String = "UniqueName"
//  val contentTypeKey: String = "Content-type"
//  val acceptKey: String = "Accept"
//  val dateKey: String = "Date"
//  val digestKey: String = "Digest"
//  val authorizationKey: String = "Authorization"
//  val userAgentKey: String = "UserAgent"
//
//  def bearerHeader(bearer:String): Map[String, String] = Map(authorizationKey -> s"Bearer $bearer")
//  def userAgentBrowserHeader(browser:String = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36"): Map[String, String] = Map(userAgentKey -> browser)
//
//}
//
//object HttpHeaders extends HttpHeaders
