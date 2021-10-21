package Snippets.Monads
import Snippets.Functor.CustomFunctor
import cats.implicits._

object MainCustomMonad extends App {
  val endResult = for {
    a <- CustomMonad(1)
    b <- CustomMonad(2)
  } yield a + b
  print(endResult)
}
