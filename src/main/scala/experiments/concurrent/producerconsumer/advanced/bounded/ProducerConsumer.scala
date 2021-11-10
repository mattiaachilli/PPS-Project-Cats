package experiments.concurrent.producerconsumer.advanced.bounded

import cats.effect.std.Console
import cats.effect.{Async, Deferred, Ref}
import cats.syntax.all._

import scala.collection.immutable.Queue

object ProducerConsumer {
  case class State[F[_], A](capacity: Int, queue: Queue[A], takers: Queue[Deferred[F, A]],
                            offerers: Queue[(A, Deferred[F, Unit])])

  object State {
    def empty[F[_], A](capacity: Int): State[F, A] = State(capacity, Queue.empty, Queue.empty, Queue.empty)
  }

  def consumer[F[_]: Async: Console](id: Int, state: Ref[F, State[F, Int]]): F[Unit] = {
    val consume: F[Int] =
      Deferred[F, Int].flatMap { taker =>
        state.modify {
          /* If queue is not empty and there aren't producers blocked */
          case State(capacity, queue, takers, offerers) if queue.nonEmpty && offerers.isEmpty =>
            val (i, rest) = queue.dequeue
            State(capacity, rest, takers, offerers) -> Async[F].pure(i)
          /* If queue is not empty and there are producers blocked */
          case State(capacity, queue, takers, offerers) if queue.nonEmpty =>
            val (i, rest) = queue.dequeue
            val ((_, release), tail) = offerers.dequeue
            State(capacity, rest, takers, tail) -> release.complete(()).as(i)
          /* If there are producers blocked */
          case State(capacity, queue, takers, offerers) if offerers.nonEmpty =>
            val ((i, release), rest) = offerers.dequeue
            State(capacity, queue, takers, rest) -> release.complete(()).as(i)
          /* Otherwise */
          case State(capacity, queue, takers, offerers) =>
            State(capacity, queue, takers.enqueue(taker), offerers) -> taker.get
        }.flatten
      }
    for {
      i <- consume
      _ <- Console[F].println(s"Consumer $id has got item: $i")
      _ <- consumer(id, state)
    } yield ()
  }

  def producer[F[_]: Async: Console](id: Int, counter: Ref[F, Int], state: Ref[F, State[F, Int]]): F[Unit] = {
    def produce(i: Int): F[Unit] =
      Deferred[F, Unit].flatMap { offerer =>
        state.modify {
          /* If there are takers blocked */
          case State(capacity, queue, takers, offerers) if takers.nonEmpty =>
            val (taker, rest) = takers.dequeue
            State(capacity, queue, rest, offerers) -> taker.complete(i).void
          /* If queue size < capacity */
          case State(capacity, queue, takers, offerers) if queue.size < capacity =>
            State(capacity, queue.enqueue(i), takers, offerers) -> Async[F].unit
          /* Otherwise */
          case State(capacity, queue, takers, offerers) =>
            State(capacity, queue, takers, offerers.enqueue(i -> offerer)) -> offerer.get
        }.flatten
      }

    for {
      i <- counter.getAndUpdate(_ + 1) // Update the item
      _ <- produce(i)
      _ <- Console[F].println(s"Producer $id product item: $i")
      _ <- producer(id, counter, state)
    } yield ()
  }
}
