package snippets

import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite

class MultipleAssertionsExampleSuiteTests extends CatsEffectSuite {
  test("multiple IO-assertions should be composed") {
    assertIO(IO(42), 42) *>
      assertIO(IO("Hello World!"), "Hello World!")
  }

  test("multiple IO-assertions should be composed via for-comprehension") {
    for {
      _ <- assertIO(IO(42), 42)
      _ <- assertIO(IO(42).map(_ * 2), 84)
    } yield ()
  }
}
