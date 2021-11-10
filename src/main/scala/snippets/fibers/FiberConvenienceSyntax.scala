package snippets.fibers

import cats.effect.{IO, IOApp}

object FiberConvenienceSyntax extends IOApp.Simple { // Main fiber like Main Thread
  override def run: IO[Unit] = IO.println("Hello") >> IO.println("World") // Or *>
}
