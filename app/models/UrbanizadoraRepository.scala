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

case class Urbanizadora(urba_id:Option[Long],
                urba_descripcion: Option[String],
                urba_estado: Option[Int],
                usua_id: Option[Long])

object Urbanizadora {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val urbaWrites = new Writes[Urbanizadora] {
        def writes(urba: Urbanizadora) = Json.obj(
            "urba_id" -> urba.urba_id,
            "urba_descripcion" -> urba.urba_descripcion,
            "urba_estado" -> urba.urba_estado,
            "usua_id" -> urba.usua_id
        )
    }

    implicit val urbaReads: Reads[Urbanizadora] = (
        (__ \ "urba_id").readNullable[Long] and
        (__ \ "urba_descripcion").readNullable[String] and
        (__ \ "urba_estado").readNullable[Int] and
        (__ \ "usua_id").readNullable[Long]
    )(Urbanizadora.apply _)

}

class UrbanizadoraRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un Urbanizadora desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("urba_id") ~
        get[Option[String]]("urba_descripcion") ~
        get[Option[Int]]("urba_estado") ~
        get[Option[Long]]("usua_id") map {
            case urba_id ~ urba_descripcion ~ urba_estado ~ usua_id => Urbanizadora(urba_id, urba_descripcion, urba_estado, usua_id)
        }
    }

    /**
    * Recuperar un Urbanizadora dado su urba_id
    * @param urba_id: Long
    */
    def buscarPorId(urba_id:Long) : Option[Urbanizadora] = {
        db.withConnection { implicit connection => 
            SQL("SELECT urba_id, urba_descripcion, urba_estado, usua_id FROM siap.urbanizadora WHERE urba_id = {urba_id} and urba_estado <> 9").
            on(
                'urba_id -> urba_id
            ).as(simple.singleOpt)
        }
    }

    /**
    * Recuperar un Urbanizadora dado su urba_descripcion
    * @param urba_descripcion: String
    */
    def buscarPorDescripcion(urba_descripcion: String) : Future[Iterable[Urbanizadora]] = Future[Iterable[Urbanizadora]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.urbanizadora WHERE urba_descripcion LIKE %{urba_descripcion}% and urba_estado = 1 ORDER BY urba_descripcion").
            on(
                'urba_descripcion -> urba_descripcion
            ).as(simple *)
        }
    }


   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.urbanizadora WHERE urba_estado <> 9").as(SqlParser.scalar[Long].single)
       result
     }
   }

    /**
    * Recuperar todos los Urbanizadora activas
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size:Long, current_page:Long): Future[Iterable[Urbanizadora]] = Future[Iterable[Urbanizadora]] {
        db.withConnection { implicit connection =>
            SQL("SELECT urba_id, urba_descripcion, urba_estado, usua_id FROM siap.urbanizadora WHERE urba_estado = 1 ORDER BY urba_id LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page                
            ).as(simple *)
        }
    }

    /**
    * Recuperar todos los Urbanizadora activas
    */
    def urbanizadoras(): Future[Iterable[Urbanizadora]] = Future[Iterable[Urbanizadora]] {
        db.withConnection { implicit connection =>
            SQL("SELECT urba_id, urba_descripcion, urba_estado, usua_id FROM siap.urbanizadora WHERE urba_estado <> 9 ORDER BY urba_descripcion").
            as(simple *)
        }
    }    

    /**
    * Crear Urbanizadora
    * @param urba: Urbanizadora
    */
    def crear(urba: Urbanizadora) : Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val id: Long = SQL("INSERT INTO siap.urbanizadora (urba_descripcion, urba_estado, usua_id) VALUES ({urba_descripcion}, {urba_estado}, {usua_id})").
            on(
               'urba_descripcion -> urba.urba_descripcion,
               'urba_estado -> urba.urba_estado,
               'usua_id -> urba.usua_id 
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> urba.usua_id,
                'audi_tabla -> "urba", 
                'audi_uid -> id,
                'audi_campo -> "urba_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> urba.urba_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar Urbanizadora
    * @param urba: Urbanizadora
    */
    def actualizar(urba: Urbanizadora) : Boolean = {
        val urba_ant: Option[Urbanizadora] = buscarPorId(urba.urba_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
             val result: Boolean = SQL("UPDATE siap.urbanizadora SET urba_descripcion = {urba_descripcion}, urba_estado = {urba_estado}, usua_id = {usua_id} WHERE urba_id = {urba_id}").
            on(
               'urba_id -> urba.urba_id,
               'urba_descripcion -> urba.urba_descripcion,
               'urba_estado -> urba.urba_estado,
               'usua_id -> urba.usua_id 
            ).executeUpdate() > 0

            if (urba_ant != None){
                if (urba_ant.get.urba_descripcion != urba.urba_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> urba.usua_id,
                    'audi_tabla -> "urba", 
                    'audi_uid -> urba.urba_id,
                    'audi_campo -> "urba_descripcion", 
                    'audi_valorantiguo -> urba_ant.get.urba_descripcion,
                    'audi_valornuevo -> urba.urba_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (urba_ant.get.urba_estado != urba.urba_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> urba.usua_id,
                    'audi_tabla -> "urba", 
                    'audi_uid -> urba.urba_id,
                    'audi_campo -> "urba_estado", 
                    'audi_valorantiguo -> urba_ant.get.urba_estado,
                    'audi_valornuevo -> urba.urba_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

                if (urba_ant.get.usua_id != urba.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> urba.usua_id,
                    'audi_tabla -> "urba", 
                    'audi_uid -> urba.usua_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> urba_ant.get.usua_id,
                    'audi_valornuevo -> urba.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                  

            }

            result
        }
    }

    /**
    * Borrar Urbanizadora
    * @param urba: Urbanizadora
    */
    def borrar(urba_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.urbanizadora SET urba_estado = 9 WHERE urba_id = {urba_id}").
            on(
                'urba_id -> urba_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "urba", 
                    'audi_uid -> urba_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}