package Snippets.Fibers

import cats.effect.kernel.Outcome
import cats.effect.{ExitCode, Fiber, FiberIO, IO, IOApp}

import scala.concurrent.duration.DurationInt

object FiberExample extends IOApp {

  val intValue: IO[Int] = IO(1)
  val stringValue: IO[String] = IO("Scala")

  implicit class Extension[A](io: IO[A]) {
    def debug: IO[A] = {
      io.map { value =>
        println(s"[${Thread.currentThread().getName}] $value")
        value
      }
    }
  }

  def sameThread(): IO[Unit] = for {
    _ <- intValue.debug
    _ <- stringValue.debug
  } yield ()

  val fiber: IO[Fiber[IO, Throwable, Int]] = intValue.debug.start

  def differentThread(): IO[Unit] = {
    for {
      _ <- fiber
      _ <- stringValue.debug
    } yield ()
  }

  def runOnAnotherTread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] = {
    for {
      fib <- io.start // fiber
      result <- fib.join
      /*
        1 - success(IO(value))
        2 - errored(e)
        3 - cancelled
       */
    } yield result
  }

  def throwOnAnotherThread(): IO[Outcome[IO, Throwable, Int]] = {
    for {
      fib <- IO.raiseError[Int](new RuntimeException("Error")).start
      result <- fib.join
    } yield result
  }

  def cancelOnAnotherThread(): IO[Outcome[IO, Throwable, String]] = {
    val task = IO("starting").debug *> IO.sleep(1.second) *> IO("done").debug
    for {
      fib <- task.start
      _ <- IO.sleep(500.millis) *> IO("cancelling").debug
      _ <- fib.cancel
      result <- fib.join
    } yield result
  }

  override def run(args: List[String]): IO[ExitCode] = {
//    cancelOnAnotherThread().debug.as(ExitCode.Success)
    cancelOnAnotherThread().debug.as(ExitCode.Success)
  }
}
