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

case class AapMedidorTipo(amet_id:Option[Long], amet_descripcion: String, amet_estado: Int, usua_id: Long)

object AapMedidorTipo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aapmedidortipoWrites = new Writes[AapMedidorTipo] {
        def writes(aapmedidortipo: AapMedidorTipo) = Json.obj(
            "amet_id" -> aapmedidortipo.amet_id,
            "amet_descripcion" -> aapmedidortipo.amet_descripcion,
            "amet_estado" -> aapmedidortipo.amet_estado,
            "usua_id" -> aapmedidortipo.usua_id
        )
    }

    implicit val aapmedidortipoReads: Reads[AapMedidorTipo] = (
        (__ \ "amet_id").readNullable[Long] and
        (__ \ "amet_descripcion").read[String] and
        (__ \ "amet_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapMedidorTipo.apply _)
}

class AapMedidorTipoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapMedidorTipo desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aapmedidortipo.amet_id") ~
        get[String]("aapmedidortipo.amet_descripcion") ~ 
        get[Int]("aapmedidortipo.amet_estado") ~ 
        get[Long]("aapmedidortipo.usua_id") map {
            case amet_id ~ amet_descripcion ~ amet_estado ~ usua_id => AapMedidorTipo(amet_id, amet_descripcion, amet_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapMedidorTipo dado su amet_id
    * @param amet_id: Long
    */
    def buscarPorId(amet_id: Long) : Option[AapMedidorTipo] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aapmedidortipo WHERE amet_id = {amet_id}").
            on(
                'amet_id -> amet_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aapmedidortipo WHERE amet_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapMedidorTipo activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapMedidorTipo]] = Future[Iterable[AapMedidorTipo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aapmedidortipo WHERE amet_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapMedidorTipo activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aapmedidortipos(): Future[Iterable[AapMedidorTipo]] = Future[Iterable[AapMedidorTipo]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aapmedidortipo WHERE amet_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear AapMedidorTipo
    * @param aapmedidortipo: AapMedidorTipo
    */
    def crear(aapmedidortipo: AapMedidorTipo) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aapmedidortipo (amet_descripcion, amet_estado, usua_id) VALUES ({amet_descripcion},{amet_estado},{usua_id})").
            on(
                'amet_descripcion -> aapmedidortipo.amet_descripcion,
                'amet_estado -> aapmedidortipo.amet_estado,
                'usua_id -> aapmedidortipo.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aapmedidortipo.usua_id,
                'audi_tabla -> "aap_medidor_tipo", 
                'audi_uid -> id,
                'audi_campo -> "amet_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aapmedidortipo.amet_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapMedidorTipo
    * @param aapmedidortipo: AapMedidorTipo
    */
    def actualizar(aapmedidortipo: AapMedidorTipo) : Boolean = {
        val aapmedidortipo_ant: Option[AapMedidorTipo] = buscarPorId(aapmedidortipo.amet_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aapmedidortipo SET amet_descripcion = {amet_descripcion}, amet_estado = {amet_estado}, usua_id = {usua_id} WHERE amet_id = {amet_id}").
            on(
                'amet_id -> aapmedidortipo.amet_id,
                'amet_descripcion -> aapmedidortipo.amet_descripcion,
                'amet_estado -> aapmedidortipo.amet_estado,
                'usua_id -> aapmedidortipo.usua_id
            ).executeUpdate() > 0

            if (aapmedidortipo_ant != None){
                if (aapmedidortipo_ant.get.amet_descripcion != aapmedidortipo.amet_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aapmedidortipo.usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> aapmedidortipo.amet_id,
                    'audi_campo -> "amet_descripcion", 
                    'audi_valorantiguo -> aapmedidortipo_ant.get.amet_descripcion,
                    'audi_valornuevo -> aapmedidortipo.amet_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aapmedidortipo_ant.get.amet_estado != aapmedidortipo.amet_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aapmedidortipo.usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> aapmedidortipo.amet_id,
                    'audi_campo -> "amet_estado", 
                    'audi_valorantiguo -> aapmedidortipo_ant.get.amet_estado,
                    'audi_valornuevo -> aapmedidortipo.amet_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapMedidorTipo
    * @param aapmedidortipo: AapMedidorTipo
    */
    def borrar(amet_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aapmedidortipo SET amet_estado = 9 WHERE amet_id = {amet_id}").
            on(
                'amet_id -> amet_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> amet_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}