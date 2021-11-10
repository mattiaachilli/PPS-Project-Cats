package snippets.countdownlatch

import cats.effect.std.CountDownLatch
import cats.effect.{IO, IOApp}

object CountDownLatchExample extends IOApp.Simple {
  override def run: IO[Unit] = {
    for {
      latch <- CountDownLatch[IO](2)
      fiber <- (latch.await >> IO.println("Latch unlocked")).start
      _ <- latch.release >> IO.println("First release")
      _ <- latch.release >> IO.println("Second release")
      _ <- fiber.join
    } yield()
  }
}
