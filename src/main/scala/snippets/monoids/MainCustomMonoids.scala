package snippets.monoids

import cats.Monoid

object MainCustomMonoids extends App {
  final case class CustomClass(value: Int)
  object CustomClass {
    implicit val customMonoid: Monoid[CustomClass] = new Monoid[CustomClass] {
      override val empty: CustomClass = CustomClass(1)

      override def combine(x: CustomClass, y: CustomClass): CustomClass = CustomClass(x.value * y.value)
    }
  }

  print(Monoid[CustomClass].combine(Monoid[CustomClass].empty, CustomClass(2)))
  print(Monoid[CustomClass].combine(CustomClass(3), CustomClass(2)))
}
