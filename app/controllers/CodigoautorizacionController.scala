package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import pdi.jwt.JwtSession

import utilities._

@Singleton
class CodigoautorizacionController @Inject()(
    codigoService: CodigoautorizacionRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {


    def codigo(tipo: Int) = authenticatedUserAction.async {
            implicit request: Request[AnyContent] =>
            val empr_id = Utility.extraerEmpresa(request)
            val usua_id = Utility.extraerUsuario(request)
            val codigo = codigoService.codigo(tipo, empr_id.get, usua_id.get)
            Future.successful(Ok(Json.toJson(codigo)))            
    }

    def validar(tipo: Int, codigo: String) = authenticatedUserAction.async {
        implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        val esvalido = codigoService.validar(tipo, codigo, empr_id.get)
        Future.successful(Ok(Json.toJson(esvalido)))            
    }    
}