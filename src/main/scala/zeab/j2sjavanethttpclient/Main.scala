package zeab.j2sjavanethttpclient

import zeab.j2sjavanethttpclient.newhttpclient.HttpClient
import io.circe.generic.auto._

object Main extends App {

  case class HttpError(code:Int, msg:String)

  case class Llama(name:String)

  case class ResponseLlama(name:String)

  val x = HttpClient.invokeHttp[String, HttpError]("http://localhost:8080/live", "GET", "", Map("Accept" -> "application/xml"))

  val e = x.right.get.responseBody

  println()
}
