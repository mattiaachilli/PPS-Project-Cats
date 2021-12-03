package rest.api

import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Request, _}
import rest.api.Entities.{Actor, Director, Movie}
import rest.api.WebServer.buildHttpApp

class RestAPITest extends CatsEffectSuite {
  private val TITANIC_ID = "9127c44c-7c72-44a8-8bcb-088e3b659eca"
  private val TOP_GUN_ID = "af9ce051-8541-42a9-88c4-e36d5036ad1e"
  private val movieToAdd: Movie = Movie(
    "The Terminator",
    1984,
    List(Actor("Arnold", "Schwarzenegger", 10)),
    Director("James", "Cameron", "Canadian", 44),
    List("Action", "Horror", "Thriller", "Fantasy"),
    50_000_000,
    3
  )
  private val directorUpdate: Director = Director("Steven", "Spielberg", "American", 20)
  private var client: Option[Client[IO]] = None

  override def beforeAll(): Unit = {
    val moviesRepository = MoviesRepository.createWithSeedData[IO].unsafeRunSync()
    client = Some(Client.fromHttpApp(buildHttpApp[IO](moviesRepository)))
  }

  private def getUriFromPath(path: String): Uri =
    Uri.fromString(s"http://localhost:8080/$path").toOption.get

  /* Movies */
  test("Get all movies") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("movies"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 2)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Get movie by id") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath(s"movies/$TITANIC_ID"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Get all movies by genre") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath(s"movies?genre=Adventure"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 1)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Top Gun"))
    } yield ()
  }

  test("Get all movies by actor") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("movies?actor=Leonardo%20DiCaprio"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 1)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Get all movies by director") {

    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("movies?director=James%20Cameron"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 1)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Get all movies by year") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("movies?year=1997"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 1)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Get the best movie by rating") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("movies/ratings"))
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asString.get).map(s => assert(s contains "Titanic"))
    } yield ()
  }

  test("Add a new movie") {
    val request: Request[IO] = Request(method=Method.POST, uri = getUriFromPath("movies")).withEntity(movieToAdd.asJson)
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asString.get).map(s => assert(s contains "Created movie with id"))
    } yield ()
  }

  test("Update a movie") {
    val request: Request[IO] = Request(method=Method.PUT, uri = getUriFromPath(s"movies/$TITANIC_ID"))
      .withEntity(movieToAdd.asJson)
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asString.get).map(s => assert(s contains "Successfully updated movie with id"))
    } yield ()
  }

  test("Delete a movie") {
    val request: Request[IO] = Request(method=Method.DELETE, uri = getUriFromPath(s"movies/$TITANIC_ID"))
    val requestAllMovies: Request[IO] = Request(method=Method.GET, uri = uri"/movies")
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asString.get).map(s => assert(s == "Successfully deleted movie with id " +
        "9127c44c-7c72-44a8-8bcb-088e3b659eca"))
      jsonAllMovies <- client.get.expect[Json](requestAllMovies)
      _ <- assertIO(IO(jsonAllMovies.asArray.fold(0)(_.size)), 2)
    } yield ()
  }

  /* Actors */
  test("Get all actors") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("actors"))
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asArray.fold(0)(_.size)).map(size => assert(size > 0))
    } yield ()
  }

  /* Directors */
  test("Get all directors") {
    val request: Request[IO] = Request(method=Method.GET, uri = getUriFromPath("directors"))
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asArray.fold(0)(_.size)).map(size => assert(size > 0))
    } yield ()
  }

  test("Update director into a movie") {
    val request: Request[IO] = Request(method=Method.PUT, uri = getUriFromPath(s"directors?movieId=$TOP_GUN_ID"))
      .withEntity(directorUpdate.asJson)
    for {
      json <- client.get.expect[Json](request)
      _ <- IO(json.asString.get).map(s => assert(s contains "Updated successfully"))
    } yield ()
  }
}
