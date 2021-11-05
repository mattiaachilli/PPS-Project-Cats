package Snippets.Concurrent

import cats.effect.{ExitCode, IO, IOApp, OutcomeIO}

import scala.concurrent.duration.DurationInt

object Racing extends IOApp.Simple {

  val intValue: IO[Int] = IO(1)

  implicit class Extension[A](io: IO[A]) {
    def debug: IO[A] = {
      io.map { value =>
        println(s"[${Thread.currentThread().getName}] $value")
        value
      }
    }
  }

  val valuableIO: IO[Int] = {
    IO("task starting").debug *> IO.sleep(1.second).debug *> IO("task completed").debug *> IO(1).debug
  }
  val vIO: IO[Int] = valuableIO.onCancel(IO("task: cancelled").debug.void)
  val timeout: IO[Unit] = {
    IO("timeout: starting").debug *> IO.sleep(500.millis).debug *> IO("timeout: finished").debug.void
  }

  def race(): IO[String] = {
    // The losing fiber get canceled
    val firstIO: IO[Either[Int, Unit]] = IO.race(vIO, timeout) //IO.race => IO[Either[A, B]]

    firstIO.flatMap {
      case Left(v) => IO(s"task won: $v")
      case Right(_) => IO("timeout won")
    }
  }

  val testTimeout: IO[Int] = vIO.timeout(500.millis)

  def racePair[A](ioA: IO[A], ioB: IO[A]): IO[OutcomeIO[A]] = {
    // Losing fiber not get canceled
    val pair = IO.racePair(ioA, ioB) // IO[Either[(OutcomeIO[A], FiberIO[B]), (FiberIO[A], OutcomeIO[B])]]

    pair.flatMap {
      case Left((outcomeA, fiberB)) => fiberB.cancel *> IO("first task won").debug *> IO(outcomeA).debug
      case Right((fiberA, outcomeB)) => fiberA.cancel *> IO("second task won").debug *> IO(outcomeB).debug
    }
  }

  val ioA: IO[Int] = IO.sleep(1.second).as(1).onCancel(IO("first cancelled").debug.void)
  val ioB: IO[Int] = IO.sleep(2.second).as(2).onCancel(IO("second cancelled").debug.void)


  def run: IO[Unit] = racePair(ioA, ioB).debug.void
}
