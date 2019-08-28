package models

import javax.inject.Inject
import java.util.Calendar
import java.util.{ Map, HashMap }
import java.io.InputStream
import java.io.FileInputStream
import java.net.URL

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
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

// Jasper
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperRunManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.JRExporterParameter
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
//

import utilities.N2T
import utilities.Utility

case class TipoSolicitud(soti_id: Option[Long], soti_descripcion: Option[String])

case class Soli(soli_id: Option[Long],
                soti_id: Option[Long],
                soti_descripcion: Option[String],
                soli_fecha: Option[DateTime],
                soli_nombre: Option[String],
                soli_radicado: Option[String],
                soli_direccion: Option[String],
                barr_id: Option[Int],
                barr_descripcion: Option[String],
                soli_telefono: Option[String],
                soli_email: Option[String],
                soli_solicitud: Option[String],
                soli_respuesta: Option[String],
                soli_informe: Option[String],
                soli_consecutivo: Option[Int],
                soli_fecharespuesta: Option[DateTime],
                soli_fechadigitado: Option[DateTime],
                soli_fechalimite: Option[DateTime],
                soli_fechasupervisor: Option[DateTime],
                soli_fechainforme: Option[DateTime],
                soli_fechavisita: Option[DateTime],
                soli_fecharte: Option[DateTime],
                soli_fechaalmacen: Option[DateTime],
                soli_numerorte: Option[Int],
                soli_puntos: Option[Int],
                soli_tipoexpansion: Option[String],
                soli_aprobada: Option[Boolean],
                soli_codigorespuesta: Option[String],
                soli_luminarias: Option[Int],
                soli_estado: Option[Int],
                soli_estado_descripcion: Option[String],
                empr_id: Option[Long],
                usua_id: Option[Long]
               )

case class SolicitudA(soli_id: Option[Long],
                     soti_id: Option[Long],
                     soli_fecha: Option[DateTime],
                     soli_nombre: Option[String],
                     soli_radicado: Option[String],
                     soli_direccion: Option[String],
                     barr_id: Option[Int],
                     soli_telefono: Option[String],
                     soli_email: Option[String],
                     soli_solicitud: Option[String],
                     soli_respuesta: Option[String],
                     soli_informe: Option[String],
                     soli_consecutivo: Option[Int]
)
case class SolicitudB(
                     soli_fecharespuesta: Option[DateTime],
                     soli_fechadigitado: Option[DateTime],
                     soli_fechalimite: Option[DateTime],
                     soli_fechasupervisor: Option[DateTime],
                     soli_fechainforme: Option[DateTime],
                     soli_fechavisita: Option[DateTime],
                     soli_fecharte: Option[DateTime],
                     soli_fechaalmacen: Option[DateTime],
                     soli_numerorte: Option[Int],
                     soli_puntos: Option[Int],
                     soli_tipoexpansion: Option[String],
                     soli_aprobada: Option[Boolean],
                     soli_codigorespuesta: Option[String],
                     soli_luminarias: Option[Int],
                     soli_estado: Option[Int],
                     empr_id: Option[Long],
                     usua_id: Option[Long])

case class Solicitud(a: SolicitudA,
                     b: SolicitudB)

case class SolicitudAR(soli_id: Option[Long],
                     soti_id: Option[Long],
                     soti_descripcion: Option[String],
                     soli_fecha: Option[DateTime],
                     soli_nombre: Option[String],
                     soli_radicado: Option[String],
                     soli_direccion: Option[String],
                     barr_id: Option[Int],
                     barr_descripcion: Option[String],
                     soli_telefono: Option[String],
                     soli_email: Option[String],
                     soli_solicitud: Option[String],
                     soli_respuesta: Option[String],
                     soli_informe: Option[String],
                     soli_consecutivo: Option[Int])

case class SolicitudBR(
                     soli_fecharespuesta: Option[DateTime],
                     soli_fechadigitado: Option[DateTime],
                     soli_fechalimite: Option[DateTime],
                     soli_fechasupervisor: Option[DateTime],
                     soli_fechainforme: Option[DateTime],
                     soli_fechavisita: Option[DateTime],
                     soli_fecharte: Option[DateTime],
                     soli_fechaalmacen: Option[DateTime],
                     soli_numerorte: Option[Int],
                     soli_puntos: Option[Int],
                     soli_tipoexpansion: Option[String],
                     soli_aprobada: Option[Boolean],
                     soli_codigorespuesta: Option[String],
                     soli_luminarias: Option[Int],
                     soli_estado: Option[Int],
                     soli_estado_descripcion: Option[String],
                     empr_id: Option[Long],
                     usua_id: Option[Long])

case class SolicitudR(a: SolicitudAR, b: SolicitudBR)

object TipoSolicitud {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")
    
    implicit val stWrites = new Writes[TipoSolicitud] {
        def writes(st: TipoSolicitud) = Json.obj(
            "soti_id" -> st.soti_id,
            "soti_descripcion" -> st.soti_descripcion
        )
    }

    implicit val stReads: Reads[TipoSolicitud] = (
        (__ \ "soti_id").readNullable[Long] and
        (__ \ "soti_descripcion").readNullable[String]
    )(TipoSolicitud.apply _)

    val _set = {
      get[Option[Long]]("soti_id") ~
      get[Option[String]]("soti_descripcion") map {
          case soti_id ~ soti_descripcion => TipoSolicitud(soti_id, soti_descripcion)
      }
    }
}

object SolicitudA {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val soliWrites = new Writes[SolicitudA] {
        def writes(soli: SolicitudA) = Json.obj(
            "soli_id" -> soli.soli_id,
            "soti_id" -> soli.soti_id,
            "soli_fecha" -> soli.soli_fecha,
            "soli_nombre" -> soli.soli_nombre,
            "soli_radicado" -> soli.soli_radicado,
            "soli_direccion" -> soli.soli_direccion,
            "barr_id" -> soli.barr_id,
            "soli_telefono" -> soli.soli_telefono,
            "soli_email" -> soli.soli_email,
            "soli_solicitud" -> soli.soli_solicitud,
            "soli_respuesta" -> soli.soli_respuesta,
            "soli_informe" -> soli.soli_informe,
            "soli_consecutivo" -> soli.soli_consecutivo
        )
    }

    implicit val soliReads: Reads[SolicitudA] = (
        (__ \ "soli_id").readNullable[Long] and
        (__ \ "soti_id").readNullable[Long] and
        (__ \ "soli_fecha").readNullable[DateTime] and
        (__ \ "soli_nombre").readNullable[String] and
        (__ \ "soli_radicado").readNullable[String] and
        (__ \ "soli_direccion").readNullable[String] and
        (__ \ "barr_id").readNullable[Int] and
        (__ \ "soli_telefono").readNullable[String] and
        (__ \ "soli_email").readNullable[String] and
        (__ \ "soli_solicitud").readNullable[String] and
        (__ \ "soli_respuesta").readNullable[String] and
        (__ \ "soli_informe").readNullable[String] and
        (__ \ "soli_consecutivo").readNullable[Int]
    )(SolicitudA.apply _)
}

object SolicitudB {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val soliWrites = new Writes[SolicitudB] {
        def writes(soli: SolicitudB) = Json.obj(
            "soli_fecharespuesta" -> soli.soli_fecharespuesta,
            "soli_fechadigitado" -> soli.soli_fechadigitado,
            "soli_fechalimite" -> soli.soli_fechalimite,
            "soli_fechasupervisor" -> soli.soli_fechasupervisor,
            "soli_fechainforme" -> soli.soli_fechainforme,
            "soli_fechavisita" -> soli.soli_fechavisita,
            "soli_fecharte" -> soli.soli_fecharte,
            "soli_fechaalmacen" -> soli.soli_fechaalmacen,
            "soli_numerorte" -> soli.soli_numerorte,
            "soli_puntos" -> soli.soli_puntos,
            "soli_tipoexpansion" -> soli.soli_tipoexpansion,
            "soli_aprobada" -> soli.soli_aprobada,
            "soli_codigorespuesta" -> soli.soli_codigorespuesta,
            "soli_luminarias" -> soli.soli_luminarias,
            "soli_estado" -> soli.soli_estado,            
            "empr_id" -> soli.empr_id,
            "usua_id" -> soli.usua_id
        )
    }

    implicit val soliReads: Reads[SolicitudB] = (
        (__ \ "soli_fecharespuesta").readNullable[DateTime] and
        (__ \ "soli_fechadigitado").readNullable[DateTime] and
        (__ \ "soli_fechalimite").readNullable[DateTime] and
        (__ \ "soli_fechasupervisor").readNullable[DateTime] and
        (__ \ "soli_fechainforme").readNullable[DateTime] and
        (__ \ "soli_fechavisita").readNullable[DateTime] and
        (__ \ "soli_fecharte").readNullable[DateTime] and
        (__ \ "soli_fechaalmacen").readNullable[DateTime] and
        (__ \ "soli_numerorte").readNullable[Int] and
        (__ \ "soli_puntos").readNullable[Int] and
        (__ \ "soli_tipoexpansion").readNullable[String] and
        (__ \ "soli_aprobada").readNullable[Boolean] and
        (__ \ "soli_codigorespuesta").readNullable[String] and
        (__ \ "soli_luminarias").readNullable[Int] and
        (__ \ "soli_estado").readNullable[Int] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long]
    )(SolicitudB.apply _)
}

object Solicitud {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val soliWrites = new Writes[Solicitud] {
        def writes(soli: Solicitud) = Json.obj(
            "a" -> soli.a,
            "b" -> soli.b
        )
    }

    implicit val soliReads: Reads[Solicitud] = (
        (__ \ "a").read[SolicitudA] and
        (__ \ "b").read[SolicitudB]
    )(Solicitud.apply _)
}

object SolicitudAR {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    implicit val swWrites = new Writes[SolicitudAR] {
        def writes(sw: SolicitudAR) = Json.obj(
            "soli_id" -> sw.soli_id,
            "soti_id" -> sw.soti_id, 
            "soti_descripcion" -> sw.soti_descripcion,
            "soli_fecha" -> sw.soli_fecha,
            "soli_nombre" -> sw.soli_nombre,
            "soli_radicado" -> sw.soli_radicado,
            "soli_direccion" -> sw.soli_direccion,
            "barr_id" -> sw.barr_id,
            "barr_descripcion" -> sw.barr_descripcion,
            "soli_telefono" -> sw.soli_telefono,
            "soli_email" -> sw.soli_email,
            "soli_solicitud" -> sw.soli_solicitud,
            "soli_respuesta" -> sw.soli_respuesta,
            "soli_informe" -> sw.soli_informe,
            "soli_consecutivo" -> sw.soli_consecutivo
        )
    }

    implicit val srReads: Reads[SolicitudAR] = (
        (__ \ "soli_id").readNullable[Long] and
        (__ \ "soti_id").readNullable[Long] and
        (__ \ "soti_descripcion").readNullable[String] and
        (__ \ "soli_fecha").readNullable[DateTime] and
        (__ \ "soli_nombre").readNullable[String] and
        (__ \ "soli_radicado").readNullable[String] and
        (__ \ "soli_direccion").readNullable[String] and
        (__ \ "barr_id").readNullable[Int] and
        (__ \ "barr_descripcion").readNullable[String] and
        (__ \ "soli_telefono").readNullable[String] and
        (__ \ "soli_email").readNullable[String] and
        (__ \ "soli_solicitud").readNullable[String] and
        (__ \ "soli_respuesta").readNullable[String] and
        (__ \ "soli_informe").readNullable[String] and
        (__ \ "soli_consecutivo").readNullable[Int]
    )(SolicitudAR.apply _)
}

object SolicitudBR {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val soliWrites = new Writes[SolicitudBR] {
        def writes(soli: SolicitudBR) = Json.obj(
            "soli_fecharespuesta" -> soli.soli_fecharespuesta,
            "soli_fechadigitado" -> soli.soli_fechadigitado,
            "soli_fechalimite" -> soli.soli_fechalimite,
            "soli_fechasupervisor" -> soli.soli_fechasupervisor,
            "soli_fechainforme" -> soli.soli_fechainforme,
            "soli_fechavisita" -> soli.soli_fechavisita,
            "soli_fecharte" -> soli.soli_fecharte,
            "soli_fechaalmacen" -> soli.soli_fechaalmacen,
            "soli_numerorte" -> soli.soli_numerorte,
            "soli_puntos" -> soli.soli_puntos,
            "soli_tipoexpansion" -> soli.soli_tipoexpansion,
            "soli_aprobada" -> soli.soli_aprobada,
            "soli_codigorespuesta" -> soli.soli_codigorespuesta,
            "soli_luminarias" -> soli.soli_luminarias,
            "soli_estado" -> soli.soli_estado,
            "soli_estado_descripcion" -> soli.soli_estado_descripcion,
            "empr_id" -> soli.empr_id,
            "usua_id" -> soli.usua_id
        )
    }

    implicit val soliReads: Reads[SolicitudBR] = (
        (__ \ "soli_fecharespuesta").readNullable[DateTime] and
        (__ \ "soli_fechadigitado").readNullable[DateTime] and
        (__ \ "soli_fechalimite").readNullable[DateTime] and
        (__ \ "soli_fechasupervisor").readNullable[DateTime] and
        (__ \ "soli_fechainforme").readNullable[DateTime] and
        (__ \ "soli_fechavisita").readNullable[DateTime] and
        (__ \ "soli_fecharte").readNullable[DateTime] and
        (__ \ "soli_fechaalmacen").readNullable[DateTime] and
        (__ \ "soli_numerorte").readNullable[Int] and
        (__ \ "soli_puntos").readNullable[Int] and
        (__ \ "soli_tipoexpansion").readNullable[String] and
        (__ \ "soli_aprobada").readNullable[Boolean] and
        (__ \ "soli_codigorespuesta").readNullable[String] and
        (__ \ "soli_luminarias").readNullable[Int] and
        (__ \ "soli_estado").readNullable[Int] and
        (__ \ "soli_estado_descripcion").readNullable[String] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long]
    )(SolicitudBR.apply _)
}

object SolicitudR {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val soliWrites = new Writes[SolicitudR] {
        def writes(soli: SolicitudR) = Json.obj(
            "a" -> soli.a,
            "b" -> soli.b
        )
    }

    implicit val soliReads: Reads[SolicitudR] = (
        (__ \ "a").read[SolicitudAR] and
        (__ \ "b").read[SolicitudBR]
    )(SolicitudR.apply _)
}

object Soli {
    val _set = {
        get[Option[Long]]("soli_id") ~
        get[Option[Long]]("soti_id") ~
        get[Option[String]]("soti_descripcion") ~
        get[Option[DateTime]]("soli_fecha") ~
        get[Option[String]]("soli_nombre") ~
        get[Option[String]]("soli_radicado") ~
        get[Option[String]]("soli_direccion") ~
        get[Option[Int]]("barr_id") ~
        get[Option[String]]("barr_descripcion") ~
        get[Option[String]]("soli_telefono") ~
        get[Option[String]]("soli_email") ~
        get[Option[String]]("soli_solicitud") ~
        get[Option[String]]("soli_respuesta") ~
        get[Option[String]]("soli_informe") ~
        get[Option[Int]]("soli_consecutivo") ~
        get[Option[DateTime]]("soli_fecharespuesta") ~
        get[Option[DateTime]]("soli_fechadigitado") ~
        get[Option[DateTime]]("soli_fechalimite") ~
        get[Option[DateTime]]("soli_fechasupervisor") ~
        get[Option[DateTime]]("soli_fechainforme") ~
        get[Option[DateTime]]("soli_fechavisita") ~
        get[Option[DateTime]]("soli_fecharte") ~
        get[Option[DateTime]]("soli_fechaalmacen") ~
        get[Option[Int]]("soli_numerorte") ~
        get[Option[Int]]("soli_puntos") ~
        get[Option[String]]("soli_tipoexpansion") ~
        get[Option[Boolean]]("soli_aprobada") ~
        get[Option[String]]("soli_codigorespuesta") ~
        get[Option[Int]]("soli_luminarias") ~
        get[Option[Int]]("soli_estado") ~
        get[Option[String]]("soli_estado_descripcion") ~        
        get[Option[Long]]("empr_id") ~ 
        get[Option[Long]]("usua_id") map {
            case soli_id ~ 
                 soti_id ~
                 soti_descripcion ~
                 soli_fecha ~ 
                 soli_nombre ~ 
                 soli_radicado ~ 
                 soli_direccion ~
                 barr_id ~
                 barr_descripcion ~
                 soli_telefono ~ 
                 soli_email ~ 
                 soli_solicitud ~ 
                 soli_respuesta ~
                 soli_informe ~
                 soli_consecutivo ~
                 soli_fecharespuesta ~ 
                 soli_fechadigitado ~ 
                 soli_fechalimite ~
                 soli_fechasupervisor ~
                 soli_fechainforme ~
                 soli_fechavisita ~
                 soli_fecharte ~
                 soli_fechaalmacen ~
                 soli_numerorte ~
                 soli_puntos ~
                 soli_tipoexpansion ~
                 soli_aprobada ~
                 soli_codigorespuesta ~
                 soli_luminarias ~
                 soli_estado ~ 
                 soli_estado_descripcion ~
                 empr_id ~ 
                 usua_id => Soli(soli_id,
                      soti_id,
                      soti_descripcion,
                      soli_fecha, 
                      soli_nombre, 
                      soli_radicado, 
                      soli_direccion, 
                      barr_id,
                      barr_descripcion,
                      soli_telefono, 
                      soli_email, 
                      soli_solicitud, 
                      soli_respuesta,
                      soli_informe,
                      soli_consecutivo,
                      soli_fecharespuesta, 
                      soli_fechadigitado,
                      soli_fechalimite,
                      soli_fechasupervisor,
                      soli_fechainforme,
                      soli_fechavisita,
                      soli_fecharte,
                      soli_fechaalmacen,
                      soli_numerorte,
                      soli_puntos,
                      soli_tipoexpansion,
                      soli_aprobada,
                      soli_codigorespuesta,
                      soli_luminarias,
                      soli_estado, 
                      soli_estado_descripcion,
                      empr_id, 
                      usua_id)
        }
    }    
}

class SolicitudRepository @Inject()(dbapi: DBApi, empresaService: EmpresaRepository, generalService: GeneralRepository)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")
    private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

    /**
    * Recuperar un Solicitud dado su soli_id
    * @param soli_id: Long
    */
    def buscarPorId(soli_id:Long) : Option[Solicitud] = {
        db.withConnection { implicit connection => 
            val s = SQL("""SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud s
            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
            WHERE s.soli_id = {soli_id} and s.soli_estado <> 9""").
            on(
                'soli_id -> soli_id
            ).as(Soli._set.singleOpt)
            s match {
                case Some(s) =>  val a = new SolicitudA(s.soli_id, 
                                                        s.soti_id, 
                                                        s.soli_fecha, 
                                                        s.soli_nombre, 
                                                        s.soli_radicado, 
                                                        s.soli_direccion, 
                                                        s.barr_id,
                                                        s.soli_telefono,
                                                        s.soli_email,
                                                        s.soli_solicitud,
                                                        s.soli_respuesta,
                                                        s.soli_informe,
                                                        s.soli_consecutivo)
                                 val b = new SolicitudB(s.soli_fecharespuesta,
                                                        s.soli_fechadigitado,
                                                        s.soli_fechalimite,
                                                        s.soli_fechasupervisor,
                                                        s.soli_fechainforme,
                                                        s.soli_fechavisita,
                                                        s.soli_fecharte,
                                                        s.soli_fechaalmacen,
                                                        s.soli_numerorte,
                                                        s.soli_puntos,
                                                        s.soli_tipoexpansion,
                                                        s.soli_aprobada,
                                                        s.soli_codigorespuesta,
                                                        s.soli_luminarias,
                                                        s.soli_estado,
                                                        s.empr_id,
                                                        s.usua_id)
                                val solicitud = new Solicitud(a, b)
                                Some(solicitud)
                case None =>    val a = new SolicitudA(None, 
                                                       None, 
                                                       None, 
                                                       None, 
                                                       None, 
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None)
                                val b = new SolicitudB(None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None)
                                val solicitud = new Solicitud(a, b)
                                Some(solicitud)
            }
        }
    }

    /**
    * Recuperar un Solicitud dado su soli_radicado
    * @param soli_radicado: String
    * @param empr_id: Long
    */
    def buscarPorRadicado(soli_radicado: String, empr_id: Long) : Option[Solicitud] = {
        db.withConnection { implicit connection => 
            val s = SQL("""SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud 
            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
            WHERE soli_radicado = {soli_radicado} and empr_id = {empr_id} and soli_estado <> 9""").
            on(
                'soli_radicado -> soli_radicado,
                'empr_id -> empr_id
            ).as(Soli._set.singleOpt)
            s match {
                case Some(s) =>  val a = new SolicitudA(s.soli_id, 
                                                        s.soti_id, 
                                                        s.soli_fecha, 
                                                        s.soli_nombre, 
                                                        s.soli_radicado, 
                                                        s.soli_direccion, 
                                                        s.barr_id,
                                                        s.soli_telefono,
                                                        s.soli_email,
                                                        s.soli_solicitud,
                                                        s.soli_respuesta,
                                                        s.soli_informe,
                                                        s.soli_consecutivo)
                                 val b = new SolicitudB(s.soli_fecharespuesta,
                                                        s.soli_fechadigitado,
                                                        s.soli_fechalimite,
                                                        s.soli_fechasupervisor,
                                                        s.soli_fechainforme,
                                                        s.soli_fechavisita,
                                                        s.soli_fecharte,
                                                        s.soli_fechaalmacen,
                                                        s.soli_numerorte,
                                                        s.soli_puntos,
                                                        s.soli_tipoexpansion,
                                                        s.soli_aprobada,
                                                        s.soli_codigorespuesta,
                                                        s.soli_luminarias,
                                                        s.soli_estado,
                                                        s.empr_id,
                                                        s.usua_id)
                                val solicitud = new Solicitud(a, b)
                                Some(solicitud)
                case None =>    val a = new SolicitudA(None, 
                                                       None, 
                                                       None, 
                                                       None, 
                                                       None, 
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None)
                                val b = new SolicitudB(None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,
                                                       None,                                                       
                                                       None)
                                val solicitud = new Solicitud(a, b)
                                Some(solicitud)
            }
        }
    }


    /**
    * Recuperar un Solicitud dado su soli_descripcion
    * @param soli_descripcion: String
    */
    def buscarPorDescripcion(soli_descripcion: String, empr_id: Long) : Future[Iterable[Solicitud]] = Future[Iterable[Solicitud]] {
        db.withConnection { implicit connection =>
            var _listBuffer = new ListBuffer[Solicitud]()
            val lsoli = SQL("""SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud s
            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
            WHERE soli_descripcion LIKE %{soli_descripcion}% and soli_estado <> 9 and empr_id = {empr_id} ORDER BY soli_descripcion""").
            on(
                'soli_descripcion -> soli_descripcion,
                'empr_id -> empr_id
            ).as(Soli._set *)
            lsoli.map { s =>
                        val a = new SolicitudA(s.soli_id, 
                                                            s.soti_id, 
                                                            s.soli_fecha, 
                                                            s.soli_nombre, 
                                                            s.soli_radicado, 
                                                            s.soli_direccion, 
                                                            s.barr_id,
                                                            s.soli_telefono,
                                                            s.soli_email,
                                                            s.soli_solicitud,
                                                            s.soli_respuesta,
                                                            s.soli_informe,
                                                            s.soli_consecutivo)
                                     val b = new SolicitudB(s.soli_fecharespuesta,
                                                            s.soli_fechadigitado,
                                                            s.soli_fechalimite,
                                                            s.soli_fechasupervisor,
                                                            s.soli_fechainforme,
                                                            s.soli_fechavisita,
                                                            s.soli_fecharte,
                                                            s.soli_fechaalmacen,
                                                            s.soli_numerorte,
                                                            s.soli_puntos,
                                                            s.soli_tipoexpansion,
                                                            s.soli_aprobada,
                                                            s.soli_codigorespuesta,
                                                            s.soli_luminarias,
                                                            s.soli_estado,
                                                            s.empr_id,
                                                            s.usua_id)
                                    val solicitud = new Solicitud(a, b)
                                    _listBuffer += solicitud
            }
            _listBuffer.toList 
        }
    }


   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(empr_id: Long): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.solicitud WHERE soli_estado <> 9 and empr_id = {empr_id}").
       on(
           'empr_id -> empr_id
       ).
       as(SqlParser.scalar[Long].single)
       result
     }
   }

    /**
    * Recuperar todos los Solicitud activas
    * @param page_size: Long
    * @param current_page: Long
    */
    def todos(page_size:Long, current_page:Long, empr_id: Long): Future[Iterable[Solicitud]] = Future[Iterable[Solicitud]] {
        db.withConnection { implicit connection =>
            var _listBuffer = new ListBuffer[Solicitud]()
            val lsoli = SQL("""SELECT soli_id, soli_descripcion, soli_estado, usua_id FROM siap.solicitud s
                            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
                            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
                            WHERE soli_estado <> 9 and empr_id = {empr_id} ORDER BY soli_id LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)""").
            on(
              'page_size -> page_size,
              'current_page -> current_page,
              'empr_id -> empr_id               
            ).as(Soli._set *)
            lsoli.map { s =>
                        val a = new SolicitudA(s.soli_id, 
                                                            s.soti_id, 
                                                            s.soli_fecha, 
                                                            s.soli_nombre, 
                                                            s.soli_radicado, 
                                                            s.soli_direccion, 
                                                            s.barr_id,
                                                            s.soli_telefono,
                                                            s.soli_email,
                                                            s.soli_solicitud,
                                                            s.soli_respuesta,
                                                            s.soli_informe,
                                                            s.soli_consecutivo)
                        val b = new SolicitudB(s.soli_fecharespuesta,
                                                            s.soli_fechadigitado,
                                                            s.soli_fechalimite,
                                                            s.soli_fechasupervisor,
                                                            s.soli_fechainforme,
                                                            s.soli_fechavisita,
                                                            s.soli_fecharte,
                                                            s.soli_fechaalmacen,
                                                            s.soli_numerorte,
                                                            s.soli_puntos,
                                                            s.soli_tipoexpansion,
                                                            s.soli_aprobada,
                                                            s.soli_codigorespuesta,
                                                            s.soli_luminarias,
                                                            s.soli_estado,
                                                            s.empr_id,
                                                            s.usua_id)
                        val solicitud = new Solicitud(a, b)
                        _listBuffer += solicitud
            }
            _listBuffer.toList             
        }
    }

    /**
    * Recuperar todos los Solicitud activas
    */
    def solis(empr_id: Long): Future[Iterable[Solicitud]] = Future[Iterable[Solicitud]] {
        db.withConnection { implicit connection =>
            var _listBuffer = new ListBuffer[Solicitud]()
            val lsoli = SQL("""SELECT soli_id, soli_descripcion, soli_estado, usua_id FROM siap.solicitud
                            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
                            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
                            WHERE soli_estado <> 9 and empr_id = {empr_id} ORDER BY soli_descripcion""").
            on(
                'empr_id -> empr_id
            ).
            as(Soli._set *)
            lsoli.map { s =>
                    val a = new SolicitudA(s.soli_id, 
                                           s.soti_id, 
                                           s.soli_fecha, 
                                           s.soli_nombre, 
                                           s.soli_radicado, 
                                           s.soli_direccion, 
                                           s.barr_id,
                                           s.soli_telefono,
                                           s.soli_email,
                                           s.soli_solicitud,
                                           s.soli_respuesta,
                                           s.soli_informe,
                                           s.soli_consecutivo)
                    val b = new SolicitudB(s.soli_fecharespuesta,
                                           s.soli_fechadigitado,
                                           s.soli_fechalimite,
                                           s.soli_fechasupervisor,
                                           s.soli_fechainforme,
                                           s.soli_fechavisita,
                                           s.soli_fecharte,
                                           s.soli_fechaalmacen,
                                           s.soli_numerorte,
                                           s.soli_puntos,
                                           s.soli_tipoexpansion,
                                           s.soli_aprobada,
                                           s.soli_codigorespuesta,
                                           s.soli_luminarias,
                                           s.soli_estado,
                                           s.empr_id,
                                           s.usua_id)
                    val solicitud = new Solicitud(a, b)
                    _listBuffer += solicitud
            }
            _listBuffer.toList            
        }
    }

    def tipos(): Future[Iterable[TipoSolicitud]] = {
        val result = db.withConnection { implicit connection =>
            SQL("""SELECT * FROM siap.solicitud_tipo st""").as(TipoSolicitud._set *)
        }
        Future.successful(result)
    }

    /**
    * Recuperar todos los Reporte dado su rango de fecha de recepcion
    * @param anho: Int
    * @param mes: Int
    * @param empr_id: scala.Long
    */
    def buscarPorRango(anho: Int, mes: Int, empr_id: scala.Long) : Future[Iterable[Solicitud]] = Future[Iterable[Solicitud]] {
        db.withConnection { implicit connection => 
        var query: String = """SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud s 
                                        LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
                                        LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
                                        WHERE s.empr_id = {empr_id} and s.soli_fecha between {fecha_inicial} and {fecha_final}
                                        and s.soli_estado <> 9 ORDER BY s.soli_id, s.soli_fecha DESC """
          val fechaini = new DateTime(anho,mes,1,0,0,0,0)
          val lastDay = fechaini.dayOfMonth().getMaximumValue()
          val fechafin = new DateTime(anho,mes,lastDay,23,59,59,999)
          println("Fecha Inicial: " + fechaini)
          println("Fecha Final: " + fechafin)
          val reps = SQL(query)
          .on(
            'empr_id -> empr_id,
            'fecha_inicial -> fechaini,
            'fecha_final -> fechafin
          )
          .as(Soli._set *)
          
          var _listBuffer = new ListBuffer[Solicitud]()
          reps.map { s =>
                    val a = new SolicitudA(s.soli_id, 
                                           s.soti_id, 
                                           s.soli_fecha, 
                                           s.soli_nombre, 
                                           s.soli_radicado, 
                                           s.soli_direccion, 
                                           s.barr_id,
                                           s.soli_telefono,
                                           s.soli_email,
                                           s.soli_solicitud,
                                           s.soli_respuesta,
                                           s.soli_informe,
                                           s.soli_consecutivo)
                    val b = new SolicitudB(s.soli_fecharespuesta,
                                           s.soli_fechadigitado,
                                           s.soli_fechalimite,
                                           s.soli_fechasupervisor,
                                           s.soli_fechainforme,
                                           s.soli_fechavisita,
                                           s.soli_fecharte,
                                           s.soli_fechaalmacen,
                                           s.soli_numerorte,
                                           s.soli_puntos,
                                           s.soli_tipoexpansion,
                                           s.soli_aprobada,
                                           s.soli_codigorespuesta,
                                           s.soli_luminarias,
                                           s.soli_estado,
                                           s.empr_id,
                                           s.usua_id)
                    val solicitud = new Solicitud(a, b)
                    _listBuffer += solicitud
        }
        _listBuffer.toList           
       }
    }

    /**
    * Recuperar todos los Reporte dado su rango de fecha de recepcion
    * @param anho: Int
    * @param mes: Int
    * @param empr_id: scala.Long
    */
    def buscarPorVencer(empr_id: scala.Long) : Future[Iterable[Solicitud]] = Future[Iterable[Solicitud]] {
        db.withConnection { implicit connection => 
        var query: String = """SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud s 
                               LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
                               LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
                               WHERE s.empr_id = {empr_id} and ((s.soli_fechalimite - CURRENT_TIMESTAMP)) <= (4 * '1 day'::interval)
                               and s.soli_estado < 6 ORDER BY s.soli_fecha DESC """
          val reps = SQL(query)
          .on(
            'empr_id -> empr_id
          )
          .as(Soli._set *)
          var _listBuffer = new ListBuffer[Solicitud]()
          reps.map { s =>
                        val a = new SolicitudA(s.soli_id, 
                                                        s.soti_id, 
                                                        s.soli_fecha, 
                                                        s.soli_nombre, 
                                                        s.soli_radicado, 
                                                        s.soli_direccion, 
                                                        s.barr_id,
                                                        s.soli_telefono,
                                                        s.soli_email,
                                                        s.soli_solicitud,
                                                        s.soli_respuesta,
                                                        s.soli_informe,
                                                        s.soli_consecutivo)
                        val b = new SolicitudB(s.soli_fecharespuesta,
                                                        s.soli_fechadigitado,
                                                        s.soli_fechalimite,
                                                        s.soli_fechasupervisor,
                                                        s.soli_fechainforme,
                                                        s.soli_fechavisita,
                                                        s.soli_fecharte,
                                                        s.soli_fechaalmacen,
                                                        s.soli_numerorte,
                                                        s.soli_puntos,
                                                        s.soli_tipoexpansion,
                                                        s.soli_aprobada, 
                                                        s.soli_codigorespuesta,
                                                        s.soli_luminarias,
                                                        s.soli_estado,
                                                        s.empr_id,
                                                        s.usua_id)
                        val solicitud = new Solicitud(a, b)
                        _listBuffer += solicitud
            }
        _listBuffer.toList           
       }
    }

    /**
    * Crear Solicitud
    * @param soli: Solicitud
    */
    def crear(soli: Solicitud) : Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val fi = soli.a.soli_fecha.get
            var ff = fi.plusDays(15)
            val dias_festivos = SQL("SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN {fecha_inicial} and {fecha_final}").
                                on(
                                    'fecha_inicial -> fi,
                                    'fecha_final -> ff
                                ).as(SqlParser.scalar[Int].single)

            ff = ff.plusDays(dias_festivos)

            var radicado: Int = SQL("SELECT gene_numero FROM siap.general WHERE gene_id = {gene_id} and empr_id = {empr_id}").
                                on(
                                    'gene_id -> 2,
                                    'empr_id -> soli.b.empr_id
                                ).as(SqlParser.scalar[Int].single)

            radicado += 1
            SQL("""UPDATE siap.general SET gene_numero = {gene_numero} 
                   WHERE gene_id = {gene_id} and empr_id = {empr_id}""").
                   on(
                    'gene_id -> 2,
                    'gene_numero -> radicado,
                    'empr_id -> soli.b.empr_id                       
                   ).executeUpdate()

            val id: Long = SQL("""INSERT INTO siap.solicitud (soti_id,
                                                            soli_fecha, 
                                                            soli_radicado,
                                                            soli_nombre,
                                                            soli_direccion,
                                                            barr_id,
                                                            soli_telefono,
                                                            soli_email,
                                                            soli_solicitud,
                                                            soli_fechalimite,
                                                            soli_estado,
                                                            empr_id,
                                                            usua_id) VALUES (
                                                                {soti_id},
                                                                {soli_fecha},
                                                                {soli_radicado},
                                                                {soli_nombre}, 
                                                                {soli_direccion},
                                                                {barr_id},
                                                                {soli_telefono},
                                                                {soli_email},
                                                                {soli_solicitud},
                                                                {soli_fechalimite},
                                                                {soli_estado},
                                                                {empr_id}, 
                                                                {usua_id})""").
            on(
               'soti_id -> soli.a.soti_id,
               'soli_fecha -> soli.a.soli_fecha,
               'soli_radicado -> radicado,
               'soli_nombre -> soli.a.soli_nombre,
               'soli_direccion -> soli.a.soli_direccion,
               'barr_id -> soli.a.barr_id,
               'soli_telefono -> soli.a.soli_telefono,
               'soli_email -> soli.a.soli_email,
               'soli_solicitud -> soli.a.soli_solicitud,
               'soli_fechalimite -> ff,
               'soli_estado -> 1,
               'empr_id -> soli.b.empr_id,
               'usua_id -> soli.b.usua_id 
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> soli.b.usua_id,
                'audi_tabla -> "soli", 
                'audi_uid -> id,
                'audi_campo -> "soli_nombre", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> soli.a.soli_nombre,
                'audi_evento -> "I").
                executeInsert()
            
            radicado.toLong             
        }
    }

    /**
    * Actualizar Solicitud
    * @param soli: Solicitud
    */
    def actualizar(soli: Solicitud) : Boolean = {
        val soli_ant: Option[Solicitud] = buscarPorId(soli.a.soli_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            var estado = 0
            soli.b.soli_fecharespuesta match {
                case Some(f) => estado = 6
                case _ => estado = 5
            }
            val result: Boolean = SQL("""UPDATE siap.solicitud SET 
                                          soli_fechainforme = {soli_fechainforme},
                                          soli_informe = {soli_informe},
                                          soli_fecharespuesta = {soli_fecharespuesta},
                                          soli_respuesta = {soli_respuesta},
                                          soli_estado = {soli_estado},
                                          usua_id = {usua_id} WHERE soli_id = {soli_id}""").
            on(
               'soli_id -> soli.a.soli_id,
               'soli_fechainforme -> soli.b.soli_fechainforme,
               'soli_informe -> soli.a.soli_informe,
               'soli_fecharespuesta -> soli.b.soli_fecharespuesta,
               'soli_respuesta -> soli.a.soli_respuesta,
               'soli_estado -> estado,
               'usua_id -> soli.b.usua_id 
            ).executeUpdate() > 0

            if (soli_ant != None){
                if (soli_ant.get.b.soli_fechainforme != soli.b.soli_fechainforme){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                        'audi_fecha -> fecha,
                        'audi_hora -> hora,
                        'usua_id -> soli.b.usua_id,
                        'audi_tabla -> "solicitud", 
                        'audi_uid -> soli.a.soli_id,
                        'audi_campo -> "soli_fechainforme", 
                        'audi_valorantiguo -> soli_ant.get.b.soli_fechainforme,
                        'audi_valornuevo -> soli.b.soli_fechainforme,
                        'audi_evento -> "A"
                    ).
                    executeInsert()                    
                }

                if (soli_ant.get.b.soli_estado != soli.b.soli_estado){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> soli.b.usua_id,
                    'audi_tabla -> "soli", 
                    'audi_uid -> soli.a.soli_id,
                    'audi_campo -> "soli_estado", 
                    'audi_valorantiguo -> soli_ant.get.b.soli_estado,
                    'audi_valornuevo -> soli.b.soli_estado,
                    'audi_evento -> "A").
                    executeInsert()        
                } 

                if (soli_ant.get.b.usua_id != soli.b.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> soli.b.usua_id,
                    'audi_tabla -> "soli", 
                    'audi_uid -> soli.b.usua_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> soli_ant.get.b.usua_id,
                    'audi_valornuevo -> soli.b.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                  

            }

            result
        }
    }

    /**
    * Borrar Solicitud
    * @param soli: Solicitud
    */
    def borrar(soli_id: Long, empr_id: Long, usua_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.soli SET soli_estado = 9 WHERE soli_id = {soli_id} and empr_id = {empr_id}").
            on(
                'soli_id -> soli_id,
                'empr_id -> empr_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "soli", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

    def entregarSupervisor(soli_id: Long, empr_id: Long, usua_id: Long): Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.solicitud SET soli_fechasupervisor = {soli_fechasupervisor}, soli_estado = {soli_estado} WHERE soli_id = {soli_id} and empr_id = {empr_id}").
            on(
                'soli_id -> soli_id,
                'empr_id -> empr_id,
                'soli_fechasupervisor -> hora,
                'soli_estado -> 2
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_fechasupervisor", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> hora,
                    'audi_evento -> "A").
                    executeInsert()

            count > 0 
        }
    }

    def entregarFormatoRTE(soli_id: Long, empr_id: Long, usua_id: Long): Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            var consecutivo: Int = SQL("SELECT g.gene_numero FROM siap.general g WHERE g.gene_id = {gene_id}").on('gene_id -> 3).as(SqlParser.scalar[Int].single)
            consecutivo += 1
            SQL("UPDATE siap.general SET gene_numero = {gene_numero} WHERE gene_id = {gene_id}").on(
                'gene_numero -> consecutivo,
                'gene_id -> 3
            ).executeUpdate()
            val count:Long = SQL("UPDATE siap.solicitud SET soli_consecutivo = {soli_consecutivo}, soli_fecharte = {soli_fecharte}, soli_estado = {soli_estado} WHERE soli_id = {soli_id} and empr_id = {empr_id}").
            on(
                'soli_id -> soli_id,
                'empr_id -> empr_id,
                'soli_consecutivo -> consecutivo,
                'soli_fecharte -> hora,
                'soli_estado -> 3
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_fecharte", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> hora,
                    'audi_evento -> "A").
                    executeInsert()

            count > 0 
        }
    }    

    def ingresarInforme(soli_id: Long, info: String, empr_id: Long, usua_id: Long): Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.solicitud SET soli_fechainforme = {soli_fechainforme}, soli_informe = {soli_informe}, soli_estado = {soli_estado} WHERE soli_id = {soli_id} and empr_id = {empr_id}").
            on(
                'soli_id -> soli_id,
                'empr_id -> empr_id,
                'soli_fechainforme -> hora,
                'soli_informe -> info,
                'soli_estado -> 3
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_fechainforme", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> hora,
                    'audi_evento -> "A").
                    executeInsert()
                    
            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_informe", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> info,
                    'audi_evento -> "A").
                executeInsert() 

            count > 0 
        }
    }    

    def asignarRte(soli_id: Long, soli_fechaalmacen: Long, soli_numerorte: Int, empr_id: Long, usua_id: Long): Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
            val count:Long = SQL("UPDATE siap.solicitud SET soli_fechaalmacen = {soli_fechaalmacen}, soli_numerorte = {soli_numerorte}, soli_estado = {soli_estado} WHERE soli_id = {soli_id} and empr_id = {empr_id}").
            on(
                'soli_id -> soli_id,
                'empr_id -> empr_id,
                'soli_fechaalmacen -> new DateTime(soli_fechaalmacen),
                'soli_numerorte -> soli_numerorte,
                'soli_estado -> 4
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_fechaalmacen", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> hora,
                    'audi_evento -> "A").
                    executeInsert()

                    SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                        'audi_fecha -> fecha,
                        'audi_hora -> hora,
                        'usua_id -> usua_id,
                        'audi_tabla -> "solicitud", 
                        'audi_uid -> soli_id,
                        'audi_campo -> "soli_numerorte", 
                        'audi_valorantiguo -> "",
                        'audi_valornuevo -> soli_numerorte,
                        'audi_evento -> "A").
                        executeInsert() 

            count > 0 
        }
    }

    def ingresarRespuesta(soli_id: Long, info: String, empr_id: Long, usua_id: Long): Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:Long = SQL("UPDATE siap.solicitud SET soli_fecharespuesta = {soli_fecharespuesta}, soli_respuesta = {soli_respuesta}, soli_estado = {soli_estado} WHERE soli_id = {soli_id} and empr_id = {empr_id}").
            on(
                'soli_id -> soli_id,
                'empr_id -> empr_id,
                'soli_fecharespuesta -> hora,
                'soli_respuesta -> info,
                'soli_estado -> 4
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_fecharespuesta", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> hora,
                    'audi_evento -> "E").
                    executeInsert()
                    
            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "solicitud", 
                    'audi_uid -> soli_id,
                    'audi_campo -> "soli_respuesta", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> info,
                    'audi_evento -> "E").
                executeInsert() 
                                   
            count > 0 
        }
    }    

    /**
    *  imprimir
    * @param soli_id: scala.Long
    * @return OutputStream
    */
    def formatoRTE(soli_id: scala.Long, empr_id: scala.Long): Array[Byte] = {
        var os = Array[Byte]()
        val reporte:Option[Solicitud] = buscarPorId(soli_id)
        db.withConnection { implicit connection => 
          empresaService.buscarPorId(empr_id).map { empresa =>
            try {
              var compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_visita_tecnica.jasper";
              var reportParams = new HashMap[String, java.lang.Object]()
              reportParams.put("SOLI_ID", new java.lang.Long(soli_id.longValue()))
              reportParams.put("EMPR_SIGLA", empresa.empr_sigla)
              os = JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
            } catch {
              case e: Exception => e.printStackTrace();
            }
          }
        }
        os
    }
  
    /**
    *  imprimir Carta
    * @param soli_id: scala.Long
    * @return OutputStream
    */
    def imprimirRespuesta(soli_id: scala.Long, empr_id: scala.Long, con_firma: Int): Array[Byte] = {
        var os = Array[Byte]()

        db.withConnection { implicit connection => 
          empresaService.buscarPorId(empr_id).map { empresa =>
              buscarPorId(soli_id) match {
                case Some(s) =>
                                    var compiledFile = ""
                                    var gerente = ""
                                    var ciudad = empresa.muni_descripcion.get
                                    var reportParams = new HashMap[String, java.lang.Object]()
                                    ciudad = ciudad.toLowerCase.capitalize
                                    val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
                                    val count:Long = SQL("UPDATE siap.solicitud SET soli_fecharespuesta = {soli_fecharespuesta}, soli_estado = {soli_estado} WHERE soli_id = {soli_id} and empr_id = {empr_id}").
                                    on(
                                        'soli_id -> soli_id,
                                        'empr_id -> empr_id,
                                        'soli_fecharespuesta -> hora,
                                        'soli_estado -> 6
                                    ).executeUpdate()
                                    s.b.soli_aprobada match {  
                                        case Some(true) => compiledFile = REPORT_DEFINITION_PATH + "siap_carta_respuesta_solicitud_aprobada.jasper"
                                                           if (con_firma == 1) {
                                                             val firma:URL = new URL("file", "localhost", REPORT_DEFINITION_PATH + "firma.png")
                                                             println("URL Firma : " + firma)
                                                             reportParams.put("FIRMA", firma)
                                                           } else {
                                                            val firma:URL = new URL("file", "localhost", REPORT_DEFINITION_PATH + "blanco.png")
                                                            println("URL Firma : " + firma)
                                                            reportParams.put("FIRMA", firma)
                                                           }
                                        case Some(false) => compiledFile = REPORT_DEFINITION_PATH + "siap_carta_respuesta_solicitud_negada.jasper"
                                        case None => None
                                    }
                                    generalService.buscarPorId(4, empr_id).map { g =>
                                        gerente = g.gene_valor.get
                                    }
                                    
                                    reportParams.put("SOLI_ID", new java.lang.Long(soli_id.longValue()))
                                    reportParams.put("EMPR_SIGLA", empresa.empr_sigla)
                                    reportParams.put("CIUDAD_LARGA", ciudad)
                                    reportParams.put("FECHA_LARGA", Utility.fechaatexto(s.b.soli_fecharespuesta))
                                    reportParams.put("CODIGO_RESPUESTA", s.b.soli_codigorespuesta.get)
                                    reportParams.put("CIUDAD_CORTA", ciudad)
                                    reportParams.put("FECHA_RADICADO_LARGA", Utility.fechaatexto(s.a.soli_fecha))
                                    reportParams.put("LUMINARIAS_LETRAS", N2T.convertirLetras(s.b.soli_luminarias.get))
                                    reportParams.put("GERENTE", gerente)
                                    os = JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
                 case None => os = new Array[Byte](0)
             }
           }
        }
        os
    }

}