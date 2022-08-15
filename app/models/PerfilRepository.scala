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

case class Perfil(perf_id: Option[Long], perf_descripcion: String, perf_estado: Int, perf_abreviatura: String, usua_id: Long)

case class Usuario_Empresa_Perfil(usua_id: Long, empr_id: Long, perf_id: Option[Long], perf_abreviatura: Option[String])

object Perfil {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val perfilWrites = new Writes[Perfil] {
        def writes(perfil: Perfil) = Json.obj(
            "perf_id" -> perfil.perf_id,
            "perf_descripcion" -> perfil.perf_descripcion,
            "perf_estado" -> perfil.perf_estado,
            "perf_abreviatura" -> perfil.perf_abreviatura,
            "usua_id" -> perfil.usua_id
        )
    }

    implicit val perfilReads: Reads[Perfil] = (
        (__ \ "perf_id").readNullable[Long] and
        (__ \ "perf_descripcion").read[String] and
        (__ \ "perf_estado").read[Int] and
        (__ \ "perf_abreviatura").read[String] and
        (__ \ "usua_id").read[Long]
    )(Perfil.apply _)    
}

object Usuario_Empresa_Perfil {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val ueperfilWrites = new Writes[Usuario_Empresa_Perfil] {
        def writes(ueperfil: Usuario_Empresa_Perfil) = Json.obj(
            "usua_id" -> ueperfil.usua_id,
            "empr_id" -> ueperfil.empr_id,
            "perf_id" -> ueperfil.perf_id,
            "perf_abreviatura" -> ueperfil.perf_abreviatura
        )
    }

    implicit val ueperfilReads: Reads[Usuario_Empresa_Perfil] = (
        (__ \ "usua_id").read[Long] and
        (__ \ "empr_id").read[Long] and
        (__ \ "perf_id").readNullable[Long] and
        (__ \ "perf_abreviatura").readNullable[String]
    )(Usuario_Empresa_Perfil.apply _)
}


class PerfilRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un Perfil desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("perfil.perf_id") ~
        get[String]("perfil.perf_descripcion") ~
        get[Int]("perfil.perf_estado") ~
        get[String]("perfil.perf_abreviatura") ~
        get[Long]("perfil.usua_id") map {
            case perf_id ~ perf_descripcion ~ perf_estado ~ perf_abreviatura ~ usua_id => Perfil(perf_id, perf_descripcion, perf_estado, perf_abreviatura, usua_id)
        }
    }

    /**
    * Parsear un Usuario_Empresa_Perfil
    */
    private val ueperfilparser = {
        get[Long]("usuario_empresa_perfil.usua_id") ~
        get[Long]("usuario_empresa_perfil.empr_id") ~
        get[Option[Long]]("usuario_empresa_perfil.perf_id") ~ 
        get[Option[String]]("perfil.perf_abreviatura")  map {
            case usua_id ~ empr_id ~ perf_id ~ perf_abreviatura => Usuario_Empresa_Perfil(usua_id, empr_id, perf_id, perf_abreviatura)
        }
    }


    /**
    * Recuperar un Perfil dado su perf_id
    * @param perf_id: Long
    */
    def buscarPorId(perf_id: Long) : Option[Perfil] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.perfil WHERE perf_id = {perf_id}").
            on(
                'perf_id -> perf_id
            ).as(simple.singleOpt)
        }        
    }

    /**
    * Recuperar Perfil dado su perf_descripcion
    * @param perf_descripcion: String
    * @param empr_id: Long
    */
    def buscarPorDescripcion(perf_descripcion: String, empr_id:Long) : Future[Iterable[Perfil]] = Future[Iterable[Perfil]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.perfil WHERE perf_descripcion LIKE %{perf_descripcion}% and perf_estado <> 9").
            on(
                'perf_descripcion -> perf_descripcion
            ).as(simple *)
        }
    }

    /**
    * Recuperar Perfila dado su usua_id y su empr_id
    * @param usua_id: Long
    * @param empr_id: Long
    * @return Perfil
     */
     def buscarPorUsuarioEmpresa(usua_id: Long, empr_id: Long): List[String] = {
         val _listB = db.withConnection { implicit connection => 
            SQL("SELECT p.perf_abreviatura FROM siap.usuario_empresa_perfil uep LEFT JOIN siap.perfil p ON uep.perf_id = p.perf_id WHERE uep.usua_id = {usua_id} and uep.empr_id = {empr_id}").
            on(
                'usua_id -> usua_id,
                'empr_id -> empr_id
            ).as(SqlParser.scalar[String] *)
         }
         _listB
     }

  def cuenta(): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.perfil WHERE perf_estado <> 9").
      as(SqlParser.scalar[Long].single)
      result
    }
  }  

    /**
    * Recuperar todos los Perfil de la empresa
    * @param empr_id: Long 
    */
    def todos(page_size: Long, current_page: Long): Future[Iterable[Perfil]] = Future[Iterable[Perfil]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.perfil WHERE perf_estado <> 9 ORDER BY perf_descripcion LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
            on(
                'page_size -> page_size,
                'current_page -> current_page
            ).
            as(simple *)
        }
    }

    /**
    * Crear perfil
    * @param perfil: Perfil
    */
    def crear(perfil: Perfil) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.perfil (perf_descripcion, perf_estado, perf_abreviatura, usua_id) VALUES ({perf_descripcion},{perf_estado},{perf_abreviatura},{usua_id}) WHERE perf_id = {perf_id}").
            on(
                'perf_id -> perfil.perf_id,
                'perf_descripcion -> perfil.perf_descripcion,
                'perf_estado -> perfil.perf_estado,
                'perf_abreviatura -> perfil.perf_abreviatura,
                'usua_id -> perfil.usua_id
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> perfil.usua_id,
                'audi_tabla -> "perfil", 
                'audi_uid -> id,
                'audi_campo -> "perf_descripcion", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> perfil.perf_descripcion,
                'audi_evento -> "I").
                executeInsert()
            
            id            
        }
    }

    /**
    * Actualizar Perfil
    * @param perfil: Perfil
    */
    def actualizar(perfil: Perfil) : Long = {
        val perfil_ant: Option[Perfil] = buscarPorId(perfil.perf_id.get)

        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val count: Long = SQL("UPDATE siap.perfil SET perf_descripcion = {perf_descripcion}, perf_estado = {perf_estado}, perf_abreviatura = {perf_abreviatura}, usua_id = {usua_id} WHERE perf_id = {perf_id}").
            on(
                'perf_descripcion -> perfil.perf_descripcion,
                'perf_estado -> perfil.perf_estado,
                'perf_abreviatura -> perfil.perf_abreviatura,
                'usua_id -> perfil.usua_id
            ).executeUpdate()

            if (perfil_ant != None) {
                if (perfil_ant.get.perf_descripcion != perfil.perf_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> perfil.usua_id,
                    'audi_tabla -> "perfil", 
                    'audi_uid -> perfil.perf_id,
                    'audi_campo -> "perf_descripcion", 
                    'audi_valorantiguo -> perfil_ant.get.perf_descripcion,
                    'audi_valornuevo -> perfil.perf_descripcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (perfil_ant.get.perf_estado != perfil.perf_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> perfil.usua_id,
                    'audi_tabla -> "perfil", 
                    'audi_uid -> perfil.perf_id,
                    'audi_campo -> "perf_estado", 
                    'audi_valorantiguo -> perfil_ant.get.perf_estado,
                    'audi_valornuevo -> perfil.perf_estado,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (perfil_ant.get.perf_abreviatura != perfil.perf_abreviatura){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> perfil.usua_id,
                    'audi_tabla -> "perfil", 
                    'audi_uid -> perfil.perf_id,
                    'audi_campo -> "perf_abreviatura",
                    'audi_valorantiguo -> perfil_ant.get.perf_abreviatura,
                    'audi_valornuevo -> perfil.perf_abreviatura,
                    'audi_evento -> "A").
                    executeInsert()                    
                }         
                if (perfil_ant.get.usua_id != perfil.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> perfil.usua_id,
                    'audi_tabla -> "perfil", 
                    'audi_uid -> perfil.perf_id,
                    'audi_campo -> "usua_id",
                    'audi_valorantiguo -> perfil_ant.get.usua_id,
                    'audi_valornuevo -> perfil.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                          
            }

            count
        }
    }

    /**
    * Eliminar Perfil
    * @param perfil: Perfil
    */
    def eliminar(perfil: Perfil) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        
            val count:Long = SQL("UPDATE siap.perfil SET perf_estado = 9 WHERE perf_id = {perf_id}").
            on(
                'perf_id -> perfil.perf_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> perfil.usua_id,
                    'audi_tabla -> "perfil", 
                    'audi_uid -> perfil.perf_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }
}