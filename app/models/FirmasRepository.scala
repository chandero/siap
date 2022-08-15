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

case class Firma(firm_id:Option[Long],
                firm_codigo: Option[Int],
                firm_descripcion: Option[String],
                firm_titulo: Option[String],
                firm_nombre: Option[String],
                empr_id: Option[Long])

object Firma {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val firmaWrites = new Writes[Firma] {
        def writes(firma: Firma) = Json.obj(
            "firm_id" -> firma.firm_id,
            "firm_codigo" -> firma.firm_codigo,
            "firm_descripcion" -> firma.firm_descripcion,
            "firm_titulo" -> firma.firm_titulo,
            "firm_nombre" -> firma.firm_nombre,
            "empr_id" -> firma.empr_id
        )
    }

    implicit val firmaReads: Reads[Firma] = (
        (__ \ "firm_id").readNullable[Long] and
        (__ \ "firm_codigo").readNullable[Int] and
        (__ \ "firm_descripcion").readNullable[String] and
        (__ \ "firm_titulo").readNullable[String] and
        (__ \ "firm_nombre").readNullable[String] and
        (__ \ "empr_id").readNullable[Long]
    )(Firma.apply _)

    /**
    * Parsear un Firma desde un ResultSet
    */
    val _set = {
        get[Option[Long]]("firma.firm_id") ~
        get[Option[Int]]("firma.firm_codigo") ~
        get[Option[String]]("firma.firm_descripcion") ~
        get[Option[String]]("firma.firm_titulo") ~
        get[Option[String]]("firma.firm_nombre") ~
        get[Option[Long]]("firma.empr_id") map {
            case firm_id ~ firm_codigo ~ firm_descripcion ~ firm_titulo ~ firm_nombre ~ empr_id => Firma(firm_id, firm_codigo, firm_descripcion, firm_titulo, firm_nombre, empr_id)
        }
    }

}

class FirmaRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Recuperar un Firma dado su firm_id
    * @param firm_id: Long
    */
    def buscarPorId(firm_id:Long) : Option[Firma] = {
        db.withConnection { implicit connection => 
            SQL("SELECT firm_id, firm_codigo, firm_descripcion, firm_titulo, firm_nombre, empr_id FROM siap.firma WHERE firm_id = {firm_id}").
            on(
                'firm_id -> firm_id
            ).as(Firma._set.singleOpt)
        }
    }

    /**
    * Recuperar un Firma dado su firm_codigo
    * @param firm_codigo: Long
    * @param empr_id: Long
    */
    def buscarPorCodigo(firm_codigo:Long, empr_id: Long) : Firma = {
        db.withConnection { implicit connection => 
            SQL("SELECT firm_id, firm_codigo, firm_descripcion, firm_titulo, firm_nombre, empr_id FROM siap.firma WHERE firm_codigo = {firm_codigo} and empr_id = {empr_id}").
            on(
                'firm_codigo -> firm_codigo,
                'empr_id -> empr_id
            ).as(Firma._set.single)
        }
    }    

    /**
    * Recuperar todos los Firma activas
    */
    def firmas(): Future[Iterable[Firma]] = Future[Iterable[Firma]] {
        db.withConnection { implicit connection =>
            SQL("SELECT firm_id, firm_codigo, firm_descripcion, firm_titulo, firm_nombre, empr_id FROM siap.firma").
            as(Firma._set *)
        }
    }    

    /**
    * Actualizar Firma
    * @param firma: Firma
    */
    def actualizar(firma: Firma, usua_id: Long) : Boolean = {
        val firm_ant: Option[Firma] = buscarPorId(firma.firm_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
             val result: Boolean = SQL("UPDATE siap.firma SET firm_nombre = {firm_nombre}, firm_titulo = {firm_titulo} WHERE firm_id = {firm_id}").
            on(
               'firm_id -> firma.firm_id,
               'firm_nombre -> firma.firm_nombre,
               'firm_titulo -> firma.firm_titulo
            ).executeUpdate() > 0

            if (firm_ant != None){
                if (firm_ant.get.firm_descripcion != firma.firm_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "firma", 
                    'audi_uid -> firma.firm_id,
                    'audi_campo -> "firm_titulo", 
                    'audi_valorantiguo -> firm_ant.get.firm_titulo,
                    'audi_valornuevo -> firma.firm_titulo,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (firm_ant.get.firm_nombre != firma.firm_nombre){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "firma", 
                    'audi_uid -> firma.firm_id,
                    'audi_campo -> "firm_nombre", 
                    'audi_valorantiguo -> firm_ant.get.firm_nombre,
                    'audi_valornuevo -> firma.firm_nombre,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

            }

            result
        }
    }
}