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

case class TipoRetiro(tire_id: Option[scala.Long], 
                  tire_descripcion: String, 
                  tire_estado: Int, 
                  usua_id: Long)

object TipoRetiro {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tireWrites = new Writes[TipoRetiro] {
        def writes(tiporetiro: TipoRetiro) = Json.obj(
           "tire_id" -> tiporetiro.tire_id,
           "tire_descripcion" -> tiporetiro.tire_descripcion,
           "tire_estado" -> tiporetiro.tire_estado,
           "usua_id" -> tiporetiro.usua_id
        )
    }

    implicit val origenReads: Reads[TipoRetiro] = (
        (__ \ "tire_id").readNullable[Long] and
        (__ \ "tire_descripcion").read[String] and
        (__ \ "tire_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(TipoRetiro.apply _)
}

class TipoRetiroRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    *  Parsear un TipoRetiro desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("tiporetiro.tire_id") ~
        get[String]("tiporetiro.tire_descripcion") ~ 
        get[Int]("tiporetiro.tire_estado") ~
        get[Long]("tiporetiro.usua_id") map {
            case tire_id ~ tire_descripcion ~ tire_estado ~ usua_id => TipoRetiro(tire_id, tire_descripcion, tire_estado, usua_id)
        }
    }

    /**
    * Recuperar un TipoRetiro por su tire_id
    * @param tire_id: Long
    */
    def buscarPorId(tire_id: Long) : Option[TipoRetiro] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tiporetiro WHERE tire_id = {tire_id} ").
            on(
                'tire_id -> tire_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar TipoRetiro por su descripcion
    * @param tire_descripcion: String
    */
    def buscarPorDescripcion(tire_descripcion: String) : Future[Iterable[TipoRetiro]] = Future[Iterable[TipoRetiro]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tiporetiro WHERE tire_descripcion LIKE %{tire_descripcion}% ORDER BY tire_descripcion").
            on(
                'tire_descripcion -> tire_descripcion
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los tiporetiro
    * @param page_size: Long
    * @param current_page: Long
    * @return Future[Iterable[TipoRetiro]]
    */
    def todos(page_size: Long, current_page: Long) : Future[Iterable[TipoRetiro]] = Future[Iterable[TipoRetiro]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tiporetiro ORDER BY tire_descripcion").
            as(simple *)
        }
    }

    /**
    * Recuperar todos los tiporetiro
    * @return Future[Iterable[TipoRetiro]]
    */
    def tiposretiro() : Future[Iterable[TipoRetiro]] = Future[Iterable[TipoRetiro]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.tiporetiro ORDER BY tire_id").
            as(simple *)
        }
    }

    /**
    * Crear TipoRetiro
    * @param tiporetiro: TipoRetiro
    */
    def crear(tiporetiro: TipoRetiro) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.tiporetiro (tire_descripcion, tire_estado, usua_id) VALUES ({tire_descripcion}, {tire_estado}, {usua_id})").
            on(
                'tire_descripcion -> tiporetiro.tire_descripcion,
                'tire_estado -> tiporetiro.tire_estado,
                'usua_id -> tiporetiro.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> tiporetiro.usua_id,
                'audi_tabla -> "tiporetiro", 
                'audi_uid -> id,
                'audi_campo -> "tire_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> tiporetiro.tire_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar TipoRetiro
    * @param tiporetiro: TipoRetiro
    */
    def actualizar(tiporetiro: TipoRetiro): Boolean = {
        val origen_ant: Option[TipoRetiro] = buscarPorId(tiporetiro.tire_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val result: Boolean = SQL("UPDATE siap.tiporetiro SET tire_descripcion = {tire_descripcion}, tire_estado = {tire_estado}, usua_id = {usua_id} WHERE tire_id = {tire_id}").
            on(
                'tire_id -> tiporetiro.tire_id,
                'tire_descripcion -> tiporetiro.tire_descripcion,
                'tire_estado -> tiporetiro.tire_estado,
                'usua_id -> tiporetiro.usua_id
            ).executeUpdate() > 0
 
            if (origen_ant != None){
                if (origen_ant.get.tire_descripcion != tiporetiro.tire_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tiporetiro.usua_id,
                    'audi_tabla -> "tiporetiro", 
                    'audi_uid -> tiporetiro.tire_id,
                    'audi_campo -> "tire_descripcion", 
                    'audi_valorantiguo -> origen_ant.get.tire_descripcion,
                    'audi_valornuevo -> tiporetiro.tire_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (origen_ant.get.tire_estado != tiporetiro.tire_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tiporetiro.usua_id,
                    'audi_tabla -> "tiporetiro", 
                    'audi_uid -> tiporetiro.tire_id,
                    'audi_campo -> "tire_estado", 
                    'audi_valorantiguo -> origen_ant.get.tire_estado,
                    'audi_valornuevo -> tiporetiro.tire_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }  
                if (origen_ant.get.usua_id != tiporetiro.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tiporetiro.usua_id,
                    'audi_tabla -> "tiporetiro", 
                    'audi_uid -> tiporetiro.tire_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> origen_ant.get.usua_id,
                    'audi_valornuevo -> tiporetiro.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                
            }

            result
        }
    }

    /**
    * Eliminar un TipoRetiro
    * @param tiporetiro: TipoRetiro
    */
    def borrar(tire_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:Long = SQL("UPDATE siap.tiporetiro SET tire_estado = 9 WHERE tire_id = {tire_id}").
            on(
                'tire_id -> tire_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "tiporetiro", 
                    'audi_uid -> tire_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

}