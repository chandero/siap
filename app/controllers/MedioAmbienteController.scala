package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import java.io.{OutputStream, ByteArrayOutputStream}

import pdi.jwt.JwtSession

import utilities._

import dto._

@Singleton
class MedioAmbienteController @Inject()(
    meamService: MedioAmbienteRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] => 
    val empr_id = Utility.extraerEmpresa(request)
    meamService.todos().map { medios =>
        Ok(Json.toJson(medios))
      }
  }
}