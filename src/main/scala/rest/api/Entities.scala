package rest.api

object Entities {
  sealed trait Person {
    def firstName: String

    def lastName: String
  }

  case class Actor(firstName: String, lastName: String, movies: Int) extends Person {
    override def toString: String = s"$firstName $lastName"
  }

  case class Director(firstName: String, lastName: String, nationality: String, moviesManaged: Int) extends Person {
    override def toString: String = s"$firstName $lastName"
  }

  case class Movie(title: String, year: Int, actors: List[Actor], director: Director, genres: List[String],
                   takings: Long, oscars: Int)

  case class MovieWithId(id: String, movie: Movie)
}