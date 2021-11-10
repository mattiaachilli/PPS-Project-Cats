package snippets.semigroup

import cats.Semigroup

object MainIntSemigroup extends App {
  implicit val multiplicationSemigroup: Semigroup[Int] = (x: Int, y: Int) => x * y

  print(Semigroup[Int].combine(2, 3))
}
