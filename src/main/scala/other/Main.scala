//package other
//
//object Main extends App {
//  implicit val e:Int = 5
//
//  val x = MyClass(8)
//  //ok so what i need is a function that takes Any and returns me a string
//  //i also need one that takes a string and gives me back the thing i asked for
//
//  def ooo[A](implicit j:Int): A => String = {_.toString + j}
//
//  def oo[A]: A => String = {_.toString + "moose"}
//
//
//  def seri2[A](f: A => String, obj: A): String = f(obj)
//
//  val ii = seri2(ooo, x)
//
//  println(ii)
//
//  def seri[A]: A => String = (obj: A) => obj.toString
//
//  val rr = seri(x)
//
//
//
//  def doubleValue: Int => Int = (x: Int) => x * x
//  def ff: Int => String = (x: Int) => (x * 3).toString
//  def gg: Int => Double = (x: Int) => (x *4).toDouble
//  def jj: (Int, Int) => Double = (x:Int, y:Int) => x + y
//  val eee = jj(5, 6)
//
//  def jja(implicit a: Int): (Int, Int) => Double = (x:Int, y:Int) => x + y
//
//
//  val eeeee = jja(9)(9, 8)
//
//  println()
//
//  val s = doubleValue(6)
//
//  println(s)
//
//  def ss:(Int, Int) => Int = ???
//
//  def addition(f: (Int, Int) => Int,a: Int, b:Int): Int = f(a,b)
//
//  addition(ss, 8, 6)
//
//}
