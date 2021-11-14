package rest.api

import cats.effect.Async
import cats.effect.implicits._
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher, ValidatingQueryParamDecoderMatcher}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import rest.api.Entities.{Actor, Director, Movie, MovieWithId}

import java.time.Year
import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.global

object Routes {
  /* Endpoints:
    - Movies:
      - GET all movies => Root/movies
      - GET movie from id => Root/movies/id
      - GET all movies from a director and year (optional)
      - GET all movies from a year
      - ADD a new movie
      - UPDATE a movie
      - DELETE a movie

    - Directors:
      - GET all directors
      - GET the best director
      - REPLACE a director in a movie

    - Actors:
      - GET all actors
      - GET the best actor
   */
  private object Params {
    implicit val yearQueryParamDecoder: QueryParamDecoder[Year] = QueryParamDecoder[Int].map(year => Year.of(year))

    case object YearQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")

    case object OptionalYearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

    case object MovieIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("movieId")

    case object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")
  }

  object IMDB {
    private val apiKey = "k_44ltof3p"

    def getInformationMovieUrl(title: String): String = s"https://imdb-api.com/en/API/Search/$apiKey/$title"

    def getRatingUrl(movieId: String): String = s"https://imdb-api.com/en/API/Ratings/$apiKey/$movieId"
  }

  import Params._

  private def movieRoutes[F[_] : Async](moviesRepository: F[MoviesRepository[F]]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    val client: Client[F] = JavaNetClientBuilder[F].create

    HttpRoutes.of[F] {
      // Get movies by director
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& OptionalYearQueryParamMatcher(maybeYear) =>
        moviesRepository.flatMap(_.findMoviesByDirectorName(director).flatMap {
          case movies if movies.nonEmpty =>
            maybeYear match {
              case Some(validatedYear) =>
                validatedYear.fold(
                  _ => BadRequest("Unable to parse argument year"),
                  year => Ok(movies.filter(_.movie.year == year.getValue).asJson)
                )
              case _ => Ok(movies.asJson)
            }
          case _ => NotFound(s"No movies with $director found")
        })
      // Get movies by year
      case GET -> Root / "movies" :? YearQueryParamMatcher(yearValidated) =>
        yearValidated.fold(
          _ => BadRequest("Unable to parse argument year"),
          year => {
            moviesRepository.flatMap(_.findMoviesByYear(year.getValue).flatMap {
              case movies if movies.nonEmpty => Ok(movies.asJson)
              case _ => NotFound(s"No movies with ${year.getValue} found")
            })
          }
        )
      // Get all movies
      case GET -> Root / "movies" =>
        moviesRepository.flatMap(_.getAllMovies.flatMap {
          case movies if movies.nonEmpty => Ok(movies.asJson)
          case _ => NoContent()
        })
      // Get movie by id
      case GET -> Root / "movies" / UUIDVar(movieId) =>
        moviesRepository.flatMap(_.findMovieById(movieId).flatMap {
          case Some(movie) => Ok(movie.asJson)
          case _ => NotFound(s"No movie with $movieId found")
        })
      // Add a movie
      case req @ POST -> Root / "movies" =>
        req.decodeJson[Movie]
          .flatMap(movie => moviesRepository.flatMap(_.addMovie(movie)))
          .flatMap(movie => Created(s"Created movie with id: $movie"))
      // Update a movie
      case req @ PUT -> Root / "movies" / UUIDVar(movieId) =>
        moviesRepository.flatMap(_.findMovieById(movieId).flatMap {
          case Some(_) =>
            req.decodeJson[Movie]
              .flatMap(movie => moviesRepository.flatMap(_.updateMovie(movieId, movie)))
              .flatMap(_ => Ok("Updated successfully"))
          case _ => NotFound(s"No movie with $movieId found")
        })
      // Delete a movie
      case DELETE -> Root / "movies" / UUIDVar(movieId) =>
        moviesRepository.flatMap(_.findMovieById(movieId).flatMap {
          case Some(_) => moviesRepository.flatMap(_.deleteMovie(movieId).flatMap(_ => Ok("Deleted successfully")))
          case _ => NotFound(s"No movie with $movieId found")
        })
      // Get the best movie by rating
      case GET -> Root / "movies" / "ratings" =>
        moviesRepository.flatMap(_.getAllMovies
          .flatMap(movies => movies
            .map(_.movie.title)
            .toVector
            .parTraverse
            (title => moviesRepository.flatMap(_.getRatingByMovie(title, client)))
          ).flatMap(_ => Ok("Prova")))
    }
  }

  def directorRoutes[F[_] : Async](moviesRepository: F[MoviesRepository[F]]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get all directors
      case GET -> Root / "directors" =>
        moviesRepository.flatMap(_.getAllDirectors.flatMap {
          case directors if directors.nonEmpty => Ok(directors.asJson)
          case _ => NoContent()
        })
      // Replace a director into a movie
      case req @ PUT -> Root / "directors" :? DirectorQueryParamMatcher(director) +& MovieIdQueryParamMatcher(movieId) =>
        moviesRepository.flatMap(_.findDirectorByName(director).flatMap {
          case Some(_) =>
            moviesRepository.flatMap(_.findMovieById(UUID.fromString(movieId)).flatMap {
              case Some(_) =>
                req.decodeJson[Director]
                  .flatMap(newDirector => moviesRepository.flatMap(_.replaceDirectorFrom(movieId, newDirector)))
                  .flatMap(_ => Ok("Updated successfully"))
              case _ => NotFound(s"No movie id found: $movieId")
            })
          case _ => NotFound(s"No director found for: $director")
        })
    }
  }

  def actorRoutes[F[_] : Async](moviesRepository: F[MoviesRepository[F]]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get all actors
      case GET -> Root / "actors" =>
        moviesRepository.flatMap(_.getAllActors.flatMap {
          case actors if actors.nonEmpty => Ok(actors.asJson)
          case _ => NoContent()
        })
    }
  }

  private def allRoutes[F[_] : Async](moviesRepository: F[MoviesRepository[F]]): HttpRoutes[F] = {
    /* Combine of semigroups */
    movieRoutes[F](moviesRepository) <+> directorRoutes[F](moviesRepository) <+> actorRoutes[F](moviesRepository)
  }

  def allRoutesComplete[F[_] : Async](moviesRepository: F[MoviesRepository[F]]): HttpApp[F] =
    allRoutes[F](moviesRepository).orNotFound
}
