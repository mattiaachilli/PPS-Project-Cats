package snippets.fibers

import cats.effect.{IO, IOApp}
import cats.implicits.catsSyntaxTuple2Parallel

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

object FiberAsynchronous extends IOApp.Simple {
  val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  override val run: IO[Unit] = {
    IO.async_[Unit] { cb =>
      scheduler.schedule(new Runnable { // Asychronous
        def run(): Unit = cb(Right())
      }, 500, TimeUnit.MILLISECONDS)
    }
  }
}
