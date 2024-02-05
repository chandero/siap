package models

import javax.inject.{ Inject, Singleton }

import org.joda.time.DateTime

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, date, double, scalar, flatten}
import anorm.JodaParameterMetaData._
import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}


case class OrdenTrabajoHistorico(
  orhi_id: Option[Long],
  orhi_fecharecepcion: Option[DateTime],
  orhi_direccion: Option[String],
  orhi_objeto: Option[String],
  orhi_fechasolucion: Option[DateTime],
  barr_id: Option[Long],
  usua_id: Option[Long],
  empr_id: Option[Long],
  orhi_estado: Option[Int],
  orhi_descripcion: Option[String],
  reti_id: Option[Long],
  orhi_consecutivo: Option[String],
  orhi_fechadigitacion: Option[DateTime],
  orhi_fechamodificacion: Option[DateTime]
)

object OrdenTrabajoHistorico {
  val _set = {
    get[Option[Long]]("orhi_id") ~
    get[Option[DateTime]]("orhi_fecharecepcion") ~
    get[Option[String]]("orhi_direccion") ~
    get[Option[String]]("orhi_objeto") ~
    get[Option[DateTime]]("orhi_fechasolucion") ~
    get[Option[Long]]("barr_id") ~
    get[Option[Long]]("usua_id") ~
    get[Option[Long]]("empr_id") ~
    get[Option[Int]]("orhi_estado") ~
    get[Option[String]]("orhi_descripcion") ~
    get[Option[Long]]("reti_id") ~
    get[Option[String]]("orhi_consecutivo") ~
    get[Option[DateTime]]("orhi_fechadigitacion") ~
    get[Option[DateTime]]("orhi_fechamodificacion") map {
      case orhi_id ~ orhi_fecharecepcion ~ orhi_direccion ~ orhi_objeto ~ orhi_fechasolucion ~ barr_id ~ usua_id ~ empr_id ~ orhi_estado ~ orhi_descripcion ~ reti_id ~ orhi_consecutivo ~ orhi_fechadigitacion ~ orhi_fechamodificacion =>
        OrdenTrabajoHistorico(
          orhi_id,
          orhi_fecharecepcion,
          orhi_direccion,
          orhi_objeto,
          orhi_fechasolucion,
          barr_id,
          usua_id,
          empr_id,
          orhi_estado,
          orhi_descripcion,
          reti_id,
          orhi_consecutivo,
          orhi_fechadigitacion,
          orhi_fechamodificacion
        )
    }
  }
}

case class OrdenTrabajoHistoricoAapMaterial(
	oham_id: Option[Long],
	orhi_id: Option[Long],
	elhi_id: Option[Long],
	usua_id: Option[Long],
	empr_id: Option[Long],
	oham_estado: Option[Long],
	oham_fecha: Option[DateTime],
	oham_codigo_instalado: Option[String],
	oham_cantidad_instalado: Option[Double],
	oham_codigo_retirado: Option[Double],
	oham_cantidad_retirado: Option[Double],
	oham_valor_unitario: Option[Double],
	oham_valor_total: Option[Double]
)

case class ElementoHistorico(
	elhi_id: Option[Long],
	elhi_descripcion: Option[String],
	elhi_codigo: Option[String],
	tiel_id: Option[Long],
	usua_id: Option[Long],
	empr_id: Option[Long],
	elhi_estado: Option[Int],
	ucap_id: Option[Long],
	elhi_unidad: Option[String]
)

case class ElementoHistoricoPrecio(
	elhi_id: Option[Long],
	elhipr_anho: Option[Long],
	elhipr_precio: Option[Double],
	elhipr_unidad: Option[String],  
)

@Singleton
class OrdenTrabajoHistoricoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def getOrders() = Future { 
    val _list = db.withConnection { implicit connection => SQL("""SELECT * FROM siap.ordentrabajo_historico o ORDER BY o.orhi_fechasolucion ASC""").as(OrdenTrabajoHistorico._set *) }
    _list.toList
  }

  def saveOrder(ordenTrabajoHistorico: OrdenTrabajoHistorico, empr_id: Long, usua_id: Long): Future[Long] = {
    Future[Long] {
      db.withConnection { implicit connection =>
        val id: Option[Long] = SQL(
          """
          INSERT INTO siap.ordentrabajohistorico (
            orhi_fecharecepcion, orhi_direccion, orhi_objeto, orhi_fechasolucion, barr_id, usua_id, empr_id, orhi_estado, orhi_descripcion, reti_id, orhi_consecutivo, orhi_fechadigitacion, orhi_fechamodificacion
          ) VALUES (
            {orhi_fecharecepcion}, {orhi_direccion}, {orhi_objeto}, {orhi_fechasolucion}, {barr_id}, {usua_id}, {empr_id}, {orhi_estado}, {orhi_descripcion}, {reti_id}, {orhi_consecutivo}, {orhi_fechadigitacion}, {orhi_fechamodificacion}
          )
          """
        ).on(
          'orhi_fecharecepcion -> ordenTrabajoHistorico.orhi_fecharecepcion,
          'orhi_direccion -> ordenTrabajoHistorico.orhi_direccion,
          'orhi_objeto -> ordenTrabajoHistorico.orhi_objeto,
          'orhi_fechasolucion -> ordenTrabajoHistorico.orhi_fechasolucion,
          'barr_id -> ordenTrabajoHistorico.barr_id,
          'usua_id -> usua_id,
          'empr_id -> empr_id,
          'orhi_estado -> ordenTrabajoHistorico.orhi_estado,
          'orhi_descripcion -> ordenTrabajoHistorico.orhi_descripcion,
          'reti_id -> ordenTrabajoHistorico.reti_id,
          'orhi_consecutivo -> ordenTrabajoHistorico.orhi_consecutivo,
          'orhi_fechadigitacion -> ordenTrabajoHistorico.orhi_fechadigitacion,
          'orhi_fechamodificacion -> ordenTrabajoHistorico.orhi_fechamodificacion
        ).executeInsert()
        id.get
      }
    }
  }

  def updateOrder(ordenTrabajoHistorico: OrdenTrabajoHistorico): Future[Boolean] = {
    Future[Boolean] {
      var result = false
      db.withConnection { implicit connection =>
        result = SQL(
          """
          UPDATE siap.ordentrabajohistorico SET
            orhi_fecharecepcion = {orhi_fecharecepcion},
            orhi_direccion = {orhi_direccion},
            orhi_objeto = {orhi_objeto},
            orhi_fechasolucion = {orhi_fechasolucion},
            barr_id = {barr_id},
            usua_id = {usua_id},
            empr_id = {empr_id},
            orhi_estado = {orhi_estado},
            orhi_descripcion = {orhi_descripcion},
            reti_id = {reti_id},
            orhi_consecutivo = {orhi_consecutivo},
            orhi_fechadigitacion = {orhi_fechadigitacion},
            orhi_fechamodificacion = {orhi_fechamodificacion}
          WHERE orhi_id = {orhi_id}
          """
        ).on(
          'orhi_id -> ordenTrabajoHistorico.orhi_id,
          'orhi_fecharecepcion -> ordenTrabajoHistorico.orhi_fecharecepcion,
          'orhi_direccion -> ordenTrabajoHistorico.orhi_direccion,
          'orhi_objeto -> ordenTrabajoHistorico.orhi_objeto,
          'orhi_fechasolucion -> ordenTrabajoHistorico.orhi_fechasolucion,
          'barr_id -> ordenTrabajoHistorico.barr_id,
          'usua_id -> ordenTrabajoHistorico.usua_id,
          'empr_id -> ordenTrabajoHistorico.empr_id,
          'orhi_estado -> ordenTrabajoHistorico.orhi_estado,
          'orhi_descripcion -> ordenTrabajoHistorico.orhi_descripcion,
          'reti_id -> ordenTrabajoHistorico.reti_id,
          'orhi_consecutivo -> ordenTrabajoHistorico.orhi_consecutivo,
          'orhi_fechadigitacion -> ordenTrabajoHistorico.orhi_fechadigitacion,
          'orhi_fechamodificacion -> ordenTrabajoHistorico.orhi_fechamodificacion
        ).executeUpdate() > 0
      }
      result
    }
  }

  def deleteOrder(orhi_id: Long): Future[Boolean] = {
    Future[Boolean] {
      var result = false
      db.withConnection { implicit connection =>
        result = SQL(
          """
          UPDATE siap.ordentrabajohistorico SET orhi_estado = 99 WHERE orhi_id = {orhi_id}
          """
        ).on(
          'orhi_id -> orhi_id
        ).executeUpdate() > 0
      }
      result
    }
  }
}