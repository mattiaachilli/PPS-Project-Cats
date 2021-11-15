package rest.api.routes

import cats.data.Validated.{Invalid, Valid}
import cats.effect.kernel.Async
import rest.api.MoviesRepository
import org.http4s.{HttpRoutes, Response}
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.dsl.Http4sDsl
import rest.api.Utility.{DirectorQueryParamMatcher, OptionalYearQueryParamMatcher, YearQueryParamMatcher}
import cats.implicits._
import cats.effect.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.toMessageSyntax
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import rest.api.Entities.Movie

object MovieRoutes {
  def route[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    val client: Client[F] = JavaNetClientBuilder[F].create

    HttpRoutes.of[F] {
      // Get movies by director
      case GET -> Root / "movies" :? DirectorQueryParamMatcher(director) +& OptionalYearQueryParamMatcher(maybeYear) =>
        moviesRepository.findMoviesByDirectorName(director).flatMap(movies =>
          if (movies.isEmpty) NotFound(s"No director found for: $director")
          else maybeYear.fold(Ok(movies.asJson)) {
            case Invalid(_) => BadRequest("Invalid year passed")
            case Valid(year) =>
              val moviesInYear = movies.filter(_.movie.year === year.getValue)
              if (moviesInYear.nonEmpty) Ok(moviesInYear.asJson)
              else NotFound(s"No movies directed by ${director} in ${year} found")
          }
        )

      // Get movies by year
      case GET -> Root / "movies" :? YearQueryParamMatcher(maybeYear) =>
        def findMoviesBy(year: Int): F[Response[F]] = for {
          movies <- moviesRepository.findMoviesByYear(year)
          response <- if (movies.nonEmpty) Ok(movies.asJson) else NotFound(s"No movies in ${year} found")
        } yield response

        maybeYear.fold(
          _ => BadRequest("Invalid year passed"),
          year => findMoviesBy(year.getValue)
        )

      // Get all movies
      case GET -> Root / "movies" =>
        moviesRepository.getAllMovies.flatMap(movies =>
          if (movies.nonEmpty) Ok(movies.asJson) else NoContent()
        )

      // Get movie by id
      case GET -> Root / "movies" / UUIDVar(movieId) =>
        moviesRepository.findMovieById(movieId).flatMap(maybeMovie =>
          maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie => Ok(movie.asJson)
          )
        )

      // Add a movie
      case req@POST -> Root / "movies" =>
        for {
          movie <- req.decodeJson[Movie]
          uid <- moviesRepository.addMovie(movie)
          response <- Created(s"Created movie with id: ${uid}")
        } yield response

      // Update a movie
      case PUT -> Root / "movies" / UUIDVar(movieId) => // FIXME
      for {
          maybeMovie <- moviesRepository.findMovieById(movieId)
          response <- maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie =>
            moviesRepository.updateMovie(movieId, movie.movie) >> Ok(s"Successfully updated movie with id ${movie.id}")
          )
        } yield response

      // Delete a movie
      case DELETE -> Root / "movies" / UUIDVar(movieId) =>
        for {
          maybeMovie <- moviesRepository.findMovieById(movieId)
          response <- maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie =>
            moviesRepository.deleteMovie(movieId) >> Ok(s"Successfully deleted movie with id ${movie.id}")
          )
        } yield response

      // Get the best movie by rating
      case GET -> Root / "movies" / "ratings" =>
        for {
          movies <- moviesRepository.getAllMovies
          titles = movies.map(_.movie.title)
          moviesAndRatings <- titles.parTraverse(title => moviesRepository.getRatingByMovie(title, client)
            .map(r => (title, r)))
          (bestTitle, score) = moviesAndRatings.maxBy(_._2)
          response <- Ok(s"The best movie is $bestTitle with a score of $score")
        } yield response
    }
  }
}