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

case class TipoElemento(tiel_id:Option[Long], tiel_descripcion: String, tiel_estado: Int, usua_id: Long)

object TipoElemento {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tipoelementoWrites = new Writes[TipoElemento] {
        def writes(tipoelemento: TipoElemento) = Json.obj(
            "tiel_id" -> tipoelemento.tiel_id,
            "tiel_descripcion" -> tipoelemento.tiel_descripcion,
            "tiel_estado" -> tipoelemento.tiel_estado,
            "usua_id" -> tipoelemento.usua_id
        )
    }

    implicit val tipoelementoReads: Reads[TipoElemento] = (
        (__ \ "tiel_id").readNullable[Long] and
        (__ \ "tiel_descripcion").read[String] and
        (__ \ "tiel_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(TipoElemento.apply _)
}

class TipoElementoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoElemento desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("tipoelemento.tiel_id") ~
        get[String]("tipoelemento.tiel_descripcion") ~ 
        get[Int]("tipoelemento.tiel_estado") ~ 
        get[Long]("tipoelemento.usua_id") map {
            case tiel_id ~ tiel_descripcion ~ tiel_estado ~ usua_id => TipoElemento(tiel_id, tiel_descripcion, tiel_estado, usua_id)
        }
    }

    /**
    * Recuperar un TipoElemento dado su tiel_id
    * @param tiel_id: Long
    */
    def buscarPorId(tiel_id: Long) : Option[TipoElemento] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipoelemento WHERE tiel_id = {tiel_id}").
            on(
                'tiel_id -> tiel_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.tipoelemento WHERE tiel_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los TipoElemento activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[TipoElemento]] = Future[Iterable[TipoElemento]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipoelemento WHERE tiel_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los TipoElemento activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def tiposelemento(): Future[Iterable[TipoElemento]] = Future[Iterable[TipoElemento]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.tipoelemento WHERE tiel_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear TipoElemento
    * @param tipoelemento: TipoElemento
    */
    def crear(tipoelemento: TipoElemento) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.tipoelemento (tiel_descripcion, tiel_estado, usua_id) VALUES ({tiel_descripcion},{tiel_estado},{usua_id})").
            on(
                'tiel_descripcion -> tipoelemento.tiel_descripcion,
                'tiel_estado -> tipoelemento.tiel_estado,
                'usua_id -> tipoelemento.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> tipoelemento.usua_id,
                'audi_tabla -> "tipoelemento", 
                'audi_uid -> id,
                'audi_campo -> "tiel_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> tipoelemento.tiel_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar TipoElemento
    * @param tipoelemento: TipoElemento
    */
    def actualizar(tipoelemento: TipoElemento) : Boolean = {
        val tipoelemento_ant: Option[TipoElemento] = buscarPorId(tipoelemento.tiel_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.tipoelemento SET tiel_descripcion = {tiel_descripcion}, tiel_estado = {tiel_estado}, usua_id = {usua_id} WHERE tiel_id = {tiel_id}").
            on(
                'tiel_id -> tipoelemento.tiel_id,
                'tiel_descripcion -> tipoelemento.tiel_descripcion,
                'tiel_estado -> tipoelemento.tiel_estado,
                'usua_id -> tipoelemento.usua_id
            ).executeUpdate() > 0

            if (tipoelemento_ant != None){
                if (tipoelemento_ant.get.tiel_descripcion != tipoelemento.tiel_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipoelemento.usua_id,
                    'audi_tabla -> "tipoelemento", 
                    'audi_uid -> tipoelemento.tiel_id,
                    'audi_campo -> "tiel_descripcion", 
                    'audi_valorantiguo -> tipoelemento_ant.get.tiel_descripcion,
                    'audi_valornuevo -> tipoelemento.tiel_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (tipoelemento_ant.get.tiel_estado != tipoelemento.tiel_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> tipoelemento.usua_id,
                    'audi_tabla -> "tipoelemento", 
                    'audi_uid -> tipoelemento.tiel_id,
                    'audi_campo -> "tiel_estado", 
                    'audi_valorantiguo -> tipoelemento_ant.get.tiel_estado,
                    'audi_valornuevo -> tipoelemento.tiel_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar TipoElemento
    * @param tipoelemento: TipoElemento
    */
    def borrar(tiel_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.tipoelemento SET tiel_estado = 9 WHERE tiel_id = {tiel_id}").
            on(
                'tiel_id -> tiel_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "tipoelemento", 
                    'audi_uid -> tiel_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}