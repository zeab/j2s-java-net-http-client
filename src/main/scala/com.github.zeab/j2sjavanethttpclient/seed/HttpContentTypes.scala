package com.github.zeab.j2sjavanethttpclient.seed

/** Http Content Types */
trait HttpContentTypes {
  /** JSON */
  val applicationJson: String = "application/json"
  /** XML */
  val applicationXml: String = "application/xml"
  /** Text Plain */
  val textPlain: String = "text/plain"
  /** HTML */
  val textHtml: String = "text/html"
}

/** Content Types */
object HttpContentTypes extends HttpContentTypes
