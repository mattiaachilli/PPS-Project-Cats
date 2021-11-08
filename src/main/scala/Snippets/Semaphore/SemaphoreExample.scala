package Snippets.Semaphore

import cats.effect.std.{Console, Semaphore}
import cats.effect.{IO, IOApp, Temporal}
import cats.implicits._
import cats.effect.syntax.all._

import scala.concurrent.duration.DurationInt

object SemaphoreExample extends IOApp.Simple {
  def useSemaphore[F[_]: Temporal](id: Int, s: Semaphore[F])(implicit F: Console[F]): F[Unit] = {
    for {
      x <- s.available
      _ <- F.println(s"$id => availibility: $x")
      _ <- s.acquire
      y <- s.available
      _ <- F.println(s"Acquire $id => availibility: $y")
      _ <- s.release.delayBy(2.seconds)
      z <- s.available
      _ <- F.println(s"Release $id => availibility: $z")
    } yield()
  }

  override def run: IO[Unit] = {
    for {
      s <- Semaphore[IO](1)
      _ <- (useSemaphore(1, s), useSemaphore(2, s))
        .parTupled
        .void
    } yield()
  }
}
