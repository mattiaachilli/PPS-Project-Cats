package Snippets.MonadCancel

import cats.effect.MonadCancel
import cats.effect.std.Semaphore
import cats.effect.syntax.all._
import cats.syntax.all._

object Guarded {


  def guarded[F[_], R, A, E](s: Semaphore[F], alloc: F[R])(use: R => F[A])(release: R => F[Unit])
                            (implicit F: MonadCancel[F, E]): F[A] =
    F uncancelable { poll =>
      for {
        r <- alloc

        _ <- poll(s.acquire).onCancel(release(r))
        releaseAll = s.release >> release(r)

        a <- poll(use(r)).guarantee(releaseAll)
      } yield a
    }
}
