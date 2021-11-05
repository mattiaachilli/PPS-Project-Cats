package Snippets.Async

import cats.effect.{ExitCode, IO, IOApp}

object ExecutionContextExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val printThread: IO[Unit] = IO.executionContext.flatMap(IO.println(_))

    for {
      _ <- printThread
    } yield ExitCode.Success
  }
}
