package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.sql.Connection



import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, scalar, int, double, date}
import anorm.JodaParameterMetaData._

case class ActaActualizaciones(
  repoId: Int,
  fechaRecepcion: DateTime,
  fechaSolucion: Date,
  reporteTecnico: String,
  retiDescripcion: String,
  reesDescripcion: String
)
object ActaActualizaciones {
  val _result = {
  get[Option[Int]]("repo_id") ~
    get[Option[DateTime]]("fecha_recepcion") ~
    get[Option[Date]]("fecha_solucion") ~
    get[Option[String]]("reporte_tecnico") ~
    get[Option[String]]("reti_descripcion") ~
    get[Option[String]]("rees_descripcion") map {
      case repo_id ~ fecha_recepcion ~ fecha_solucion ~ reporte_tecnico ~ reti_descripcion ~ rees_descripcion =>
      ActaActualizaciones(
        repo_id,
        fecha_recepcion,
        fecha_solucion,
        reporte_tecnico,
        reti_descripcion,
        rees_descripcion
      )
    }
  }
}

class ActaActualizacionesRepository @Inject()(
    empresaService: EmpresaRepository,
    usuarioService: UsuarioRepository,
    generalService: GeneralRepository,
    dbapi: DBApi
)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default") 
  def }