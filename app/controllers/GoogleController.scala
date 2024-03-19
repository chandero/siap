// GoogleController.scala

package controllers

import javax.inject._
import play.api.Configuration
import play.api.mvc._

import utilities.ImplicitJsonFormats
import utilities.DateTimeSerializer

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse
import net.liftweb.json.NoTypeHints
import net.liftweb.json.Serialization

@Singleton
class GoogleController @Inject()(
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

  def getKey() = authenticatedUserAction  {
    val apiKey: String = config.get[String]("google.apiKey")
    Ok(apiKey)
  }
}