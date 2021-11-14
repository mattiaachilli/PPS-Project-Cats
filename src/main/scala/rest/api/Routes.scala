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
      - ADD a new director
      - UPDATE a director
      - DELETE a director

    - Actors:
      - GET all actors
      - GET the best actor
      - ADD an actor
      - UPDATE an actor
      - DELETE an actor
   */
  private object Params {
    implicit val yearQueryParamDecoder: QueryParamDecoder[Year] = QueryParamDecoder[Int].map(year => Year.of(year))

    case object YearQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")

    case object OptionalYearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

    case object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")
  }

  object IMDB {
    private val apiKey = "k_44ltof3p"

    def getInformationMovieUrl(title: String): String = s"https://imdb-api.com/en/API/Search/$apiKey/$title"

    def getRatingUrl(movieId: String): String = s"https://imdb-api.com/en/API/Ratings/$apiKey/$movieId"
  }

  import Params._

  private def movieRoutes[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    val client: Client[F] = JavaNetClientBuilder[F].create

    HttpRoutes.of[F] {
      // Get movies by director
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& OptionalYearQueryParamMatcher(maybeYear) =>
        val moviesByDirector: F[ListBuffer[MovieWithId]] = moviesRepository.findMoviesByDirectorName(director)
        maybeYear match {
          case Some(validatedYear) =>
            validatedYear.fold(
              _ => BadRequest("Unable to parse argument year"),
              year => {
                moviesByDirector.flatMap {
                  case movies if movies.nonEmpty => Ok(movies.filter(_.movie.year == year.getValue).asJson)
                  case _ => NotFound(s"No movies with $director found")
                }
              }
            )
          case None => moviesByDirector.flatMap {
            case movies if movies.nonEmpty => Ok(movies.asJson)
            case _ => NotFound(s"No movies with $director found")
          }
        }
      // Get movies by year
      case GET -> Root / "movies" :? YearQueryParamMatcher(yearValidated) =>
        yearValidated.fold(
          _ => BadRequest("Unable to parse argument year"),
          year => {
            val moviesByYear = moviesRepository.findMoviesByYear(year.getValue)
            moviesByYear.flatMap {
              case movies if movies.nonEmpty => Ok(movies.asJson)
              case _ => NotFound(s"No movies with ${year.getValue} found")
            }
          }
        )
      // Get all movies
      case GET -> Root / "movies" =>
        moviesRepository.getAllMovies.flatMap {
          case movies if movies.nonEmpty => Ok(movies.asJson)
          case _ => NoContent()
        }
      // Get movie by id
      case GET -> Root / "movies" / UUIDVar(movieId) =>
        moviesRepository.findMovieById(movieId).flatMap {
          case Some(movie) => Ok(movie.asJson)
          case _ => NotFound(s"No movie with $movieId found")
        }
      // Add a movie
      case req@POST -> Root / "movies" =>
        req.decodeJson[Movie]
          .flatMap(moviesRepository.addMovie)
          .flatMap(movie => Created(s"Created movie with id: $movie"))
      // Update a movie
      case req@PUT -> Root / "movies" / UUIDVar(movieId) =>
        val exist = moviesRepository.findMovieById(movieId)
        exist.flatMap {
          case Some(_) =>
            req.decodeJson[Movie]
              .flatMap(movie => moviesRepository.updateMovie(movieId, movie))
              .flatMap(_ => Ok("Updated successfully"))
          case _ => NotFound(s"No movie with $movieId found")
        }
      // Delete a movie
      case DELETE -> Root / "movies" / UUIDVar(movieId) =>
        val exist = moviesRepository.findMovieById(movieId)
        exist.flatMap {
          case Some(_) => moviesRepository.deleteMovie(movieId).flatMap(_ => Ok("Deleted successfully"))
          case _ => NotFound(s"No movie with $movieId found")
        }
      // Get the best movie by rating
      case GET -> Root / "movies" / "ratings" =>
        moviesRepository.getAllMovies
          .flatMap(movies => movies
            .map(_.movie.title)
            .toVector
            .parTraverse
            (title => moviesRepository.getRatingByMovie(title, client))
          ).flatMap(_ => Ok("Prova"))
    }
  }

  def directorRoutes[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get all directors
      case GET -> Root / "directors" =>
        moviesRepository.getAllDirectors.flatMap {
          case directors if directors.nonEmpty => Ok(directors.asJson)
          case _ => NoContent()
        }
      // Add a new director
      case req@POST -> Root / "directors" =>
        req.decodeJson[Director]
          .flatMap(moviesRepository.addDirector)
          .flatMap {
            case true => Created("Created!")
            case false => NotAcceptable("This director already exists!")
          }
      // Update a director
      case req@PUT -> Root / "directors" / director =>
        val exist = moviesRepository.findDirectorByName(director)
        exist.flatMap {
          case true =>
            req.decodeJson[Director]
              .flatMap(newDirector => moviesRepository.updateDirector(director, newDirector))
              .flatMap(_ => Ok("Updated successfully"))
          case _ => NotFound(s"No director found for: $director")
        }
      // Delete a director
      case DELETE -> Root / "directors" / director =>
        val exist = moviesRepository.findDirectorByName(director)
        exist.flatMap {
          case true => moviesRepository.deleteDirector(director).flatMap(_ => Ok("Deleted successfully"))
          case _ => NotFound(s"No director found for: $director")
        }
    }
  }

  def actorRoutes[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      // Get all actors
      case GET -> Root / "actors" =>
        moviesRepository.getAllActors.flatMap {
          case actors if actors.nonEmpty => Ok(actors.asJson)
          case _ => NoContent()
        }
      // Add a new actor
      case req@POST -> Root / "actors" =>
        req.decodeJson[Actor]
          .flatMap(moviesRepository.addActor)
          .flatMap {
            case true => Created("Created!")
            case false => NotAcceptable("This actor already exists!")
          }
      // Update an actor
      case req@PUT -> Root / "actors" / actor =>
        val exist = moviesRepository.findActorByName(actor)
        exist.flatMap {
          case true =>
            req.decodeJson[Actor]
              .flatMap(newActor => moviesRepository.updateActor(actor, newActor))
              .flatMap(_ => Ok("Updated successfully"))
          case _ => NotFound(s"No actor found for: $actor")
        }
      // Delete an actor
      case DELETE -> Root / "actors" / actor =>
        val exist = moviesRepository.findActorByName(actor)
        exist.flatMap {
          case true => moviesRepository.deleteActor(actor).flatMap(_ => Ok("Deleted successfully"))
          case _ => NotFound(s"No actor found for: $actor")
        }
    }
  }

  private def allRoutes[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    /* Combine of semigroups */
    movieRoutes[F](moviesRepository) <+> directorRoutes[F](moviesRepository) <+> actorRoutes[F](moviesRepository)
  }

  def allRoutesComplete[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpApp[F] =
    allRoutes[F](moviesRepository).orNotFound
}
