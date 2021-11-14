package rest.api

import cats.effect.{Async, IO, Sync}
import cats.implicits._
import io.circe.Json
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import rest.api.Entities._
import rest.api.Routes.IMDB
import io.circe.optics.JsonPath._

import java.util.UUID
import scala.collection.mutable.ListBuffer

class MoviesRepository[F[_] : Async](var movies: ListBuffer[MovieWithId], var directors: ListBuffer[Director],
                                    var actors: ListBuffer[Actor]) {
  private val makeId: F[String] = Sync[F].delay(UUID.randomUUID().toString)

  /* Movie */
  def getAllMovies: F[ListBuffer[MovieWithId]] = Sync[F].delay(movies)

  def findMovieById(id: UUID): F[Option[MovieWithId]] = Sync[F].delay(movies.find(_.id == id.toString))

  def findMoviesByDirectorName(director: String): F[ListBuffer[MovieWithId]] = director.split(" ") match {
    case arr: Array[String] if arr.length == 2 =>
      Sync[F].delay(movies.filter(movieWithId => movieWithId.movie.director.firstName == arr(0)
        && movieWithId.movie.director.lastName == arr(1)))
    case _ => Sync[F].delay(ListBuffer())
  }

  def addMovie(movie: Movie): F[String] = {
    for {
      uuid <- makeId
      _ <- Sync[F].delay {
        movies += MovieWithId(uuid, movie)
      }
    } yield uuid
  }

  def updateMovie(id: UUID, movie: Movie): F[Unit] = {
    val index = movies.indexWhere(movie => movie.id == id.toString)
    movies -= movies(index)
    movies += MovieWithId(id.toString, movie)
    Sync[F].unit
  }

  def deleteMovie(id: UUID): F[Unit] = Sync[F].delay {
    val index = movies.indexWhere(movie => movie.id == id.toString)
    movies.remove(index)
  }

  def findMoviesByYear(year: Int): F[ListBuffer[MovieWithId]] = Sync[F].delay(movies.filter(_.movie.year == year))

  def getRatingByMovie(title: String, client: Client[F])(implicit jsonDecoder: EntityDecoder[F, Json] = jsonOf[F, Json])
    : F[Unit] = {
    val informationMovieUrl = IMDB.getRatingUrl(title)
    val id = for {
      json <- client.expect[Json](informationMovieUrl)
      // root.results.index(0).id.string
    } yield 1
    Sync[F].unit
  }


  /* Directors */
  def getAllDirectors: F[ListBuffer[Director]] = Sync[F].delay(directors)

  def findDirectorByName(director: String): F[Boolean] = Sync[F].delay(director.split(" ") match {
    case arr: Array[String] if arr.nonEmpty => directors.find(d => d.firstName == arr(0) && d.lastName ==
        arr(1)) match {
        case Some(_) => true
        case _ => false
      }
      case _ => false
    }
  )

  def addDirector(director: Director): F[Boolean] = Sync[F].delay(directors.find(d => d.firstName == director.firstName
    && d.lastName == director.lastName) match {
    case Some(_) => false
    case _ =>
      directors += director
      true
    })

  def updateDirector(oldDirector: String, newDirector: Director): F[Unit] = {
    val split = oldDirector.split(" ")
    val index = directors.indexWhere(d => d.firstName == split(0) && d.lastName == split(1))
    directors -= directors(index)
    directors += newDirector
    Sync[F].unit
  }

  def deleteDirector(director: String): F[Unit] = Sync[F].delay {
    val split = director.split(" ")
    val index = directors.indexWhere(d => d.firstName == split(0) && d.lastName == split(1))
    directors.remove(index)
  }

  /* Actors */
  def getAllActors: F[ListBuffer[Actor]] = Sync[F].delay(actors)

  def findActorByName(actor: String): F[Boolean] = Sync[F].delay(actor.split(" ") match {
    case arr: Array[String] if arr.nonEmpty => actors.find(a => a.firstName == arr(0) && a.lastName ==
        arr(1)) match {
        case Some(_) => true
        case _ => false
      }
      case _ => false
    }
  )

  def addActor(actor: Actor): F[Boolean] = Sync[F].delay(actors.find(a => a.firstName == actor.firstName
    && a.lastName == actor.lastName) match {
    case Some(_) => false
    case _ =>
      actors += actor
      true
  })

  def updateActor(oldActor: String, newActor: Actor): F[Unit] = {
    val split = oldActor.split(" ")
    val index = actors.indexWhere(a => a.firstName == split(0) && a.lastName == split(1))
    actors -= actors(index)
    actors += newActor
    Sync[F].unit
  }

  def deleteActor(actor: String): F[Unit] = Sync[F].delay {
    val split = actor.split(" ")
    val index = actors.indexWhere(a => a.firstName == split(0) && a.lastName == split(1))
    actors.remove(index)
  }
}

private object SeedData {
  private object Directors {
    val jamesCameron: Director = Director("James", "Cameron", "Canadian", 44)
    val tonyScott: Director = Director("Tony", "Scott", "British", 55)
  }

  private object Actors {
    val actorsTitanic: ListBuffer[Actor] = ListBuffer(Actor("Kate", "Winslet", 57), Actor("Leonardo", "DiCaprio", 44),
      Actor("Billy", "Zane", 63))
    val actorsTopGun: ListBuffer[Actor] = ListBuffer(Actor("Tom", "Cruise", 73), Actor("Kelly", "McGillis", 10),
      Actor("Val", "Kilmer", 25))
  }

  import Actors._
  import Directors._

  val titanic: Movie = Movie(
    "Titanic",
    1997,
    actorsTitanic,
    jamesCameron,
    List("Romance", "Drama", "Epic", "Disaster"),
    2_195_170_204L,
    11
  )
  val topGun: Movie = Movie(
    "Top Gun",
    1986,
    actorsTopGun,
    tonyScott,
    List("Action", "Romance", "Drama", "Adventure"),
    356_800_000L,
    0
  )

  def movies: ListBuffer[MovieWithId] = ListBuffer(MovieWithId("9127c44c-7c72-44a8-8bcb-088e3b659eca", titanic),
    MovieWithId("af9ce051-8541-42a9-88c4-e36d5036ad1e", topGun))

  def directors: ListBuffer[Director] = ListBuffer(jamesCameron, tonyScott)

  def actors: ListBuffer[Actor] = actorsTitanic ++ actorsTopGun
}

object MoviesRepository {
  def empty[F[_]: Async]: MoviesRepository[F] = new MoviesRepository[F](ListBuffer(), ListBuffer(), ListBuffer())

  def createWithSeedData[F[_]: Async]: MoviesRepository[F] = {
    val movies: ListBuffer[MovieWithId] = SeedData.movies
    val directors: ListBuffer[Director] = SeedData.directors
    val actors: ListBuffer[Actor] = SeedData.actors

    new MoviesRepository[F](movies, directors, actors)
  }
}
