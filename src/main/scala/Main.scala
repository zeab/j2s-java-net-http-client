import zeab.j2sjavanet.httpclient.HttpClient

object Main extends App{

  val x = HttpClient.invokeHttpCore("http://google.com")

  println(x)

}
