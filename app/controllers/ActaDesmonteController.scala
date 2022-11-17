package controllers

import javax.inject.Inject
import java.io.{OutputStream, ByteArrayOutputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.Base64

import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration
import play.filters.csrf._
import play.filters.csrf.CSRF.Token

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import pdi.jwt.JwtSession

import utilities._

import dto._
import java.text.SimpleDateFormat

@Singleton
class ActaDesmonteController @Inject()(
    actaService: ActaDesmonteRepository,
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )
  def todos(): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      // val json = request.body.asJson.get
      // val page_size = (json \ "page_size").as[Long]
      // val current_page = (json \ "current_page").as[Long]
      // val orderby = (json \ "orderby").as[String]
      // val filter = (json \ "filter").as[QueryDto]
      // var filtro = Utility.procesarFiltrado(filter)
      // if (filtro == "()") {
      //  filtro = ""
      // }
      val empr_id = Utility.extraerEmpresa(request)
      val total = actaService.cuenta(empr_id.get)
      actaService
        // .todos(page_size, current_page, empr_id.get, orderby, filtro)
        .todos(empr_id.get)
        .map { actas =>
          Ok(write(new ResultDto[ActaDesmonte](actas, total)))
        }
    }

  def actas(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      actaService.todos(empr_id.get).map { actas =>
        Ok(write(actas))
      }
  }

  def crearActas(fecha_inicial: Long,
      fecha_final: Long,
      tireuc_id: Int
    ) = authenticatedUserAction.async { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        val result = actaService.ActaDesmonteGenerador(fecha_inicial, fecha_final, tireuc_id, empr_id.get, usua_id.get)
        Future.successful(Ok(write(result)))
    }

  def ActaDesmonteXls(acde_id: Long) =
    authenticatedUserAction.async { implicit request =>
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val response = actaService.ActaDesmonteXls(
        acde_id,
        empr_id.get,
        usua_id.get
      )
      if (response._1 > 0) {
        val filename = "Acta_de_Desmonte_No." + response._1 + ".xlsx"
        val attach = "attachment; filename=" + filename
        Future.successful(
          Ok(response._2)
            .as(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            .withHeaders("Content-Disposition" -> attach)
        )
      } else {
        Future.successful(NotFound(Json.toJson("false")))
      }
    }
}