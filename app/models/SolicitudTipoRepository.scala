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
import org.joda.time.LocalDateTime

case class SolicitudTipo(soti_id: Option[Long], soti_descripcion: String, soti_estado: Int, usua_id: Long)

object SolicitudTipo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val stWrites = new Writes[SolicitudTipo] {
        def writes(st: SolicitudTipo) = Json.obj(
            "soti_id" -> st.soti_id,
            "soti_descripcion" -> st.soti_descripcion,
            "soti_estado" -> st.soti_estado,
            "usua_id" -> st.usua_id
        )
    }

    implicit val stReads: Reads[SolicitudTipo] = (
        (__ \ "soti_id").readNullable[Long] and
        (__ \ "soti_descripcion").read[String] and
        (__ \ "soti_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(SolicitudTipo.apply _)
}

class SolicitudTipoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un SolicitudTipo desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("soti_id") ~
        get[String]("soti_descripcion") ~ 
        get[Int]("soti_estado") ~ 
        get[Long]("usua_id") map {
            case soti_id ~ soti_descripcion ~ soti_estado ~ usua_id => SolicitudTipo(soti_id, soti_descripcion, soti_estado, usua_id)
        }
    }

    /**
    * Recuperar un SolicitudTipo dado su soti_id
    * @param soti_id: Long
    */
    def buscarPorId(soti_id: Long) : Option[SolicitudTipo] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.solicitud_tipo WHERE soti_id = {soti_id}").
            on(
                'soti_id -> soti_id
            ).
            as(simple.singleOpt)
        }
    }

   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.solicitud_tipo WHERE soti_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los SolicitudTipo activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[SolicitudTipo]] = Future[Iterable[SolicitudTipo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.solicitud_tipo WHERE soti_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los SolicitudTipo activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def solicitudtipos(): Future[Iterable[SolicitudTipo]] = Future[Iterable[SolicitudTipo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.solicitud_tipo WHERE soti_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear SolicitudTipo
    * @param st: SolicitudTipo
    */
    def crear(st: SolicitudTipo) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.solicitud_tipo (soti_descripcion, soti_estado, usua_id) VALUES ({soti_descripcion},{soti_estado},{usua_id})").
            on(
                'soti_descripcion -> st.soti_descripcion,
                'soti_estado -> st.soti_estado,
                'usua_id -> st.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> st.usua_id,
                'audi_tabla -> "solicitud_tipo", 
                'audi_uid -> id,
                'audi_campo -> "soti_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> st.soti_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar SolicitudTipo
    * @param st: SolicitudTipo
    */
    def actualizar(st: SolicitudTipo) : Boolean = {
        val st_ant: Option[SolicitudTipo] = buscarPorId(st.soti_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.solicitud_tipo SET soti_descripcion = {soti_descripcion}, soti_estado = {soti_estado}, usua_id = {usua_id} WHERE soti_id = {soti_id}").
            on(
                'soti_id -> st.soti_id,
                'soti_descripcion -> st.soti_descripcion,
                'soti_estado -> st.soti_estado,
                'usua_id -> st.usua_id
            ).executeUpdate() > 0

            if (st_ant != None){
                if (st_ant.get.soti_descripcion != st.soti_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> st.usua_id,
                    'audi_tabla -> "solicitud_tipo", 
                    'audi_uid -> st.soti_id,
                    'audi_campo -> "soti_descripcion", 
                    'audi_valorantiguo -> st_ant.get.soti_descripcion,
                    'audi_valornuevo -> st.soti_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (st_ant.get.soti_estado != st.soti_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> st.usua_id,
                    'audi_tabla -> "solicitud_tipo", 
                    'audi_uid -> st.soti_id,
                    'audi_campo -> "soti_estado", 
                    'audi_valorantiguo -> st_ant.get.soti_estado,
                    'audi_valornuevo -> st.soti_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }
            result
        }
    }

    /**
    * Elimnar SolicitudTipo
    * @param st: SolicitudTipo
    */
    def borrar(soti_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.st SET soti_estado = 9 WHERE soti_id = {soti_id}").
            on(
                'soti_id -> soti_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud_tipo", 
                    'audi_uid -> soti_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}