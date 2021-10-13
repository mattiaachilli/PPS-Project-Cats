package Snippets.Fibers

import cats.effect._
import cats.effect.cps.{AwaitSyntax, async}

object FiberAsyncAwait extends IOApp.Simple {
  override def run: IO[Unit] =
    async[IO] {
      IO.println("Hello").await
      IO.println("World").await
    }
}
