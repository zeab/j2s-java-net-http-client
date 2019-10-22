package mine

object Main extends App {

  val reqBody = Mouse("gilbert", 3)

  val response = MyHttpClient.invoke("http://localhost:8080", "POST", reqBody)

  println(response)

}
