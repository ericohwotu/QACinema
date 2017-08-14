package traits

import models.{Booking, Movie}

trait MovieRecActions {
  def getUserMoviesList(username: String): List[Booking]
  def getMoviesPerUser(): List[Movie]
  def getPopularGenre(): String
  def getRecommendations(): List[Movie]
}
