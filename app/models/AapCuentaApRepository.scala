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

case class AapCuentaAp(aacu_id:Option[Long], aacu_descripcion: String, aacu_estado: Int, empr_id: Long, usua_id: Long)

object AapCuentaAp {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_cuentaapWrites = new Writes[AapCuentaAp] {
        def writes(aap_cuentaap: AapCuentaAp) = Json.obj(
            "aacu_id" -> aap_cuentaap.aacu_id,
            "aacu_descripcion" -> aap_cuentaap.aacu_descripcion,
            "aacu_estado" -> aap_cuentaap.aacu_estado,
            "empr_id" -> aap_cuentaap.empr_id,
            "usua_id" -> aap_cuentaap.usua_id
        )
    }

    implicit val aap_cuentaapReads: Reads[AapCuentaAp] = (
        (__ \ "aacu_id").readNullable[Long] and
        (__ \ "aacu_descripcion").read[String] and
        (__ \ "aacu_estado").read[Int] and
        (__ \ "empr_id").read[Long] and
        (__ \ "usua_id").read[Long]
    )(AapCuentaAp.apply _)
}

class AapCuentaApRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapCuentaAp desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_cuentaap.aacu_id") ~
        get[String]("aap_cuentaap.aacu_descripcion") ~ 
        get[Int]("aap_cuentaap.aacu_estado") ~
        get[Long]("aap_cuentaap.empr_id") ~ 
        get[Long]("aap_cuentaap.usua_id") map {
            case aacu_id ~ aacu_descripcion ~ aacu_estado ~ empr_id ~ usua_id => AapCuentaAp(aacu_id, aacu_descripcion, aacu_estado, empr_id, usua_id)
        }
    }

    /**
    * Recuperar un AapCuentaAp dado su aacu_id
    * @param aacu_id: Long
    */
    def buscarPorId(aacu_id: Long) : Option[AapCuentaAp] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_cuentaap WHERE aacu_id = {aacu_id}").
            on(
                'aacu_id -> aacu_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_cuentaap WHERE aacu_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapCuentaAp activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapCuentaAp]] = Future[Iterable[AapCuentaAp]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_cuentaap WHERE aacu_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapCuentaAp activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aapcuentasap(empr_id:Long): Future[Iterable[AapCuentaAp]] = Future[Iterable[AapCuentaAp]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_cuentaap WHERE empr_id = {empr_id} and aacu_estado <> 9").
            on(
                'empr_id -> empr_id
            ).
            as(simple *)
        }        
    }    

    /**
    * Crear AapCuentaAp
    * @param aap_cuentaap: AapCuentaAp
    */
    def crear(aap_cuentaap: AapCuentaAp) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_cuentaap (aacu_descripcion, aacu_estado, empr_id, usua_id) VALUES ({aacu_descripcion},{aacu_estado},{empr_id},{usua_id})").
            on(
                'aacu_descripcion -> aap_cuentaap.aacu_descripcion,
                'aacu_estado -> aap_cuentaap.aacu_estado,
                'empr_id -> aap_cuentaap.empr_id,
                'usua_id -> aap_cuentaap.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_cuentaap.usua_id,
                'audi_tabla -> "aap_aap_cuentaap", 
                'audi_uid -> id,
                'audi_campo -> "aacu_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_cuentaap.aacu_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapCuentaAp
    * @param aap_cuentaap: AapCuentaAp
    */
    def actualizar(aap_cuentaap: AapCuentaAp) : Boolean = {
        val aap_cuentaap_ant: Option[AapCuentaAp] = buscarPorId(aap_cuentaap.aacu_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_cuentaap SET aacu_descripcion = {aacu_descripcion}, aacu_estado = {aacu_estado}, usua_id = {usua_id} WHERE aacu_id = {aacu_id}").
            on(
                'aacu_id -> aap_cuentaap.aacu_id,
                'aacu_descripcion -> aap_cuentaap.aacu_descripcion,
                'aacu_estado -> aap_cuentaap.aacu_estado,
                'usua_id -> aap_cuentaap.usua_id
            ).executeUpdate() > 0

            if (aap_cuentaap_ant != None){
                if (aap_cuentaap_ant.get.aacu_descripcion != aap_cuentaap.aacu_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_cuentaap.usua_id,
                    'audi_tabla -> "aap_cuentaap", 
                    'audi_uid -> aap_cuentaap.aacu_id,
                    'audi_campo -> "aacu_descripcion", 
                    'audi_valorantiguo -> aap_cuentaap_ant.get.aacu_descripcion,
                    'audi_valornuevo -> aap_cuentaap.aacu_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_cuentaap_ant.get.aacu_estado != aap_cuentaap.aacu_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_cuentaap.usua_id,
                    'audi_tabla -> "aap_cuentaap", 
                    'audi_uid -> aap_cuentaap.aacu_id,
                    'audi_campo -> "aacu_estado", 
                    'audi_valorantiguo -> aap_cuentaap_ant.get.aacu_estado,
                    'audi_valornuevo -> aap_cuentaap.aacu_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapCuentaAp
    * @param aap_cuentaap: AapCuentaAp
    */
    def borrar(aacu_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_cuentaap SET aacu_estado = 9 WHERE aacu_id = {aacu_id}").
            on(
                'aacu_id -> aacu_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_cuentaap", 
                    'audi_uid -> aacu_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}