package Snippets.Monads

import cats.Monad

import scala.annotation.tailrec

case class CustomMonad[A](value:A)

object CustomMonad {
  implicit val customMonad: Monad[CustomMonad] = new Monad[CustomMonad] {
    override def pure[A](x: A): CustomMonad[A] = CustomMonad(x)

    override def flatMap[A, B](fa: CustomMonad[A])(f: A => CustomMonad[B]): CustomMonad[B] = f(fa.value)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => CustomMonad[Either[A, B]]): CustomMonad[B] = f(a) match {
      case CustomMonad(either) => either match {
        case Left(a) => tailRecM(a)(f)
        case Right(b) => CustomMonad(b)
      }
    }
  }
}
