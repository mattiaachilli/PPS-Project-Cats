package Snippets.Monoids

import cats.Monoid

object MainCustomMonoids extends App {
  final case class CustomClass(value: Int)
  object CustomClass {
    implicit val customMonoid: Monoid[CustomClass] = new Monoid[CustomClass] {
      override def empty: CustomClass = CustomClass(0)

      override def combine(x: CustomClass, y: CustomClass): CustomClass = CustomClass(x.value * y.value)
    }
  }

  print(Monoid[CustomClass].combine(Monoid[CustomClass].empty, CustomClass(2)))
  print(Monoid[CustomClass].combine(CustomClass(3), CustomClass(2)))
}
