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

case class AapTipoCarcasa(aatc_id:Option[Long], aatc_descripcion: String, aatc_estado: Int, usua_id: Long)

object AapTipoCarcasa {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_tipo_carcasaWrites = new Writes[AapTipoCarcasa] {
        def writes(aap_tipo_carcasa: AapTipoCarcasa) = Json.obj(
            "aatc_id" -> aap_tipo_carcasa.aatc_id,
            "aatc_descripcion" -> aap_tipo_carcasa.aatc_descripcion,
            "aatc_estado" -> aap_tipo_carcasa.aatc_estado,
            "usua_id" -> aap_tipo_carcasa.usua_id
        )
    }

    implicit val aap_tipo_carcasaReads: Reads[AapTipoCarcasa] = (
        (__ \ "aatc_id").readNullable[Long] and
        (__ \ "aatc_descripcion").read[String] and
        (__ \ "aatc_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapTipoCarcasa.apply _)
}

class AapTipoCarcasaRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapTipoCarcasa desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_tipo_carcasa.aatc_id") ~
        get[String]("aap_tipo_carcasa.aatc_descripcion") ~ 
        get[Int]("aap_tipo_carcasa.aatc_estado") ~ 
        get[Long]("aap_tipo_carcasa.usua_id") map {
            case aatc_id ~ aatc_descripcion ~ aatc_estado ~ usua_id => AapTipoCarcasa(aatc_id, aatc_descripcion, aatc_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapTipoCarcasa dado su aatc_id
    * @param aatc_id: Long
    */
    def buscarPorId(aatc_id: Long) : Option[AapTipoCarcasa] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_tipo_carcasa WHERE aatc_id = {aatc_id}").
            on(
                'aatc_id -> aatc_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_tipo_carcasa WHERE aatc_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapTipoCarcasa activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapTipoCarcasa]] = Future[Iterable[AapTipoCarcasa]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_tipo_carcasa WHERE aatc_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapTipoCarcasa activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def aaptiposcarcasa(): Future[Iterable[AapTipoCarcasa]] = Future[Iterable[AapTipoCarcasa]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_tipo_carcasa WHERE aatc_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear AapTipoCarcasa
    * @param aap_tipo_carcasa: AapTipoCarcasa
    */
    def crear(aap_tipo_carcasa: AapTipoCarcasa) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_tipo_carcasa (aatc_descripcion, aatc_estado, usua_id) VALUES ({aatc_descripcion},{aatc_estado},{usua_id})").
            on(
                'aatc_descripcion -> aap_tipo_carcasa.aatc_descripcion,
                'aatc_estado -> aap_tipo_carcasa.aatc_estado,
                'usua_id -> aap_tipo_carcasa.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_tipo_carcasa.usua_id,
                'audi_tabla -> "aap_medidor_tipo", 
                'audi_uid -> id,
                'audi_campo -> "aatc_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_tipo_carcasa.aatc_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapTipoCarcasa
    * @param aap_tipo_carcasa: AapTipoCarcasa
    */
    def actualizar(aap_tipo_carcasa: AapTipoCarcasa) : Boolean = {
        val aap_tipo_carcasa_ant: Option[AapTipoCarcasa] = buscarPorId(aap_tipo_carcasa.aatc_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_tipo_carcasa SET aatc_descripcion = {aatc_descripcion}, aatc_estado = {aatc_estado}, usua_id = {usua_id} WHERE aatc_id = {aatc_id}").
            on(
                'aatc_id -> aap_tipo_carcasa.aatc_id,
                'aatc_descripcion -> aap_tipo_carcasa.aatc_descripcion,
                'aatc_estado -> aap_tipo_carcasa.aatc_estado,
                'usua_id -> aap_tipo_carcasa.usua_id
            ).executeUpdate() > 0

            if (aap_tipo_carcasa_ant != None){
                if (aap_tipo_carcasa_ant.get.aatc_descripcion != aap_tipo_carcasa.aatc_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_tipo_carcasa.usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> aap_tipo_carcasa.aatc_id,
                    'audi_campo -> "aatc_descripcion", 
                    'audi_valorantiguo -> aap_tipo_carcasa_ant.get.aatc_descripcion,
                    'audi_valornuevo -> aap_tipo_carcasa.aatc_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_tipo_carcasa_ant.get.aatc_estado != aap_tipo_carcasa.aatc_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_tipo_carcasa.usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> aap_tipo_carcasa.aatc_id,
                    'audi_campo -> "aatc_estado", 
                    'audi_valorantiguo -> aap_tipo_carcasa_ant.get.aatc_estado,
                    'audi_valornuevo -> aap_tipo_carcasa.aatc_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapTipoCarcasa
    * @param aap_tipo_carcasa: AapTipoCarcasa
    */
    def borrar(aatc_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_tipo_carcasa SET aatc_estado = 9 WHERE aatc_id = {aatc_id}").
            on(
                'aatc_id -> aatc_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_medidor_tipo", 
                    'audi_uid -> aatc_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}