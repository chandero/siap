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

case class Ucap(ucap_id:Option[Long],
                ucap_descripcion: Option[String],
                ucap_estado: Option[Int],
                usua_id: Option[Long])

object Ucap {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val ucapWrites = new Writes[Ucap] {
        def writes(ucap: Ucap) = Json.obj(
            "ucap_id" -> ucap.ucap_id,
            "ucap_descripcion" -> ucap.ucap_descripcion,
            "ucap_estado" -> ucap.ucap_estado,
            "usua_id" -> ucap.usua_id
        )
    }

    implicit val ucapReads: Reads[Ucap] = (
        (__ \ "ucap_id").readNullable[Long] and
        (__ \ "ucap_descripcion").readNullable[String] and
        (__ \ "ucap_estado").readNullable[Int] and
        (__ \ "usua_id").readNullable[Long]
    )(Ucap.apply _)

}

class UcapRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un Ucap desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("ucap.ucap_id") ~
        get[Option[String]]("ucap.ucap_descripcion") ~
        get[Option[Int]]("ucap.ucap_estado") ~
        get[Option[Long]]("ucap.usua_id") map {
            case ucap_id ~ ucap_descripcion ~ ucap_estado ~ usua_id => Ucap(ucap_id, ucap_descripcion, ucap_estado, usua_id)
        }
    }

    /**
    * Recuperar un Ucap dado su ucap_id
    * @param ucap_id: Long
    */
    def buscarPorId(ucap_id:Long) : Option[Ucap] = {
        db.withConnection { implicit connection => 
            SQL("SELECT ucap_id, ucap_descripcion, ucap_estado, usua_id FROM siap.ucap WHERE ucap_id = {ucap_id} and ucap_estado <> 9").
            on(
                'ucap_id -> ucap_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar un Ucap dado su ucap_descripcion
    * @param ucap_descripcion: String
    */
    def buscarPorDescripcion(ucap_descripcion: String) : Future[Iterable[Ucap]] = Future[Iterable[Ucap]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.ucap WHERE ucap_descripcion LIKE %{ucap_descripcion}% and ucap_estado = 1 ORDER BY ucap_descripcion").
            on(
                'ucap_descripcion -> ucap_descripcion
            ).as(simple *)
        }
    }


   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.ucap WHERE ucap_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }

    /**
    * Recuperar todos los Ucap activas
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size:Long, current_page:Long): Future[Iterable[Ucap]] = Future[Iterable[Ucap]] {
        db.withConnection { implicit connection =>
            SQL("SELECT ucap_id, ucap_descripcion, ucap_estado, usua_id FROM siap.ucap WHERE ucap_estado = 1 ORDER BY ucap_id LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page                
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los Ucap activas
    */
    def ucaps(): Future[Iterable[Ucap]] = Future[Iterable[Ucap]] {
        db.withConnection { implicit connection =>
            SQL("SELECT ucap_id, ucap_descripcion, ucap_estado, usua_id FROM siap.ucap WHERE ucap_estado <> 9 ORDER BY ucap_descripcion").
            as(simple *)
        }
    }    

    /**
    * Crear Ucap
    * @param ucap: Ucap
    */
    def crear(ucap: Ucap) : Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val id: Long = SQL("INSERT INTO siap.ucap (ucap_descripcion, ucap_estado, usua_id) VALUES ({ucap_descripcion}, {ucap_estado}, {usua_id})").
            on(
               'ucap_descripcion -> ucap.ucap_descripcion,
               'ucap_estado -> ucap.ucap_estado,
               'usua_id -> ucap.usua_id 
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> ucap.usua_id,
                'audi_tabla -> "ucap", 
                'audi_uid -> id,
                'audi_campo -> "ucap_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> ucap.ucap_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar Ucap
    * @param ucap: Ucap
    */
    def actualizar(ucap: Ucap) : Boolean = {
        val ucap_ant: Option[Ucap] = buscarPorId(ucap.ucap_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
             val result: Boolean = SQL("UPDATE siap.ucap SET ucap_descripcion = {ucap_descripcion}, ucap_estado = {ucap_estado}, usua_id = {usua_id} WHERE ucap_id = {ucap_id}").
            on(
               'ucap_id -> ucap.ucap_id,
               'ucap_descripcion -> ucap.ucap_descripcion,
               'ucap_estado -> ucap.ucap_estado,
               'usua_id -> ucap.usua_id 
            ).executeUpdate() > 0

            if (ucap_ant != None){
                if (ucap_ant.get.ucap_descripcion != ucap.ucap_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ucap.usua_id,
                    'audi_tabla -> "ucap", 
                    'audi_uid -> ucap.ucap_id,
                    'audi_campo -> "ucap_descripcion", 
                    'audi_valorantiguo -> ucap_ant.get.ucap_descripcion,
                    'audi_valornuevo -> ucap.ucap_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (ucap_ant.get.ucap_estado != ucap.ucap_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ucap.usua_id,
                    'audi_tabla -> "ucap", 
                    'audi_uid -> ucap.ucap_id,
                    'audi_campo -> "ucap_estado", 
                    'audi_valorantiguo -> ucap_ant.get.ucap_estado,
                    'audi_valornuevo -> ucap.ucap_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

                if (ucap_ant.get.usua_id != ucap.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ucap.usua_id,
                    'audi_tabla -> "ucap", 
                    'audi_uid -> ucap.usua_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> ucap_ant.get.usua_id,
                    'audi_valornuevo -> ucap.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                  

            }

            result
        }
    }

    /**
    * Borrar Ucap
    * @param ucap: Ucap
    */
    def borrar(ucap_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.ucap SET ucap_estado = 9 WHERE ucap_id = {ucap_id}").
            on(
                'ucap_id -> ucap_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "ucap", 
                    'audi_uid -> ucap_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}