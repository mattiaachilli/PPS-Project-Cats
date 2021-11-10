package experiments.concurrent.producerconsumer.advanced.unbounded

import cats.effect.std.Console
import cats.effect.{Async, Deferred, Ref, Sync}
import cats.syntax.all._

import scala.collection.immutable.Queue

object ProducerConsumer {
  case class State[F[_], A](queue: Queue[A], takers: Queue[Deferred[F, A]])

  object State {
    def empty[F[_], A]: State[F, A] = State(Queue.empty, Queue.empty)
  }

  def consumer[F[_] : Async : Console](id: Int, state: Ref[F, State[F, Int]]): F[Unit] = {
    val consume: F[Int] = {
      Deferred[F, Int].flatMap {
        taker =>
          state.modify {
            case State(queue, takers) if queue.nonEmpty => // If queue is not empty
              val (i, rest) = queue.dequeue
              State(rest, takers) -> Async[F].pure(i) // Got element in queue, we can just return it
            case State(queue, takers) => State(queue, takers.enqueue(taker)) -> taker.get // No element in queue, must block caller until some is available
          }.flatten
      }
    }

    for {
      i <- consume
      _ <- Console[F].println(s"Consumer $id has got item: $i")
      _ <- consumer(id, state)
    } yield ()
  }

  def producer[F[_] : Sync : Console](id: Int, counter: Ref[F, Int], state: Ref[F, State[F, Int]]): F[Unit] = {
    def produce(i: Int): F[Unit] =
      state.modify {
        case State(queue, takers) if takers.nonEmpty => // If there is almost a taker dequeue it and release it
          val (taker, rest) = takers.dequeue
          State(queue, rest) -> taker.complete(i).void
        case State(queue, takers) => // If there aren't takers produce element
          State(queue.enqueue(i), takers) -> Sync[F].unit
      }.flatten

    for {
      i <- counter.getAndUpdate(_ + 1) // Update the item
      _ <- produce(i)
      _ <- Console[F].println(s"Producer $id product item: $i")
      _ <- producer(id, counter, state)
    } yield ()
  }
}
