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

case class Evento(
                  even_fecha: Option[DateTime],
                  even_codigo_instalado: Option[String],
                  even_cantidad_instalado: Option[Double],
                  even_codigo_retirado: Option[String],
                  even_cantidad_retirado: Option[Double],
                  even_estado: Option[Int],
                  aap_id: Option[Long],
                  repo_id: Option[Long],
                  elem_id: Option[Long],
                  usua_id: Option[Long],
                  empr_id: Option[Long],
                  even_id: Option[Long])

object Evento {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val eventoWrites = new Writes[Evento] {
    def writes(evento: Evento) = Json.obj(
      "even_fecha" -> evento.even_fecha,
      "even_codigo_instalado" -> evento.even_codigo_instalado,
      "even_cantidad_instalado" -> evento.even_cantidad_instalado,
      "even_codigo_retirado" -> evento.even_codigo_retirado,
      "even_cantidad_retirado" -> evento.even_cantidad_retirado,      
      "even_estado" -> evento.even_estado,
      "aap_id" -> evento.aap_id,
      "repo_id" -> evento.repo_id,
      "elem_id" -> evento.elem_id,
      "usua_id" -> evento.usua_id,
      "empr_id" -> evento.empr_id,
      "even_id" -> evento.even_id
    )
  }

  implicit val eventoReads: Reads[Evento] = (
      (__ \ "even_fecha").readNullable[DateTime] and
      (__ \ "even_codigo_instalado").readNullable[String] and
      (__ \ "even_cantidad_instalado").readNullable[Double] and
      (__ \ "even_codigo_retirado").readNullable[String] and
      (__ \ "even_cantidad_retirado").readNullable[Double] and
      (__ \ "even_estado").readNullable[Int] and
      (__ \ "aap_id").readNullable[Long] and
      (__ \ "repo_id").readNullable[Long] and
      (__ \ "elem_id").readNullable[Long] and
      (__ \ "usua_id").readNullable[Long] and
      (__ \ "empr_id").readNullable[Long] and
      (__ \ "even_id").readNullable[Long]
  )(Evento.apply _)

  val eventoSet = {
      get[Option[DateTime]]("even_fecha") ~
      get[Option[String]]("even_codigo_instalado") ~
      get[Option[Double]]("even_cantidad_instalado") ~
      get[Option[String]]("even_codigo_retirado") ~
      get[Option[Double]]("even_cantidad_retirado") ~
      get[Option[Int]]("even_estado") ~
      get[Option[Long]]("aap_id") ~
      get[Option[Long]]("repo_id") ~
      get[Option[Long]]("elem_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("even_id") map {
      case even_fecha ~ even_codigo_instalado ~ even_cantidad_instalado ~ even_codigo_retirado ~ even_cantidad_retirado ~ even_estado ~ aap_id ~ repo_id ~ elem_id ~ usua_id ~ empr_id ~ even_id =>
        Evento(
               even_fecha,
               even_codigo_instalado,
               even_cantidad_instalado,
               even_codigo_retirado,
               even_cantidad_retirado,
               even_estado,
               aap_id,
               repo_id,
               elem_id,
               usua_id,
               empr_id,
               even_id)
    }
  }
}

class EventoRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Recuperar eventos de un Aap
    @param aap_id: Long
    */
  def buscarPorAap(aap_id: Long): Future[Iterable[Evento]] =
    Future[Iterable[Evento]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.reporte_evento WHERE aap_id = {aap_id} ORDER BY even_id ASC")
          .on(
            'aap_id -> aap_id
          )
          .as(Evento.eventoSet *)
      }
    }

 /**
    Crear Evento
    @param evento: Evento
    */
  def crear(evento: Evento): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      val id: Long = SQL(
        """INSERT INTO siap.reporte_evento (even_fecha, 
                                    even_codigo_instalado,
                                    even_cantidad_instalado,
                                    even_codigo_retirado,
                                    even_cantidad_retirado, 
                                    even_estado, 
                                    aap_id, 
                                    repo_id, 
                                    elem_id, 
                                    usua_id, 
                                    empr_id,
                                    even_id) VALUES (
                                    {even_fecha}, 
                                    {even_codigo_instalado},
                                    {even_cantidad_instalado},
                                    {even_codigo_retirado},
                                    {even_cantidad_retirado},
                                    {even_estado},
                                    {aap_id}, 
                                    {repo_id}, 
                                    {elem_id}, 
                                    {usua_id}, 
                                    {empr_id},
                                    {even_id})""")
        .on(
          "even_fecha" -> hora,
          "even_codigo_instalado" -> evento.even_codigo_instalado,
          "even_cantidad_instalado" -> evento.even_cantidad_instalado,
          "even_codigo_retirado" -> evento.even_codigo_retirado,
          "even_cantidad_retirado" -> evento.even_cantidad_retirado,
          "even_estado" -> evento.even_estado,
          "aap_id" -> evento.aap_id,
          "repo_id" -> evento.repo_id,
          "elem_id" -> evento.elem_id,
          "usua_id" -> evento.usua_id,
          "empr_id" -> evento.empr_id,
          "even_id" -> evento.even_id
        )
        .executeInsert()
        .get

      id

    }
  }
}
