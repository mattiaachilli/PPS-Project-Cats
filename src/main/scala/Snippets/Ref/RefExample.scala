package Snippets.Ref

import cats.effect.{IO, IOApp, Ref, Sync}
import cats.implicits._

object RefExample extends IOApp.Simple {
  class Worker[F[_]](id: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {
    def start: F[Unit] = {
      for {
        v <- ref.get
        _ <- F.delay(println(s"Worker $id value $v"))
        v1 <- ref.modify(x => (x + 1, x))
        _ <- F.delay(println(s"Worker $id value $v1"))
      } yield ()
    }
  }


  override def run: IO[Unit] = {
    for {
      ref <- Ref[IO].of(0)
      worker1 = new Worker[IO](1, ref)
      worker2 = new Worker[IO](2, ref)
      worker3 = new Worker[IO](3, ref)
      _ <- (worker1.start, worker2.start, worker3.start).parTupled.void
    } yield()
  }
}
