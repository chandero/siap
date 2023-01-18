package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str, long, date, double }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

case class Unitario(unit_id:Option[Long],
                unit_codigo: Option[String],
                unit_descripcion: Option[String],
                empr_id: Option[Long])

object Unitario {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val uWrites = new Writes[Unitario] {
    def writes(u: Unitario) = Json.obj(
      "unit_id" -> u.unit_id,
      "unit_codigo" -> u.unit_codigo,
      "unit_descripcion" -> u.unit_descripcion,
      "empr_id" -> u.empr_id
    )
  }

  implicit val elementoReads: Reads[Unitario] = (
    (__ \ "unit_id").readNullable[Long] and
      (__ \ "unit_codigo").readNullable[String] and
      (__ \ "unit_descripcion").readNullable[String] and
      (__ \ "empr_id").readNullable[scala.Long]
  )(Unitario.apply _)

    /**
    * Parsear un Unitario desde un ResultSet
    */
    val _set = {
        get[Option[Long]]("unit_id") ~
        get[Option[String]]("unit_codigo") ~
        get[Option[String]]("unit_descripcion") ~
        get[Option[Long]]("empr_id") map {
            case unit_id ~ unit_codigo ~ unit_descripcion ~ empr_id => Unitario(unit_id, unit_codigo, unit_descripcion, empr_id)
        }
    }

}

class UnitarioRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Recuperar un Unitario dado su unit_id
    * @param unit_id: Long
    */
    def buscarPorId(unit_id:Long) : Option[Unitario] = {
        db.withConnection { implicit connection => 
            SQL("SELECT unit_id, unit_codigo, unit_descripcion, empr_id FROM siap.unitario WHERE unit_id = {unit_id}").
            on(
                'unit_id -> unit_id
            ).as(Unitario._set.singleOpt)
        }
    }

    /**
    * Recuperar un Unitario dado su unit_id
    * @param unit_codigo: String
    */
    def buscarPorCodigo(unit_codigo:String) : Option[Unitario] = {
        db.withConnection { implicit connection => 
            SQL("SELECT unit_id, unit_codigo, unit_descripcion, empr_id FROM siap.unitario WHERE unit_codigo = {unit_codigo}").
            on(
                'unit_codigo -> unit_codigo
            ).as(Unitario._set.singleOpt)
        }
    }    

    /**
    * Recuperar un Unitario dado su unit_descripcion
    * @param unit_descripcion: String
    */
    def buscarPorDescripcion(unit_descripcion: String, empr_id: Long) : Future[Iterable[Unitario]] = Future[Iterable[Unitario]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.unitario WHERE unit_descripcion LIKE %{unit_descripcion}% AND empr_id = {empr_id} ORDER BY unit_descripcion").
            on(
                'unit_descripcion -> unit_descripcion
            ).as(Unitario._set *)
        }
    }


   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(empr_id: Long): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.unitario WHERE empr_id = {empr_id}")
       .on(
           'empr_id -> empr_id
       )
       .as(SqlParser.scalar[Long].single)
       result
     }
   }

    /**
    * Recuperar todos los Unitario activas
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(empr_id:Long, page_size:Long, current_page:Long): Future[Iterable[Unitario]] = Future[Iterable[Unitario]] {
        db.withConnection { implicit connection =>
            SQL("SELECT unit_id, unit_codigo, unit_descripcion, empr_id FROM siap.unitario WHERE empr_id = {empr_id} ORDER BY unit_codigo LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
              'page_size -> page_size,
              'current_page -> current_page                
            ).as(Unitario._set *)
        }
    }

    /**
    * Recuperar todos los Unitario activas
    */
    def unitarios(empr_id: Long): Future[Iterable[Unitario]] = Future[Iterable[Unitario]] {
        db.withConnection { implicit connection =>
            SQL("SELECT unit_id, unit_codigo, unit_descripcion, empr_id FROM siap.unitario WHERE empr_id = {empr_id} ORDER BY unit_codigo")
            .on(
                'empr_id -> empr_id,
            ).
            as(Unitario._set *)
        }
    }    

    /**
    * Crear Unitario
    * @param unitario: Unitario
    */
    def crear(unitario: Unitario, usua_id: Long) : Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val id: Long = SQL("INSERT INTO siap.unitario (unit_codigo, unit_descripcion, empr_id) VALUES ({unit_codigo}, {unit_descripcion}, {empr_id})").
            on(
               
               'unit_descripcion -> unitario.unit_descripcion,
               'empr_id -> unitario.empr_id 
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> usua_id,
                'audi_tabla -> "unitario", 
                'audi_uid -> id,
                'audi_campo -> "unit_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> unitario.unit_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id             
        }
    }

    /**
    * Actualizar Unitario
    * @param unitario: Unitario
    */
    def actualizar(unitario: Unitario, usua_id: Long) : Boolean = {
        val unit_ant: Option[Unitario] = buscarPorId(unitario.unit_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
             val result: Boolean = SQL("UPDATE siap.unitario SET unit_descripcion = {unit_descripcion}, unit_estado = {unit_estado}, usua_id = {usua_id} WHERE unit_id = {unit_id}").
            on(
               'unit_id -> unitario.unit_id,
               'unit_descripcion -> unitario.unit_descripcion,
               'empr_id -> unitario.empr_id
            ).executeUpdate() > 0

            if (unit_ant != None){
                if (unit_ant.get.unit_descripcion != unitario.unit_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "unitario", 
                    'audi_uid -> unitario.unit_id,
                    'audi_campo -> "unit_descripcion", 
                    'audi_valorantiguo -> unit_ant.get.unit_descripcion,
                    'audi_valornuevo -> unitario.unit_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (unit_ant.get.unit_codigo != unitario.unit_codigo){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "unitario", 
                    'audi_uid -> unitario.unit_id,
                    'audi_campo -> "unit_codigo", 
                    'audi_valorantiguo -> unit_ant.get.unit_codigo,
                    'audi_valornuevo -> unitario.unit_codigo,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

            }

            result
        }
    }

    /**
    * Borrar Unitario
    * @param unitario: Unitario
    */
    def borrar(unit_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.unitario SET unit_estado = 9 WHERE unit_id = {unit_id}").
            on(
                'unit_id -> unit_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "unitario", 
                    'audi_uid -> unit_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

    def material(empr_id: Long): Future[Iterable[(Long, String, String, Long, String, String, Double)]] = Future[List[(Long, String, String, Long, String, String, Double)]] {
        val _parse = long("ucap") ~ str("ucap_codigo") ~ str("ucap_descripcion") ~ long("elem_id") ~ str("material_codigo") ~ str("material_nombre") ~ double("material_precio") map {
            case ucap ~ ucap_codigo ~ ucap_descripcion ~ elem_id ~ material_codigo ~ material_descripcion ~ material_precio => (ucap, ucap_codigo, ucap_descripcion, elem_id, material_codigo, material_descripcion, material_precio)
        }
        val _query = """select 
	                        u1.unit_id as ucap, 
	                        u1.unit_codigo as ucap_codigo,
                            case u1.unit_codigo 
                                when '1.01' then
                                    'SUMINISTRO E INSTALACION DE LUMINARIA'
                                when '1.02' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	                            when '1.03' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	                            when '1.08' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	                            when '1.09' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	                            when '1.10' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	                            when '1.11' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	                            when '1.12' then
                                    CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)          
                                when '1.04' then  u1.unit_descripcion
                                when '1.05' then CONCAT('SUMINISTRO DE ', u1.unit_descripcion)
                                when '1.06' then u1.unit_descripcion
                                when '1.07' then CONCAT('INSTALACION ', u1.unit_descripcion)
                                else u1.unit_descripcion
                            end as ucap_descripcion, 
	                        e1.elem_id, 
	                        e1.elem_codigo as material_codigo, 
	                        e1.elem_descripcion as material_nombre,
	                        (select ep1.elpr_precio from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id and ep1.elpr_anho = 2021 order by ep1.elpr_fecha desc limit 1) as material_precio
                        from siap.unitario u1 
                        left join siap.elemento_unitario eu1 on eu1.unit_id = u1.unit_id 
                        left join siap.elemento e1 on e1.elem_id = eu1.elem_id 
                        where empr_id = {empr_id}
                        order by u1.unit_id, e1.elem_descripcion """
        val _lista = db.withConnection { implicit connection =>
            SQL(_query).on('empr_id -> empr_id).as(_parse * )
        }
        _lista.toList
    }
}