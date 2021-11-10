package rest.api

import cats.effect.Sync
import rest.api.Entities._

import java.util.UUID

/*
  Case class where are stored data
 */
case class MoviesRepository[F[_]: Sync](private val movies: Map[String, Movie],
                                  private val directors: List[Director]) {
  private val makeId: F[String] = Sync[F].delay(UUID.randomUUID().toString)

  def getAllMovies: F[List[Movie]] = {
    Sync[F].delay(movies.values.toList)
  }

  def findMovieById(id: UUID): F[Option[Movie]] = {
    Sync[F].delay(movies.get(id.toString))
  }

  def findMoviesByDirector(director: String): F[List[Movie]] = director.split(" ") match {
    case arr: Array[String] if arr.length == 2 =>
      Sync[F].delay(movies.values.filter {
        case movie if movie.director.firstName == arr(0) && movie.director.lastName == arr(1) => true
        case _ => false
      }.toList)
    case _ => Sync[F].delay(List())
  }

  def findMoviesByYear(year: Int): F[List[Movie]] = year match {
    case year if movies.filter()
  }
}

private object SeedData {
  val jamesCameron: Director = Director("James", "Cameron", "Canadian", 44)
  val tonyScott: Director =  Director("Tony", "Scott", "British", 55)
  val titanic: Movie = Movie(
    "9127c44c-7c72-44a8-8bcb-088e3b659eca",
    "Titanic",
    1997,
    List(Actor("Kate", "Winslet", 57), Actor("Leonardo", "DiCaprio", 44), Actor("Billy", "Zane", 63)),
    jamesCameron,
    List("Romance", "Drama", "Epic", "Disaster"),
    2_195_170_204L,
    11
  )
  val topGun: Movie = Movie(
    "af9ce051-8541-42a9-88c4-e36d5036ad1e",
    "Top Gun",
    1986,
    List(Actor("Tom", "Cruise", 73), Actor("Kelly", "McGillis", 10), Actor("Val", "Kilmer", 25)),
    tonyScott,
    List("Action", "Romance", "Drama", "Adventure"),
    356_800_000L,
    0
  )
  def movies: Map[String, Movie] = Map(titanic.id -> titanic, topGun.id -> topGun)
  def directors: List[Director] = List(jamesCameron, tonyScott)
}

object MoviesRepository {
  def empty[F[_]: Sync]: MoviesRepository[F] = new MoviesRepository[F](Map(), List())
  def createWithSeedData[F[_]: Sync]: MoviesRepository[F] = {
    val movies: Map[String, Movie] = SeedData.movies
    val directorDetails: List[Director] = SeedData.directors

    new MoviesRepository[F](movies, directorDetails)
  }
}
