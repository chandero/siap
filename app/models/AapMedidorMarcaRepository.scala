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

case class AapMedidorMarca(amem_id:Option[Long], amem_descripcion: String, amem_estado: Int, usua_id: Long)

object AapMedidorMarca {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aap_medidor_marcaWrites = new Writes[AapMedidorMarca] {
        def writes(aap_medidor_marca: AapMedidorMarca) = Json.obj(
            "amem_id" -> aap_medidor_marca.amem_id,
            "amem_descripcion" -> aap_medidor_marca.amem_descripcion,
            "amem_estado" -> aap_medidor_marca.amem_estado,
            "usua_id" -> aap_medidor_marca.usua_id
        )
    }

    implicit val aap_medidor_marcaReads: Reads[AapMedidorMarca] = (
        (__ \ "amem_id").readNullable[Long] and
        (__ \ "amem_descripcion").read[String] and
        (__ \ "amem_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(AapMedidorMarca.apply _)
}

class AapMedidorMarcaRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un AapMedidorMarca desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap_medidor_marca.amem_id") ~
        get[String]("aap_medidor_marca.amem_descripcion") ~ 
        get[Int]("aap_medidor_marca.amem_estado") ~ 
        get[Long]("aap_medidor_marca.usua_id") map {
            case amem_id ~ amem_descripcion ~ amem_estado ~ usua_id => AapMedidorMarca(amem_id, amem_descripcion, amem_estado, usua_id)
        }
    }

    /**
    * Recuperar un AapMedidorMarca dado su amem_id
    * @param amem_id: Long
    */
    def buscarPorId(amem_id: Long) : Option[AapMedidorMarca] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_medidor_marca WHERE amem_id = {amem_id}").
            on(
                'amem_id -> amem_id
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
       val result = SQL("SELECT COUNT(*) AS c FROM siap.aap_medidor_marca WHERE amem_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los AapMedidorMarca activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[AapMedidorMarca]] = Future[Iterable[AapMedidorMarca]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_medidor_marca WHERE amem_estado <> 9 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page
            ).as(simple *)
        }        
    }

    /**
    * Recuperar todos los AapMedidorMarca activos
    */
    def aapmedidormarcas(): Future[Iterable[AapMedidorMarca]] = Future[Iterable[AapMedidorMarca]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.aap_medidor_marca WHERE amem_estado <> 9").
            as(simple *)
        }        
    }    

    /**
    * Crear AapMedidorMarca
    * @param aap_medidor_marca: AapMedidorMarca
    */
    def crear(aap_medidor_marca: AapMedidorMarca) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("INSERT INTO siap.aap_medidor_marca (amem_descripcion, amem_estado, usua_id) VALUES ({amem_descripcion},{amem_estado},{usua_id})").
            on(
                'amem_descripcion -> aap_medidor_marca.amem_descripcion,
                'amem_estado -> aap_medidor_marca.amem_estado,
                'usua_id -> aap_medidor_marca.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap_medidor_marca.usua_id,
                'audi_tabla -> "aap_medidor_marca", 
                'audi_uid -> id,
                'audi_campo -> "amem_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap_medidor_marca.amem_descripcion,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar AapMedidorMarca
    * @param aap_medidor_marca: AapMedidorMarca
    */
    def actualizar(aap_medidor_marca: AapMedidorMarca) : Boolean = {
        val aap_medidor_marca_ant: Option[AapMedidorMarca] = buscarPorId(aap_medidor_marca.amem_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.aap_medidor_marca SET amem_descripcion = {amem_descripcion}, amem_estado = {amem_estado}, usua_id = {usua_id} WHERE amem_id = {amem_id}").
            on(
                'amem_id -> aap_medidor_marca.amem_id,
                'amem_descripcion -> aap_medidor_marca.amem_descripcion,
                'amem_estado -> aap_medidor_marca.amem_estado,
                'usua_id -> aap_medidor_marca.usua_id
            ).executeUpdate() > 0

            if (aap_medidor_marca_ant != None){
                if (aap_medidor_marca_ant.get.amem_descripcion != aap_medidor_marca.amem_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_medidor_marca.usua_id,
                    'audi_tabla -> "aap_medidor_marca", 
                    'audi_uid -> aap_medidor_marca.amem_id,
                    'audi_campo -> "amem_descripcion", 
                    'audi_valorantiguo -> aap_medidor_marca_ant.get.amem_descripcion,
                    'audi_valornuevo -> aap_medidor_marca.amem_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (aap_medidor_marca_ant.get.amem_estado != aap_medidor_marca.amem_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap_medidor_marca.usua_id,
                    'audi_tabla -> "aap_medidor_marca", 
                    'audi_uid -> aap_medidor_marca.amem_id,
                    'audi_campo -> "amem_estado", 
                    'audi_valorantiguo -> aap_medidor_marca_ant.get.amem_estado,
                    'audi_valornuevo -> aap_medidor_marca.amem_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

            }


            result
        }
    }

    /**
    * Elimnar AapMedidorMarca
    * @param aap_medidor_marca: AapMedidorMarca
    */
    def borrar(amem_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.aap_medidor_marca SET amem_estado = 9 WHERE amem_id = {amem_id}").
            on(
                'amem_id -> amem_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap_medidor_marca", 
                    'audi_uid -> amem_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}