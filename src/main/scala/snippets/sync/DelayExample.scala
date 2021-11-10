package snippets.sync

import cats.effect.{ExitCode, IO, IOApp, Sync}

import java.util.concurrent.atomic.AtomicLong

object DelayExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val counter = new AtomicLong(0)

    for {
      counterIncremented <- Sync[IO].delay(counter.incrementAndGet())
      _ <- IO.println(counterIncremented)
    } yield ExitCode.Success
  }
}
