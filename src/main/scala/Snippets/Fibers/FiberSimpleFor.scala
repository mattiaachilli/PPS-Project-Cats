package Snippets.Fibers

import cats.effect.{IO, IOApp}

object FiberSimpleFor extends IOApp.Simple {
  override val run: IO[Unit] = for {
    _ <- IO.println("Hello")
    _ <- IO.println("World")
  } yield ()
}
