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

case class orden (
  cotr_id: Int,
  reti_descripcion: String,
  cotr_tipo_obra_tipo: String,
  cotr_consecutivo: Int
)
case class cobro_factura (
    cofa_id: Option[scala.Long],
    cofa_fecha: Option[DateTime],
    cofa_anho: Option[Int],
    cofa_periodo: Option[Int],
    cofa_factura: Option[scala.Long],
    cofa_prefijo: Option[String],
    cofa_estado: Option[Int],
    empr_id: Option[Long],
    ordenes: List[orden]
)

object orden {
  val _set = {
    get[Int]("cotr_id") ~
    get[String]("reti_descripcion") ~
    get[String]("cotr_tipo_obra_tipo") ~
    get[Int]("cotr_consecutivo") map {
      case a ~ b ~ c ~ d => orden(a,b,c,d)
    }
  }
}
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

/**
    * Recuperar total de registros
    * @param empr_id: Long
    * @return total
    */
  def cuenta(empr_id: Long, filter: String): Long = {
    db.withConnection { implicit connection =>
      var query = "SELECT COUNT(*) AS c FROM siap.cobro_factura cf WHERE cf.empr_id = {empr_id} and cf.cofa_estado <> 9 "
      if (!filter.isEmpty){
        query = query + " and " + filter
      }
      val result = SQL(query).on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    Recuperar todos los Elemento de una empresa
    @param empr_id: Long
    */
  def todos(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[Iterable[cobro_factura]] =
    Future[Iterable[cobro_factura]] {
      var _list = ListBuffer[cobro_factura]()
      db.withConnection { implicit connection =>
        var query = "SELECT * FROM siap.cobro_factura cf WHERE cf.empr_id = {empr_id} and cf.cofa_estado <> 9" 
        if (!filter.isEmpty) {
          query = query + " and " + filter
        }
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY cf.cofa_factura DESC"
        }
        query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""          
        val lista: List[cobro_factura] = SQL(
          query
        ).on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'elem_descripcion -> (filter + "%"),
            'elem_codigo -> (filter + "%")
          )
          .as(cobro_factura._set *)
        lista.map { f =>
          val _ordenes = SQL("""SELECT cotr1.cotr_id, rt1.reti_descripcion, cotr1.cotr_tipo_obra_tipo, cotr1.cotr_consecutivo FROM siap.cobro_factura_orden_trabajo cofaot1
                 INNER JOIN siap.cobro_orden_trabajo cotr1 ON cotr1.cotr_id = cofaot1.cotr_id
                 INNER JOIN siap.reporte_tipo rt1 ON rt1.reti_id =  cotr1.cotr_tipo_obra
                 WHERE cofaot1.cofa_id = {cofa_id}""").on(
                   'cofa_id -> f.cofa_id
                 ).as(orden._set *)
          val _factura = f.copy(ordenes = _ordenes)
          _list += _factura
        }
        _list.toList
      }
  }

  def ordenSinFactura(empr_id: Long): Future[List[(Int, String, String, Int)]] = Future {
    db.withConnection { implicit connection =>
      val _parser = int("cotr_id") ~ str("reti_descripcion") ~ str("cotr_tipo_obra_tipo") ~ int("cotr_consecutivo") map {
        case cotr_id ~ reti_descripcion ~ cotr_tipo_obra_tipo ~ cotr_consecutivo => (cotr_id, reti_descripcion, cotr_tipo_obra_tipo, cotr_consecutivo)
      }
      val lista = SQL(
        """SELECT cotr1.cotr_id, rt1.reti_descripcion, cotr1.cotr_tipo_obra_tipo, cotr1.cotr_consecutivo FROM siap.cobro_orden_trabajo cotr1
           LEFT JOIN siap.reporte_tipo rt1 on rt1.reti_id = cotr1.cotr_tipo_obra
           WHERE cotr1.cotr_id not in (select cfot1.cotr_id from siap.cobro_factura_orden_trabajo cfot1 inner join siap.cobro_factura cofa1 on cofa1.cofa_id = cfot1.cofa_id WHERE cofa1.cofa_estado = 1) and cotr1.empr_id = {empr_id}
           ORDER BY 2,3"""
      ).on(
          'empr_id -> empr_id
        )
        .as(_parser *)
      lista
    }
  }

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
      val sql = "INSERT INTO siap.cobro_factura (cofa_fecha, cofa_anho, cofa_periodo, cofa_factura, cofa_prefijo, cofa_estado, empr_id) VALUES ({cofa_fecha}, {cofa_anho}, {cofa_periodo}, {cofa_factura}, {cofa_prefijo}, {cofa_estado}, {empr_id})"
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
      val sql = "UPDATE siap.cobro_factura SET cofa_fecha = {cofa_fecha}, cofa_anho = {cofa_anho}, cofa_periodo = {cofa_periodo}, cofa_prefijo = {cofa_prefijo} WHERE cofa_id = {cofa_id} and empr_id = {empr_id}"
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
      SQL("DELETE FROM siap.cobro_factura_orden_trabajo WHERE cofa_id = {cofa_id}").on(
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
      val sql = "UPDATE siap.cobro_factura SET cofa_estado = {cofa_estado} WHERE cofa_id = {cofa_id} and empr_id = {empr_id}"
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