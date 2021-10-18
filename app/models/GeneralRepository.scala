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

case class General(gene_id: Option[Long], 
                   gene_parametro: Option[String], 
                   gene_valor: Option[String],
                   empr_id: Option[Long], 
                   gene_numero: Option[Int])


object General {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val GeneralWrites = new Writes[General] {
        def writes(m: General) = Json.obj(
            "gene_id" -> m.gene_id,
            "gene_parametro" -> m.gene_parametro,
            "gene_valor" -> m.gene_valor,
            "empr_id" -> m.empr_id,
            "gene_numero" -> m.gene_numero
        )
    }

    implicit val GeneralReads: Reads[General] = (
        (__ \ "gene_id").readNullable[Long] and
        (__ \ "gene_parametro").readNullable[String] and
        (__ \ "gene_valor").readNullable[String] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "gene_numero").readNullable[Int]
    )(General.apply _)

    val _set = {
      get[Option[Long]]("gene_id") ~
      get[Option[String]]("gene_parametro") ~ 
      get[Option[String]]("gene_valor") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Int]]("gene_numero") map {
          case gene_id ~
               gene_parametro ~
               gene_valor ~
               empr_id ~
               gene_numero => General(gene_id,
               gene_parametro,
               gene_valor,
               empr_id,
               gene_numero)
      }
  }    
}

class GeneralRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Recuperar un General dado su tran_id
    * @param gene_id: Long
    */
    def buscarPorId(gene_id: Long, empr_id: Long) : Option[General] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.general WHERE gene_id = {gene_id} and empr_id = {empr_id}").
            on(
                'gene_id -> gene_id,
                'empr_id -> empr_id
            ).
            as(General._set.singleOpt)
        }
    }

   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(empr_id: Long): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.general WHERE empr_id = {empr_id}")
       .on(
         'empr_id -> empr_id
       )
       .as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los General
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todos(page_size: Long, current_page: Long, empr_id: Long): Future[Iterable[General]] = Future[Iterable[General]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.general WHERE empr_id = {empr_id} LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) ORDER BY gene_id").
            on(
              'page_size -> page_size,
              'current_page -> current_page,
              'empr_id -> empr_id
            ).as(General._set *)
        }        
    }

    /**
    * Actualizar General
    * @param general: General
    */
    def actualizar(general: General, usua_id: scala.Long) : Boolean = {
        val general_ant: Option[General] = buscarPorId(general.gene_id.get, general.empr_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.general SET gene_valor = {gene_valor}, gene_numero = {gene_numero} WHERE gene_id = {gene_id} and empr_id = {empr_id}").
            on(
              'gene_id -> general.gene_id,
              'gene_numero -> general.gene_numero,
              'gene_varor -> general.gene_valor,
              'empr_id -> general.empr_id,
            ).executeUpdate() > 0

            if (general_ant != None){
                if (general_ant.get.gene_numero != general.gene_numero){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "General", 
                    'audi_uid -> general.gene_numero,
                    'audi_campo -> "gene_numero", 
                    'audi_valorantiguo -> general_ant.get.gene_numero,
                    'audi_valornuevo -> general.gene_numero,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (general_ant.get.gene_valor != general.gene_valor){
                    SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                        'audi_fecha -> fecha,
                        'audi_hora -> hora,
                        'usua_id -> usua_id,
                        'audi_tabla -> "General", 
                        'audi_uid -> general.gene_valor,
                        'audi_campo -> "gene_valor", 
                        'audi_valorantiguo -> general_ant.get.gene_valor,
                        'audi_valornuevo -> general.gene_valor,
                        'audi_evento -> "A").
                        executeInsert()                    
                    }
            }

            result
        }
    }
}