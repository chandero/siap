package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import pdi.jwt.JwtSession

import utilities._
import java.util.Date

@Singleton
class CodigoautorizacionController @Inject()(
    codigoService: CodigoautorizacionRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {

    implicit val formats = Serialization.formats(NoTypeHints) ++ List(DateTimeSerializer)  

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

    def informeUsoCodigoAutorizacion(fi: scala.Long, ff: scala.Long) = authenticatedUserAction.async {
        implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        codigoService.informeUsoCodigoAutorizacion(fi, ff, empr_id.get).map { informe =>
            Ok(write(informe))
        }
    }

    def informeUsoCodigoAutorizacionXlsx(fi: scala.Long, ff: scala.Long) = authenticatedUserAction.async {
        implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        val os = codigoService.informeUsoCodigoAutorizacionXls(fi, ff, empr_id.get, usua_id.get)
        val sdf = new java.text.SimpleDateFormat("yyyyMMdd")
        val filename = "informe_uso_codigo_autorizacion_" + sdf.format(new Date(fi)) + "_" + sdf.format(new Date(ff)) + ".xlsx"
        val attach = "attachment; filename=" + filename
        Future.successful(Ok(os)
            .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .withHeaders("Content-Disposition" -> attach)
        )
    }
}