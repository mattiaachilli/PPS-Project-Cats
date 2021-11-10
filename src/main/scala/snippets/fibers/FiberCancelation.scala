package snippets.fibers

import cats.effect.{IO, IOApp}

import scala.concurrent.duration.DurationInt

object FiberCancelation extends IOApp.Simple {
  override val run: IO[Unit] = {
    lazy val loop: IO[Unit] = IO.println("Hello, World!") >> loop

    loop.timeout(5.seconds) // Interrupt execution of fiber
  }
}
