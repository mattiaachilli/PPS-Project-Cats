package SnippetsTests

import cats.effect.{IO, Resource}
import munit.CatsEffectSuite

class SuiteLocalExampleSuite extends CatsEffectSuite {
  val myFixture: Fixture[Unit] = ResourceSuiteLocalFixture(
    "my-fixture",
    Resource.make(IO.unit)(_ => IO.unit)
  )

  override def munitFixtures = List(myFixture)

  test("first test") {
    IO(myFixture()).assertEquals(())
  }

  test("second test") {
    IO(myFixture()).assertEquals(())
  }
}
