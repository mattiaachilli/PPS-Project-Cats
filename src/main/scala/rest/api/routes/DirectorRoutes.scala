package rest.api.routes

import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import rest.api.MoviesRepository
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.toMessageSyntax
import rest.api.Entities.Director
import rest.api.Utility.MovieIdQueryParamMatcher

import java.util.UUID

object DirectorRoutes {
  def route[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get all directors
      case GET -> Root / "directors" =>
        moviesRepository.getAllDirectors.flatMap {
          case directors if directors.nonEmpty => Ok(directors.asJson)
          case _ => NoContent()
        }
      // Replace a director into a movie
      case req@PUT -> Root / "directors" :? MovieIdQueryParamMatcher(movieId) =>
        moviesRepository.findMovieById(UUID.fromString(movieId)).flatMap {
          case Some(_) =>
            req.decodeJson[Director]
              .flatMap(newDirector => moviesRepository.replaceDirectorFrom(movieId, newDirector))
              .flatMap(_ => Ok("Updated successfully"))
          case _ => NotFound(s"No movie id found: $movieId")
        }
    }
  }
}
