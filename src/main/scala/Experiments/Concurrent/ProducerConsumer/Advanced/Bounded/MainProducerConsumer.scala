
package Experiments.Concurrent.ProducerConsumer.Advanced.Bounded

import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp, Ref}
import ProducerConsumer.{State, consumer, producer}
import cats.implicits.catsSyntaxParallelSequence1

/*
Producer consumer with bounded buffer
 */
object MainProducerConsumer extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      state <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int](capacity=10))
      counter <- Ref.of[IO, Int](1)
      producers = List.range(1, 11).map(producer(_, counter, state))
      consumers = List.range(1, 11).map(consumer(_, state))
      res <- (producers ++ consumers)
        .parSequence.as(ExitCode.Success) // Run producer and consumer in parallel until done (cancelling with CTRL-C)
        .handleErrorWith {
          t => Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    } yield res
  }
}
