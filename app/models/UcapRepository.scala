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

case class UcapIppIpc(
    ucap_ipp_id: Option[scala.Long],
    ucap_ipp_anho: Option[Int],
    ucap_ipp_valor: Option[Double],
    ucap_ipc_valor: Option[Double],
    empr_id: Option[scala.Long],
)                

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

object UcapIppIpc {
    val _set = {
        get[Option[scala.Long]]("ucap_ipp_id") ~
        get[Option[Int]]("ucap_ipp_anho") ~
        get[Option[Double]]("ucap_ipp_valor") ~
        get[Option[Double]]("ucap_ipc_valor") ~
        get[Option[scala.Long]]("empr_id") map {
            case ucap_ipp_id ~ ucap_ipp_anho ~ ucap_ipp_valor ~ ucap_ipc_valor ~ empr_id => UcapIppIpc(ucap_ipp_id, ucap_ipp_anho, ucap_ipp_valor, ucap_ipc_valor, empr_id)
        }
    }
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

    /**
     * Lista Ucap Valor
     * @param empr_id: Long
     */
    def listaUcapIppIpc(empr_id: scala.Long): Future[Iterable[UcapIppIpc]] = Future {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.ucap_ipp WHERE empr_id = {empr_id}").
            on(
                'empr_id -> empr_id
            ).as(UcapIppIpc._set *)
        }
    }

    /**
     * Guardar Ucap Valor
     * @param ucap_ipp_id: Ucap Ipp Id
     * @param ucap_ipp_valor: Ucap Ipp Valor
     * @param ucap_ipc_valor: Ucap Ipc Valor
     * @param empr_id: Long
     */
    def guardarUcapIpp(uipp: UcapIppIpc, usua_id: scala.Long, empr_id: scala.Long) = {
        db.withTransaction { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val ucap_ipp_ant: Option[UcapIppIpc] = SQL("SELECT * FROM siap.ucap_ipp WHERE ucap_ipp_id = {ucap_ipp_id}").
            on(
                'ucap_ipp_id -> uipp.ucap_ipp_id
            ).as(UcapIppIpc._set.singleOpt)

            val esActualizado: Boolean = SQL("UPDATE siap.ucap_ipp SET ucap_ipp_valor = {ucap_ipp_valor}, ucap_ipc_valor = {ucap_ipc_valor} WHERE ucap_ipp_id = {ucap_ipp_id}").
            on(
               'ucap_ipp_id -> uipp.ucap_ipp_id,
               'ucap_ipp_valor -> uipp.ucap_ipp_valor,
               'ucap_ipc_valor -> uipp.ucap_ipc_valor,
               'empr_id -> empr_id
            ).executeUpdate() > 0

            val esInsertado = if (!esActualizado) {
                SQL("""INSERT INTO siap.ucap_ipp (ucap_ipp_anho, ucap_ipp_valor, ucap_ipc_valor, empr_id) VALUES ({ucap_ipp_anho}, {ucap_ipp_valor}, {ucap_ipc_valor}, {empr_id})""").
                on(
                    'ucap_ipp_anho -> uipp.ucap_ipp_anho,
                    'ucap_ipp_valor -> uipp.ucap_ipp_valor,
                    'ucap_ipc_valor -> uipp.ucap_ipc_valor,
                    'empr_id -> empr_id
                ).executeUpdate() > 0
            } else {
                false
            }

            if (ucap_ipp_ant != None){
                if (ucap_ipp_ant.get.ucap_ipp_valor != uipp.ucap_ipp_valor){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "ucap_ipp", 
                    'audi_uid -> uipp.ucap_ipp_id,
                    'audi_campo -> "ucap_ipp_valor", 
                    'audi_valorantiguo -> ucap_ipp_ant.get.ucap_ipp_valor,
                    'audi_valornuevo -> uipp.ucap_ipp_valor,
                    'audi_evento -> "U").
                    executeInsert()                    
                }

                if (ucap_ipp_ant.get.ucap_ipc_valor != uipp.ucap_ipc_valor){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "ucap_ipp", 
                    'audi_uid -> uipp.ucap_ipp_id,
                    'audi_campo -> "ucap_ipc_valor", 
                    'audi_valorantiguo -> ucap_ipp_ant.get.ucap_ipc_valor,
                    'audi_valornuevo -> uipp.ucap_ipc_valor,
                    'audi_evento -> "U").
                    executeInsert()
                }
            } else {
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "ucap_ipp", 
                    'audi_uid -> uipp.ucap_ipp_id,
                    'audi_evento -> "I").
                    executeInsert()                    
            }

            esActualizado || esInsertado
        }
    }
    
    /**
     * Eliminar Ucap Valor
     * @param ucap_ipp_id: Ucap Ipp Id
     * @param empr_id: Long
     */
    def borrarUcapIpp(ucap_ipp_id: scala.Long, usua_id: scala.Long, empr_id: scala.Long) = {
        db.withTransaction { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val ucap_ipp_ant: Option[UcapIppIpc] = SQL("SELECT * FROM siap.ucap_ipp WHERE ucap_ipp_id = {ucap_ipp_id}").
            on(
                'ucap_ipp_id -> ucap_ipp_id
            ).as(UcapIppIpc._set.singleOpt)

            val result = SQL("DELETE FROM siap.ucap_ipp WHERE ucap_ipp_id = {ucap_ipp_id}").
            on(
                'ucap_ipp_id -> ucap_ipp_id
            ).executeUpdate() > 0

            if (ucap_ipp_ant != None){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "ucap_ipp", 
                    'audi_uid -> ucap_ipp_id,
                    'audi_campo -> "ucap_ipp_anho", 
                    'audi_valorantiguo -> None,
                    'audi_valornuevo -> None,
                    'audi_evento -> "D").
                    executeInsert()
            }
            result
        }
    }
}