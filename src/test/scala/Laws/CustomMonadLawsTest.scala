package Laws

import Snippets.Monads.CustomMonad
import cats.instances.int._
import cats.laws.discipline.MonadTests
import cats.laws.discipline.SemigroupalTests.Isomorphisms
import org.scalacheck.Arbitrary
import org.scalacheck.Test.Parameters


class CustomMonadLawsTest extends munit.DisciplineSuite {
  MonadTests[Option].monad[Int, Int, Int].all.check(Parameters.default)

}
