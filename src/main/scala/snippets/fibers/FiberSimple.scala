package snippets.fibers
import cats.effect._

object FiberSimple extends IOApp.Simple {
  override def run: IO[Unit] =  {
    IO.println("Hello") flatMap { _ =>
      IO.println("World")
    }
  }
}
