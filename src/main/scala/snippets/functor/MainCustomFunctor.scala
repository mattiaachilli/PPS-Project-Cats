package snippets.functor

import cats.implicits._

object MainCustomFunctor extends App{
  print(CustomFunctor(1).map(_ + 1))
}
