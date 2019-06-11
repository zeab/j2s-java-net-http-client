package zeab.urlbuilder

//Imports
import org.scalatest.FunSpec
import zeab.j2sjavanethttpclient.seed.urlbuilder.urlbuilder.UrlBuilder

class UrlBuilderSpec extends FunSpec {

  describe("UrlString Builder") {

    it("should return a http://localhost") {
      val url: String =
        UrlBuilder
          .schema("http")
          .host("localhost")
          .toString
      assert(url === "http://localhost")
    }

    it("should return a http://localhost/this") {
      val url: String =
        UrlBuilder
          .schema("http")
          .host("localhost")
          .hostParameter("this")
          .toString
      assert(url === "http://localhost/this")
    }

    it("should return a http://localhost/this?a=parameter") {
      val url: String =
        UrlBuilder
          .schema("http")
          .host("localhost")
          .hostParameter("this")
          .queryParameter("a", "parameter")
          .toString
      assert(url === "http://localhost/this?a=parameter")
    }

    it("should return a http://localhost/this?a=parameter&b=parameter") {
      val url: String =
        UrlBuilder
          .schema("http")
          .host("localhost")
          .hostParameter("this")
          .queryParameter("a", "parameter")
          .queryParameter("b", "parameter")
          .toString
      assert(url === "http://localhost/this?a=parameter&b=parameter")
    }

    it("should return a http://localhost/this?c=parameter&d=parameter") {
      val url: String =
        UrlBuilder
          .schema("http")
          .host("localhost")
          .hostParameter("this")
          .queryParameter(Map("c" -> "parameter"))
          .queryParameter("d", "parameter")
          .toString
      assert(url === "http://localhost/this?c=parameter&d=parameter")
    }

  }

}
