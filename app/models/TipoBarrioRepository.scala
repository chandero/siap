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

case class TipoBarrio(tiba_id:Option[Long], tiba_descripcion: String, tiba_estado: Int, usua_id: Long)

object TipoBarrio {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tipobarrioWrites = new Writes[TipoBarrio] {
        def writes(tipobarrio: TipoBarrio) = Json.obj(
            "tiba_id" -> tipobarrio.tiba_id,
            "tiba_descripcion" -> tipobarrio.tiba_descripcion,
            "tiba_estado" -> tipobarrio.tiba_estado,
            "usua_id" -> tipobarrio.usua_id
        )
    }

    implicit val tipobarrioReads: Reads[TipoBarrio] = (
        (__ \ "tiba_id").readNullable[Long] and
        (__ \ "tiba_descripcion").read[String] and
        (__ \ "tiba_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(TipoBarrio.apply _)
}

class TipoBarrioRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoBarrio desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("tipobarrio.tiba_id") ~
        get[String]("tipobarrio.tiba_descripcion") ~ 
        get[Int]("tipobarrio.tiba_estado") ~ 
        get[Long]("tipobarrio.usua_id") map {
            case tiba_id ~ tiba_descripcion ~ tiba_estado ~ usua_id => TipoBarrio(tiba_id, tiba_descripcion, tiba_estado, usua_id)
        }
    }

    /**
    * Recuperar un TipoBarrio dado su tiba_id
    * @param tiba_id: Long
    */
    def buscarPorId(tiba_id: Long) : Option[TipoBarrio] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipobarrio WHERE tiba_id = {tiba_id}").
            on(
                'tiba_id -> tiba_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.tipobarrio WHERE tiba_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los TipoBarrio activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[TipoBarrio]] = Future[Iterable[TipoBarrio]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipobarrio WHERE tiba_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los TipoBarrio activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def tiposbarrio(): Future[Iterable[TipoBarrio]] = Future[Iterable[TipoBarrio]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipobarrio WHERE tiba_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear TipoBarrio
    * @param tipobarrio: TipoBarrio
    */
    def crear(tipobarrio: TipoBarrio) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.tipobarrio (tiba_descripcion, tiba_estado, usua_id) VALUES ({tiba_descripcion},{tiba_estado},{usua_id})").
            on(
                'tiba_descripcion -> tipobarrio.tiba_descripcion,
                'tiba_estado -> tipobarrio.tiba_estado,
                'usua_id -> tipobarrio.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> tipobarrio.usua_id,
                'audi_tabla -> "tipobarrio", 
                'audi_uid -> id,
                'audi_campo -> "tiba_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> tipobarrio.tiba_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar TipoBarrio
    * @param tipobarrio: TipoBarrio
    */
    def actualizar(tipobarrio: TipoBarrio) : Boolean = {
        val tipobarrio_ant: Option[TipoBarrio] = buscarPorId(tipobarrio.tiba_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.tipobarrio SET tiba_descripcion = {tiba_descripcion}, tiba_estado = {tiba_estado}, usua_id = {usua_id} WHERE tiba_id = {tiba_id}").
            on(
                'tiba_id -> tipobarrio.tiba_id,
                'tiba_descripcion -> tipobarrio.tiba_descripcion,
                'tiba_estado -> tipobarrio.tiba_estado,
                'usua_id -> tipobarrio.usua_id
            ).executeUpdate() > 0

            if (tipobarrio_ant != None){
                if (tipobarrio_ant.get.tiba_descripcion != tipobarrio.tiba_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipobarrio.usua_id,
                    'audi_tabla -> "tipobarrio", 
                    'audi_uid -> tipobarrio.tiba_id,
                    'audi_campo -> "tiba_descripcion", 
                    'audi_valorantiguo -> tipobarrio_ant.get.tiba_descripcion,
                    'audi_valornuevo -> tipobarrio.tiba_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (tipobarrio_ant.get.tiba_estado != tipobarrio.tiba_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipobarrio.usua_id,
                    'audi_tabla -> "tipobarrio", 
                    'audi_uid -> tipobarrio.tiba_id,
                    'audi_campo -> "tiba_estado", 
                    'audi_valorantiguo -> tipobarrio_ant.get.tiba_estado,
                    'audi_valornuevo -> tipobarrio.tiba_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar TipoBarrio
    * @param tipobarrio: TipoBarrio
    */
    def borrar(tiba_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.tipobarrio SET tiba_estado = 9 WHERE tiba_id = {tiba_id}").
            on(
                'tiba_id -> tiba_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "tipobarrio", 
                    'audi_uid -> tiba_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}