package Snippets.MonadCancel

import cats.effect.IO
import cats.effect.unsafe.implicits.global

object Uncancelable extends App {
  val run = for {
    fib <- (IO.uncancelable(_ => IO.canceled >> IO.println("Hello world!"))
      >> IO.println("Hello World 2!")).start
    res <- fib.join
  } yield res

  println(run.unsafeRunSync())
}
