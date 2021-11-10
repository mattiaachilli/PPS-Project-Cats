package snippets.monoids

import scala.language.implicitConversions

trait Monoid[A] {
  def combine(x: A, y: A): A
  def empty: A
}

object Monoid {
  object syntax extends MonoidSyntax

  implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
    def combine(a: Int, b: Int): Int = a + b
    def empty: Int = 0
  }
  implicit val StringMonoid: Monoid[String] = new Monoid[String] {
    def combine(a: String, b: String): String = a + b
    def empty: String = ""
  }
}

trait MonoidSyntax {
  implicit final def syntaxMonoid[A: Monoid](a: A): MonoidOps[A] = new MonoidOps[A](a)
}

final class MonoidOps[A: Monoid](lhs: A) {
  def |+|(rhs: A): A = implicitly[Monoid[A]].combine(lhs, rhs)
}

object MonoidsSyntax extends App {
  import Monoid.syntax._
  println(3 |+| 4)
  println("a" |+| "b")
}
