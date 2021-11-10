package snippets.sync

import cats.effect.{ExitCode, IO, IOApp, Sync}

import scala.io.Source

object BlockingExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      f <- Sync[IO].blocking(Source.fromFile("files/prova.txt"))
      s <- Sync[IO].blocking(f.mkString)
      _ <- Sync[IO].blocking(f.close()) >> IO.println(s)
    } yield ExitCode.Success
  }
}
