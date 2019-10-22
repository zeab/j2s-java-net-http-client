package mine

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait HttpClient {

  def invoke[ReqBody](url: String, method: String, body: ReqBody)(implicit classTag: ClassTag[ReqBody], typeTag: TypeTag[ReqBody]): Either[Throwable, String] ={
    val possibleRequestBody: Either[Throwable, String] = serialize[ReqBody](baseSerializer, body)
    possibleRequestBody
  }

  def serialize[ReqBody](serializer: ReqBody => Either[Throwable, String], obj: ReqBody)(implicit classTag: ClassTag[ReqBody], typeTag: TypeTag[ReqBody]): Either[Throwable, String] =
    serializer(obj)

  def baseSerializer[ReqBody](implicit classTag: ClassTag[ReqBody], typeTag: TypeTag[ReqBody]): ReqBody => Either[Throwable, String] = body => Right(body.toString)

}

object HttpClient extends HttpClient
