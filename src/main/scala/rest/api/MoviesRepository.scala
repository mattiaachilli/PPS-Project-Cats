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
import cats.effect.kernel.Ref
import cats.mtl.instances.state

class MoviesRepository[F[_] : Async](private val stateRef: F[Ref[F, MoviesRepository.State]]) {

  /* Movies */
  def getAllMovies: F[List[MovieWithId]] = stateRef.flatMap(_.get)

  def findMovieById(id: UUID): F[Option[MovieWithId]] = stateRef.flatMap(_.get.map(_.find(_.id == id.toString)))

  def findMoviesByDirectorName(director: String): F[List[MovieWithId]] = stateRef.flatMap(_.get.map( state =>
      director.split(" ") match {
        case Array(name, lastName) =>
            state.filter(movieWithId => movieWithId.movie.director.firstName == name
              && movieWithId.movie.director.lastName == lastName)
        case _ => Nil
      }
    )
  )

  def addMovie(movie: Movie): F[String] = for {
      uuid <- Sync[F].delay(UUID.randomUUID().toString)
      movieToAdd = MovieWithId(uuid, movie)
      _ <- stateRef.flatMap(_.tryModify(state => (movieToAdd :: state, state)))
    } yield uuid

  def updateMovie(id: UUID, movie: Movie): F[Unit] = stateRef.flatMap(_.update(state =>
      state.find(_.id == id.toString) match {
        case Some(_) => MovieWithId(id.toString, movie) :: state.filterNot(_.id == id.toString)
        case None => state
      }
    )
  )

  def deleteMovie(id: UUID): F[Unit] = stateRef.flatMap(_.update(state =>
      state.filterNot(_.id == id.toString)
    )
  )

  def findMoviesByYear(year: Int): F[List[MovieWithId]] = stateRef.flatMap(_.get.map(_.filter(_.movie.year == year)))

  // TODO: Fix later
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
  def getAllDirectors: F[List[Director]] = stateRef.flatMap(_.get.map(_.map(_.movie.director)))

  def findDirectorByName(director: String): F[Option[Director]] = 
    getAllDirectors.map ( directors =>
      director.split(" ") match {
        case Array(name, lastName) => directors.find(d => d.firstName == name && d.lastName == lastName)
        case _ => None
      }
    )

  def replaceDirectorFrom(movieId:String, newDirector: Director): F[Unit] = stateRef.flatMap(_.update(state =>
      state.find(_.id == movieId) match {
        case Some(MovieWithId(id, movie)) =>
          val newMovie: Movie = movie.copy(director = newDirector)
          MovieWithId(id, newMovie) :: state.filterNot(_.id == movieId)
        case None => state
      }
    )
  )

  /* Actors */
  def getAllActors: F[List[Actor]] = stateRef.flatMap(_.get.map(_.flatMap(_.movie.actors)))

  def findActorByName(actor: String): F[Option[Actor]] = getAllActors.map( actors =>
    actor.split(" ") match {
      case Array(name, lastName) => actors.find(a => a.firstName == name && a.lastName == lastName)
      case _ => None
    }
  )
}

object MoviesRepository {
  type State = List[MovieWithId]

  val empty: State = List()

  private val seedState: State = List(
    MovieWithId("9127c44c-7c72-44a8-8bcb-088e3b659eca", Movie(
      "Titanic",
      1997,
      List(Actor("Kate", "Winslet", 57), Actor("Leonardo", "DiCaprio", 44), Actor("Billy", "Zane", 63)),
      Director("James", "Cameron", "Canadian", 44),
      List("Romance", "Drama", "Epic", "Disaster"),
      2_195_170_204L,
      11
    )
    ),
    MovieWithId("af9ce051-8541-42a9-88c4-e36d5036ad1e", Movie(
      "Top Gun",
      1986,
      List(Actor("Tom", "Cruise", 73), Actor("Kelly", "McGillis", 10), Actor("Val", "Kilmer", 25)),
      Director("Tony", "Scott", "British", 55),
      List("Action", "Romance", "Drama", "Adventure"),
      356_800_000L,
      0
    )
    )
  )

  def empty[F[_]: Async]: F[MoviesRepository[F]] = Sync[F].delay(new MoviesRepository[F](Ref.of[F, State](empty)))

  def createWithSeedData[F[_]: Async]: F[MoviesRepository[F]] =
    Sync[F].delay(new MoviesRepository[F](Ref.of[F, State](seedState)))
}
