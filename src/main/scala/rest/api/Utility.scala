package rest.api

import org.http4s._
import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher, ValidatingQueryParamDecoderMatcher}

import java.time.Year

object Utility {
  implicit val yearQueryParamDecoder: QueryParamDecoder[Year] = QueryParamDecoder[Int].map(year => Year.of(year))

  case object YearQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")

  case object OptionalYearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")

  case object MovieIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("movieId")

  case object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")

  object IMDB {
    private val apiKey = "k_44ltof3p"

    def getInformationMovieUrl(title: String): String = s"https://imdb-api.com/en/API/Search/$apiKey/$title"

    def getRatingUrl(movieId: String): String = s"https://imdb-api.com/en/API/Ratings/$apiKey/$movieId"
  }
}
