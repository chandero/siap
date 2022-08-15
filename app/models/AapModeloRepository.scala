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

case class AapModelo(aamo_id:Option[Long], aamo_descripcion: String, aamo_estado: Int, usua_id: Long)

object AapModelo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_modeloWrites = new Writes[AapModelo] {
        def writes(aap_modelo: AapModelo) = Json.obj(
            "aamo_id" -> aap_modelo.aamo_id,
            "aamo_descripcion" -> aap_modelo.aamo_descripcion,
            "aamo_estado" -> aap_modelo.aamo_estado,
            "usua_id" -> aap_modelo.usua_id
        )
    }

    implicit val aap_modeloReads: Reads[AapModelo] = (
        (__ \ "aamo_id").readNullable[Long] and
        (__ \ "aamo_descripcion").read[String] and
        (__ \ "aamo_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapModelo.apply _)
}

class AapModeloRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapModelo desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_modelo.aamo_id") ~
        get[String]("aap_modelo.aamo_descripcion") ~ 
        get[Int]("aap_modelo.aamo_estado") ~ 
        get[Long]("aap_modelo.usua_id") map {
            case aamo_id ~ aamo_descripcion ~ aamo_estado ~ usua_id => AapModelo(aamo_id, aamo_descripcion, aamo_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapModelo dado su aamo_id
    * @param aamo_id: Long
    */
    def buscarPorId(aamo_id: Long) : Option[AapModelo] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_modelo WHERE aamo_id = {aamo_id}").
            on(
                'aamo_id -> aamo_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_modelo WHERE aamo_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapModelo activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapModelo]] = Future[Iterable[AapModelo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_modelo WHERE aamo_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapModelo activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aapmodelos(): Future[Iterable[AapModelo]] = Future[Iterable[AapModelo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_modelo WHERE aamo_estado <> 9 ORDER BY aamo_descripcion").
            as(simple *)
        }        
    }    

    /**
    * Crear AapModelo
    * @param aap_modelo: AapModelo
    */
    def crear(aap_modelo: AapModelo) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_modelo (aamo_descripcion, aamo_estado, usua_id) VALUES ({aamo_descripcion},{aamo_estado},{usua_id})").
            on(
                'aamo_descripcion -> aap_modelo.aamo_descripcion,
                'aamo_estado -> aap_modelo.aamo_estado,
                'usua_id -> aap_modelo.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_modelo.usua_id,
                'audi_tabla -> "aap_modelo_tipo", 
                'audi_uid -> id,
                'audi_campo -> "aamo_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_modelo.aamo_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapModelo
    * @param aap_modelo: AapModelo
    */
    def actualizar(aap_modelo: AapModelo) : Boolean = {
        val aap_modelo_ant: Option[AapModelo] = buscarPorId(aap_modelo.aamo_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_modelo SET aamo_descripcion = {aamo_descripcion}, aamo_estado = {aamo_estado}, usua_id = {usua_id} WHERE aamo_id = {aamo_id}").
            on(
                'aamo_id -> aap_modelo.aamo_id,
                'aamo_descripcion -> aap_modelo.aamo_descripcion,
                'aamo_estado -> aap_modelo.aamo_estado,
                'usua_id -> aap_modelo.usua_id
            ).executeUpdate() > 0

            if (aap_modelo_ant != None){
                if (aap_modelo_ant.get.aamo_descripcion != aap_modelo.aamo_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_modelo.usua_id,
                    'audi_tabla -> "aap_modelo", 
                    'audi_uid -> aap_modelo.aamo_id,
                    'audi_campo -> "aamo_descripcion", 
                    'audi_valorantiguo -> aap_modelo_ant.get.aamo_descripcion,
                    'audi_valornuevo -> aap_modelo.aamo_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_modelo_ant.get.aamo_estado != aap_modelo.aamo_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_modelo.usua_id,
                    'audi_tabla -> "aap_modelo", 
                    'audi_uid -> aap_modelo.aamo_id,
                    'audi_campo -> "aamo_estado", 
                    'audi_valorantiguo -> aap_modelo_ant.get.aamo_estado,
                    'audi_valornuevo -> aap_modelo.aamo_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapModelo
    * @param aap_modelo: AapModelo
    */
    def borrar(aamo_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_modelo SET aamo_estado = 9 WHERE aamo_id = {aamo_id}").
            on(
                'aamo_id -> aamo_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_modelo", 
                    'audi_uid -> aamo_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}