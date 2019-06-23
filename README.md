# j2s-java-net-http-client
Scala wrapper around Java.net Http Client

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.zeab/j2sjavanethttpclient_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.zeab/j2sjavanethttpclient_2.12)
[![Build Status](https://travis-ci.org/zeab/j2s-java-net-http-client.svg?branch=master)](https://travis-ci.org/zeab/j2s-java-net-http-client)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fb3f95b4346347da9aed1cfafe7bc960)](https://www.codacy.com/app/zeab/j2s-java-net-http-client?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zeab/j2s-java-net-http-client&amp;utm_campaign=Badge_Grade)

Sync Example: 
```scala
import io.circe.generic.auto._
import zeab.j2sjavanethttpclient.httpclient.HttpClient
HttpClient.invokeHttp[String, String](http://google.com)
```
   
Async Example: 
```scala
import io.circe.generic.auto._
import zeab.j2sjavanethttpclient.httpclient.HttpClient
HttpClient.invokeAsyncHttp[String, String](http://google.com)
```

Request and Response Bodys:
```scala
import io.circe.generic.auto._
import zeab.j2sjavanethttpclient.httpclient.HttpClient

case class MyRequest(id:String)
case class MyResponse(id:String)

HttpClient.invokeHttp[MyRequest, MyResponse](http://google.com, "POST", MyRequest("1"), Map("Content-Type" -> "application/json", "Accept" -> "application/json"))
```

Performance Mode (skips dealing with the response body completely): 
```scala
import io.circe.generic.auto._
import zeab.j2sjavanethttpclient.httpclient.HttpClient
HttpClient.invokeHttp[String, NoBody](http://google.com)
```














