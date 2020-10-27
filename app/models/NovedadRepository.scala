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

case class Novedad(nove_id: Option[scala.Long], 
                  nove_descripcion: Option[String],
                  nove_estado: Option[Int],
                  nove_tipo: Option[Int],
                  empr_id: Option[Long],
                  usua_id: Option[Long])

object Novedad {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val wWrites = new Writes[Novedad] {
        def writes(n: Novedad) = Json.obj(
           "nove_id" -> n.nove_id,
           "nove_descripcion" -> n.nove_descripcion,
           "nove_estado" -> n.nove_estado,
           "nove_tipo" -> n.nove_tipo,
           "empr_id" -> n.empr_id,
           "usua_id" -> n.usua_id
        )
    }

    implicit val rReads: Reads[Novedad] = (
        (__ \ "nove_id").readNullable[Long] and
        (__ \ "nove_descripcion").readNullable[String] and
        (__ \ "nove_estado").readNullable[Int] and
        (__ \ "nove_tipo").readNullable[Int] and        
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long]
    )(Novedad.apply _)

    val _set = {
        get[Option[Long]]("nove_id") ~
        get[Option[String]]("nove_descripcion") ~ 
        get[Option[Int]]("nove_estado") ~
        get[Option[Int]]("nove_tipo") ~
        get[Option[Long]]("empr_id") ~
        get[Option[Long]]("usua_id") map {
            case nove_id ~ nove_descripcion ~ nove_estado ~ nove_tipo ~ empr_id ~ usua_id => Novedad(nove_id, nove_descripcion, nove_estado, nove_tipo, empr_id, usua_id)
        }
    }

}

class NovedadRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Recuperar un Novedad por su nove_id
    * @param nove_id: Long
    */
    def buscarPorId(nove_id: Long) : Option[Novedad] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.novedad WHERE nove_id = {nove_id} ").
            on(
                'nove_id -> nove_id
            ).as(Novedad._set.singleOpt)
        }
    }

    /**
    * Recuperar Novedad por su descripcion
    * @param nove_descripcion: String
    */
    def buscarPorDescripcion(nove_descripcion: String) : Future[Iterable[Novedad]] = Future[Iterable[Novedad]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.novedad WHERE nove_descripcion LIKE %{nove_descripcion}% ORDER BY nove_descripcion").
            on(
                'nove_descripcion -> nove_descripcion
            ).as(Novedad._set *)
        }
    }

    /**
    * Recuperar todos los novedad
    * @param page_size: Long
    * @param current_page: Long
    * @return Future[Iterable[Novedad]]
    */
    def todos(page_size: Long, current_page: Long, nove_tipo: Int) : Future[Iterable[Novedad]] = Future[Iterable[Novedad]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.novedad WHERE nove_tipo = {nove_tipo} ORDER BY nove_descripcion").
            on(
                'nove_tipo -> nove_tipo
            ).
            as(Novedad._set *)
        }
    }

    /**
    * Recuperar todos los novedad
    * @return Future[Iterable[Novedad]]
    */
    def novedades(nove_tipo: Int) : Future[Iterable[Novedad]] = Future[Iterable[Novedad]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.novedad WHERE nove_tipo = {nove_tipo} ORDER BY nove_id").
            on(
                'nove_tipo -> nove_tipo
            ).
            as(Novedad._set *)
        }
    }

    /**
    * Crear Novedad
    * @param novedad: Novedad
    */
    def crear(novedad: Novedad) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.novedad (nove_descripcion, nove_estado, nove_tipo, empr_id, usua_id) VALUES ({nove_descripcion}, {nove_estado}, {empr_id}, {usua_id})").
            on(
                'nove_descripcion -> novedad.nove_descripcion,
                'nove_estado -> novedad.nove_estado,
                'nove_tipo -> novedad.nove_tipo,
                'empr_id -> novedad.empr_id,
                'usua_id -> novedad.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> novedad.usua_id,
                'audi_tabla -> "novedad", 
                'audi_uid -> id,
                'audi_campo -> "nove_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> novedad.nove_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar Novedad
    * @param novedad: Novedad
    */
    def actualizar(novedad: Novedad): Boolean = {
        val origen_ant: Option[Novedad] = buscarPorId(novedad.nove_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val result: Boolean = SQL("UPDATE siap.novedad SET nove_descripcion = {nove_descripcion}, nove_estado = {nove_estado}, nove_tipo = {nove_tipo},usua_id = {usua_id} WHERE nove_id = {nove_id}").
            on(
                'nove_id -> novedad.nove_id,
                'nove_descripcion -> novedad.nove_descripcion,
                'nove_estado -> novedad.nove_estado,
                'nove_tipo -> novedad.nove_tipo,
                'usua_id -> novedad.usua_id
            ).executeUpdate() > 0
 
            if (origen_ant != None){
                if (origen_ant.get.nove_descripcion != novedad.nove_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> novedad.usua_id,
                    'audi_tabla -> "novedad", 
                    'audi_uid -> novedad.nove_id,
                    'audi_campo -> "nove_descripcion", 
                    'audi_valorantiguo -> origen_ant.get.nove_descripcion,
                    'audi_valornuevo -> novedad.nove_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (origen_ant.get.nove_estado != novedad.nove_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> novedad.usua_id,
                    'audi_tabla -> "novedad", 
                    'audi_uid -> novedad.nove_id,
                    'audi_campo -> "nove_estado", 
                    'audi_valorantiguo -> origen_ant.get.nove_estado,
                    'audi_valornuevo -> novedad.nove_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }  
                if (origen_ant.get.usua_id != novedad.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> novedad.usua_id,
                    'audi_tabla -> "novedad", 
                    'audi_uid -> novedad.nove_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> origen_ant.get.usua_id,
                    'audi_valornuevo -> novedad.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                
            }

            result
        }
    }

    /**
    * Eliminar un Novedad
    * @param novedad: Novedad
    */
    def borrar(nove_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:Long = SQL("UPDATE siap.novedad SET nove_estado = 9 WHERE nove_id = {nove_id}").
            on(
                'nove_id -> nove_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "novedad", 
                    'audi_uid -> nove_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

}