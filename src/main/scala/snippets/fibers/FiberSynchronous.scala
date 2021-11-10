package snippets.fibers

import cats.effect.{IO, IOApp}

object FiberSynchronous extends IOApp.Simple {
  override val run: IO[Unit] = IO(Thread.sleep(500)) >> IO.println("Hello World") // Suspend current fiber for 500ms then print
}
