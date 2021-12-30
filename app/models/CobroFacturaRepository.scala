package models

import javax.inject.Inject
import java.util.Calendar

import play.api.db.DBApi

import org.joda.time.DateTime

import anorm._
import anorm.SqlParser.{get, str, int, double, date, bool, scalar, flatten}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.immutable.List
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime


case class cobro_factura (
    cofa_id: Option[scala.Long],
    cofa_fecha: Option[DateTime],
    cofa_anho: Option[Int],
    cofa_periodo: Option[Int],
    cofa_factura: Option[scala.Long],
    cofa_prefijo: Option[String],
    cofa_estado: Option[Int],
    empr_id: Option[Long],
    ordenes: List[orden_trabajo_cobro]
)

object cobro_factura {
    val _set = {
        get[Option[scala.Long]]("cofa_id") ~
        get[Option[DateTime]]("cofa_fecha") ~
        get[Option[Int]]("cofa_anho") ~
        get[Option[Int]]("cofa_periodo") ~
        get[Option[scala.Long]]("cofa_factura") ~
        get[Option[String]]("cofa_prefijo") ~
        get[Option[Int]]("cofa_estado") ~
        get[Option[Long]]("empr_id") map {
            case cofa_id ~ cofa_fecha ~ cofa_anho ~ cofa_periodo ~ cofa_factura ~ cofa_prefijo ~ cofa_estado ~ empr_id => cobro_factura(
                cofa_id,
                cofa_fecha,
                cofa_anho,
                cofa_periodo,
                cofa_factura,
                cofa_prefijo,
                cofa_estado,
                empr_id,
                Nil
            )
        }
    }
}

class CobroFacturaRepository @Inject()(
    dbapi: DBApi,
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"


  def buscarPorNumero(cofa_factura: Long, empr_id: Long) = Future {
    db.withConnection { implicit connection =>
      val sql = "SELECT * FROM cobro_factura WHERE cofa_factura = {cofa_factura} and empr_id = {empr_id}"
      SQL(sql).on(
        'cofa_factura -> cofa_factura,
        'empr_id -> empr_id
      ).as(cobro_factura._set.singleOpt)
    }
  }

  def crear(factura: cobro_factura, usua_id: Long, empr_id: Long) = {
    val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
    val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())    
    db.withConnection { implicit connection =>
      val fecha = new DateTime()
      val sql = "INSERT INTO cobro_factura (cofa_fecha, cofa_anho, cofa_periodo, cofa_factura, cofa_prefijo, cofa_estado, empr_id) VALUES ({cofa_fecha}, {cofa_anho}, {cofa_periodo}, {cofa_factura}, {cofa_prefijo}, {cofa_estado}, {empr_id})"
      val _idx = SQL(sql).on(
        'cofa_fecha -> fecha,
        'cofa_anho -> factura.cofa_anho,
        'cofa_periodo -> factura.cofa_periodo,
        'cofa_factura -> factura.cofa_factura,
        'cofa_prefijo -> factura.cofa_prefijo,
        'cofa_estado -> factura.cofa_estado,
        'empr_id -> empr_id
      ).executeInsert().get
      if (_idx > 0) {
        factura.ordenes.map{ _o => 
          val _query = """INSERT INTO siap.cobro_factura_orden_trabajo (cofa_id, cotr_id, cofaot_activa) 
                          VALUES ({cofa_id}, {cotr_id}, {cofaot_activa})"""
          SQL(_query).on(
            'cofa_id -> _idx,
            'cotr_id -> _o.cotr_id,
            'cofaot_activa -> true
          ).executeInsert()
        }
      }
        SQL(
          "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
        ).on(
            'audi_fecha -> fecha,
            'audi_hora -> hora,
            'usua_id -> usua_id,
            'audi_tabla -> "cobro_factura",
            'audi_uid -> _idx,
            'audi_campo -> "cofa_id",
            'audi_valorantiguo -> "",
            'audi_valornuevo -> _idx,
            'audi_evento -> "I"
          )
          .executeInsert()  
      _idx    
    }
  }

  def actualizar(factura: cobro_factura, usua_id: Long, empr_id: Long) = {
    val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
    val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())    
    db.withConnection { implicit connection =>
      val fecha = new DateTime()
      val sql = "UPDATE cobro_factura SET cofa_fecha = {cofa_fecha}, cofa_anho = {cofa_anho}, cofa_periodo = {cofa_periodo}, cofa_prefijo = {cofa_prefijo} WHERE cofa_id = {cofa_id} and empr_id = {empr_id}"
      SQL(sql).on(
        'cofa_fecha -> factura.cofa_fecha,
        'cofa_anho -> factura.cofa_anho,
        'cofa_periodo -> factura.cofa_periodo,
        'cofa_factura -> factura.cofa_factura,
        'cofa_prefijo -> factura.cofa_prefijo,
        'cofa_id -> factura.cofa_id.get,
        'empr_id -> empr_id
      ).executeUpdate()
      // Eliminar las ordenes de trabajo anteriores
      SQL("DELETE FROM cobro_factura_orden_trabajo WHERE cofa_id = {cofa_id}").on(
        'cofa_id -> factura.cofa_id.get
      ).executeUpdate()
      //
      factura.ordenes.map{ _o => 
        val _query = """INSERT INTO siap.cobro_factura_orden_trabajo (cofa_id, cotr_id, cofaot_activa) 
                        VALUES ({cofa_id}, {cotr_id}, {cofaot_activa})"""
        SQL(_query).on(
          'cofa_id -> factura.cofa_id.get,
          'cotr_id -> _o.cotr_id,
          'cofaot_activa -> true
        ).executeInsert()
      }
      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "cobro_factura",
          'audi_uid -> factura.cofa_id.get,
          'audi_campo -> "cofa_id",
          'audi_valorantiguo -> factura.cofa_id.get,
          'audi_valornuevo -> factura.cofa_id.get,
          'audi_evento -> "U"
        )
        .executeInsert()
      true
    }
  }

  def borrar(cofa_id: Long, usua_id: Long, empr_id: Long) = {
    val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
    val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())    
    db.withConnection { implicit connection =>
      val fecha = new DateTime()
      val sql = "UPDATE cobro_factura SET cofa_estado = {cofa_estado} WHERE cofa_id = {cofa_id} and empr_id = {empr_id}"
      SQL(sql).on(
        'cofa_estado -> 9,
        'cofa_id -> cofa_id,
        'empr_id -> empr_id
      ).executeUpdate()
      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "cobro_factura",
          'audi_uid -> cofa_id,
          'audi_campo -> "cofa_estado",
          'audi_valorantiguo -> cofa_id,
          'audi_valornuevo -> 9,
          'audi_evento -> "E"
        )
        .executeInsert()
    }
  }
}