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

case class AapConexion(aaco_id:Option[Long], aaco_descripcion: String, aaco_estado: Int, usua_id: Long)

object AapConexion {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_conexionWrites = new Writes[AapConexion] {
        def writes(aap_conexion: AapConexion) = Json.obj(
            "aaco_id" -> aap_conexion.aaco_id,
            "aaco_descripcion" -> aap_conexion.aaco_descripcion,
            "aaco_estado" -> aap_conexion.aaco_estado,
            "usua_id" -> aap_conexion.usua_id
        )
    }

    implicit val aap_conexionReads: Reads[AapConexion] = (
        (__ \ "aaco_id").readNullable[Long] and
        (__ \ "aaco_descripcion").read[String] and
        (__ \ "aaco_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapConexion.apply _)
}

class AapConexionRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapConexion desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_conexion.aaco_id") ~
        get[String]("aap_conexion.aaco_descripcion") ~ 
        get[Int]("aap_conexion.aaco_estado") ~ 
        get[Long]("aap_conexion.usua_id") map {
            case aaco_id ~ aaco_descripcion ~ aaco_estado ~ usua_id => AapConexion(aaco_id, aaco_descripcion, aaco_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapConexion dado su aaco_id
    * @param aaco_id: Long
    */
    def buscarPorId(aaco_id: Long) : Option[AapConexion] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_conexion WHERE aaco_id = {aaco_id}").
            on(
                'aaco_id -> aaco_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_conexion WHERE aaco_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapConexion activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapConexion]] = Future[Iterable[AapConexion]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_conexion WHERE aaco_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapConexion activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aapconexiones(): Future[Iterable[AapConexion]] = Future[Iterable[AapConexion]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_conexion WHERE aaco_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear AapConexion
    * @param aap_conexion: AapConexion
    */
    def crear(aap_conexion: AapConexion) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_conexion (aaco_descripcion, aaco_estado, usua_id) VALUES ({aaco_descripcion},{aaco_estado},{usua_id})").
            on(
                'aaco_descripcion -> aap_conexion.aaco_descripcion,
                'aaco_estado -> aap_conexion.aaco_estado,
                'usua_id -> aap_conexion.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_conexion.usua_id,
                'audi_tabla -> "aap_conexion", 
                'audi_uid -> id,
                'audi_campo -> "aaco_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_conexion.aaco_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapConexion
    * @param aap_conexion: AapConexion
    */
    def actualizar(aap_conexion: AapConexion) : Boolean = {
        val aap_conexion_ant: Option[AapConexion] = buscarPorId(aap_conexion.aaco_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_conexion SET aaco_descripcion = {aaco_descripcion}, aaco_estado = {aaco_estado}, usua_id = {usua_id} WHERE aaco_id = {aaco_id}").
            on(
                'aaco_id -> aap_conexion.aaco_id,
                'aaco_descripcion -> aap_conexion.aaco_descripcion,
                'aaco_estado -> aap_conexion.aaco_estado,
                'usua_id -> aap_conexion.usua_id
            ).executeUpdate() > 0

            if (aap_conexion_ant != None){
                if (aap_conexion_ant.get.aaco_descripcion != aap_conexion.aaco_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_conexion.usua_id,
                    'audi_tabla -> "aap_conexion", 
                    'audi_uid -> aap_conexion.aaco_id,
                    'audi_campo -> "aaco_descripcion", 
                    'audi_valorantiguo -> aap_conexion_ant.get.aaco_descripcion,
                    'audi_valornuevo -> aap_conexion.aaco_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_conexion_ant.get.aaco_estado != aap_conexion.aaco_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_conexion.usua_id,
                    'audi_tabla -> "aap_conexion", 
                    'audi_uid -> aap_conexion.aaco_id,
                    'audi_campo -> "aaco_estado", 
                    'audi_valorantiguo -> aap_conexion_ant.get.aaco_estado,
                    'audi_valornuevo -> aap_conexion.aaco_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapConexion
    * @param aap_conexion: AapConexion
    */
    def borrar(aaco_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_conexion SET aaco_estado = 9 WHERE aaco_id = {aaco_id}").
            on(
                'aaco_id -> aaco_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_conexion", 
                    'audi_uid -> aaco_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}