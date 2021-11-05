package Snippets.Spawn

import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration.DurationInt

object Cancelable extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      target <- IO.println("Hello World").foreverM.start
      _ <- IO.sleep(1.second)
      _ <- target.cancel
    } yield ExitCode.Success
  }
}
