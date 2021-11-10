package snippets.semigroup

import cats.Semigroup
import cats.implicits.catsSyntaxSemigroup

object MainCustomSemigroup extends App {
  final case class CustomClass(value: Int)
  object CustomClass {
    implicit val productIntSemigroup: Semigroup[CustomClass] =
      (x: CustomClass, y: CustomClass) => CustomClass(x.value * y.value)
  }

  print(CustomClass(10) |+| CustomClass(3))
}
