package controllers

import javax.inject.Inject

import models._

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import utilities._

@Singleton
class PerfilController  @Inject()(perfilService: PerfilRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def todos(page_size:Long, current_page:Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
     // val empr_id = Utility.extraerEmpresa(request);
     val total = perfilService.cuenta()
     perfilService.todos(page_size:Long, current_page:Long).map { usuarios =>
       Ok(Json.obj("perfiles" -> usuarios, "total" -> total))
     }
  }

  def buscarPorId(perf_id: Long) = authenticatedUserAction.async {
    val perfil = perfilService.buscarPorId(perf_id).get
    Future.successful(Ok(Json.toJson(perfil)))
  }

  def buscarPorUsuaId(usua_id: Long) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val perfil = perfilService.buscarPorUsuaId(usua_id, empr_id.get).get
    Future.successful(Ok(Json.toJson(perfil)))
  }  

}