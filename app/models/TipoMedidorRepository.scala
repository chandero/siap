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

case class TipoMedidor(amet_id:Option[Long], amet_descripcion: String, amet_estado: Int, usua_id: Long)

object TipoMedidor {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tipomedidorWrites = new Writes[TipoMedidor] {
        def writes(tipomedidor: TipoMedidor) = Json.obj(
            "amet_id" -> tipomedidor.amet_id,
            "amet_descripcion" -> tipomedidor.amet_descripcion,
            "amet_estado" -> tipomedidor.amet_estado,
            "usua_id" -> tipomedidor.usua_id
        )
    }

    implicit val tipomedidorReads: Reads[TipoMedidor] = (
        (__ \ "amet_id").readNullable[Long] and
        (__ \ "amet_descripcion").read[String] and
        (__ \ "amet_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(TipoMedidor.apply _)
}

class TipoMedidorRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoMedidor desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("amet_id") ~
        get[String]("amet_descripcion") ~ 
        get[Int]("amet_estado") ~ 
        get[Long]("usua_id") map {
            case amet_id ~ amet_descripcion ~ amet_estado ~ usua_id => TipoMedidor(amet_id, amet_descripcion, amet_estado, usua_id)
        }
    }

    /**
    * Recuperar un TipoMedidor dado su amet_id
    * @param amet_id: Long
    */
    def buscarPorId(amet_id: Long) : Option[TipoMedidor] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_medidor_tipo WHERE amet_id = {amet_id}").
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_medidor_tipo WHERE amet_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los TipoMedidor activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[TipoMedidor]] = Future[Iterable[TipoMedidor]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_medidor_tipo WHERE amet_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los TipoMedidor activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def tiposmedidor(): Future[Iterable[TipoMedidor]] = Future[Iterable[TipoMedidor]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_medidor_tipo WHERE amet_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear TipoMedidor
    * @param tipomedidor: TipoMedidor
    */
    def crear(tipomedidor: TipoMedidor) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_medidor_tipo (amet_descripcion, amet_estado, usua_id) VALUES ({amet_descripcion},{amet_estado},{usua_id})").
            on(
                'amet_descripcion -> tipomedidor.amet_descripcion,
                'amet_estado -> tipomedidor.amet_estado,
                'usua_id -> tipomedidor.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> tipomedidor.usua_id,
                'audi_tabla -> "aap_medidor_tipo", 
                'audi_uid -> id,
                'audi_campo -> "amet_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> tipomedidor.amet_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar TipoMedidor
    * @param tipomedidor: TipoMedidor
    */
    def actualizar(tipomedidor: TipoMedidor) : Boolean = {
        val tipomedidor_ant: Option[TipoMedidor] = buscarPorId(tipomedidor.amet_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_medidor_tipo SET amet_descripcion = {amet_descripcion}, amet_estado = {amet_estado}, usua_id = {usua_id} WHERE amet_id = {amet_id}").
            on(
                'amet_id -> tipomedidor.amet_id,
                'amet_descripcion -> tipomedidor.amet_descripcion,
                'amet_estado -> tipomedidor.amet_estado,
                'usua_id -> tipomedidor.usua_id
            ).executeUpdate() > 0

            if (tipomedidor_ant != None){
                if (tipomedidor_ant.get.amet_descripcion != tipomedidor.amet_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipomedidor.usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> tipomedidor.amet_id,
                    'audi_campo -> "amet_descripcion", 
                    'audi_valorantiguo -> tipomedidor_ant.get.amet_descripcion,
                    'audi_valornuevo -> tipomedidor.amet_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (tipomedidor_ant.get.amet_estado != tipomedidor.amet_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipomedidor.usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> tipomedidor.amet_id,
                    'audi_campo -> "amet_estado", 
                    'audi_valorantiguo -> tipomedidor_ant.get.amet_estado,
                    'audi_valornuevo -> tipomedidor.amet_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }
            result
        }
    }

    /**
    * Elimnar TipoMedidor
    * @param tipomedidor: TipoMedidor
    */
    def borrar(amet_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.tipomedidor SET amet_estado = 9 WHERE amet_id = {amet_id}").
            on(
                'amet_id -> amet_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "tipomedidor", 
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