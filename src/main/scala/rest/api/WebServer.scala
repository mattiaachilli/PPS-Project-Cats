package rest.api

import cats.effect._
import org.http4s.server.blaze.BlazeServerBuilder

import Routes.allRoutesComplete

object WebServer extends IOApp {
  private val moviesRepository: IO[MoviesRepository[IO]] = MoviesRepository.createWithSeedData[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](runtime.compute)
      .bindHttp(8080, "localhost")
      .withHttpApp(allRoutesComplete[IO](moviesRepository))
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
