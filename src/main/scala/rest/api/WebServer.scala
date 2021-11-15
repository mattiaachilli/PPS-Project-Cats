package rest.api

import cats.effect._
import cats.implicits.toSemigroupKOps
import org.http4s.HttpApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import rest.api.routes.{ActorRoutes, DirectorRoutes, MovieRoutes}

object WebServer extends IOApp {
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
  def createServer(app: HttpApp[IO]): IO[ExitCode] =
    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(app)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)

  def buildHttpApp[F[_] : Async](moviesRepository: MoviesRepository[F]): HttpApp[F] =
    (MovieRoutes.route(moviesRepository)
      <+> DirectorRoutes.route(moviesRepository)
      <+> ActorRoutes.route(moviesRepository)).orNotFound

  override def run(args: List[String]): IO[ExitCode] = for {
    repository <- MoviesRepository.createWithSeedData[IO]
    httpApp = buildHttpApp(repository)
    exitCode <- createServer(httpApp)
  } yield exitCode
}