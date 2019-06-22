package zeab.j2sjavanethttpclient.seed

trait HttpHeaders {
  val contentType: String = "Content-Type"
  val accept: String = "Accept"
}

object HttpHeaders extends HttpHeaders
