package snippets

import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite

class ExampleSuiteTests extends CatsEffectSuite {
  test("tests can return IO[Unit] with assertions expressed via a map") {
    IO(42).map(it => assertEquals(it, 42))
  }

  test("alternatively, asertions can be written via assertIO") {
    assertIO(IO(42), 42)
  }

  test("map an IO") {
    assertIO(IO(42).map(_ * 2), 84)
  }
}