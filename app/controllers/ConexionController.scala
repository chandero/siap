package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import org.joda.time.DateTime

import pdi.jwt.JwtSession

import utilities._

@Singleton
class ConexionController @Inject()(conexionService: ConexionRepository, authenticatedUserAction: AuthenticatedUserAction, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def conexiones() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    conexionService.conexiones().map { conexiones =>
      Ok(Json.toJson(conexiones))
    }
  }    
}