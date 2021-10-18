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

case class Unidad(unid_id:Option[Long],unid_descripcion:String, unid_abreviatura: String, unid_tipo: String, unid_estado: Int, usua_id: Long)

object Unidad {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val unidadWrites = new Writes[Unidad] {
        def writes(unidad: Unidad) = Json.obj(
            "unid_id" -> unidad.unid_id,
            "unid_descripcion" -> unidad.unid_descripcion,
            "unid_abreviatura" -> unidad.unid_abreviatura,
            "unid_tipo" -> unidad.unid_tipo,
            "unid_estado" -> unidad.unid_estado,
            "usua_id" -> unidad.usua_id
        )
    }

    implicit val unidadReads: Reads[Unidad] = (
        (__ \ "unid_id").readNullable[Long] and
        (__ \ "unid_descripcion").read[String] and
        (__ \ "unid_abreviatura").read[String] and
        (__ \ "unid_tipo").read[String] and
        (__ \ "unid_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(Unidad.apply _)

}

class UnidadRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un Unidad desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("unidad.unid_id") ~
        get[String]("unidad.unid_descripcion") ~
        get[String]("unidad.unid_abreviatura") ~
        get[String]("unid_tipo") ~
        get[Int]("unidad.unid_estado") ~
        get[Long]("unidad.usua_id") map {
            case unid_id ~ unid_descripcion ~ unid_abreviatura ~ unid_tipo ~ unid_estado ~ usua_id => Unidad(unid_id, unid_descripcion, unid_abreviatura, unid_tipo, unid_estado, usua_id)
        }
    }

    /**
    * Recuperar un Unidad dado su unid_id
    * @param unid_id: Long
    */
    def buscarPorId(unid_id:Long) : Option[Unidad] = {
        db.withConnection { implicit connection => 
            SQL("SELECT unid_id, unid_descripcion, unid_abreviatura, unid_tipo::text, unid_estado, usua_id FROM siap.unidad WHERE unid_id = {unid_id} and unid_estado <> 9").
            on(
                'unid_id -> unid_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar un Unidad dado su unid_descripcion
    * @param unid_descripcion: String
    */
    def buscarPorDescripcion(unid_descripcion: String) : Future[Iterable[Unidad]] = Future[Iterable[Unidad]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.unidad WHERE unid_descripcion LIKE %{unid_descripcion}% and unid_estado = 1 ORDER BY unid_descripcion").
            on(
                'unid_descripcion -> unid_descripcion
            ).as(simple *)
        }
    }


   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.unidad WHERE unid_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }

    /**
    * Recuperar todos los Unidad activas
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size:Long, current_page:Long): Future[Iterable[Unidad]] = Future[Iterable[Unidad]] {
        db.withConnection { implicit connection =>
            SQL("SELECT unid_id, unid_descripcion, unid_abreviatura, unid_tipo::text, unid_estado, usua_id FROM siap.unidad WHERE unid_estado = 1 ORDER BY unid_descripcion LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page                
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los Unidad activas
    */
    def unidades(): Future[Iterable[Unidad]] = Future[Iterable[Unidad]] {
        db.withConnection { implicit connection =>
            SQL("SELECT unid_id, unid_descripcion, unid_abreviatura, unid_tipo::text, unid_estado, usua_id FROM siap.unidad WHERE unid_estado <> 9 ORDER BY unid_descripcion").
            as(simple *)
        }
    }    

    /**
    * Crear Unidad
    * @param unidad: Unidad
    */
    def crear(unidad: Unidad) : Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val id: Long = SQL("INSERT INTO siap.unidad (unid_descripcion, unid_abreviatura, unid_tipo, unid_estado, usua_id) VALUES ({unid_descripcion}, {unid_abreviatura}, {unid_tipo}::siap.tipodato, {unid_estado}, {usua_id})").
            on(
               'unid_descripcion -> unidad.unid_descripcion,
               'unid_abreviatura -> unidad.unid_abreviatura,
               'unid_tipo -> unidad.unid_tipo,
               'unid_estado -> unidad.unid_estado,
               'usua_id -> unidad.usua_id 
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> unidad.usua_id,
                'audi_tabla -> "unidad", 
                'audi_uid -> id,
                'audi_campo -> "unid_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> unidad.unid_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar Unidad
    * @param unidad: Unidad
    */
    def actualizar(unidad: Unidad) : Boolean = {
        val unidad_ant: Option[Unidad] = buscarPorId(unidad.unid_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
             val result: Boolean = SQL("UPDATE siap.unidad SET unid_descripcion = {unid_descripcion}, unid_abreviatura = {unid_abreviatura}, unid_tipo = {unid_tipo}::text, unid_estado = {unid_estado}, usua_id = {usua_id} WHERE unid_id = {unid_id}").
            on(
               'unid_id -> unidad.unid_id,
               'unid_descripcion -> unidad.unid_descripcion,
               'unid_abreviatura -> unidad.unid_abreviatura,
               'unid_tipo -> unidad.unid_tipo,
               'unid_estado -> unidad.unid_estado,
               'usua_id -> unidad.usua_id 
            ).executeUpdate() > 0

            if (unidad_ant != None){
                if (unidad_ant.get.unid_descripcion != unidad.unid_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> unidad.usua_id,
                    'audi_tabla -> "unidad", 
                    'audi_uid -> unidad.unid_id,
                    'audi_campo -> "unid_descripcion", 
                    'audi_valorantiguo -> unidad_ant.get.unid_descripcion,
                    'audi_valornuevo -> unidad.unid_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (unidad_ant.get.unid_abreviatura != unidad.unid_abreviatura){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> unidad.usua_id,
                    'audi_tabla -> "unidad", 
                    'audi_uid -> unidad.unid_id,
                    'audi_campo -> "unid_abreviatura", 
                    'audi_valorantiguo -> unidad_ant.get.unid_abreviatura,
                    'audi_valornuevo -> unidad.unid_abreviatura,
                    'audi_evento -> "A").
                    executeInsert()                    
                }  

                if (unidad_ant.get.unid_tipo != unidad.unid_tipo){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> unidad.usua_id,
                    'audi_tabla -> "unidad", 
                    'audi_uid -> unidad.unid_id,
                    'audi_campo -> "unid_tipo", 
                    'audi_valorantiguo -> unidad_ant.get.unid_tipo,
                    'audi_valornuevo -> unidad.unid_tipo,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                  

                if (unidad_ant.get.unid_estado != unidad.unid_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> unidad.usua_id,
                    'audi_tabla -> "unidad", 
                    'audi_uid -> unidad.unid_id,
                    'audi_campo -> "unid_estado", 
                    'audi_valorantiguo -> unidad_ant.get.unid_estado,
                    'audi_valornuevo -> unidad.unid_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

                if (unidad_ant.get.usua_id != unidad.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> unidad.usua_id,
                    'audi_tabla -> "unidad", 
                    'audi_uid -> unidad.usua_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> unidad_ant.get.usua_id,
                    'audi_valornuevo -> unidad.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                  

            }

            result
        }
    }

    /**
    * Borrar Unidad
    * @param unidad: Unidad
    */
    def borrar(unid_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.unidad SET unid_estado = 9 WHERE unid_id = {unid_id}").
            on(
                'unid_id -> unid_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "unidad", 
                    'audi_uid -> unid_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}