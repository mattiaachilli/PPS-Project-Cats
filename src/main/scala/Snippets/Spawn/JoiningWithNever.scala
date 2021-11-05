package Snippets.Spawn

import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration.DurationInt

object JoiningWithNever extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      fiberA <- (IO.println("Hello World Fiber A").foreverM.timeoutTo(5.seconds, IO.unit) >> IO.pure(1)).start
      fiberB <- (IO.println("Hello World Fiber B").foreverM.timeoutTo(5.seconds, IO.unit) >> IO.pure(2)).start

      a <- fiberA.joinWithNever
      b <- fiberB.joinWithNever

      _ <- IO.println(s"a: $a, b: $b")
    } yield ExitCode.Success
  }
}
