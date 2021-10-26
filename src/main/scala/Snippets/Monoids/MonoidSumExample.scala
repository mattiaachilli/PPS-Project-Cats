package Snippets.Monoids

import cats.Monoid


object MonoidSumExample extends App {

  implicit val intMonoid: Monoid[Int] = new Monoid[Int] {
    override def empty: Int = 0

    override def combine(x: Int, y: Int): Int = x + y
  }

  implicit val stringMonoid: Monoid[String] = new Monoid[String] {
    override def empty: String = ""

    override def combine(x: String, y: String): String = x + y
  }


  def sum[A: Monoid](xs: List[A]): A = {
    val m = implicitly[Monoid[A]]
    xs.foldLeft(m.empty)(m.combine)
  }

  println(sum(List(1, 2, 3, 4)))
  println(sum(List("a", "b", "c")))
}
