package Experiments.Concurrent.ProducerConsumer.Simple

import Experiments.Concurrent.ProducerConsumer.Simple.ProducerConsumer.{consumer, producer}
import cats.effect._
import cats.effect.std.Console
import cats.syntax.all._

import scala.collection.immutable.Queue

/*
This solution isn't efficient because producers runs faster than the consumer so the queue is constantly growing.
 */
object MainProducerConsumer extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      queue <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      res <- (consumer(queue), producer(queue, 0))
        .parMapN((_, _) => ExitCode.Success) // Run producer and consumer in parallel until done (cancelling with CTRL-C)
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    } yield res
  }

  /*
  Alternative solution, but if there is an error it's not catched.
  override def run(args: List[String]): IO[ExitCode] =
    for {
      queueR <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      producerFiber <- producer(queueR, 0).start
      consumerFiber <- consumer(queueR).start
      _ <- producerFiber.join
      _ <- consumerFiber.join
    } yield ExitCode.Error
   */
}
