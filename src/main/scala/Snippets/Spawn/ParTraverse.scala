package Snippets.Spawn

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._

object ParTraverse extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      l <- (-10 to 10).toList.parTraverse(i => IO(5f / i))
      _ <- IO.println(l)
    } yield ExitCode.Success
  }
}
