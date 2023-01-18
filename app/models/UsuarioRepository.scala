package models

import javax.inject.Inject
import java.util.Calendar
import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import scala.util.{ Failure, Success }
import anorm._
import anorm.SqlParser.{ get, str }
import anorm.JodaParameterMetaData._
import play.api.db.DBApi
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._
import notifiers._

import java.util.UUID.randomUUID

case class Usuario(usua_id: Option[Long], usua_email: String, usua_clave: Option[String], usua_nombre: String, usua_apellido: String, usua_activo: Boolean, usua_ultimasesion: Option[DateTime], usua_token: Option[String])

case class Enlace(enla_id: Long, enla_uuid: String, enla_activo: Boolean, enla_fecha: DateTime, usua_id: Long)

object Usuario {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")
    
    implicit val usuarioWrites = new Writes[Usuario] {
      def writes(usuario: Usuario) = Json.obj(
        "usua_id" -> usuario.usua_id,
        "usua_email" -> usuario.usua_email,
        "usua_clave" -> usuario.usua_clave,
        "usua_nombre" -> usuario.usua_nombre,
        "usua_apellido" -> usuario.usua_apellido,
        "usua_activo" -> usuario.usua_activo,
        "usua_ultimasesion" -> usuario.usua_ultimasesion,
        "usua_token" -> usuario.usua_token
      )
    }

    implicit val usuarioReads: Reads[Usuario] = (
        (__ \ "usua_id").readNullable[Long] and
          (__ \ "usua_email").read[String] and
          (__ \ "usua_clave").readNullable[String] and
          (__ \ "usua_nombre").read[String] and
          (__ \ "usua_apellido").read[String] and
          (__ \ "usua_activo").read[Boolean] and
          (__ \ "usua_ultimasesion").readNullable[DateTime] and
          (__ \ "usua_token").readNullable[String]
    )(Usuario.apply _)
    
    val simple = {
        get[Option[Long]]("usuario.usua_id") ~ 
          get[String]("usuario.usua_email") ~
          get[Option[String]]("usuario.usua_clave") ~
          get[String]("usuario.usua_nombre") ~
          get[String]("usuario.usua_apellido") ~
          get[Boolean]("usuario.usua_activo") ~
          get[Option[DateTime]]("usuario.usua_ultimasesion") ~ 
          get[Option[String]]("usuario.usua_token") map {
            case usua_id ~ usua_email ~ usua_clave ~ usua_nombre ~ usua_apellido ~ usua_activo ~ usua_ultimasesion ~ usua_token => Usuario(usua_id, usua_email, Some(""), usua_nombre, usua_apellido, usua_activo, usua_ultimasesion, usua_token)
        }
    }    
}

class UsuarioRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    /**
    * Parsear el enlace
    */
    val enlace = {
        get[Long]("enlace.enla_id") ~
        get[String]("enlace.enla_uuid") ~
        get[Boolean]("enlace.enla_activo") ~
        get[DateTime]("enlace.enla_fecha") ~
        get[Long]("enlace.usua_id") map {
            case enla_id ~ enla_uuid ~ enla_activo ~ enla_fecha ~ usua_id => Enlace(enla_id, enla_uuid, enla_activo, enla_fecha, usua_id)
        }
    }

    /**
    * Recuperar un usuario por su usua_id
    */
    def buscarPorId(usua_id: Long): Option[Usuario] = {
      db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.usuario WHERE usua_id = {usua_id}").on('usua_id -> usua_id).as(Usuario.simple.singleOpt)
      }
    }

    /**
    * Recuperar un usuario por su usua_email
    */
    def buscarPorEmail(usua_email: String): Future[Option[Usuario]] = Future {
      db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.usuario WHERE usua_email = {usua_email}").on('usua_email -> usua_email).as(Usuario.simple.singleOpt)
      }
    }(ec)  

    /**
    * Recuperar un usuario dado su usua_token
    */
    def buscarPorToken(usua_token: String): Future[Option[Usuario]] = Future {
      db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.usuario WHERE usua_token = {usua_token}").on('usua_token -> usua_token).as(Usuario.simple.singleOpt)
      }
    }(ec)        

    def autenticar(usua_email: String, usua_clave: String): Future[Boolean] = Future {
        db.withConnection { implicit connection =>
            SQL("UPDATE siap.usuario SET usua_ultimasesion = now() WHERE usua_email = {usua_email} AND usua_clave::text = public.crypt({usua_clave}::text, usua_clave::text)").
            on(
                "usua_email" -> usua_email, 
                "usua_clave" -> usua_clave).executeUpdate() > 0
        }
    }(ec)

    def validarEnlace(enla_uuid:String): Future[Boolean] = Future {
        val fecha: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
        db.withConnection { implicit connection =>
            val link: Option[Enlace] = SQL("SELECT * FROM siap.enlace WHERE enla_uuid = {enla_uuid} and enla_activo = {enla_activo}").
            on(
                'enla_uuid -> enla_uuid,
                'enla_activo -> true
            ).as(enlace.singleOpt)
         link match {
          case None => {
            false
          }
          case Some(link) => {
               val duration = fecha.toDate().getTime()  - link.enla_fecha.toDate().getTime()
               if (duration < 86400000) {
                   true
               } else {
                   SQL("UPDATE siap.enlace SET enla_activo = {enla_activo} WHERE enla_uuid = {enla_uuid}").
                   on(
                       'enla_activo -> false,
                       'enla_uuid -> enla_uuid
                   ).executeUpdate()
                   false
               }
          }
         }
        }
    }

    def recuperar(hosturl: String, usua_email: String): Future[Boolean] = Future {
        val fecha: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
        db.withConnection { implicit connection =>
            val usuario = SQL("SELECT * FROM siap.usuario WHERE usua_email = {usua_email} and usua_activo = {usua_activo}").
            on(
                'usua_email -> usua_email,
                'usua_activo -> true,
            ).as(Usuario.simple.singleOpt).get

            if (usuario != null) {
                // enviar correo con el enlace
                var uuid = randomUUID().toString
                val enlace = hosturl + "/#/r/" + uuid 

                SQL("INSERT INTO siap.enlace (enla_uuid, enla_activo, enla_fecha, usua_id) VALUES ({enla_uuid},{enla_activo},{enla_fecha},{usua_id})").
                on(
                    'enla_uuid -> uuid,
                    'enla_activo -> true,
                    'enla_fecha -> fecha,
                    'usua_id -> usuario.usua_id
                ).executeInsert()
                val sender = new EmailSender()
                sender.send(usuario.usua_email, usuario.usua_nombre + ' ' + usuario.usua_apellido, enlace)
                true
            } else {
                false
            }
        }

    }

    /*
    * Cambiar clave
    * @param link:String
    * @param clave: String
    */

    def cambiarClave(link: String, clave: String): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
        db.withConnection { implicit connection => 
            val usua_id = SQL("SELECT usua_id FROM siap.enlace WHERE enla_uuid = {enla_uuid}").
            on(
                'enla_uuid -> link
            ).as(SqlParser.scalar[Long].single)

            val result:Boolean = SQL("UPDATE siap.usuario SET usua_clave = public.crypt({usua_clave}, public.gen_salt('bf')) WHERE usua_id = {usua_id}").
            on(
                'usua_id -> usua_id,
                'usua_clave -> clave
            ).executeUpdate() > 0

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> usua_id,
                'audi_tabla -> "usuario", 
                'audi_uid -> usua_id,
                'audi_campo -> "usua_clave", 
                'audi_valorantiguo -> '*',
                'audi_valornuevo -> '*',
                'audi_evento -> "A").
                executeInsert()                    
            result
        }
    }

    /***
    * Crear Token al autenticar al usuario
    * @param usua_id: Long
    * @return String
    */
    def crearToken(usua_id: Long): Future[String] = Future {
      val sr = new java.security.SecureRandom()
      val token = new java.math.BigInteger(130, sr).toString(32)
      db.withConnection { implicit connection =>
        SQL("UPDATE siap.usuario SET usua_token = {usua_token} WHERE usua_id = {usua_id}").on('usua_id -> usua_id, 'usua_token -> token).executeUpdate()
      }
      token
    }

    def crear(usua_email: String, usua_clave: String, usua_nombre: String, usua_apellido: String, usua_activo: Boolean, usua_ultimasesion: String, usuario_id: Long, empr_id: Long, perf_id: Long ): Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val id: Long = SQL("INSERT INTO siap.usuario (usua_email, usua_clave, usua_nombre, usua_apellido, usua_activo) VALUES({usua_email},public.crypt({usua_clave}::text, public.gen_salt('bf')), {usua_nombre}, {usua_apellido}, {usua_activo})").
            on(
                'usua_email -> usua_email, 
                'usua_clave -> usua_clave, 
                'usua_nombre -> usua_nombre, 
                'usua_apellido -> usua_apellido, 
                'usua_activo -> usua_activo, 
                'usua_ultimasesion -> usua_ultimasesion).
                executeInsert().get

            SQL("INSERT INTO siap.usuario_empresa (usua_id, empr_id) VALUES ({usua_id}, {empr_id})").
            on(
                'usua_id -> id,
                'empr_id -> empr_id
            ).executeInsert()


            SQL("INSERT INTO siap.usuario_empresa_perfil (usua_id, empr_id, perf_id) VALUES ({usua_id}, {empr_id}, {perf_id})").
            on(
                'usua_id -> id,
                'empr_id -> empr_id,
                'perf_id -> perf_id
            ).executeInsert()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> usuario_id,
                'audi_tabla -> "usuario", 
                'audi_uid -> id,
                'audi_campo -> "usua_email", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> usua_email,
                'audi_evento -> "I").
                executeInsert()
            
            id
        }
    }

    def actualizar(usua_email: String, usua_clave: String): Future[Boolean] = Future {
        db.withConnection { implicit connection =>
            SQL("UPDATE siap.usuario SET usua_ultimasesion = now() WHERE usua_email = {usua_email} AND usua_clave::text = public.crypt({usua_clave}::text, usua_clave::text) AND usua_activo = {usua_activo}").
            on(
                "usua_email" -> usua_email, 
                "usua_clave" -> usua_clave,
                "usua_activo" -> true).executeUpdate() > 0
        }
    }(ec)

    def actualizar(usuario: Usuario, usua_id: Long) : Future[Boolean] = {
        //val usuario_ant: Future[Option[Usuario]] = 
        val usuario_ant : Option[Usuario] = buscarPorId(usuario.usua_id.get)
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val count: Long = SQL("UPDATE siap.usuario SET usua_clave = public.crypt({usua_clave}, public.gen_salt('bf')), usua_nombre = {usua_nombre}, usua_apellido = {usua_apellido}, usua_activo = {usua_activo}, usua_ultimasesion = {usua_ultimasesion} WHERE usua_id = {usua_id}").
            on(
                'usua_id -> usuario.usua_id,
                'usua_clave -> usuario.usua_clave, 
                'usua_nombre -> usuario.usua_nombre, 
                'usua_apellido -> usuario.usua_apellido, 
                'usua_activo -> usuario.usua_activo, 
                'usua_ultimasesion -> usuario.usua_ultimasesion                
            ).executeUpdate()
            
            if (usuario_ant != None){
                if (usuario_ant.get.usua_email != usuario.usua_email){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usuario.usua_id,
                    'audi_tabla -> "usuario", 
                    'audi_uid -> usuario.usua_id,
                    'audi_campo -> "usua_email", 
                    'audi_valorantiguo -> usuario_ant.get.usua_email,
                    'audi_valornuevo -> usuario.usua_email,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (usuario_ant.get.usua_nombre != usuario.usua_nombre){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usuario.usua_id,
                    'audi_tabla -> "usuario", 
                    'audi_uid -> usuario.usua_id,
                    'audi_campo -> "usua_nombre", 
                    'audi_valorantiguo -> usuario_ant.get.usua_nombre,
                    'audi_valornuevo -> usuario.usua_nombre,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

                if (usuario_ant.get.usua_apellido != usuario.usua_apellido){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usuario.usua_id,
                    'audi_tabla -> "usuario", 
                    'audi_uid -> usuario.usua_id,
                    'audi_campo -> "usua_apellido", 
                    'audi_valorantiguo -> usuario_ant.get.usua_apellido,
                    'audi_valornuevo -> usuario.usua_apellido,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (usuario_ant.get.usua_activo != usuario.usua_activo){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usuario.usua_id,
                    'audi_tabla -> "usuario",
                    'audi_uid -> usuario.usua_id,
                    'audi_campo -> "usua_activo",
                    'audi_valorantiguo -> usuario_ant.get.usua_activo,
                    'audi_valornuevo -> usuario.usua_activo,
                    'audi_evento -> "A").
                    executeInsert()
                }

                if (usuario_ant.get.usua_ultimasesion != usuario.usua_ultimasesion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usuario.usua_id,
                    'audi_tabla -> "usuario",
                    'audi_uid -> usuario.usua_id,
                    'audi_campo -> "usua_ultimasesion",
                    'audi_valorantiguo -> usuario_ant.get.usua_ultimasesion,
                    'audi_valornuevo -> usuario.usua_ultimasesion,
                    'audi_evento -> "A").
                    executeInsert()
                }                                               
            }

            Future.successful(count > 0)
        }
    }   

  /**
  * Recuperar total de registros
  * @return total
  */
  def cuenta(empr_id: Long): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.usuario u INNER JOIN siap.usuario_empresa e ON u.usua_id = e.usua_id WHERE e.empr_id = {empr_id}").
      on(
          'empr_id -> empr_id
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }     

    def todos(empr_id: Long, page_size:Long, current_page:Long): Future[Iterable[Usuario]] = Future[Iterable[Usuario]] {
                db.withConnection { implicit connection => 
                    SQL("SELECT * FROM siap.usuario u INNER JOIN siap.usuario_empresa e ON u.usua_id = e.usua_id WHERE e.empr_id = {empr_id} ORDER BY usua_apellido LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
                    on(
                       'empr_id -> empr_id,
                       'page_size -> page_size,
                       'current_page -> current_page
                    ).as(Usuario.simple *)
                }   
    }

    def all(empr_id: Long): Future[Iterable[Usuario]] = Future[Iterable[Usuario]] {
                db.withConnection { implicit connection => 
                    SQL("SELECT * FROM siap.usuario u INNER JOIN siap.usuario_empresa_perfil uep ON uep.usua_id = u.usua_id WHERE uep.empr_id = {empr_id} AND u.usua_activo is true ORDER BY usua_nombre").
                    on(
                       'empr_id -> empr_id
                    ).as(Usuario.simple *)
                }   
    }    

    def paraCuadrilla(empr_id: Long): Future[Iterable[Usuario]] = Future[Iterable[Usuario]] {
                db.withConnection { implicit connection => 
                    SQL("SELECT * FROM siap.usuario u INNER JOIN siap.usuario_empresa_perfil e ON u.usua_id = e.usua_id WHERE e.empr_id = {empr_id} and e.perf_id = {perf_id} ORDER BY usua_apellido").
                    on(
                       'empr_id -> empr_id,
                       'perf_id -> 6,
                    ).as(Usuario.simple *)
                }   
    }    

 
}