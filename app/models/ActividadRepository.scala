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

case class Actividad(acti_id:Option[Long], acti_descripcion:String, acti_estado:Int, usua_id: Long)

object Actividad {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val actividadWrites = new Writes[Actividad] {
        def writes(actividad: Actividad) = Json.obj(
            "acti_id" -> actividad.acti_id,
            "acti_descripcion" -> actividad.acti_descripcion,
            "acti_estado" -> actividad.acti_estado,
            "usua_id" -> actividad.usua_id
        )
    }

    implicit val actividadReads: Reads[Actividad] = (
        (__ \ "acti_id").readNullable[Long] and 
          (__ \ "acti_descripcion").read[String] and
          (__ \ "acti_estado").read[Int] and
          (__ \ "usua_id").read[Long]
    )(Actividad.apply _)
}

class ActividadRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    /**
    * Parsear un Actividad desde un Resultset
    */
    private val simple = {
        get[Option[Long]]("actividad.acti_id") ~
           get[String]("actividad.acti_descripcion") ~
           get[Int]("actividad.acti_estado") ~
           get[Long]("actividad.usua_id") map {
               case acti_id ~ acti_descripcion ~ acti_estado ~ usua_id => Actividad(acti_id, acti_descripcion, acti_estado, usua_id)
           }
    }

    /**
    * Recuperar un Actividad por su acti_id
    */
    def buscarPorId(acti_id: Long): Option[Actividad] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.actividad WHERE acti_id = {acti_id}").
            on(
                'acti_id -> acti_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar un Actividad por su acti_descripcion
    */
    def buscarPorDescripcion(acti_descripcion: String): Future[Iterable[Actividad]] = Future[Iterable[Actividad]] {
        db.withConnection { implicit connection =>
          SQL("SELECT * FROM siap.actividad WHERE acti_descripcion like %{acti_descripcion}%").
            on(
                'acti_descripcion -> acti_descripcion
            ).as(simple *)
        }
    }

    /**
    * Recuperar todas las Actividad
    */
    def todos(page_size:Long, current_page:Long): Future[Iterable[Actividad]] = Future[Iterable[Actividad]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.actividad WHERE acti_estado <> 9").as(simple *)
        }
    }

    /**
    * Recuperar todas las Actividad
    */
    def actividades(): Future[Iterable[Actividad]] = Future[Iterable[Actividad]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.actividad WHERE acti_estado <> 9 ORDER BY acti_descripcion ASC").as(simple *)
        }
    }

    /**
    * Crear una Actividad
    */
    def crear(actividad: Actividad) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha            
            val id: Long = SQL("INSERT INTO siap.actividad (acti_descripcion, acti_estado, usua_id) VALUES ({acti_descripcion},{acti_estado},{usua_id})").
            on(
                "acti_descripcion" -> actividad.acti_descripcion,
                "acti_estado" -> actividad.acti_estado,
                "usua_id" -> actividad.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> actividad.usua_id,
                'audi_tabla -> "actividad", 
                'audi_uid -> id,
                'audi_campo -> "acti_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> actividad.acti_descripcion,
                'audi_evento -> "I").
                executeInsert()

            id     
        }
    }
    
    /**
    * Actualizar Actividad
    */
    def actualizar(actividad:Actividad) : Boolean = {
        val actividad_ant: Option[Actividad] = buscarPorId(actividad.acti_id.get)
        db.withConnection { implicit connection => 
                val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
                val hora: LocalDate = fecha                            
                val result: Boolean = SQL("UPDATE siap.actividad SET acti_descripcion = {acti_descripcion}, acti_estado = {acti_estado}, usua_id = {usua_id} WHERE acti_id = {acti_id}").
                on(
                    'acti_descripcion -> actividad.acti_descripcion,
                    'acti_estado -> actividad.acti_estado,
                    'usua_id -> actividad.usua_id
                ).executeUpdate() > 0

                if (actividad_ant != None) {
                    if (actividad_ant.get.acti_descripcion != actividad.acti_descripcion){
                        SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                        on(
                         'audi_fecha -> fecha,
                         'audi_hora -> hora,
                         'usua_id -> actividad.usua_id,
                         'audi_tabla -> "actividad", 
                         'audi_uid -> actividad.acti_id,
                         'audi_campo -> "acti_descripcion", 
                         'audi_valorantiguo -> actividad_ant.get.acti_descripcion,
                         'audi_valornuevo -> actividad.acti_descripcion,
                         'audi_evento -> "A").
                        executeInsert()
                    }
                    if (actividad_ant.get.acti_estado != actividad.acti_estado){
                        SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                        on(
                         'audi_fecha -> fecha,
                         'audi_hora -> hora,
                         'usua_id -> actividad.usua_id,
                         'audi_tabla -> "actividad", 
                         'audi_uid -> actividad.acti_id,
                         'audi_campo -> "acti_estado", 
                         'audi_valorantiguo -> actividad_ant.get.acti_estado,
                         'audi_valornuevo -> actividad.acti_estado,
                         'audi_evento -> "A").
                        executeInsert()
                    }
                    if (actividad_ant.get.usua_id != actividad.usua_id) {
                        SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                        on(
                         'audi_fecha -> fecha,
                         'audi_hora -> hora,
                         'usua_id -> actividad.usua_id,
                         'audi_tabla -> "actividad", 
                         'audi_uid -> actividad.acti_id,
                         'audi_campo -> "usua_id", 
                         'audi_valorantiguo -> actividad_ant.get.usua_id,
                         'audi_valornuevo -> actividad.usua_id,
                         'audi_evento -> "A").
                        executeInsert()                        
                    }
                }
                result
        }
    }

    /**
    * Eliminar Actividad
    */
    def borrar(acti_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha

            val count: Long = SQL("UPDATE siap.actividad SET acti_estado = 9 WHERE acti_id = { acti_id }").
            on(
                'acti_id -> acti_id
            ).executeUpdate()
            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "actividad", 
                    'audi_uid -> acti_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0
        }
    }

    

}