package snippets.deferred

import cats.effect.{Deferred, IO, IOApp}
import cats.implicits._

object DeferredExample extends IOApp.Simple {
  def raceCompletion(deferred: Deferred[IO, Int]): IO[Unit] = {
    val attemptCompletion: Int => IO[Unit] = n => deferred.complete(n).attempt.void
    val race: IO[Either[Unit, Unit]] = IO.race(attemptCompletion(1), attemptCompletion(2))
    (
      race,
      deferred.get.flatMap { n => IO(println(show"Result: $n")) }
    ).parTupled.void
  }

  override def run: IO[Unit] = {
    for {
      deferred <- Deferred[IO, Int]
      _ <- raceCompletion(deferred)
    } yield ()
  }
}
