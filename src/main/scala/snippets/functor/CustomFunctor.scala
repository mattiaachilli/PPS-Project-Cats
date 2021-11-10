package snippets.functor

import cats._

case class CustomFunctor[A](value: A)

object CustomFunctor {
  implicit val functor: Functor[CustomFunctor] = new Functor[CustomFunctor] {
    def map[A, B](fa: CustomFunctor[A])(f: A => B): CustomFunctor[B] = CustomFunctor(f(fa.value))
  }
}
