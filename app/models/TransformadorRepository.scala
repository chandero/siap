package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str, int, date, float }
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
                         barr_id: Option[Long],
                         tran_direccion: Option[String],
                         tran_codigo_apoyo: Option[String],
                         tran_propietario: Option[String],
                         tran_marca: Option[String],
                         tran_serial: Option[String],
                         tran_kva: Option[Double],
                         tipo_id: Option[Int],
                         tran_fases: Option[String],
                         tran_tension_p: Option[Double],
                         tran_tension_s: Option[Double],
                         tran_referencia: Option[String],
                         tran_estado: Option[Int])

case class InformeT( tran_id: Option[Long],
                     tran_numero: Option[String], 
                     tran_direccion: Option[String], 
                     tran_codigo_apoyo: Option[String],
                     tran_propietario: Option[String],
                     tran_marca: Option[String],
                     tran_serial: Option[String],
                     tran_kva: Option[Double],
                     tipo_id: Option[Int],
                     tran_fases: Option[String],
                     tran_tension_p: Option[Double],
                     tran_tension_s: Option[Double],
                     tran_referencia: Option[String],
                     tran_estado: Option[Int], 
                     barr_descripcion: Option[String], 
                     tipo_descripcion: Option[String],
                     cantidad: Option[Int])

object Transformador {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    val _set = {
      get[Option[Long]]("tran_id") ~
      get[Option[String]]("tran_numero") ~ 
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[Long]]("barr_id") ~
      get[Option[String]]("tran_direccion") ~
      get[Option[String]]("tran_codigo_apoyo") ~
      get[Option[String]]("tran_propietario") ~
      get[Option[String]]("tran_marca") ~
      get[Option[String]]("tran_serial") ~
      get[Option[Double]]("tran_kva") ~
      get[Option[Int]]("tipo_id") ~
      get[Option[String]]("tran_fases") ~
      get[Option[Double]]("tran_tension_p") ~
      get[Option[Double]]("tran_tension_s") ~
      get[Option[String]]("tran_referencia") ~
      get[Option[Int]]("tran_estado") map {
          case tran_id ~
               tran_numero ~
               empr_id ~
               usua_id ~
               barr_id ~
               tran_direccion ~
               tran_codigo_apoyo ~
               tran_propietario ~
               tran_marca ~
               tran_serial ~
               tran_kva ~
               tipo_id ~
               tran_fases ~
               tran_tension_p ~
               tran_tension_s ~
               tran_referencia ~
               tran_estado => Transformador(
                 tran_id,
                 tran_numero,
                 empr_id,
                 usua_id,
                 barr_id,               
                 tran_direccion,
                 tran_codigo_apoyo,
                 tran_propietario,
                 tran_marca,
                 tran_serial,
                 tran_kva,
                 tipo_id,
                 tran_fases,
                 tran_tension_p,
                 tran_tension_s,
                 tran_referencia,
                 tran_estado)
      }
  }    
}

object InformeT {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    val _set = {
      get[Option[Long]]("tran_id") ~
      get[Option[String]]("tran_numero") ~ 
      get[Option[String]]("tran_direccion") ~
      get[Option[String]]("tran_codigo_apoyo") ~
      get[Option[String]]("tran_propietario") ~
      get[Option[String]]("tran_marca") ~
      get[Option[String]]("tran_serial") ~
      get[Option[Double]]("tran_kva") ~
      get[Option[Int]]("tipo_id") ~
      get[Option[String]]("tran_fases") ~
      get[Option[Double]]("tran_tension_p") ~
      get[Option[Double]]("tran_tension_s") ~
      get[Option[String]]("tran_referencia") ~
      get[Option[Int]]("tran_estado") ~      
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("tipo_descripcion") ~
      get[Option[Int]]("cantidad") map {
          case tran_id ~
               tran_numero ~
               tran_direccion ~
               tran_codigo_apoyo ~
               tran_propietario ~
               tran_marca ~
               tran_serial ~
               tran_kva ~
               tipo_id ~
               tran_fases ~
               tran_tension_p ~
               tran_tension_s ~
               tran_referencia ~
               tran_estado ~
               barr_descripcion ~  
               tipo_descripcion ~           
               cantidad => InformeT(
                 tran_id,
                 tran_numero,
                 tran_direccion,
                 tran_codigo_apoyo,
                 tran_propietario,
                 tran_marca,
                 tran_serial,
                 tran_kva,
                 tipo_id,
                 tran_fases,
                 tran_tension_p,
                 tran_tension_s,
                 tran_referencia,
                 tran_estado,
                 barr_descripcion,
                 tipo_descripcion,
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
            val id: Long = SQL("""INSERT INTO siap.transformador
                    (empr_id, tran_numero, tran_direccion, barr_id, usua_id, tran_estado, tran_codigo_apoyo, tran_propietario, tran_marca, tran_serial, tran_kva, tipo_id, tran_fases, tran_tension_p, tran_tension_s, tran_referencia)
                    VALUES({empr_id}, {tran_numero}, {tran_direccion}, {barr_id}, {usua_id}, {tran_estado}, {tran_codigo_apoyo}, {tran_propietario}, {tran_marca}, {tran_serial}, {tran_kva}, {tipo_id}, {tran_fases}, {tran_tension_p}, {tran_tension_s}, {tran_referencia});""").
            on(
              'tran_numero -> transformador.tran_numero,
              'empr_id -> transformador.empr_id,
              'usua_id -> transformador.usua_id,
              'tran_direccion -> transformador.tran_direccion,
              'barr_id -> transformador.barr_id,
              'tran_codigo_apoyo -> transformador.tran_codigo_apoyo,
              'tran_propietario -> transformador.tran_propietario,
              'tran_marca -> transformador.tran_marca,
              'tran_serial -> transformador.tran_serial,
              'tran_kva -> transformador.tran_kva,
              'tipo_id -> transformador.tipo_id,
              'tran_fases -> transformador.tran_fases,
              'tran_tension_p -> transformador.tran_tension_p,
              'tran_tension_s -> transformador.tran_tension_s,
              'tran_referencia -> transformador.tran_referencia,
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
            val result: Boolean = SQL("""UPDATE siap.transformador
                         SET tran_numero = {tran_numero},
                            tran_direccion = {tran_direccion},
                            barr_id = {barr_id},
                            usua_id = {usua_id},
                            tran_estado = {tran_estado},
                            tran_codigo_apoyo = {tran_codigo_apoyo},
                            tran_propietario = {tran_propietario},
                            tran_marca = {tran_marca},
                            tran_serial = {tran_serial},
                            tran_kva = {tran_kva},
                            tipo_id = {tipo_id},
                            tran_fases = {tran_fases},
                            tran_tension_p = {tran_tension_p},
                            tran_tension_s = {tran_tension_s},
                            tran_referencia = {tran_referencia}
                         WHERE tran_id = {tran_id} and empr_id = {empr_id}""").
            on(
                'tran_id -> transformador.tran_id,
                'tran_numero -> transformador.tran_numero,
                'empr_id -> transformador.empr_id,
                'usua_id -> transformador.usua_id,
                'barr_id -> transformador.barr_id,
                'tran_direccion -> transformador.tran_direccion,
                'tran_codigo_apoyo -> transformador.tran_codigo_apoyo,
                'tran_propietario -> transformador.tran_propietario,
                'tran_marca -> transformador.tran_marca,
                'tran_serial -> transformador.tran_serial,
                'tran_kva -> transformador.tran_kva,
                'tipo_id -> transformador.tipo_id,
                'tran_fases -> transformador.tran_fases,
                'tran_tension_p -> transformador.tran_tension_p,
                'tran_tension_s -> transformador.tran_tension_s,
                'tran_referencia -> transformador.tran_referencia,
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

    def buscarParaVerificar(aap_id: Long, empr_id: Long): Int = {
      db.withConnection { implicit connection =>
        var result = 0
        val _aapParser = int("aap_id") ~ int("esta_id") map { case a ~ e => (a,e)}
        val a = SQL("""SELECT a.tran_id AS aap_id, a.tran_estado as esta_id FROM siap.transformador a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE a.tran_id = {aap_id} and a.empr_id = {empr_id}""").
        on(
            'aap_id -> aap_id,
            'empr_id -> empr_id
        ).as(_aapParser.singleOpt)
        a match {
            case None => result = 404
            case Some(a) => 
                if (a._2 == 9) {
                  result = 401
                } else {
                  result = 200
                }
        }
        result
      }
    }   
    
    /**
    * Recuperar un aap por su aap_id
    */
    def buscarParaEditar(aap_id: Long, empr_id: Long): Option[Transformador] = {
      db.withConnection { implicit connection => 
        SQL("""SELECT a.*, b.*, t.* FROM siap.transformador a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE a.tran_id = {aap_id} and a.empr_id = {empr_id}""").
        on('aap_id -> aap_id, 'empr_id-> empr_id).as(Transformador._set.singleOpt)
      }
    }      

    def informe_siap_transformador(empr_id: scala.Long): Future[Iterable[InformeT]] = Future[Iterable[InformeT]] {
        db.withConnection { implicit connection => 
            SQL("""SELECT m.*, b.barr_descripcion, tp.tipo_descripcion, COUNT(a.*) AS cantidad FROM siap.transformador m
            LEFT JOIN siap.aap_transformador am ON am.tran_id = m.tran_id AND am.empr_id = m.empr_id
            LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
            LEFT JOIN siap.barrio b ON b.barr_id = m.barr_id
            LEFT JOIN siap.tipo_poste tp ON tp.tipo_id = m.tipo_id
            WHERE m.empr_id = {empr_id}
            GROUP BY m.tran_id, m.tran_direccion, b.barr_descripcion, tp.tipo_descripcion
            ORDER BY m.tran_numero""").
            on(
                'empr_id -> empr_id
            ).
            as(InformeT._set *)
        }
    }
}