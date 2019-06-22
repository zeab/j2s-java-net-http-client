package zeab.archive

//Imports
import org.scalatest.FunSpec
import zeab.archive.httpclient.HttpClient

class J2sJavaNetHttpClientSpec extends FunSpec {

  describe("J2S Java Net Http Client") {
    it("Should get a 200 response from google.com"){
      HttpClient.invokeHttpClientResponse("http://google.com") match {
        case Right(resp) =>
          resp.responseStatusCode match {
            case 200 => succeed
            case _ => fail()
          }
        case Left(_) => fail()
      }
    }
  }

}
