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

case class TipoActivo(tiac_id: Option[scala.Long], 
                  tiac_descripcion: String, 
                  tiac_estado: Int, 
                  usua_id: Long)

object TipoActivo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tireWrites = new Writes[TipoActivo] {
        def writes(tipoactivo: TipoActivo) = Json.obj(
           "tiac_id" -> tipoactivo.tiac_id,
           "tiac_descripcion" -> tipoactivo.tiac_descripcion,
           "tiac_estado" -> tipoactivo.tiac_estado,
           "usua_id" -> tipoactivo.usua_id
        )
    }

    implicit val origenReads: Reads[TipoActivo] = (
        (__ \ "tiac_id").readNullable[Long] and
        (__ \ "tiac_descripcion").read[String] and
        (__ \ "tiac_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(TipoActivo.apply _)
}

class TipoActivoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    *  Parsear un TipoActivo desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("tiac_id") ~
        get[String]("tiac_descripcion") ~ 
        get[Int]("tiac_estado") ~
        get[Long]("usua_id") map {
            case tiac_id ~ tiac_descripcion ~ tiac_estado ~ usua_id => TipoActivo(tiac_id, tiac_descripcion, tiac_estado, usua_id)
        }
    }

    /**
    * Recuperar un TipoActivo por su tiac_id
    * @param tiac_id: Long
    */
    def buscarPorId(tiac_id: Long) : Option[TipoActivo] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tipo_activo WHERE tiac_id = {tiac_id} and tiac_estado <> 9 ").
            on(
                'tiac_id -> tiac_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar TipoActivo por su descripcion
    * @param tiac_descripcion: String
    */
    def buscarPorDescripcion(tiac_descripcion: String) : Future[Iterable[TipoActivo]] = Future[Iterable[TipoActivo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipo_activo WHERE tiac_descripcion LIKE %{tiac_descripcion}% AND tiac_estado <> 9 ORDER BY tiac_descripcion").
            on(
                'tiac_descripcion -> tiac_descripcion
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los tipoactivo
    * @param page_size: Long
    * @param current_page: Long
    * @return Future[Iterable[TipoActivo]]
    */
    def todos(page_size: Long, current_page: Long) : Future[Iterable[TipoActivo]] = Future[Iterable[TipoActivo]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tipo_activo WHERE tiac_estado <> 9 ORDER BY tiac_descripcion").
            as(simple *)
        }
    }

    /**
    * Recuperar todos los tipoactivo
    * @return Future[Iterable[TipoActivo]]
    */
    def tiposretiro() : Future[Iterable[TipoActivo]] = Future[Iterable[TipoActivo]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tipo_activo ORDER BY tiac_id").
            as(simple *)
        }
    }

    /**
    * Crear TipoActivo
    * @param tipoactivo: TipoActivo
    */
    def crear(tipoactivo: TipoActivo) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.tipo_activo (tiac_descripcion, tiac_estado, usua_id) VALUES ({tiac_descripcion}, {tiac_estado}, {usua_id})").
            on(
                'tiac_descripcion -> tipoactivo.tiac_descripcion,
                'tiac_estado -> tipoactivo.tiac_estado,
                'usua_id -> tipoactivo.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> tipoactivo.usua_id,
                'audi_tabla -> "tipo_activo", 
                'audi_uid -> id,
                'audi_campo -> "tiac_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> tipoactivo.tiac_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar TipoActivo
    * @param tipoactivo: TipoActivo
    */
    def actualizar(tipoactivo: TipoActivo): Boolean = {
        val origen_ant: Option[TipoActivo] = buscarPorId(tipoactivo.tiac_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val result: Boolean = SQL("UPDATE siap.tipo_activo SET tiac_descripcion = {tiac_descripcion}, tiac_estado = {tiac_estado}, usua_id = {usua_id} WHERE tiac_id = {tiac_id}").
            on(
                'tiac_id -> tipoactivo.tiac_id,
                'tiac_descripcion -> tipoactivo.tiac_descripcion,
                'tiac_estado -> tipoactivo.tiac_estado,
                'usua_id -> tipoactivo.usua_id
            ).executeUpdate() > 0
 
            if (origen_ant != None){
                if (origen_ant.get.tiac_descripcion != tipoactivo.tiac_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipoactivo.usua_id,
                    'audi_tabla -> "tipo_activo", 
                    'audi_uid -> tipoactivo.tiac_id,
                    'audi_campo -> "tiac_descripcion", 
                    'audi_valorantiguo -> origen_ant.get.tiac_descripcion,
                    'audi_valornuevo -> tipoactivo.tiac_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (origen_ant.get.tiac_estado != tipoactivo.tiac_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipoactivo.usua_id,
                    'audi_tabla -> "tipo_activo", 
                    'audi_uid -> tipoactivo.tiac_id,
                    'audi_campo -> "tiac_estado", 
                    'audi_valorantiguo -> origen_ant.get.tiac_estado,
                    'audi_valornuevo -> tipoactivo.tiac_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }  
                if (origen_ant.get.usua_id != tipoactivo.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipoactivo.usua_id,
                    'audi_tabla -> "tipo_activo", 
                    'audi_uid -> tipoactivo.tiac_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> origen_ant.get.usua_id,
                    'audi_valornuevo -> tipoactivo.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                
            }

            result
        }
    }

    /**
    * Eliminar un TipoActivo
    * @param tipoactivo: TipoActivo
    */
    def borrar(tiac_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:Long = SQL("UPDATE siap.tipo_activo SET tiac_estado = 9 WHERE tiac_id = {tiac_id}").
            on(
                'tiac_id -> tiac_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "tipo_activo", 
                    'audi_uid -> tiac_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

}