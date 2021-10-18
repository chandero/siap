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

case class Sesion(sesi_id:Option[Long], sesi_fecha:DateTime, sesi_hora:DateTime, sesi_evento: String, sesi_uid: String, sesi_alias: String, sesi_estado: String, sesi_fechacierre:DateTime, sesi_horacierre:DateTime, sesi_tipocierre: String)

object Sesion {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val sesionWrites = new Writes[Sesion] {
        def writes(sesion: Sesion) = Json.obj(
            "sesi_id" -> sesion.sesi_id,
            "sesi_fecha" -> sesion.sesi_fecha,
            "sesi_hora" -> sesion.sesi_hora,
            "sesi_evento" -> sesion.sesi_evento,
            "sesi_uid" -> sesion.sesi_uid,
            "sesi_alias" -> sesion.sesi_alias,
            "sesi_estado" -> sesion.sesi_estado,
            "sesi_fechacierre" -> sesion.sesi_fechacierre,
            "sesi_horacierre" -> sesion.sesi_horacierre,
            "sesi_tipocierre" -> sesion.sesi_tipocierre
        )
    }

    implicit val sesionReads: Reads[Sesion] = (
        (__ \ "sesi_id").readNullable[Long] and
        (__ \ "sesi_fecha").read[DateTime] and
        (__ \ "sesi_hora").read[DateTime] and
        (__ \ "sesi_evento").read[String] and
        (__ \ "sesi_uid").read[String] and
        (__ \ "sesi_alias").read[String] and
        (__ \ "sesi_estado").read[String] and
        (__ \ "sesi_fechacierre").read[DateTime] and
        (__ \ "sesi_horacierre").read[DateTime] and
        (__ \ "sesi_tipocierre").read[String]
    )(Sesion.apply _)
}

class SesionRepository @Inject()(dbapi: DBApi)(implicit ec:DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    /**
    * Parsear un Sesion desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("sesion.sesi_id") ~
        get[DateTime]("sesion.sesi_fecha") ~
        get[DateTime]("sesion.sesi_hora") ~
        get[String]("sesion.sesi_evento") ~
        get[String]("sesion.sesi_uid") ~
        get[String]("sesion.sesi_alias") ~
        get[String]("sesion.sesi_estado") ~
        get[DateTime]("sesion.sesi_fechacierre") ~
        get[DateTime]("sesion.sesi_horacierre") ~
        get[String]("sesion.sesi_tipocierre") map {
            case sesi_id ~ sesi_fecha ~ sesi_hora ~ sesi_evento ~ sesi_uid ~ sesi_alias ~ sesi_estado ~ sesi_fechacierre ~ sesi_horacierre ~ sesi_tipocierre => Sesion(sesi_id, sesi_fecha, sesi_hora, sesi_evento, sesi_uid, sesi_alias, sesi_estado, sesi_fechacierre, sesi_horacierre, sesi_tipocierre)
        }
    }

    /**
    * Recuperar Sesion por su sesi_id
    * @param sesi_id: Long
    */
    def buscarPorId(sesi_id: Long):Option[Sesion] = {
       db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.sesion WHERE sesi_id = {sesi_id}").
        on(
            'sesi_id -> sesi_id
        ).as(simple.singleOpt)
       } 
    }

    /**
    * Recuperar Sesion dado su sesi_alias
    * @param sesi_alias: String
    */
    def buscarPorAlias(sesi_alias: String): Future[Iterable[Sesion]] = Future[Iterable[Sesion]] {
       db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.sesion WHERE sesi_alias = {sesi_alias}").
        on(
            'sesi_alias -> sesi_alias
        ).as(simple *)
       }
    }

    /**
    * Recuperar Sesion dado su sesi_fecha
    * @param sesi_fecha: String
    */
    def buscarPorFecha(sesi_fecha: DateTime): Future[Iterable[Sesion]] = Future[Iterable[Sesion]] {
       db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.sesion WHERE sesi_fecha = {sesi_fecha}").
        on(
            'sesi_fecha -> sesi_fecha
        ).as(simple *)
       }
    }    

    /**
    * Recuperar Sesion dado un rango de fechas y sesi_alias
    * @param fecha_inicial: DateTime
    * @param fecha_final: DateTime
    * @param sesi_alias: String
    */
    def buscarPorFecha(fecha_inicial: DateTime, fecha_final: DateTime, sesi_alias: String): Future[Iterable[Sesion]] = Future[Iterable[Sesion]] {
       db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.sesion WHERE sesi_fecha BETWEEN {fecha_inicial} and {fecha_final} and sesi_alias = {sesi_alias}").
        on(
            'fecha_inicial -> fecha_inicial,
            'fecha_final -> fecha_final,
            'sesi_alias -> sesi_alias
        ).as(simple *)
       }
    }

    /**
    * Crear Sesion
    */
    def crear(sesion: Sesion) : Future[Long] = Future {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val id: Long = SQL("INSERT INTO siap.sesion (sesi_fecha, sesi_hora, sesi_evento, sesi_uid, sesi_alias, sesi_estado, sesi_fechacierre, sesi_horacierre, sesi_tipocierre) VALUES ({sesi_fecha}, {sesi_hora}, {sesi_evento}, {sesi_uid}, {sesi_alias}, {sesi_estado}, {sesi_fechacierre}, {sesi_horacierre}, {sesi_tipocierre})").
            on(
                'sesi_fecha -> sesion.sesi_fecha,
                'sesi_hora -> sesion.sesi_hora,
                'sesi_evento -> sesion.sesi_evento,
                'sesi_uid -> sesion.sesi_uid,
                'sesi_alias -> sesion.sesi_alias,
                'sesi_estado -> sesion.sesi_estado,
                'sesi_fechacierre -> sesion.sesi_fechacierre,
                'sesi_horacierre -> sesion.sesi_horacierre,
                'sesi_tipocierre -> sesion.sesi_tipocierre
            ).executeInsert().get

            id
        }
    }

    /**
    * Actualizar Sesion
    * @param sesion: Sesion
    */
    def actualizar(sesion: Sesion) : Long = {
        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.sesion SET sesi_fecha = {sesi_fecha}, sesi_hora = {sesi_hora}, sesi_evento = {sesi_evento}, sesi_uid = {sesi_uid}, sesi_alias = {sesi_alias}, sesi_estado = {sesi_estado}, sesi_fechacierre = {sesi_fechacierre}, sesi_horacierre = {sesi_horacierre}, sesi_tipocierre = {sesi_tipocierre} WHERE sesi_id = {sesi_id}").
            on(
                'sesi_id -> sesion.sesi_id,
                'sesi_fecha -> sesion.sesi_fecha,
                'sesi_hora -> sesion.sesi_hora,
                'sesi_evento -> sesion.sesi_evento,
                'sesi_uid -> sesion.sesi_uid,
                'sesi_alias -> sesion.sesi_alias,
                'sesi_estado -> sesion.sesi_estado,
                'sesi_fechacierre -> sesion.sesi_fechacierre,
                'sesi_horacierre -> sesion.sesi_horacierre,
                'sesi_tipocierre -> sesion.sesi_tipocierre                
            ).executeUpdate()

            count
        }
    }
}