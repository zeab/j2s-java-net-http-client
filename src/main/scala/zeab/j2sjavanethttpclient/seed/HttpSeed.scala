package zeab.j2sjavanethttpclient.seed

//Imports
import zeab.j2sjavanethttpclient.seed.HttpMethods.get

case class HttpSeed(
                     url: String,
                     method: String = get,
                     body: String = "",
                     headers: Map[String, String] = Map.empty,
                     metaData: Map[String, String] = Map.empty
                   )
