package Snippets.Unique

import cats.{Monad, effect}
import cats.effect.{ExitCode, IO, IOApp, Unique}
import cats.implicits._
import cats.kernel.laws.IsEqArrow

object UniqueExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val token: IO[Unique.Token] = effect.Unique[IO].unique
    (token, token).mapN { (x, y) => x === y } <-> Monad[IO].pure(false)
    IO(ExitCode.Success)
  }
}
