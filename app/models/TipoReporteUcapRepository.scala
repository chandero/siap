package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }

import org.joda.time.DateTime
import org.joda.time.LocalDate

case class TipoReporteUcap(tireuc_id: Option[scala.Long], 
                  tireuc_descripcion: String, 
                  tireuc_estado: Int, 
                  usua_id: Long)

object TipoReporteUcap {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tireWrites = new Writes[TipoReporteUcap] {
        def writes(tire: TipoReporteUcap) = Json.obj(
           "tireuc_id" -> tire.tireuc_id,
           "tireuc_descripcion" -> tire.tireuc_descripcion,
           "tireuc_estado" -> tire.tireuc_estado,
           "usua_id" -> tire.usua_id
        )
    }

    implicit val origenReads: Reads[TipoReporteUcap] = (
        (__ \ "tireuc_id").readNullable[Long] and
        (__ \ "tireuc_descripcion").read[String] and
        (__ \ "tireuc_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(TipoReporteUcap.apply _)
}

class TipoReporteUcapRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    *  Parsear un TipoReporteUcap desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("tireuc_id") ~
        get[String]("tireuc_descripcion") ~ 
        get[Int]("tireuc_estado") ~
        get[Long]("usua_id") map {
            case tireuc_id ~ tireuc_descripcion ~ tireuc_estado ~ usua_id => TipoReporteUcap(tireuc_id, tireuc_descripcion, tireuc_estado, usua_id)
        }
    }

    /**
    * Recuperar un TipoReporteUcap por su tireuc_id
    * @param tireuc_id: Long
    */
    def buscarPorId(tireuc_id: Long) : Option[TipoReporteUcap] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tipo_reporte_ucap WHERE tireuc_id = {tireuc_id} ").
            on(
                'tireuc_id -> tireuc_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar TipoRetiro por su descripcion
    * @param tireuc_descripcion: String
    */
    def buscarPorDescripcion(tireuc_descripcion: String) : Future[Iterable[TipoReporteUcap]] = Future[Iterable[TipoReporteUcap]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipo_reporte_ucap WHERE tireuc_descripcion LIKE %{tireuc_descripcion}% ORDER BY tireuc_descripcion").
            on(
                'tireuc_descripcion -> tireuc_descripcion
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los TipoReporteUcap
    * @param page_size: Long
    * @param current_page: Long
    * @return Future[Iterable[TipoReporteUcap]]
    */
    def todos(page_size: Long, current_page: Long) : Future[Iterable[TipoReporteUcap]] = Future[Iterable[TipoReporteUcap]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tipo_reporte_ucap ORDER BY tireuc_descripcion").
            as(simple *)
        }
    }

    /**
    * Recuperar todos los TipoReporteUcap
    * @return Future[Iterable[TipoReporteUcap]]
    */
    def tiposretiro() : Future[Iterable[TipoReporteUcap]] = Future[Iterable[TipoReporteUcap]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tipo_reporte_ucap ORDER BY tireuc_id").
            as(simple *)
        }
    }

    /**
    * Crear TipoReporteUcap
    * @param TipoReporteUcap: TipoReporteUcap
    */
    def crear(tr: TipoReporteUcap) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.tipo_reporte_ucap (tireuc_descripcion, tireucu_estado, usua_id) VALUES ({tireuc_descripcion}, {tireuc_estado}, {usua_id})").
            on(
                'tireuc_descripcion -> tr.tireuc_descripcion,
                'tireuc_estado -> tr.tireuc_estado,
                'usua_id -> tr.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> tr.usua_id,
                'audi_tabla -> "tipo_reporte_ucap", 
                'audi_uid -> id,
                'audi_campo -> "tireuc_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> tr.tireuc_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar TipoReporteUcap
    * @param tr: TipoReporteUcap
    */
    def actualizar(tr: TipoReporteUcap): Boolean = {
        val origen_ant: Option[TipoReporteUcap] = buscarPorId(tr.tireuc_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val result: Boolean = SQL("UPDATE siap.tipo_reporte_ucap SET tireuc_descripcion = {tireuc_descripcion}, tireuc_estado = {tireuc_estado}, usua_id = {usua_id} WHERE tireuc_id = {tireuc_id}").
            on(
                'tireuc_id -> tr.tireuc_id,
                'tireuc_descripcion -> tr.tireuc_descripcion,
                'tireuc_estado -> tr.tireuc_estado,
                'usua_id -> tr.usua_id
            ).executeUpdate() > 0
 
            if (origen_ant != None){
                if (origen_ant.get.tireuc_descripcion != tr.tireuc_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tr.usua_id,
                    'audi_tabla -> "tipo_reporte_ucap", 
                    'audi_uid -> tr.tireuc_id,
                    'audi_campo -> "tireuc_descripcion", 
                    'audi_valorantiguo -> origen_ant.get.tireuc_descripcion,
                    'audi_valornuevo -> tr.tireuc_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (origen_ant.get.tireuc_estado != tr.tireuc_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tr.usua_id,
                    'audi_tabla -> "tire", 
                    'audi_uid -> tr.tireuc_id,
                    'audi_campo -> "tireuc_estado", 
                    'audi_valorantiguo -> origen_ant.get.tireuc_estado,
                    'audi_valornuevo -> tr.tireuc_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }  
                if (origen_ant.get.usua_id != tr.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tr.usua_id,
                    'audi_tabla -> "tipo_reporte_ucap", 
                    'audi_uid -> tr.tireuc_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> origen_ant.get.usua_id,
                    'audi_valornuevo -> tr.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                
            }

            result
        }
    }

    /**
    * Eliminar un TipoReporteUcap
    * @param tr: TipoReporteUcap
    */
    def borrar(tireuc_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:Long = SQL("UPDATE siap.tipo_reporte_ucap SET tireuc_estado = 9 WHERE tireuc_id = {tireuc_id}").
            on(
                'tireuc_id -> tireuc_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "tipo_reporte_ucap", 
                    'audi_uid -> tireuc_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

}