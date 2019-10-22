package mine

import io.circe.generic.auto._
import io.circe.syntax._
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

object MyHttpClient extends HttpClient {

  override def baseSerializer[ReqBody](implicit classTag: ClassTag[ReqBody], typeTag: TypeTag[ReqBody]): ReqBody => Either[Throwable, String] = {reqBody: ReqBody =>

//    implicit val mirror: Mirror = runtimeMirror(getClass.getClassLoader)
//
//    val objInstanceMirror: InstanceMirror = mirror.reflect(reqBody)
//    val values =
//      objInstanceMirror.symbol.typeSignature.members.toStream.collect { case termSymbol: TermSymbol if !termSymbol.isMethod => objInstanceMirror.reflectField(termSymbol) }
//        .map { fieldMirror: FieldMirror =>
//          val mirrorKey: String = fieldMirror.symbol.name.toString.trim
//          val mirrorValue: Any = fieldMirror.get
//          mirrorValue
//        }.reverse.toList
//
//    val outputClass: ClassSymbol = mirror.staticClass(typeTag.tpe.toString)
//    val classMirror: ClassMirror = mirror.reflectClass(outputClass)
//    val constructor: MethodSymbol = outputClass.primaryConstructor.asMethod
//    val constructorMirror: MethodMirror = classMirror.reflectConstructor(constructor)
//
//    val instance = constructorMirror.apply(values: _*)

    //val x = reqBody.asJsonObject
    //implicit val fooDecoder: Decoder[Foo] = deriveDecoder
    //implicit val fooEncoder: Encoder[Mouse] = deriveEncoder
    val x = reqBody.key.asJson.noSpaces
    //val x = json()
    //val y = reqBody

    println()
    //reqBody
    Right("asd")
  }

//  final def json[ReqBody: Encoder](obj:ReqBody): String ={
//    //implicit val fooEncoder: Encoder[Mouse] = deriveEncoder
//    obj.asJson.noSpaces
//
//  }



}

//
//
//import io.circe.{ Decoder, Encoder }
//import shapeless.Unwrapped
//
//trait AnyValCirceEncoding {
//  implicit def anyValEncoder[V, U](implicit ev: V <:< AnyVal,
//                                   V: Unwrapped.Aux[V, U],
//                                   encoder: Encoder[U]): Encoder[V] = {
//    val _ = ev
//    encoder.contramap(V.unwrap)
//  }
//
//  implicit def anyValDecoder[V, U](implicit ev: V <:< AnyVal,
//                                   V: Unwrapped.Aux[V, U],
//                                   decoder: Decoder[U]): Decoder[V] = {
//    val _ = ev
//    decoder.map(V.wrap)
//  }
//}
//
//object AnyValCirceEncoding extends AnyValCirceEncoding
//
////object CirceSupport
////  extends de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
////    with AnyValCirceEncoding