package zeab.j2sjavanethttpclient.httpclient

//Imports
import zeab.j2sjavanethttpclient.httpclient.HttpClientSettings._
//Scala
import scala.io.Codec
import scala.collection.JavaConverters._
//Java
import java.nio.charset.CodingErrorAction
import java.net.HttpURLConnection
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

trait HttpClientHelpers {

  //Set the Charset to be used
  val charSet: String = "UTF-8"

  //Set the codec to be used for buffered source
  //TODO this still doesn't feel 100% right to me but I have yet to figure out why... not the code it self but there had got to be a better way to pull the codec ... something connected to the response headers...???
  implicit val codec: Codec = Codec(charSet)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
  codec.onMalformedInput(CodingErrorAction.REPLACE)

  //Format the response headers so they are easy to consume
  def removeNullFromHeaders(openConn: HttpURLConnection): Map[String, String] = {
    //Map the java collection into scala
    openConn.getHeaderFields.asScala.mapValues(_.asScala.toList).mapValues(_.toList).toMap.map { headers =>
      val (headerKey, headerValues): (String, List[String]) = headers
      //replaces nulls with strings of null so we don't blow up later
      val hk: String = if (headerKey == null) "null" else headerKey
      hk -> headerValues.mkString(" ")
    }
  }

  def getTimestamp(metaData: Map[String, String]): String =
    ZonedDateTime.now(
      ZoneId.of(metaData.getOrElse(zoneId, defaultZoneId)))
      .format(DateTimeFormatter
        .ofPattern(metaData.getOrElse(timestampFormat, defaultTimestampFormat)))

}
