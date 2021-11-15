package rest.api.routes

import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import rest.api.MoviesRepository
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

object ActorRoutes {
  def route[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get all actors
      case GET -> Root / "actors" =>
        moviesRepository.getAllActors.flatMap {
          case actors if actors.nonEmpty => Ok(actors.asJson)
          case _ => NoContent()
        }
    }
  }
}
