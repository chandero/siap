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
import org.joda.time.LocalDateTime

case class Caracteristica(cara_id: Option[Long],
                          cara_descripcion: String,
                          cara_estado: Int,
                          cara_valores: Option[String],
                          unid_id: Long,
                          usua_id: Long
                          )

object Caracteristica {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val caracteristicaWrites = new Writes[Caracteristica] {
    def writes(caracteristica: Caracteristica) = Json.obj(
      "cara_id" -> caracteristica.cara_id,
      "cara_descripcion" -> caracteristica.cara_descripcion,
      "cara_estado" -> caracteristica.cara_estado,
      "cara_valores" -> caracteristica.cara_valores,
      "unid_id" -> caracteristica.unid_id,
      "usua_id" -> caracteristica.usua_id,
    )
  }

  implicit val caracteristicaReads: Reads[Caracteristica] = (
    (__ \ "cara_id").readNullable[Long] and
      (__ \ "cara_descripcion").read[String] and
      (__ \ "cara_estado").read[Int] and     
      (__ \ "cara_valores").readNullable[String] and 
      (__ \ "unid_id").read[Long] and
      (__ \ "usua_id").read[Long]
  )(Caracteristica.apply _)

  val simple = {
    get[Option[Long]]("caracteristica.cara_id") ~
      get[String]("caracteristica.cara_descripcion") ~
      get[Int]("caracteristica.cara_estado") ~
      get[Option[String]]("caracteristica.cara_valores") ~
      get[Long]("caracteristica.unid_id") ~
      get[Long]("caracteristica.usua_id") map {
      case cara_id ~ cara_descripcion ~ cara_estado ~ cara_valores ~ unid_id ~ usua_id =>
        Caracteristica(cara_id, cara_descripcion, cara_estado, cara_valores, unid_id, usua_id)
    }
  }  

}

class CaracteristicaRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    *  Parsear un Caracteristica desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("caracteristica.cara_id") ~
      get[String]("caracteristica.cara_descripcion") ~
      get[Int]("caracteristica.cara_estado") ~
      get[Option[String]]("caracteristica.cara_valores") ~
      get[Long]("caracteristica.unid_id") ~
      get[Long]("caracteristica.usua_id") map {
      case cara_id ~ cara_descripcion ~ cara_estado ~ cara_valores ~ unid_id ~ usua_id =>
        Caracteristica(cara_id, cara_descripcion, cara_estado, cara_valores, unid_id, usua_id)
    }
  }

  /**
    *  Recuperar un caracteristica por su cara_id
    */
  def buscarPorId(cara_id: Long): Option[Caracteristica] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.caracteristica WHERE cara_id = {cara_id} and cara_estado <> 9")
        .on(
          'cara_id -> cara_id
        )
        .as(simple.singleOpt)
    }
  }

  /**
    Recuperar un Caracteristica por su cara_descripcion
    */
  def buscarPorDescripcion(
      cara_descripcion: String): Future[Iterable[Caracteristica]] =
    Future[Iterable[Caracteristica]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.caracteristica WHERE cara_descripcion LIKE %{cara_descripcion}% ORDER BY cara_descripcion")
          .on(
            'cara_descripcion -> cara_descripcion
          )
          .as(simple *)
      }
    }

  /**
  * Recuperar total de registros
  * @return total
  */
  def cuenta(): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.caracteristica WHERE cara_estado <> 9").as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    Recuperar todas las caracteristicas con paginacion
    * @param page_size: Long
    * @param current_page: Long
    */
  def todos(page_size: Long, current_page:Long): Future[Iterable[Caracteristica]] =
    Future[Iterable[Caracteristica]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.caracteristica WHERE cara_estado <> 9 ORDER BY cara_descripcion LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
        on(
          'page_size -> page_size,
          'current_page -> current_page
          ).as(
          simple *)
      }
    }

  /**
    Recuperar todas las caracteristicas
    */
  def caracteristicas(): Future[Iterable[Caracteristica]] =
    Future[Iterable[Caracteristica]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.caracteristica WHERE cara_estado <> 9 ORDER BY cara_descripcion").
        as(simple *)
      }
    }

  /**
        Crear una Caracteristica
    */
  def crear(caracteristica: Caracteristica): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      val id: Long = SQL(
        "INSERT INTO siap.caracteristica(cara_descripcion, cara_estado, cara_valores, unid_id, usua_id) VALUES ({cara_descripcion},{cara_estado},{cara_valores},{unid_id},{usua_id})")
        .on(
          'cara_descripcion -> caracteristica.cara_descripcion,
          'cara_estado -> caracteristica.cara_estado,
          'cara_valores -> caracteristica.cara_valores,
          'unid_id -> caracteristica.unid_id,
          'usua_id -> caracteristica.usua_id
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> caracteristica.usua_id,
          'audi_tabla -> "caracteristica",
          'audi_uid -> id,
          'audi_campo -> "cara_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> caracteristica.cara_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    Actualizar caracteristica
    */
  def actualizar(caracteristica: Caracteristica): Boolean = {
    val caracteristica_ant: Option[Caracteristica] = buscarPorId(
      caracteristica.cara_id.get)
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val result: Boolean = SQL(
        "UPDATE siap.caracteristica SET cara_descripcion = {cara_descripcion}, cara_estado = {cara_estado}, cara_valores = {cara_valores}, unid_id = {unid_id}, usua_id = {usua_id} WHERE cara_id = {cara_id}")
        .on(
          'cara_descripcion -> caracteristica.cara_descripcion,
          'cara_estado -> caracteristica.cara_estado,
          'cara_valores -> caracteristica.cara_valores,
          'unid_id -> caracteristica.unid_id,
          'usua_id -> caracteristica.usua_id,
          'cara_id -> caracteristica.cara_id
        )
        .executeUpdate() > 0

      if (caracteristica_ant != None){
          if (caracteristica_ant.get.cara_descripcion != caracteristica.cara_descripcion){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> caracteristica.usua_id,
              'audi_tabla -> "caracteristica",
              'audi_uid -> caracteristica.cara_id,
              'audi_campo -> "cara_descripcion",
              'audi_valorantiguo -> caracteristica_ant.get.cara_descripcion,
              'audi_valornuevo -> caracteristica.cara_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()              
          }

          if (caracteristica_ant.get.unid_id != caracteristica.unid_id){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> caracteristica.usua_id,
              'audi_tabla -> "caracteristica",
              'audi_uid -> caracteristica.cara_id,
              'audi_campo -> "unid_id",
              'audi_valorantiguo -> caracteristica_ant.get.unid_id,
              'audi_valornuevo -> caracteristica.unid_id,
              'audi_evento -> "A"
            )
            .executeInsert()               
          }

          if (caracteristica_ant.get.usua_id != caracteristica.usua_id){
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> caracteristica.usua_id,
              'audi_tabla -> "caracteristica",
              'audi_uid -> caracteristica.cara_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> caracteristica_ant.get.usua_id,
              'audi_valornuevo -> caracteristica.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()               
          }          
      }  
      result

    }
  }

  /**
    Borrar Caracteristica
   */
   def borrar(cara_id: Long, usua_id: Long): Boolean = {
      val caracteristica_ant: Option[Caracteristica] = buscarPorId(cara_id)
      val caracteristica = caracteristica_ant.get     
       db.withConnection { implicit connection => 
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =  new LocalDateTime(Calendar.getInstance().getTimeInMillis())

      val count: Long =
        SQL("UPDATE siap.caracteristica SET cara_estado = 9 WHERE cara_id = {cara_id}").
        on(
            'cara_id -> caracteristica.cara_id
        ).executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "caracteristica",
          'audi_uid -> caracteristica.cara_id,
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
