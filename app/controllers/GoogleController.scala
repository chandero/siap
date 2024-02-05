// GoogleController.scala

package controllers

import javax.inject._
import play.api.Configuration
import play.api.mvc._

@Singleton
class GoogleController @Inject()(
  authenticatedUserAction: AuthenticatedUserAction,
  cc: ControllerComponents, 
  configuration: Configuration
) extends AbstractController(cc) {

  def getKey() = authenticatedUserAction  {
    val apiKey: Option[String] = configuration.getOptional[String]("google.apiKey")
    apiKey.map { apiKey =>
      Ok(apiKey)
    }.getOrElse {
      BadRequest("API Key no configurada")
    }
  }
}
 









