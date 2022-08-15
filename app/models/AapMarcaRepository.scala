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

case class AapMarca(aama_id:Option[Long], aama_descripcion: String, aama_estado: Int, usua_id: Long)

object AapMarca {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_marcaWrites = new Writes[AapMarca] {
        def writes(aap_marca: AapMarca) = Json.obj(
            "aama_id" -> aap_marca.aama_id,
            "aama_descripcion" -> aap_marca.aama_descripcion,
            "aama_estado" -> aap_marca.aama_estado,
            "usua_id" -> aap_marca.usua_id
        )
    }

    implicit val aap_marcaReads: Reads[AapMarca] = (
        (__ \ "aama_id").readNullable[Long] and
        (__ \ "aama_descripcion").read[String] and
        (__ \ "aama_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapMarca.apply _)
}

class AapMarcaRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapMarca desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_marca.aama_id") ~
        get[String]("aap_marca.aama_descripcion") ~ 
        get[Int]("aap_marca.aama_estado") ~ 
        get[Long]("aap_marca.usua_id") map {
            case aama_id ~ aama_descripcion ~ aama_estado ~ usua_id => AapMarca(aama_id, aama_descripcion, aama_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapMarca dado su aama_id
    * @param aama_id: Long
    */
    def buscarPorId(aama_id: Long) : Option[AapMarca] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_marca WHERE aama_id = {aama_id}").
            on(
                'aama_id -> aama_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_marca WHERE aama_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapMarca activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapMarca]] = Future[Iterable[AapMarca]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_marca WHERE aama_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapMarca activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aapmarcas(): Future[Iterable[AapMarca]] = Future[Iterable[AapMarca]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_marca WHERE aama_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear AapMarca
    * @param aap_marca: AapMarca
    */
    def crear(aap_marca: AapMarca) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_marca (aama_descripcion, aama_estado, usua_id) VALUES ({aama_descripcion},{aama_estado},{usua_id})").
            on(
                'aama_descripcion -> aap_marca.aama_descripcion,
                'aama_estado -> aap_marca.aama_estado,
                'usua_id -> aap_marca.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_marca.usua_id,
                'audi_tabla -> "aap_marca", 
                'audi_uid -> id,
                'audi_campo -> "aama_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_marca.aama_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapMarca
    * @param aap_marca: AapMarca
    */
    def actualizar(aap_marca: AapMarca) : Boolean = {
        val aap_marca_ant: Option[AapMarca] = buscarPorId(aap_marca.aama_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_marca SET aama_descripcion = {aama_descripcion}, aama_estado = {aama_estado}, usua_id = {usua_id} WHERE aama_id = {aama_id}").
            on(
                'aama_id -> aap_marca.aama_id,
                'aama_descripcion -> aap_marca.aama_descripcion,
                'aama_estado -> aap_marca.aama_estado,
                'usua_id -> aap_marca.usua_id
            ).executeUpdate() > 0

            if (aap_marca_ant != None){
                if (aap_marca_ant.get.aama_descripcion != aap_marca.aama_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_marca.usua_id,
                    'audi_tabla -> "aap_marca", 
                    'audi_uid -> aap_marca.aama_id,
                    'audi_campo -> "aama_descripcion", 
                    'audi_valorantiguo -> aap_marca_ant.get.aama_descripcion,
                    'audi_valornuevo -> aap_marca.aama_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_marca_ant.get.aama_estado != aap_marca.aama_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_marca.usua_id,
                    'audi_tabla -> "aap_marca", 
                    'audi_uid -> aap_marca.aama_id,
                    'audi_campo -> "aama_estado", 
                    'audi_valorantiguo -> aap_marca_ant.get.aama_estado,
                    'audi_valornuevo -> aap_marca.aama_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapMarca
    * @param aap_marca: AapMarca
    */
    def borrar(aama_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_marca SET aama_estado = 9 WHERE aama_id = {aama_id}").
            on(
                'aama_id -> aama_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_marca", 
                    'audi_uid -> aama_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}