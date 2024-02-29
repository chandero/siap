package controllers
import ActaRepository
import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.sql.Connection
import org.joda.time.DateTime
import models.ActaRepository


import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, scalar, int, double, date}
import anorm.JodaParameterMetaData._




class ActaController @Inject()(actaService: ActaRepository, cc: ControllerComponents)
(implicit ec: ExecutionContext) extends BaseController {

  private val db = dbapi.database("default") 

  def getActa91(fecha_inicial: Long, fecha_final: Long): Action[AnyContent] = Action.async { implicit request =>
    actaService.getActa91(fecha_inicial, fecha_final).map { result =>
      Ok(result)
    }
  }
}
