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
  orhi_reti_tipo: Option[String],
  orhi_consecutivo: Option[Int],
  orhi_fechadigitacion: Option[DateTime],
  orhi_fechamodificacion: Option[DateTime],
  orhi_material: Option[List[OrdenTrabajoHistoricoMaterial]]
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
    get[Option[String]]("orhi_reti_tipo") ~
    get[Option[Int]]("orhi_consecutivo") ~
    get[Option[DateTime]]("orhi_fechadigitacion") ~
    get[Option[DateTime]]("orhi_fechamodificacion") map {
      case orhi_id ~ orhi_fecharecepcion ~ orhi_direccion ~ orhi_objeto ~ orhi_fechasolucion ~ barr_id ~ usua_id ~ empr_id ~ orhi_estado ~ orhi_descripcion ~ reti_id ~ orhi_reti_tipo ~ orhi_consecutivo ~ orhi_fechadigitacion ~ orhi_fechamodificacion =>
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
          orhi_reti_tipo,
          orhi_consecutivo,
          orhi_fechadigitacion,
          orhi_fechamodificacion,
          None
        )
    }
  }
}

case class OrdenTrabajoHistoricoMaterial(
	othm_id: Option[Long],
	orhi_id: Option[Long],
	elhi_id: Option[Long],
	usua_id: Option[Long],
	empr_id: Option[Long],
	othm_estado: Option[Long],
	othm_cantidad: Option[Double],
	othm_valor_unitario: Option[Double],
	othm_valor_total: Option[Double],
  othm_unidad: Option[String],
  othm_item: Option[Int],
)

object OrdenTrabajoHistoricoMaterial {
  val _set = {
    get[Option[Long]]("othm_id") ~
    get[Option[Long]]("orhi_id") ~
    get[Option[Long]]("elhi_id") ~
    get[Option[Long]]("usua_id") ~
    get[Option[Long]]("empr_id") ~
    get[Option[Long]]("othm_estado") ~
    get[Option[Double]]("othm_cantidad") ~
    get[Option[Double]]("othm_valor_unitario") ~
    get[Option[Double]]("othm_valor_total") ~
    get[Option[String]]("othm_unidad") ~
    get[Option[Int]]("othm_item") map {
      case othm_id ~ orhi_id ~ elhi_id ~ usua_id ~ empr_id ~ othm_estado ~ othm_cantidad ~ othm_valor_unitario ~ othm_valor_total ~ othm_unidad ~ othm_item =>
        OrdenTrabajoHistoricoMaterial(
          othm_id,
          orhi_id,
          elhi_id,
          usua_id,
          empr_id,
          othm_estado,
          othm_cantidad,
          othm_valor_unitario,
          othm_valor_total,
          othm_unidad,
          othm_item
        )
    }
  }
}

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

object ElementoHistorico {
  val _set = {
    get[Option[Long]]("elhi_id") ~
    get[Option[String]]("elhi_descripcion") ~
    get[Option[String]]("elhi_codigo") ~
    get[Option[Long]]("tiel_id") ~
    get[Option[Long]]("usua_id") ~
    get[Option[Long]]("empr_id") ~
    get[Option[Int]]("elhi_estado") ~
    get[Option[Long]]("ucap_id") ~
    get[Option[String]]("elhi_unidad") map {
      case elhi_id ~ elhi_descripcion ~ elhi_codigo ~ tiel_id ~ usua_id ~ empr_id ~ elhi_estado ~ ucap_id ~ elhi_unidad =>
        ElementoHistorico(
          elhi_id,
          elhi_descripcion,
          elhi_codigo,
          tiel_id,
          usua_id,
          empr_id,
          elhi_estado,
          ucap_id,
          elhi_unidad
        )
    }
  }
}

case class ElementoHistoricoPrecio(
	elhi_id: Option[Long],
	elhipr_anho: Option[Long],
	elhipr_precio: Option[Double],
	elhipr_unidad: Option[String],  
)

object ElementoHistoricoPrecio {
  val _set = {
    get[Option[Long]]("elhi_id") ~
    get[Option[Long]]("elhipr_anho") ~
    get[Option[Double]]("elhipr_precio") ~
    get[Option[String]]("elhipr_unidad") map {
      case elhi_id ~ elhipr_anho ~ elhipr_precio ~ elhipr_unidad =>
        ElementoHistoricoPrecio(
          elhi_id,
          elhipr_anho,
          elhipr_precio,
          elhipr_unidad
        )
    }
  }
}

@Singleton
class OrdenTrabajoHistoricoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def getOrden(orhi_id: Long) = Future { 
    val _orden = db.withConnection {
      implicit connection => 
        var orden = SQL("""SELECT * FROM siap.ordentrabajo_historico o WHERE o.orhi_id = {orhi_id}""").on('orhi_id -> orhi_id).as(OrdenTrabajoHistorico._set.singleOpt)
          orden match {
            case Some(o) => {
              val _materiales = db.withConnection { 
                implicit connection => 
                  SQL("""SELECT * FROM siap.ordentrabajo_historico_material o WHERE o.orhi_id = {orhi_id}""").on('orhi_id -> orhi_id).as(OrdenTrabajoHistoricoMaterial._set *)
              }
              o.copy(orhi_material = Some(_materiales.toList))
            }
            case None => orden
          }
    }
    println("Orden: " + _orden)
    _orden
  }

  def getOrders() = Future { 
    val _list = db.withConnection { implicit connection => SQL("""SELECT * FROM siap.ordentrabajo_historico o ORDER BY o.orhi_fechasolucion ASC""").as(OrdenTrabajoHistorico._set *) }
    _list.toList
  }

  def save(ordenTrabajoHistorico: OrdenTrabajoHistorico, empr_id: Long, usua_id: Long) = {
    ordenTrabajoHistorico.orhi_id match {
      case Some(_) => updateOrder(ordenTrabajoHistorico, empr_id, usua_id)
      case None => saveOrder(ordenTrabajoHistorico, empr_id, usua_id)
    }
  }

  def saveOrder(ordenTrabajoHistorico: OrdenTrabajoHistorico, empr_id: Long, usua_id: Long): Future[Long] = {
    Future[Long] {
      db.withConnection { implicit connection =>
        val id: Option[Long] = SQL(
          """
          INSERT INTO siap.ordentrabajo_historico (
            orhi_fecharecepcion, orhi_direccion, orhi_objeto, orhi_fechasolucion, barr_id, usua_id, empr_id, orhi_estado, orhi_descripcion, reti_id, orhi_reti_tipo, orhi_consecutivo, orhi_fechadigitacion
          ) VALUES (
            {orhi_fecharecepcion}, {orhi_direccion}, {orhi_objeto}, {orhi_fechasolucion}, {barr_id}, {usua_id}, {empr_id}, {orhi_estado}, {orhi_descripcion}, {reti_id}, {orhi_reti_tipo}, {orhi_consecutivo}, {orhi_fechadigitacion}
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
          'orhi_reti_tipo -> ordenTrabajoHistorico.orhi_reti_tipo,
          'orhi_consecutivo -> ordenTrabajoHistorico.orhi_consecutivo,
          'orhi_fechadigitacion -> new DateTime()
        ).executeInsert()
        id match {
          case Some(orhi_id) =>
            ordenTrabajoHistorico.orhi_material match {
              case Some(material) => {
                for (m <- material) {
                  val id = SQL(
                    """
                    INSERT INTO siap.ordentrabajo_historico_material (
                      orhi_id, elhi_id, usua_id, empr_id, othm_estado, othm_cantidad, othm_valor_unitario, othm_valor_total, othm_unidad, othm_item
                    ) VALUES (
                      {orhi_id}, {elhi_id}, {usua_id}, {empr_id}, {othm_estado}, {othm_cantidad}, {othm_valor_unitario}, {othm_valor_total}, {othm_unidad}, {othm_item}
                    )
                    """
                  ).on(
                    'orhi_id -> orhi_id,
                    'elhi_id -> m.elhi_id,
                    'usua_id -> usua_id,
                    'empr_id -> empr_id,
                    'othm_estado -> 1,
                    'othm_cantidad -> m.othm_cantidad,
                    'othm_valor_unitario -> m.othm_valor_unitario,
                    'othm_valor_total -> m.othm_valor_total,
                    'othm_unidad -> m.othm_unidad,
                    'othm_item -> m.othm_item
                  ).executeInsert()
                }
              }
              case None => None
            }
          case None => None
        }
        id.get
      }
    }
  }

  def updateOrder(ordenTrabajoHistorico: OrdenTrabajoHistorico, empr_id: Long, usua_id: Long): Future[Boolean] = {
    Future[Boolean] {
      var result = false
      db.withConnection { implicit connection =>
        result = SQL(
          """
          UPDATE siap.ordentrabajo_historico SET
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
            orhi_reti_tipo = {orhi_reti_tipo},
            orhi_consecutivo = {orhi_consecutivo},
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
          'usua_id -> usua_id,
          'empr_id -> empr_id,
          'orhi_estado -> ordenTrabajoHistorico.orhi_estado,
          'orhi_descripcion -> ordenTrabajoHistorico.orhi_descripcion,
          'reti_id -> ordenTrabajoHistorico.reti_id,
          'orhi_reti_tipo -> ordenTrabajoHistorico.orhi_reti_tipo,
          'orhi_consecutivo -> ordenTrabajoHistorico.orhi_consecutivo,
          'orhi_fechamodificacion -> new DateTime()
        ).executeUpdate() > 0

        ordenTrabajoHistorico.orhi_material match {
          case Some(material) => {
            SQL(
              """
              DELETE FROM siap.ordentrabajo_historico_material WHERE orhi_id = {orhi_id}
              """
            ).on(
              'orhi_id -> ordenTrabajoHistorico.orhi_id
            ).executeUpdate()
            for (m <- material) {
              val id = SQL(
                """
                INSERT INTO siap.ordentrabajo_historico_material (
                  orhi_id, elhi_id, usua_id, empr_id, othm_estado, othm_cantidad, othm_valor_unitario, othm_valor_total, othm_unidad, othm_item
                ) VALUES (
                  {orhi_id}, {elhi_id}, {usua_id}, {empr_id}, {othm_estado}, {othm_cantidad}, {othm_valor_unitario}, {othm_valor_total}, {othm_unidad}, {othm_item}
                )
                """
              ).on(
                'orhi_id -> ordenTrabajoHistorico.orhi_id,
                'elhi_id -> m.elhi_id,
                'usua_id -> usua_id,
                'empr_id -> empr_id,
                'othm_estado -> 1,
                'othm_cantidad -> m.othm_cantidad,
                'othm_valor_unitario -> m.othm_valor_unitario,
                'othm_valor_total -> m.othm_valor_total,
                'othm_unidad -> m.othm_unidad,
                'othm_item -> m.othm_item
              ).executeInsert()
            }
          }
          case None => None
        
        }
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
          UPDATE siap.ordentrabajo_historico SET orhi_estado = 99 WHERE orhi_id = {orhi_id}
          """
        ).on(
          'orhi_id -> orhi_id
        ).executeUpdate() > 0
      }
      result
    }
  }

  def elementos(empr_id: Long): Future[List[ElementoHistorico]] =
    Future {
      db.withConnection { implicit connection =>
        val list = SQL(
          "SELECT * FROM siap.elemento_historico WHERE empr_id = {empr_id} ORDER BY elhi_descripcion"
        ).on(
            'empr_id -> empr_id
          )
          .as(ElementoHistorico._set *)
        list
     }
    }

  def getElementoByDescripcion(elhi_descripcion: String, empr_id: Long): Future[List[ElementoHistorico]] =
    Future {
      db.withConnection { implicit connection =>
       val _descripcion = s"${elhi_descripcion}%"
        val list = SQL(
          """SELECT * FROM siap.elemento_historico WHERE empr_id = {empr_id} and elhi_descripcion LIKE {elhi_descripcion} ORDER BY elhi_descripcion"""
        ).on(
            'empr_id -> empr_id,
            'elhi_descripcion -> _descripcion.toUpperCase
          )
          .as(ElementoHistorico._set *)
        list
     }
    }

  def getElementoByCodigo(elhi_codigo: String, empr_id: Long): Future[List[ElementoHistorico]] =
    Future {
      db.withConnection { implicit connection =>
        val list = SQL(
          """SELECT * FROM siap.elemento_historico WHERE empr_id = {empr_id} and elhi_codigo::integer = {elhi_codigo} ORDER BY elhi_descripcion"""
        ).on(
            'empr_id -> empr_id,
            'elhi_codigo -> elhi_codigo.toInt
          )
          .as(ElementoHistorico._set *)
        list
     }
    }


}