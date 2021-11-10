package rest.api

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{QueryParamDecoderMatcher, ValidatingQueryParamDecoderMatcher}
import org.http4s.implicits._

import java.time.Year

object Routes {
  /* Endpoints:
    - GET all movies => Root/movies
    - GET movie from id => Root/movies/id
    - GET all movies for a director
    - GET details about a director
    - POST a new director
    - POST a new film
   */
  private implicit val yearQueryParamDecoder: QueryParamDecoder[Year] = QueryParamDecoder[Int].map(year => Year.of(year))

  private object YearQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")
  private object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")

  private def movieRoutes[F[_]: Monad: Sync](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get movies by director
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) =>
        moviesRepository.findMoviesByDirector(director).flatMap {
          case movies if movies.nonEmpty => Ok(movies.asJson)
          case _ => NotFound(s"No movie found with director $director")
        }
      // Get movies by year
      // Get all movies
      case GET -> Root / "movies" =>
        moviesRepository.getAllMovies.flatMap {
          case movies if movies.nonEmpty => Ok(movies.asJson)
          case _ => Sync[F].delay(Response(status = NoContent))
        }
      // Get movie by id
      case GET -> Root / "movies" / UUIDVar(movieId) =>
        moviesRepository.findMovieById(movieId).flatMap {
          case Some(movie) => Ok(movie.asJson)
          case _ => NotFound(s"No movie with $movieId found")
        }
    }
  }

//  def directorRoutes[F[_] : Monad](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
//    val dsl = Http4sDsl[F]
//    import dsl._
//
//    HttpRoutes.of[F] {
//      case GET -> Root / "directors" / DirectorPath(director) =>
//       directorDetails.get(director) match {
//          case Some(directorDetails) => Ok(directorDetails.asJson)
//          case None => NotFound(s"No directory found for: $director")
//        }
//    }
//  }

  private def allRoutes[F[_] : Monad: Sync](moviesRepository: MoviesRepository[F]): HttpRoutes[F] =
    movieRoutes[F](moviesRepository) // <+> directorRoutes[F](moviesRepository) //Combine of semigroup

  def allRoutesComplete[F[_] : Monad: Sync](moviesRepository: MoviesRepository[F]): HttpApp[F] =
    allRoutes[F](moviesRepository).orNotFound
}
