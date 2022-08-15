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

case class AapUso(aaus_id:Option[Long], aaus_descripcion: String, aaus_estado: Int, usua_id: Long)

object AapUso {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_usoWrites = new Writes[AapUso] {
        def writes(aap_uso: AapUso) = Json.obj(
            "aaus_id" -> aap_uso.aaus_id,
            "aaus_descripcion" -> aap_uso.aaus_descripcion,
            "aaus_estado" -> aap_uso.aaus_estado,
            "usua_id" -> aap_uso.usua_id
        )
    }

    implicit val aap_usoReads: Reads[AapUso] = (
        (__ \ "aaus_id").readNullable[Long] and
        (__ \ "aaus_descripcion").read[String] and
        (__ \ "aaus_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapUso.apply _)
}

class AapUsoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapUso desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_uso.aaus_id") ~
        get[String]("aap_uso.aaus_descripcion") ~ 
        get[Int]("aap_uso.aaus_estado") ~ 
        get[Long]("aap_uso.usua_id") map {
            case aaus_id ~ aaus_descripcion ~ aaus_estado ~ usua_id => AapUso(aaus_id, aaus_descripcion, aaus_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapUso dado su aaus_id
    * @param aaus_id: Long
    */
    def buscarPorId(aaus_id: Long) : Option[AapUso] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_uso WHERE aaus_id = {aaus_id}").
            on(
                'aaus_id -> aaus_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_uso WHERE aaus_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapUso activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapUso]] = Future[Iterable[AapUso]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_uso WHERE aaus_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapUso activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aapusos(): Future[Iterable[AapUso]] = Future[Iterable[AapUso]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_uso WHERE aaus_estado <> 9 ORDER BY aaus_descripcion").
            as(simple *)
        }        
    }    

    /**
    * Crear AapUso
    * @param aap_uso: AapUso
    */
    def crear(aap_uso: AapUso) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_uso (aaus_descripcion, aaus_estado, usua_id) VALUES ({aaus_descripcion},{aaus_estado},{usua_id})").
            on(
                'aaus_descripcion -> aap_uso.aaus_descripcion,
                'aaus_estado -> aap_uso.aaus_estado,
                'usua_id -> aap_uso.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_uso.usua_id,
                'audi_tabla -> "aap_uso_tipo", 
                'audi_uid -> id,
                'audi_campo -> "aaus_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_uso.aaus_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapUso
    * @param aap_uso: AapUso
    */
    def actualizar(aap_uso: AapUso) : Boolean = {
        val aap_uso_ant: Option[AapUso] = buscarPorId(aap_uso.aaus_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_uso SET aaus_descripcion = {aaus_descripcion}, aaus_estado = {aaus_estado}, usua_id = {usua_id} WHERE aaus_id = {aaus_id}").
            on(
                'aaus_id -> aap_uso.aaus_id,
                'aaus_descripcion -> aap_uso.aaus_descripcion,
                'aaus_estado -> aap_uso.aaus_estado,
                'usua_id -> aap_uso.usua_id
            ).executeUpdate() > 0

            if (aap_uso_ant != None){
                if (aap_uso_ant.get.aaus_descripcion != aap_uso.aaus_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_uso.usua_id,
                    'audi_tabla -> "aap_uso", 
                    'audi_uid -> aap_uso.aaus_id,
                    'audi_campo -> "aaus_descripcion", 
                    'audi_valorantiguo -> aap_uso_ant.get.aaus_descripcion,
                    'audi_valornuevo -> aap_uso.aaus_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_uso_ant.get.aaus_estado != aap_uso.aaus_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_uso.usua_id,
                    'audi_tabla -> "aap_uso", 
                    'audi_uid -> aap_uso.aaus_id,
                    'audi_campo -> "aaus_estado", 
                    'audi_valorantiguo -> aap_uso_ant.get.aaus_estado,
                    'audi_valornuevo -> aap_uso.aaus_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapUso
    * @param aap_uso: AapUso
    */
    def borrar(aaus_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_uso SET aaus_estado = 9 WHERE aaus_id = {aaus_id}").
            on(
                'aaus_id -> aaus_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_uso", 
                    'audi_uid -> aaus_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}