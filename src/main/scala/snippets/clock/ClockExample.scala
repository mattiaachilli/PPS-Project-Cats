package snippets.clock

import cats.effect.{Clock, ExitCode, IO, IOApp}

import scala.concurrent.duration.DurationInt

object ClockExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      start <- Clock[IO].realTime
      _ <- IO.sleep(5.seconds)
      end <- Clock[IO].realTime
      _ <- IO.println(end - start)

    } yield ExitCode.Success
  }
}
