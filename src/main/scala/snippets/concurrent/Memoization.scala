package snippets.concurrent

import cats.effect.{ExitCode, IO, IOApp}

object Memoization extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val action: IO[String] = IO.println("This is only printed once").as("action")
    for {
        memoized <- action.memoize
        res1 <- memoized
        res2 <- memoized
        _ <- IO.println(res1 ++ res2)
    } yield ExitCode.Success
  }
}
