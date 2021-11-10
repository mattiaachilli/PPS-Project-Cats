package snippets.cyclicbarrier

import cats.effect.std.CyclicBarrier
import cats.effect.{IO, IOApp}
import cats.implicits._

import scala.concurrent.duration.DurationInt

object CyclicBarrierExample extends IOApp.Simple {
  override def run: IO[Unit] = {
    for {
      barrier <- CyclicBarrier[IO](2)
      fiberA <- (IO.println("First fiber") >> barrier.await >> IO.println("First fiber after barrier")).start
      fiberB <- (IO.println("Second fiber") >> barrier.await >> IO.sleep(1.second) >>
        IO.println("Second fiber after barrier")).start
      _ <- (fiberA.join, fiberB.join).tupled
    } yield ()
  }
}
