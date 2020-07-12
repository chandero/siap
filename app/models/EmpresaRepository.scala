package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}

import org.joda.time.DateTime
import org.joda.time.LocalDate

case class Empresa(empr_id: Option[Long],
                   empr_descripcion: String,
                   empr_sigla: String,
                   empr_identificacion: String,
                   empr_direccion: String,
                   empr_telefono: String,
                   empr_concesion: Option[String],
                   empr_estado: Int,
                   usua_id: Long,
                   depa_id: Long,
                   muni_id: Long,
                   muni_descripcion: Option[String],
                   depa_descripcion: Option[String])

object Empresa {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val empresaWrites = new Writes[Empresa] {
    def writes(empresa: Empresa) = Json.obj(
      "empr_id" -> empresa.empr_id,
      "empr_descripcion" -> empresa.empr_descripcion,
      "empr_sigla" -> empresa.empr_sigla,
      "empr_identificacion" -> empresa.empr_identificacion,
      "empr_direccion" -> empresa.empr_direccion,
      "empr_telefono" -> empresa.empr_telefono,
      "empr_concesion" -> empresa.empr_concesion,
      "empr_estado" -> empresa.empr_estado,
      "usua_id" -> empresa.usua_id,
      "depa_id" -> empresa.depa_id,
      "muni_id" -> empresa.muni_id,
      "muni_descripcion" -> empresa.muni_descripcion,
      "depa_descripcion" -> empresa.depa_descripcion
    )
  }

  implicit val empresaReads: Reads[Empresa] = (
    (__ \ "empr_id").readNullable[Long] and
      (__ \ "empr_descripcion").read[String] and
      (__ \ "empr_sigla").read[String] and
      (__ \ "empr_identificacion").read[String] and
      (__ \ "empr_direccion").read[String] and
      (__ \ "empr_telefono").read[String] and
      (__ \ "empr_concesion").readNullable[String] and
      (__ \ "empr_estado").read[Int] and
      (__ \ "usua_id").read[Long] and
      (__ \ "depa_id").read[Long] and
      (__ \ "muni_id").read[Long] and
      (__ \ "muni_descripcion").readNullable[String] and
      (__ \ "depa_descripcion").readNullable[String]
  )(Empresa.apply _)

  /**
            Parsear un Empresa desde el ResultSet
    */
  val simple = {
    get[Option[Long]]("empresa.empr_id") ~
      get[String]("empresa.empr_descripcion") ~
      get[String]("empresa.empr_sigla") ~
      get[String]("empresa.empr_identificacion") ~
      get[String]("empresa.empr_direccion") ~
      get[String]("empresa.empr_telefono") ~
      get[Option[String]]("empresa.empr_concesion") ~
      get[Int]("empresa.empr_estado") ~
      get[Long]("empresa.usua_id") ~
      get[Long]("empresa.depa_id") ~
      get[Long]("empresa.muni_id") ~
      get[Option[String]]("municipio.muni_descripcion") ~ 
      get[Option[String]]("departamento.depa_descripcion") map {
      case empr_id ~ empr_descripcion ~ empr_sigla ~ empr_identificacion ~ empr_direccion ~ empr_telefono ~ empr_concesion ~ empr_estado ~ usua_id ~ depa_id ~ muni_id ~ muni_descripcion ~ depa_descripcion =>
        Empresa(empr_id,
                empr_descripcion,
                empr_sigla,
                empr_identificacion,
                empr_direccion,
                empr_telefono,
                empr_concesion,
                empr_estado,
                usua_id,
                depa_id,
                muni_id,
                muni_descripcion,
                depa_descripcion)
    }
  }  
}

class EmpresaRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Recuperar un Empresa usando su empr_id
    @param empr_id: Long
    */
  def buscarPorId(empr_id: Long): Option[Empresa] = {
    db.withConnection { implicit connection =>
      SQL("""SELECT * FROM siap.empresa e 
              INNER JOIN siap.municipio m ON e.muni_id = m.muni_id 
              INNER JOIN siap.departamento d ON m.depa_id = d.depa_id
              WHERE empr_id = {empr_id} and empr_estado <> 9""")
        .on(
          'empr_id -> empr_id
        ).as(Empresa.simple.singleOpt)
    }
  }

  /**
    Recuperar Empresa por su empr_descripcion
    @param empr_descripcion: String
    */
  def buscarPorDescripcion(
      empr_descripcion: String): Future[Iterable[Empresa]] =
    Future[Iterable[Empresa]] {
      db.withConnection { implicit connection =>
        SQL("""SELECT * FROM siap.empresa e 
               INNER JOIN siap.municipio m ON e.muni_id = m.muni_id 
               INNER JOIN siap.departamento d ON m.depa_id = d.depa_id               
               WHERE empr_descripcion LIKE %{empr_descripcion}% ORDER BY empr_descripcion""")
          .on(
            'empr_descripcion -> empr_descripcion
          )
          .as(Empresa.simple *)
      }
    }

  /**
  * Recuperar total de registros
  * @return total
  */
  def cuenta(): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.empresa WHERE empr_estado <> 9").as(SqlParser.scalar[Long].single)
      result
    }
  }
  /**
    Recuperar todas las Empresas
    @param page_size: Long
    @param current_page: Long
    */
  def todos(page_size:Long, current_page:Long): Future[Iterable[Empresa]] = Future[Iterable[Empresa]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT e.*, m.muni_descripcion, d.depa_descripcion FROM siap.empresa e 
              INNER JOIN siap.municipio m ON e.muni_id = m.muni_id 
              INNER JOIN siap.departamento d ON m.depa_id = d.depa_id              
              WHERE empr_estado <> 9 ORDER BY empr_descripcion LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)""").
      on(
        'page_size -> page_size,
        'current_page -> current_page
      ).as(Empresa.simple *)
    }
  }

  /**
  * Recuperar las Empresas de un Usuario dado su usua_id
  * @param usua_id: Long
  */
  def buscarPorUsuario(usua_id: Long): Future[Iterable[Empresa]] = Future[Iterable[Empresa]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT e.*, m.muni_descripcion, d.depa_descripcion FROM siap.usuario_empresa ue 
             INNER JOIN siap.empresa e ON e.empr_id = ue.empr_id 
             INNER JOIN siap.municipio m ON e.muni_id = m.muni_id 
             INNER JOIN siap.departamento d ON m.depa_id = d.depa_id
             WHERE ue.usua_id = {usua_id} and empr_estado <> 9 ORDER BY e.empr_descripcion""").
      on(
        'usua_id -> usua_id
      ).as(Empresa.simple *)

    }
  }

  /**
    Crear empresa
    @param empresa: Empresa
    */
  def crear(empresa: Empresa): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val id: Long = SQL(
        "INSERT INTO siap.empresa (empr_descripcion, empr_sigla, empr_identificacion, empr_direccion, empr_telefono, empr_estado, usua_id, depa_id, muni_id) VALUES ({empr_descripcion}, {empr_sigla}, {empr_identificacion}, {empr_direccion}, {empr_telefono}, {empr_estado}, {usua_id}, {depa_id}, {muni_id})")
        .on(
          'empr_descripcion -> empresa.empr_descripcion,
          'empr_sigla -> empresa.empr_sigla, 
          'empr_identificacion -> empresa.empr_identificacion,
          'empr_direccion -> empresa.empr_direccion,
          'empr_telefono -> empresa.empr_telefono,
          'empr_estado -> empresa.empr_estado,
          'usua_id -> empresa.usua_id,
          'depa_id -> empresa.depa_id,
          'muni_id -> empresa.muni_id
        )
        .executeInsert()
        .get
      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> empresa.usua_id,
          'audi_tabla -> "empresa",
          'audi_uid -> id,
          'audi_campo -> "empr_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> empresa.empr_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
      Actualizar Empresa
      @param empresa: Empresa
    */
  def actualizar(empresa: Empresa): Boolean = {
    val empresa_ant: Option[Empresa] = buscarPorId(empresa.empr_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val result: Boolean = SQL(
        "UPDATE siap.empresa SET empr_descripcion = {empr_descripcion}, empr_identificacion = {empr_identificacion}, empr_sigla = {empr_sigla}, empr_direccion = {empr_direccion}, empr_telefono = {empr_telefono}, empr_concesion = {empr_concesion}, empr_estado = {empr_estado}, usua_id = {usua_id}, depa_id = {depa_id}, muni_id = {muni_id} WHERE empr_id = {empr_id}")
        .on(
          'empr_id -> empresa.empr_id,
          'empr_descripcion -> empresa.empr_descripcion,
          'empr_identificacion -> empresa.empr_identificacion,
          'empr_sigla -> empresa.empr_sigla,
          'empr_direccion -> empresa.empr_direccion,
          'empr_telefono -> empresa.empr_telefono,
          'empr_concesion -> empresa.empr_concesion,
          'empr_estado -> empresa.empr_estado,
          'usua_id -> empresa.usua_id,
          'depa_id -> empresa.depa_id,
          'muni_id -> empresa.muni_id
        )
        .executeUpdate() > 0

      if (empresa_ant != None) {
        if (empresa_ant.get.empr_descripcion != empresa.empr_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "empr_descripcion",
              'audi_valorantiguo -> empresa_ant.get.empr_descripcion,
              'audi_valornuevo -> empresa.empr_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (empresa_ant.get.empr_identificacion != empresa.empr_identificacion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "empr_identificacion",
              'audi_valorantiguo -> empresa_ant.get.empr_identificacion,
              'audi_valornuevo -> empresa.empr_identificacion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (empresa_ant.get.empr_direccion != empresa.empr_direccion){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "empr_direccion",
              'audi_valorantiguo -> empresa_ant.get.empr_direccion,
              'audi_valornuevo -> empresa.empr_direccion,
              'audi_evento -> "A"
            )
            .executeInsert()          
        }
        if (empresa_ant.get.empr_telefono != empresa.empr_telefono){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "empr_telefono",
              'audi_valorantiguo -> empresa_ant.get.empr_telefono,
              'audi_valornuevo -> empresa.empr_telefono,
              'audi_evento -> "A"
            )
            .executeInsert()           
        }
        if (empresa_ant.get.empr_concesion != empresa.empr_concesion){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "empr_concesion",
              'audi_valorantiguo -> empresa_ant.get.empr_concesion,
              'audi_valornuevo -> empresa.empr_concesion,
              'audi_evento -> "A"
            )
            .executeInsert()           
        }        
        if (empresa_ant.get.empr_estado != empresa.empr_estado){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "empr_estado",
              'audi_valorantiguo -> empresa_ant.get.empr_estado,
              'audi_valornuevo -> empresa.empr_estado,
              'audi_evento -> "A"
            )
            .executeInsert()           
        }        
        if (empresa_ant.get.usua_id != empresa.usua_id){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> empresa_ant.get.usua_id,
              'audi_valornuevo -> empresa.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()           
        }
        if (empresa_ant.get.depa_id != empresa.depa_id){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "depa_id",
              'audi_valorantiguo -> empresa_ant.get.depa_id,
              'audi_valornuevo -> empresa.depa_id,
              'audi_evento -> "A"
            )
            .executeInsert()           
        }
        if (empresa_ant.get.muni_id != empresa.muni_id){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> empresa.usua_id,
              'audi_tabla -> "empresa",
              'audi_uid -> empresa.empr_id,
              'audi_campo -> "muni_id",
              'audi_valorantiguo -> empresa_ant.get.muni_id,
              'audi_valornuevo -> empresa.muni_id,
              'audi_evento -> "A"
            )
            .executeInsert()           
        }         
      }
      result
    }
  }

  /**
    Borrar empresa
    @param empr_id: Long
  */
  def borrar(empr_id: Long): Boolean = {
    val empresa_ant: Option[Empresa] = buscarPorId(empr_id)
    val empresa = empresa_ant.get
     db.withConnection { implicit connection => 
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: Long =
        SQL("UPDATE siap.empresa SET empr_estado = 9 WHERE empr_id = {empr_id}").
        on(
            'empr_id -> empr_id
        ).executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> empresa.usua_id,
          'audi_tabla -> "empresa",
          'audi_uid -> empresa.empr_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> "",
          'audi_evento -> "E"
        )
        .executeInsert()

      count > 0        
     }    
  }
}
