package controllers

import javax.inject.Inject

import models._

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MunicipioController @Inject()(municipioService: MunicipioRepository, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {
    def todos: Action[AnyContent] = Action.async { request =>
        municipioService.todos().map { municipios => 
            Ok(Json.toJson(municipios))
        }
    }

    def buscarpordepartamento(depa_id: Long): Action[AnyContent] = Action.async { request =>
        municipioService.buscarPorDepartamento(depa_id).map { municipios =>
            Ok(Json.toJson(municipios))
        }
    }
}