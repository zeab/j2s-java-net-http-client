package com.github.zeab.j2sjavanethttpclient.seed

/** Http Methods for Http Calls */
trait HttpMethods {
  val get: String = "GET"
  val put: String = "PUT"
  val post: String = "POST"
  val delete: String = "DELETE"
  val trace: String = "TRACE"
}

/** Http Methods for Http Calls */
object HttpMethods extends HttpMethods
