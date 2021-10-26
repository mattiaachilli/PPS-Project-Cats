package Snippets.Monoids

import cats.Monoid

object MainIntMonoids extends App {
  implicit val multiplicationMonoid: Monoid[Int] = new Monoid[Int] {
    override def empty: Int = 0

    override def combine(x: Int, y: Int): Int = x * y
  }

  print(Monoid[Int].combine(Monoid[Int].empty, 2))
  print(Monoid[Int].combine(1, 2))
}
