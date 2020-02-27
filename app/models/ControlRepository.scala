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


case class Control(aap_id: Option[Long], 
                   empr_id: Option[Long], 
                   usua_id: Option[Long], 
                   aap_direccion: Option[String], 
                   barr_id: Option[Long], 
                   esta_id: Option[Int])

case class InformeC(aap_id: Option[Int],
                    aap_direccion: Option[String], 
                    barr_descripcion: Option[String], 
                    cantidad: Option[Int])

object Control {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val ControlWrites = new Writes[Control] {
        def writes(m: Control) = Json.obj(
            "aap_id" -> m.aap_id,
            "empr_id" -> m.empr_id,
            "usua_id" -> m.usua_id,            
            "aap_direccion" -> m.aap_direccion,
            "barr_id" -> m.barr_id,
            "esta_id" -> m.esta_id
        )
    }

    implicit val ControlReads: Reads[Control] = (
        (__ \ "aap_id").readNullable[Long] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long] and
        (__ \ "aap_direccion").readNullable[String] and
        (__ \ "barr_id").readNullable[Long] and
        (__ \ "esta_id").readNullable[Int]
    )(Control.apply _)

    val _set = {
      get[Option[Long]]("aap_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[String]]("aap_direccion") ~      
      get[Option[Long]]("barr_id") ~
      get[Option[Int]]("esta_id") map {
          case aap_id ~
               empr_id ~
               usua_id ~
               aap_direccion ~
               barr_id ~               
               esta_id => Control(aap_id,
               empr_id,
               usua_id,
               aap_direccion,
               barr_id,
               esta_id)
      }
  }    
}

object InformeC {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val mWrites = new Writes[InformeC] {
        def writes(m: InformeC) = Json.obj(
            "aap_id" -> m.aap_id,
            "aap_direccion" -> m.aap_direccion,
            "barr_descripcion" -> m.barr_descripcion,
            "cantidad" -> m.cantidad
        )
    }

    val _set = {
      get[Option[Int]]("aap_id") ~ 
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[Int]]("cantidad") map {
          case aap_id ~
               aap_direccion ~
               barr_descripcion ~
               cantidad => InformeC(aap_id,
                                   aap_direccion,
                                   barr_descripcion,
                                   cantidad)
      }
  }
}


class ControlRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoControl desde un ResultSet
    */


    /**
    * Recuperar un Control dado su aap_id
    * @param aap_id: Long
    */
    def buscarPorId(aap_id: Long, empr_id: Long) : Option[Control] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.control WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).
            as(Control._set.singleOpt)
        }
    }

   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(empr_id: Long): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.control WHERE esta_id <> 9 and empr_id = {empr_id}")
       .on(
         'empr_id -> empr_id
       )
       .as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los Control activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todos(page_size: Long, current_page: Long, empr_id: Long): Future[Iterable[Control]] = Future[Iterable[Control]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.control WHERE esta_id <> 9 and empr_id = {empr_id} LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) ORDER BY aap_id").
            on(
              'page_size -> page_size,
              'current_page -> current_page,
              'empr_id -> empr_id
            ).as(Control._set *)
        }        
    }

    /**
    * Recuperar todos los TipoControl activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def controles(empr_id: Long): Future[Iterable[Control]] = Future[Iterable[Control]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.control WHERE esta_id <> 9 and empr_id = {empr_id} ORDER BY aap_id").
            on(
              'empr_id -> empr_id
            ).
            as(Control._set *)
        }        
    }    

    /**
    * Crear TipoControl
    * @param Control: Control
    */
    def crear(control: Control) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("""INSERT INTO siap.control (
            aap_id,
            empr_id,
            usua_id,
            aap_direccion,
            barr_id,
            esta_id) VALUES (
            {aap_id},
            {empr_id},
            {usua_id},
            {aap_direccion},
            {barr_id},
            {esta_id})""").
            on(
              'aap_id -> control.aap_id,
              'empr_id -> control.empr_id,
              'usua_id -> control.usua_id,
              'aap_direccion -> control.aap_direccion,
              'barr_id -> control.barr_id,
              'esta_id -> 1
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> control.usua_id,
                'audi_tabla -> "control", 
                'audi_uid -> id,
                'audi_campo -> "aap_id", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> control.aap_id,
                'audi_evento -> "I").
                executeInsert()

            id
        }
    }

    /**
    * Actualizar Control
    * @param Control: Control
    */
    def actualizar(control: Control) : Boolean = {
        val control_ant: Option[Control] = buscarPorId(control.aap_id.get, control.empr_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.control SET aap_id = {aap_id}, usua_id = {usua_id}, aap_direccion = {aap_direccion}, barr_id = {barr_id}, esta_id = {esta_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
              'aap_id -> control.aap_id,
              'empr_id -> control.empr_id,
              'usua_id -> control.usua_id,
              'aap_direccion -> control.aap_direccion,
              'barr_id -> control.barr_id,
              'esta_id -> control.esta_id
            ).executeUpdate() > 0

            if (control_ant != None){
                if (control_ant.get.aap_id != control.aap_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> control.usua_id,
                    'audi_tabla -> "control", 
                    'audi_uid -> control.aap_id,
                    'audi_campo -> "aap_id", 
                    'audi_valorantiguo -> control_ant.get.aap_id,
                    'audi_valornuevo -> control.aap_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (control_ant.get.usua_id != control.usua_id){
                    SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                            'audi_fecha -> fecha,
                            'audi_hora -> hora,
                            'usua_id -> control.usua_id,
                            'audi_tabla -> "control", 
                            'audi_uid -> control.usua_id,
                            'audi_campo -> "usua_id", 
                            'audi_valorantiguo -> control_ant.get.usua_id,
                            'audi_valornuevo -> control.usua_id,
                            'audi_evento -> "A").
                            executeInsert()                    
                        }
                        if (control_ant.get.aap_direccion != control.aap_direccion){
                          SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                          on(
                              'audi_fecha -> fecha,
                              'audi_hora -> hora,
                              'usua_id -> control.usua_id,
                              'audi_tabla -> "control", 
                              'audi_uid -> control.aap_direccion,
                              'audi_campo -> "aap_direccion", 
                              'audi_valorantiguo -> control_ant.get.aap_direccion,
                              'audi_valornuevo -> control.aap_direccion,
                              'audi_evento -> "A").
                              executeInsert()                    
                          }
                          if (control_ant.get.barr_id != control.barr_id){
                            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                            on(
                                'audi_fecha -> fecha,
                                'audi_hora -> hora,
                                'usua_id -> control.usua_id,
                                'audi_tabla -> "control", 
                                'audi_uid -> control.barr_id,
                                'audi_campo -> "barr_id", 
                                'audi_valorantiguo -> control_ant.get.barr_id,
                                'audi_valornuevo -> control.barr_id,
                                'audi_evento -> "A").
                                executeInsert()                    
                            }        
            }

            result
        }
    }

    /**
    * Elimnar TipoControl
    * @param Control: TipoControl
    */
    def borrar(aap_id: Long, empr_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.control SET esta_id = 9 WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "control", 
                    'audi_uid -> aap_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

    def informe_siap_control(empr_id: scala.Long): Future[Iterable[InformeC]] = Future[Iterable[InformeC]] {
        db.withConnection { implicit connection => 
            SQL("""SELECT m.aap_id, m.aap_direccion, b.barr_descripcion, COUNT(a.*) AS cantidad FROM siap.control m
            LEFT JOIN siap.aap_control am ON am.aap_id = m.aap_id AND am.empr_id = m.empr_id
            LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
            LEFT JOIN siap.barrio b ON b.barr_id = m.barr_id
            WHERE m.empr_id = {empr_id}
            GROUP BY m.aap_id, m.aap_direccion, b.barr_descripcion
            ORDER BY m.aap_id""").
            on(
                'empr_id -> empr_id
            ).
            as(InformeC._set *)
        }
    }
}