package controllers

import javax.inject.Inject

import models._

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DepartamentoController @Inject()(depaService: DepartamentoRepository,cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {
    def todos() = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        depaService.todos().map { departamentos =>
            Ok(Json.toJson(departamentos))
        }
    }
}