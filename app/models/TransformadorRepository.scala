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


case class Transformador(tran_id: Option[Long], 
                         tran_numero: Option[String], 
                         empr_id: Option[Long], 
                         usua_id: Option[Long], 
                         tran_direccion: Option[String], 
                         barr_id: Option[Long], 
                         tran_estado: Option[Int])

case class InformeT(tran_numero: Option[String], 
                    tran_direccion: Option[String], 
                    barr_descripcion: Option[String], 
                    cantidad: Option[Int])

object Transformador {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val TransformadorWrites = new Writes[Transformador] {
        def writes(m: Transformador) = Json.obj(
            "tran_id" -> m.tran_id,
            "tran_numero" -> m.tran_numero,
            "empr_id" -> m.empr_id,
            "usua_id" -> m.usua_id,            
            "tran_direccion" -> m.tran_direccion,
            "barr_id" -> m.barr_id,
            "tran_estado" -> m.tran_estado
        )
    }

    implicit val TransformadorReads: Reads[Transformador] = (
        (__ \ "tran_id").readNullable[Long] and
        (__ \ "tran_numero").readNullable[String] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long] and
        (__ \ "tran_direccion").readNullable[String] and
        (__ \ "barr_id").readNullable[Long] and
        (__ \ "tran_estado").readNullable[Int]
    )(Transformador.apply _)

    val _set = {
      get[Option[Long]]("tran_id") ~
      get[Option[String]]("tran_numero") ~ 
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[Long]]("barr_id") ~
      get[Option[String]]("tran_direccion") ~
      get[Option[Int]]("tran_estado") map {
          case tran_id ~
               tran_numero ~
               empr_id ~
               usua_id ~
               barr_id ~
               tran_direccion ~
               tran_estado => Transformador(tran_id,
               tran_numero,
               empr_id,
               usua_id,
               tran_direccion,
               barr_id,
               tran_estado)
      }
  }    
}

object InformeT {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val mWrites = new Writes[InformeT] {
        def writes(m: InformeT) = Json.obj(
            "tran_numero" -> m.tran_numero,
            "tran_direccion" -> m.tran_direccion,
            "barr_descripcion" -> m.barr_descripcion,
            "cantidad" -> m.cantidad
        )
    }

    val _set = {
      get[Option[String]]("tran_numero") ~ 
      get[Option[String]]("tran_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[Int]]("cantidad") map {
          case tran_numero ~
               tran_direccion ~
               barr_descripcion ~
               cantidad => InformeT(tran_numero,
                                   tran_direccion,
                                   barr_descripcion,
                                   cantidad)
      }
  }    
}


class TransformadorRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoTransformador desde un ResultSet
    */


    /**
    * Recuperar un Transformador dado su tran_id
    * @param tran_id: Long
    */
    def buscarPorId(tran_id: Long, empr_id: Long) : Option[Transformador] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.transformador WHERE tran_id = {tran_id} and empr_id = {empr_id}").
            on(
                'tran_id -> tran_id,
                'empr_id -> empr_id
            ).
            as(Transformador._set.singleOpt)
        }
    }

   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(empr_id: Long): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.transformador WHERE tran_estado <> 9 and empr_id = {empr_id}")
       .on(
         'empr_id -> empr_id
       )
       .as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los Transformador activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todos(page_size: Long, current_page: Long, empr_id: Long): Future[Iterable[Transformador]] = Future[Iterable[Transformador]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.transformador WHERE tran_estado <> 9 and empr_id = {empr_id} LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) ORDER BY tran_id").
            on(
              'page_size -> page_size,
              'current_page -> current_page,
              'empr_id -> empr_id
            ).as(Transformador._set *)
        }        
    }

    /**
    * Recuperar todos los TipoTransformador activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def transformadores(empr_id: Long): Future[Iterable[Transformador]] = Future[Iterable[Transformador]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.transformador WHERE tran_estado <> 9 and empr_id = {empr_id} ORDER BY tran_id").
            on(
              'empr_id -> empr_id
            ).
            as(Transformador._set *)
        }        
    }    

    /**
    * Crear TipoTransformador
    * @param Transformador: Transformador
    */
    def crear(transformador: Transformador) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("""INSERT INTO siap.transformador (
            tran_numero,
            empr_id,
            usua_id,
            tran_direccion,
            barr_id,
            tran_estado) VALUES (
            {tran_numero},
            {empr_id},
            {usua_id},
            {tran_direccion},
            {barr_id},
            {tran_estado})""").
            on(
              'tran_numero -> transformador.tran_numero,
              'empr_id -> transformador.empr_id,
              'usua_id -> transformador.usua_id,
              'tran_direccion -> transformador.tran_direccion,
              'barr_id -> transformador.barr_id,
              'tran_estado -> 1
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> transformador.usua_id,
                'audi_tabla -> "Transformador", 
                'audi_uid -> id,
                'audi_campo -> "tran_numero", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> transformador.tran_numero,
                'audi_evento -> "I").
                executeInsert()


            id
        }
    }

    /**
    * Actualizar Transformador
    * @param Transformador: Transformador
    */
    def actualizar(transformador: Transformador) : Boolean = {
        val transformador_ant: Option[Transformador] = buscarPorId(transformador.tran_id.get, transformador.empr_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.transformador SET tran_numero = {tran_numero}, usua_id = {usua_id}, tran_direccion = {tran_direccion}, barr_id = {barr_id}, tran_estado = {tran_estado} WHERE tran_id = {tran_id} and empr_id = {empr_id}").
            on(
              'tran_id -> transformador.tran_id,
              'tran_numero -> transformador.tran_numero,
              'empr_id -> transformador.empr_id,
              'usua_id -> transformador.usua_id,
              'tran_direccion -> transformador.tran_direccion,
              'barr_id -> transformador.barr_id,
              'tran_estado -> transformador.tran_estado
            ).executeUpdate() > 0

            if (transformador_ant != None){
                if (transformador_ant.get.tran_numero != transformador.tran_numero){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> transformador.usua_id,
                    'audi_tabla -> "Transformador", 
                    'audi_uid -> transformador.tran_numero,
                    'audi_campo -> "tran_numero", 
                    'audi_valorantiguo -> transformador_ant.get.tran_numero,
                    'audi_valornuevo -> transformador.tran_numero,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (transformador_ant.get.usua_id != transformador.usua_id){
                    SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                            'audi_fecha -> fecha,
                            'audi_hora -> hora,
                            'usua_id -> transformador.usua_id,
                            'audi_tabla -> "Transformador", 
                            'audi_uid -> transformador.usua_id,
                            'audi_campo -> "usua_id", 
                            'audi_valorantiguo -> transformador_ant.get.usua_id,
                            'audi_valornuevo -> transformador.usua_id,
                            'audi_evento -> "A").
                            executeInsert()                    
                        }
                        if (transformador_ant.get.tran_direccion != transformador.tran_direccion){
                          SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                          on(
                              'audi_fecha -> fecha,
                              'audi_hora -> hora,
                              'usua_id -> transformador.usua_id,
                              'audi_tabla -> "Transformador", 
                              'audi_uid -> transformador.tran_direccion,
                              'audi_campo -> "tran_direccion", 
                              'audi_valorantiguo -> transformador_ant.get.tran_direccion,
                              'audi_valornuevo -> transformador.tran_direccion,
                              'audi_evento -> "A").
                              executeInsert()                    
                          }
                          if (transformador_ant.get.barr_id != transformador.barr_id){
                            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                            on(
                                'audi_fecha -> fecha,
                                'audi_hora -> hora,
                                'usua_id -> transformador.usua_id,
                                'audi_tabla -> "Transformador", 
                                'audi_uid -> transformador.barr_id,
                                'audi_campo -> "barr_id", 
                                'audi_valorantiguo -> transformador_ant.get.barr_id,
                                'audi_valornuevo -> transformador.barr_id,
                                'audi_evento -> "A").
                                executeInsert()                    
                            }        
            }

            result
        }
    }

    /**
    * Elimnar TipoTransformador
    * @param Transformador: TipoTransformador
    */
    def borrar(tran_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.transformador SET tran_estado = 9 WHERE tran_id = {tran_id}").
            on(
                'tran_id -> tran_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "Transformador", 
                    'audi_uid -> tran_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

    def informe_siap_transformador(empr_id: scala.Long): Future[Iterable[InformeT]] = Future[Iterable[InformeT]] {
        db.withConnection { implicit connection => 
            SQL("""SELECT m.tran_numero, m.tran_direccion, b.barr_descripcion, COUNT(a.*) AS cantidad FROM siap.transformador m
            LEFT JOIN siap.aap_transformador am ON am.tran_id = m.tran_id AND am.empr_id = m.empr_id
            LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
            LEFT JOIN siap.barrio b ON b.barr_id = m.barr_id
            WHERE m.empr_id = {empr_id}
            GROUP BY m.tran_numero, m.tran_direccion, b.barr_descripcion
            ORDER BY m.tran_numero""").
            on(
                'empr_id -> empr_id
            ).
            as(InformeT._set *)
        }
    }
}