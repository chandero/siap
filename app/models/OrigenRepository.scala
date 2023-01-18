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

case class Origen(orig_id: Option[Long], 
                  orig_descripcion: String, 
                  orig_estado: Int, 
                  usua_id: Long)

object Origen {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val origenWrites = new Writes[Origen] {
        def writes(origen: Origen) = Json.obj(
           "orig_id" -> origen.orig_id,
           "orig_descripcion" -> origen.orig_descripcion,
           "orig_estado" -> origen.orig_estado,
           "usua_id" -> origen.usua_id
        )
    }

    implicit val origenReads: Reads[Origen] = (
        (__ \ "orig_id").readNullable[Long] and
        (__ \ "orig_descripcion").read[String] and
        (__ \ "orig_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(Origen.apply _)
}

class OrigenRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    *  Parsear un Origen desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("origen.orig_id") ~
        get[String]("origen.orig_descripcion") ~ 
        get[Int]("origen.orig_estado") ~
        get[Long]("origen.usua_id") map {
            case orig_id ~ orig_descripcion ~ orig_estado ~ usua_id => Origen(orig_id, orig_descripcion, orig_estado, usua_id)
        }
    }

    /**
    * Recuperar un Origen por su orig_id
    * @param orig_id: Long
    */
    def buscarPorId(orig_id: Long) : Option[Origen] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.origne WHERE orig_id = {orig_id} ").
            on(
                'orig_id -> orig_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar Origen por su descripcion
    * @param orig_descripcion: String
    */
    def buscarPorDescripcion(orig_descripcion: String) : Future[Iterable[Origen]] = Future[Iterable[Origen]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.origen WHERE orig_descripcion LIKE %{orig_descripcion}% ORDER BY orig_descripcion").
            on(
                'orig_descripcion -> orig_descripcion
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los origen
    * @param page_size: Long
    * @param current_page: Long
    * @return Future[Iterable[Origen]]
    */
    def todos(page_size: Long, current_page: Long) : Future[Iterable[Origen]] = Future[Iterable[Origen]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.origen ORDER BY orig_descripcion").
            as(simple *)
        }
    }

    /**
    * Recuperar todos los origen
    * @return Future[Iterable[Origen]]
    */
    def origenes() : Future[Iterable[Origen]] = Future[Iterable[Origen]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.origen ORDER BY orig_descripcion").
            as(simple *)
        }
    }

    /**
    * Crear Origen
    * @param origen: Origen
    */
    def crear(origen: Origen) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.origen (orig_descripcion, orig_estado, usua_id) VALUES ({orig_descripcion}, {orig_estado}, {usua_id})").
            on(
                'orig_descripcion -> origen.orig_descripcion,
                'orig_estado -> origen.orig_estado,
                'usua_id -> origen.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> origen.usua_id,
                'audi_tabla -> "origen", 
                'audi_uid -> id,
                'audi_campo -> "orig_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> origen.orig_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar Origen
    * @param origen: Origen
    */
    def actualizar(origen: Origen): Boolean = {
        val origen_ant: Option[Origen] = buscarPorId(origen.orig_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val result: Boolean = SQL("UPDATE siap.origen SET orig_descripcion = {orig_descripcion}, orig_estado = {orig_estado}, usua_id = {usua_id} WHERE orig_id = {orig_id}").
            on(
                'orig_id -> origen.orig_id,
                'orig_descripcion -> origen.orig_descripcion,
                'orig_estado -> origen.orig_estado,
                'usua_id -> origen.usua_id
            ).executeUpdate() > 0
 
            if (origen_ant != None){
                if (origen_ant.get.orig_descripcion != origen.orig_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> origen.usua_id,
                    'audi_tabla -> "origen", 
                    'audi_uid -> origen.orig_id,
                    'audi_campo -> "orig_descripcion", 
                    'audi_valorantiguo -> origen_ant.get.orig_descripcion,
                    'audi_valornuevo -> origen.orig_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (origen_ant.get.orig_estado != origen.orig_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> origen.usua_id,
                    'audi_tabla -> "origen", 
                    'audi_uid -> origen.orig_id,
                    'audi_campo -> "orig_estado", 
                    'audi_valorantiguo -> origen_ant.get.orig_estado,
                    'audi_valornuevo -> origen.orig_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }  
                if (origen_ant.get.usua_id != origen.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> origen.usua_id,
                    'audi_tabla -> "origen", 
                    'audi_uid -> origen.orig_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> origen_ant.get.usua_id,
                    'audi_valornuevo -> origen.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                
            }

            result
        }
    }

    /**
    * Eliminar un Origen
    * @param origen: Origen
    */
    def borrar(orig_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:Long = SQL("UPDATE siap.origen SET orig_estado = 9 WHERE orig_id = {orig_id}").
            on(
                'orig_id -> orig_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "origen", 
                    'audi_uid -> orig_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

}