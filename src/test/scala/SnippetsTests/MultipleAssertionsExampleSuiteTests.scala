package SnippetsTests

import cats.effect.{IO, SyncIO}
import munit.CatsEffectSuite

class MultipleAssertionsExampleSuiteTests extends CatsEffectSuite {
  test("multiple IO-assertions should be composed") {
    assertIO(IO(42), 42) *>
      assertIO_(IO.unit)
  }

  test("multiple IO-assertions should be composed via for-comprehension") {
    for {
      _ <- assertIO(IO(42), 42)
      _ <- assertIO_(IO.unit)
    } yield ()
  }

  test("multiple SyncIO-assertions should be composed") {
    assertSyncIO(SyncIO(42), 42) *>
      assertSyncIO_(SyncIO.unit)
  }

  test("multiple SyncIO-assertions should be composed via for-comprehension") {
    for {
      _ <- assertSyncIO(SyncIO(42), 42)
      _ <- assertSyncIO_(SyncIO.unit)
    } yield ()
  }
}
