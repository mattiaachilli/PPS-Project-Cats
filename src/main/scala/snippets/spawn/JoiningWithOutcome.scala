package snippets.spawn

import cats.MonadError
import cats.effect.{ExitCode, IO, IOApp, MonadCancel, Outcome, Spawn}

import scala.concurrent.duration.DurationInt

object JoiningWithOutcome extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      fiberA <- (IO.println("Hello World Fiber A").foreverM.timeoutTo(5.seconds, IO.unit) >> IO.pure(1)).start
      fiberB <- (IO.println("Hello World Fiber B").foreverM.timeoutTo(5.seconds, IO.unit) >> IO.pure(2)).start

      a <- fiberA.join flatMap {
        case Outcome.Succeeded(fa) => fa
        case Outcome.Errored(e) => MonadError[IO, Throwable].raiseError(e)
        case Outcome.Canceled() => MonadCancel[IO].canceled >> Spawn[IO].never
      }
      b <- fiberB.joinWithNever

      _ <- IO.println(s"a: $a, b: $b")
    } yield ExitCode.Success
  }
}
