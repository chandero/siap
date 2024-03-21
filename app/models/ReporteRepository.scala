package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream}
import java.util.{Map, HashMap, Date}
import java.lang.Long
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream

// Jasper
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperRunManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.JRExporterParameter
import net.sf.jasperreports.engine.JasperCompileManager
//import net.sf.jasperreports.engine.export.JRPdfExporter
//import net.sf.jasperreports.export.SimpleExporterInput
//import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
//import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
//import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
//

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, date, double, scalar, flatten}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

// Excel Export
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellStyleInheritance
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.norbitltd.spoiwo.model.enums.{
  CellBorderStyle,
  CellFill,
  Pane,
  CellHorizontalAlignment => HA,
  CellVerticalAlignment => VA
}

// Utility
import utilities.Utility

case class ReporteResult(reportes: Iterable[Reporte], total: scala.Long)

case class ReporteRequest(
    coau_codigo: Option[String],
    coau_tipo: Option[Int],
    reporte: Reporte
)

case class ReporteNovedad(
    tireuc_id: Option[scala.Long],
    repo_id: Option[scala.Long],
    nove_id: Option[Int],
    even_id: Option[Int],
    even_estado: Option[Int],
    reno_horaini: Option[String],
    reno_horafin: Option[String],
    reno_observacion: Option[String]
)

case class ReporteAdicional(
    repo_id: Option[scala.Long],
    repo_fechadigitacion: Option[DateTime],
    repo_modificado: Option[DateTime],
    repo_tipo_expansion: Option[Int],
    repo_luminaria: Option[Boolean],
    repo_redes: Option[Boolean],
    repo_poste: Option[Boolean],
    repo_subreporte: Option[Boolean],
    repo_subid: Option[scala.Long],
    repo_email: Option[String],
    acti_id: Option[scala.Long],
    repo_codigo: Option[String],
    repo_apoyo: Option[String],
    urba_id: Option[scala.Long],
    muot_id: Option[scala.Long],
    medi_id: Option[scala.Long],
    tran_id: Option[scala.Long],
    medi_acta: Option[String],
    aaco_id_anterior: Option[scala.Long],
    aaco_id_nuevo: Option[scala.Long],
    ortr_id: Option[Int]
)

case class ReporteDireccionDato(
    aatc_id: Option[scala.Long],
    aatc_id_anterior: Option[scala.Long],
    aama_id: Option[scala.Long],
    aama_id_anterior: Option[scala.Long],
    aamo_id: Option[scala.Long],
    aamo_id_anterior: Option[scala.Long],
    aaco_id: Option[scala.Long],
    aaco_id_anterior: Option[scala.Long],
    aap_potencia: Option[Int],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia: Option[String],
    aap_tecnologia_anterior: Option[String],
    aap_brazo: Option[String],
    aap_brazo_anterior: Option[String],
    aap_collarin: Option[String],
    aap_collarin_anterior: Option[String],
    tipo_id: Option[scala.Long],
    tipo_id_anterior: Option[scala.Long],
    aap_poste_altura: Option[Double],
    aap_poste_altura_anterior: Option[Double],
    aap_poste_propietario: Option[String],
    aap_poste_propietario_anterior: Option[String]
)

case class ReporteDireccionDatoAdicional(
    aacu_id_anterior: Option[scala.Long],
    aacu_id: Option[scala.Long],
    aaus_id_anterior: Option[scala.Long],
    aaus_id: Option[scala.Long],
    medi_id_anterior: Option[scala.Long],
    medi_id: Option[scala.Long],
    tran_id_anterior: Option[scala.Long],
    tran_id: Option[scala.Long],
    aap_apoyo_anterior: Option[String],
    aap_apoyo: Option[String],
    aap_lat_anterior: Option[String],
    aap_lat: Option[String],
    aap_lng_anterior: Option[String],
    aap_lng: Option[String]
)

case class ReporteDireccionFoto(
    tireuc_id: Option[scala.Long],
    repo_id: Option[scala.Long],
    aap_id: Option[scala.Long],
    refo_id: Option[Int],
    refo_tipo: Option[Int],
    refo_data: Option[String]
)

object ReporteDireccionFoto {
  implicit val fotoWrites = new Writes[ReporteDireccionFoto] {
    def writes(foto: ReporteDireccionFoto) = Json.obj(
      "tireuc_id" -> foto.tireuc_id,
      "repo_id" -> foto.repo_id,
      "aap_id" -> foto.aap_id,
      "refo_id" -> foto.refo_id,
      "refo_tipo" -> foto.refo_tipo,
      "refo_data" -> foto.refo_data
    )
  }

  implicit val datoRead: Reads[ReporteDireccionFoto] = (
    (__ \ "tireuc_id").readNullable[scala.Long] and
      (__ \ "repo_id").readNullable[scala.Long] and
      (__ \ "aap_id").readNullable[scala.Long] and
      (__ \ "refo_id").readNullable[Int] and
      (__ \ "refo_tipo").readNullable[Int] and
      (__ \ "refo_data").readNullable[String]
  )(ReporteDireccionFoto.apply _)

  val _set = {
    get[Option[scala.Long]]("tireuc_id") ~
      get[Option[scala.Long]]("repo_id") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[Int]]("refo_id") ~
      get[Option[Int]]("refo_tipo") ~
      get[Option[String]]("refo_data") map {
      case tireuc_id ~ repo_id ~ aap_id ~ refo_id ~ refo_tipo ~ refo_data =>
        ReporteDireccionFoto(
          tireuc_id,
          repo_id,
          aap_id,
          refo_id,
          refo_tipo,
          refo_data
        )
    }
  }
}
case class ReporteDireccion(
    repo_id: Option[scala.Long],
    aap_id: Option[scala.Long],
    even_direccion: Option[String],
    barr_id: Option[scala.Long],
    even_direccion_anterior: Option[String],
    barr_id_anterior: Option[scala.Long],
    even_id: Option[Int],
    even_estado: Option[Int],
    even_horaini: Option[String],
    even_horafin: Option[String],
    tire_id: Option[scala.Long],
    coau_codigo: Option[String],
    aap_fechatoma: Option[DateTime],
    dato: Option[ReporteDireccionDato],
    dato_adicional: Option[ReporteDireccionDatoAdicional],
    fotos: Option[List[ReporteDireccionFoto]]
)

// ya tiene los 22 elementos
case class Reporte(
    repo_id: Option[scala.Long],
    tireuc_id: Option[scala.Long],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[scala.Long],
    repo_fecharecepcion: Option[DateTime],
    repo_direccion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_fechasolucion: Option[DateTime],
    repo_horainicio: Option[String],
    repo_horafin: Option[String],
    repo_reportetecnico: Option[String],
    repo_descripcion: Option[String],
    repo_subrepoconsecutivo: Option[String],
    rees_id: Option[scala.Long],
    orig_id: Option[scala.Long],
    barr_id: Option[scala.Long],
    empr_id: Option[scala.Long],
    tiba_id: Option[scala.Long],
    usua_id: Option[scala.Long],
    adicional: Option[ReporteAdicional],
    meams: Option[List[scala.Long]],
    eventos: Option[List[Evento]],
    direcciones: Option[List[ReporteDireccion]],
    novedades: Option[List[ReporteNovedad]]
)

case class ReporteDesdeMovil(
    uuid: Option[String],
    repo_id: Option[scala.Long],
    tireuc_id: Option[scala.Long],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[scala.Long],
    repo_fecharecepcion: Option[DateTime],
    repo_direccion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_fechasolucion: Option[DateTime],
    repo_horainicio: Option[String],
    repo_horafin: Option[String],
    repo_reportetecnico: Option[String],
    repo_descripcion: Option[String],
    repo_subrepoconsecutivo: Option[String],
    rees_id: Option[scala.Long],
    orig_id: Option[scala.Long],
    barr_id: Option[scala.Long],
    empr_id: Option[scala.Long],
    tiba_id: Option[scala.Long],
    usua_id: Option[scala.Long],
    adicional: Option[ReporteAdicional],
    meams: Option[List[scala.Long]],
    eventos: Option[List[Evento]],
    direcciones: Option[List[ReporteDireccion]],
    novedades: Option[List[ReporteNovedad]]
)

case class Vencido(
    repo_id: Option[scala.Long],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[scala.Long],
    repo_fecharecepcion: Option[DateTime],
    fecha_limite: Option[DateTime],
    horas: Option[Int],
    repo_direccion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_descripcion: Option[String],
    rees_id: Option[scala.Long],
    orig_descripcion: Option[String],
    barr_descripcion: Option[String],
    cuad_descripcion: Option[String]
)

case class ReporteWeb(
    repo_consecutivo: Option[scala.Long],
    repo_fecharecepcion: Option[DateTime],
    repo_fechadigitacion: Option[DateTime],
    repo_direccion: Option[String],
    barr_id: Option[scala.Long],
    barr_descripcion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_email: Option[String],
    repo_descripcion: Option[String],
    rees_id: Option[scala.Long],
    rees_descripcion: Option[String],
    repo_reportetecnico: Option[String]
)

case class ReporteConsulta(
    repo_id: Option[scala.Long],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[scala.Long],
    repo_fecharecepcion: Option[DateTime],
    repo_direccion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_reportetecnico: Option[String],
    repo_descripcion: Option[String],
    rees_id: Option[scala.Long],
    orig_id: Option[scala.Long],
    barr_id: Option[scala.Long],
    empr_id: Option[scala.Long],
    tiba_id: Option[scala.Long],
    usua_id: Option[scala.Long],
    adicional: Option[ReporteAdicional]
)

case class ReporteTipo(
    reti_id: Option[scala.Long],
    reti_descripcion: String,
    reti_consecutivo: scala.Long,
    reti_estado: Int,
    usua_id: scala.Long
)

case class ReporteEstado(
    rees_id: Option[scala.Long],
    rees_descripcion: String,
    rees_estado: Int,
    usua_id: scala.Long
)

case class RepoElemento(
    rere_id: Option[scala.Long],
    rere_descripcion: Option[String],
    rere_estado: Option[Int],
    usua_id: Option[scala.Long]
)

case class RepoElemDanho(
    reda_id: Option[scala.Long],
    reda_descripcion: Option[String],
    reda_estado: Option[Int],
    usua_id: Option[Long]
)

case class ElementoHistoria(
    aap_id: Option[scala.Long],
    aael_fecha: Option[DateTime],
    aap_bombillo_retirado: Option[String],
    aap_bombillo_instalado: Option[String],
    aap_balasto_retirado: Option[String],
    aap_balasto_instalado: Option[String],
    aap_arrancador_retirado: Option[String],
    aap_arrancador_instalado: Option[String],
    aap_condensador_retirado: Option[String],
    aap_condensador_instalado: Option[String],
    aap_fotocelda_retirado: Option[String],
    aap_fotocelda_instalado: Option[String],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[scala.Long]
)

case class Pendiente(
    tire_descripcion: Option[String],
    reti_descripcion: Option[String],
    repo_consecutivo: Option[Int],
    repo_fecharecepcion: Option[DateTime],
    fecha_limite: Option[DateTime],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_direccion: Option[String],
    barr_descripcion: Option[String],
    orig_descripcion: Option[String],
    acti_descripcion: Option[String],
    cuad_descripcion: Option[String],
    estado: Option[String]
)
object Pendiente {

  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val pWrites = new Writes[Pendiente] {
    def writes(p: Pendiente) = Json.obj(
      "tire_descripcion" -> p.tire_descripcion,
      "reti_descripcion" -> p.reti_descripcion,
      "repo_consecutivo" -> p.repo_consecutivo,
      "repo_fecharecepcion" -> p.repo_fecharecepcion,
      "fecha_limite" -> p.fecha_limite,
      "repo_nombre" -> p.repo_nombre,
      "repo_telefono" -> p.repo_telefono,
      "repo_direccion" -> p.repo_direccion,
      "barr_descripcion" -> p.barr_descripcion,
      "orig_descripcion" -> p.orig_descripcion,
      "acti_descripcion" -> p.acti_descripcion,
      "cuad_descripcion" -> p.cuad_descripcion,
      "estado" -> p.estado
    )
  }

  implicit val adicionalRead: Reads[Pendiente] = (
    (__ \ "tire_descripcion").readNullable[String] and
      (__ \ "reti_descripcion").readNullable[String] and
      (__ \ "repo_consecutivo").readNullable[Int] and
      (__ \ "repo_fecharecepcion").readNullable[DateTime] and
      (__ \ "fecha_limite").readNullable[DateTime] and
      (__ \ "repo_nombre").readNullable[String] and
      (__ \ "repo_telefono").readNullable[String] and
      (__ \ "repo_direccion").readNullable[String] and
      (__ \ "barr_descripcion").readNullable[String] and
      (__ \ "orig_descripcion").readNullable[String] and
      (__ \ "acti_descripcion").readNullable[String] and
      (__ \ "cuad_descripcion").readNullable[String] and
      (__ \ "estado").readNullable[String]
  )(Pendiente.apply _)

  val _set = {
    get[Option[String]]("tire_descripcion") ~
      get[Option[String]]("reti_descripcion") ~
      get[Option[Int]]("repo_consecutivo") ~
      get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("fecha_limite") ~
      get[Option[String]]("repo_nombre") ~
      get[Option[String]]("repo_telefono") ~
      get[Option[String]]("repo_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("orig_descripcion") ~
      get[Option[String]]("acti_descripcion") ~
      get[Option[String]]("cuad_descripcion") ~
      get[Option[String]]("estado") map {
      case tire_descripcion ~
            reti_descripcion ~
            repo_consecutivo ~
            repo_fecharecepcion ~
            fecha_limite ~
            repo_nombre ~
            repo_telefono ~
            repo_direccion ~
            barr_descripcion ~
            orig_descripcion ~
            acti_descripcion ~
            cuad_descripcion ~
            estado =>
        Pendiente(
          tire_descripcion,
          reti_descripcion,
          repo_consecutivo,
          repo_fecharecepcion,
          fecha_limite,
          repo_nombre,
          repo_telefono,
          repo_direccion,
          barr_descripcion,
          orig_descripcion,
          acti_descripcion,
          cuad_descripcion,
          estado
        )
    }
  }

}

object ReporteNovedad {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  val _set = {
    get[Option[scala.Long]]("tireuc_id") ~
      get[Option[scala.Long]]("repo_id") ~
      get[Option[Int]]("nove_id") ~
      get[Option[Int]]("even_id") ~
      get[Option[Int]]("even_estado") ~
      get[Option[String]]("reno_horaini") ~
      get[Option[String]]("reno_horafin") ~
      get[Option[String]]("reno_observacion") map {
      case tireuc_id ~
            repo_id ~
            nove_id ~
            even_id ~
            even_estado ~
            reno_horaini ~
            reno_horafin ~
            reno_observacion =>
        new ReporteNovedad(
          tireuc_id,
          repo_id,
          nove_id,
          even_id,
          even_estado,
          reno_horaini,
          reno_horafin,
          reno_observacion
        )
    }
  }

}

object ReporteAdicional {

  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val adicionalWrites = new Writes[ReporteAdicional] {
    def writes(adicional: ReporteAdicional) = Json.obj(
      "repo_id" -> adicional.repo_id,
      "repo_fechadigitacion" -> adicional.repo_fechadigitacion,
      "repo_modificado" -> adicional.repo_modificado,
      "repo_tipo_expansion" -> adicional.repo_tipo_expansion,
      "repo_luminaria" -> adicional.repo_luminaria,
      "repo_redes" -> adicional.repo_redes,
      "repo_poste" -> adicional.repo_poste,
      "repo_subreporte" -> adicional.repo_subreporte,
      "repo_subid" -> adicional.repo_subid,
      "repo_email" -> adicional.repo_email,
      "acti_id" -> adicional.acti_id,
      "repo_codigo" -> adicional.repo_codigo,
      "repo_apoyo" -> adicional.repo_apoyo,
      "urba_id" -> adicional.urba_id,
      "muot_id" -> adicional.muot_id,
      "medi_id" -> adicional.medi_id,
      "tran_id" -> adicional.tran_id,
      "medi_acta" -> adicional.medi_acta,
      "aaco_id_anterior" -> adicional.aaco_id_anterior,
      "aaco_id_nuevo" -> adicional.aaco_id_nuevo,
      "ortr_id" -> adicional.ortr_id
    )
  }

  implicit val adicionalRead: Reads[ReporteAdicional] = (
    (__ \ "repo_id").readNullable[scala.Long] and
      (__ \ "repo_fechadigitacion").readNullable[DateTime] and
      (__ \ "repo_modificado").readNullable[DateTime] and
      (__ \ "repo_tipo_expansion").readNullable[Int] and
      (__ \ "repo_luminaria").readNullable[Boolean] and
      (__ \ "repo_redes").readNullable[Boolean] and
      (__ \ "repo_poste").readNullable[Boolean] and
      (__ \ "repo_subreporte").readNullable[Boolean] and
      (__ \ "repo_subid").readNullable[scala.Long] and
      (__ \ "repo_email").readNullable[String] and
      (__ \ "acti_id").readNullable[scala.Long] and
      (__ \ "repo_codigo").readNullable[String] and
      (__ \ "repo_apoyo").readNullable[String] and
      (__ \ "urba_id").readNullable[scala.Long] and
      (__ \ "muot_id").readNullable[scala.Long] and
      (__ \ "medi_id").readNullable[scala.Long] and
      (__ \ "tran_id").readNullable[scala.Long] and
      (__ \ "medi_acta").readNullable[String] and
      (__ \ "aaco_id_anterior").readNullable[scala.Long] and
      (__ \ "aaco_id_nuevo").readNullable[scala.Long] and
      (__ \ "ortr_id").readNullable[Int]
  )(ReporteAdicional.apply _)

  val reporteAdicionalSet = {
    get[Option[scala.Long]]("repo_id") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[DateTime]]("repo_modificado") ~
      get[Option[Int]]("repo_tipo_expansion") ~
      get[Option[Boolean]]("repo_luminaria") ~
      get[Option[Boolean]]("repo_redes") ~
      get[Option[Boolean]]("repo_poste") ~
      get[Option[Boolean]]("repo_subreporte") ~
      get[Option[scala.Long]]("repo_subid") ~
      get[Option[String]]("repo_email") ~
      get[Option[scala.Long]]("acti_id") ~
      get[Option[String]]("repo_codigo") ~
      get[Option[String]]("repo_apoyo") ~
      get[Option[scala.Long]]("urba_id") ~
      get[Option[scala.Long]]("muot_id") ~
      get[Option[scala.Long]]("medi_id") ~
      get[Option[scala.Long]]("tran_id") ~
      get[Option[String]]("medi_acta") ~
      get[Option[scala.Long]]("aaco_id_anterior") ~
      get[Option[scala.Long]]("aaco_id_nuevo") ~
      get[Option[Int]]("ortr_id") map {
      case repo_id ~
            repo_fechadigitacion ~
            repo_modificado ~
            repo_tipo_expansion ~
            repo_luminaria ~
            repo_redes ~
            repo_poste ~
            repo_subreporte ~
            repo_subid ~
            repo_email ~
            acti_id ~
            repo_codigo ~
            repo_apoyo ~
            urba_id ~
            muot_id ~
            medi_id ~
            tran_id ~
            medi_acta ~
            aaco_id_anterior ~
            aaco_id_nuevo ~
            ortr_id =>
        ReporteAdicional(
          repo_id,
          repo_fechadigitacion,
          repo_modificado,
          repo_tipo_expansion,
          repo_luminaria,
          repo_redes,
          repo_poste,
          repo_subreporte,
          repo_subid,
          repo_email,
          acti_id,
          repo_codigo,
          repo_apoyo,
          urba_id,
          muot_id,
          medi_id,
          tran_id,
          medi_acta,
          aaco_id_anterior,
          aaco_id_nuevo,
          ortr_id
        )
    }
  }

}

object ReporteDireccionDato {

  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val datoWrites = new Writes[ReporteDireccionDato] {
    def writes(dato: ReporteDireccionDato) = Json.obj(
      "aatc_id" -> dato.aatc_id,
      "aatc_id_anterior" -> dato.aatc_id_anterior,
      "aama_id" -> dato.aama_id,
      "aama_id_anterior" -> dato.aama_id_anterior,
      "aamo_id" -> dato.aamo_id,
      "aamo_id_anterior" -> dato.aamo_id_anterior,
      "aaco_id" -> dato.aaco_id,
      "aaco_id_anterior" -> dato.aaco_id_anterior,
      "aap_potencia" -> dato.aap_potencia,
      "aap_potencia_anterior" -> dato.aap_potencia_anterior,
      "aap_tecnologia" -> dato.aap_tecnologia,
      "aap_tecnologia_anterior" -> dato.aap_tecnologia_anterior,
      "aap_brazo" -> dato.aap_brazo,
      "aap_brazo_anterior" -> dato.aap_brazo_anterior,
      "aap_collarin" -> dato.aap_collarin,
      "aap_collarin_anterior" -> dato.aap_collarin_anterior,
      "tipo_id" -> dato.tipo_id,
      "tipo_id_anterior" -> dato.tipo_id_anterior,
      "aap_poste_altura" -> dato.aap_poste_altura,
      "aap_poste_altura_anterior" -> dato.aap_poste_altura_anterior,
      "aap_poste_propietario" -> dato.aap_poste_propietario,
      "aap_poste_propietario_anterior" -> dato.aap_poste_propietario_anterior
    )
  }

  implicit val datoRead: Reads[ReporteDireccionDato] = (
    (__ \ "aatc_id").readNullable[scala.Long] and
      (__ \ "aatc_id_anterior").readNullable[scala.Long] and
      (__ \ "aama_id").readNullable[scala.Long] and
      (__ \ "aama_id_anterior").readNullable[scala.Long] and
      (__ \ "aamo_id").readNullable[scala.Long] and
      (__ \ "aamo_id_anterior").readNullable[scala.Long] and
      (__ \ "aaco_id").readNullable[scala.Long] and
      (__ \ "aaco_id_anterior").readNullable[scala.Long] and
      (__ \ "aap_potencia").readNullable[Int] and
      (__ \ "aap_potencia_anterior").readNullable[Int] and
      (__ \ "aap_tecnologia").readNullable[String] and
      (__ \ "aap_tecnologia_anterior").readNullable[String] and
      (__ \ "aap_brazo").readNullable[String] and
      (__ \ "aap_brazo_anterior").readNullable[String] and
      (__ \ "aap_collarin").readNullable[String] and
      (__ \ "aap_collarin_anterior").readNullable[String] and
      (__ \ "tipo_id").readNullable[scala.Long] and
      (__ \ "tipo_id_anterior").readNullable[scala.Long] and
      (__ \ "aap_poste_altura").readNullable[Double] and
      (__ \ "aap_poste_altura_anterior").readNullable[Double] and
      (__ \ "aap_poste_propietario").readNullable[String] and
      (__ \ "aap_poste_propietario_anterior").readNullable[String]
  )(ReporteDireccionDato.apply _)

  val reporteDireccionDatoSet = {
    get[Option[scala.Long]]("aatc_id") ~
      get[Option[scala.Long]]("aatc_id_anterior") ~
      get[Option[scala.Long]]("aama_id") ~
      get[Option[scala.Long]]("aama_id_anterior") ~
      get[Option[scala.Long]]("aamo_id") ~
      get[Option[scala.Long]]("aamo_id_anterior") ~
      get[Option[scala.Long]]("aaco_id") ~
      get[Option[scala.Long]]("aaco_id_anterior") ~
      get[Option[scala.Long]]("tipo_id") ~
      get[Option[scala.Long]]("tipo_id_anterior") ~
      get[Option[Double]]("aap_poste_altura") ~
      get[Option[Double]]("aap_poste_altura_anterior") ~
      get[Option[String]]("aap_poste_propietario") ~
      get[Option[String]]("aap_poste_propietario_anterior") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aap_brazo") ~
      get[Option[String]]("aap_brazo_anterior") ~
      get[Option[String]]("aap_collarin") ~
      get[Option[String]]("aap_collarin_anterior") map {
      case aatc_id ~
            aatc_id_anterior ~
            aama_id ~
            aama_id_anterior ~
            aamo_id ~
            aamo_id_anterior ~
            aaco_id ~
            aaco_id_anterior ~
            tipo_id ~
            tipo_id_anterior ~
            aap_poste_altura ~
            aap_poste_altura_anterior ~
            aap_poste_propietario ~
            aap_poste_propietario_anterior ~
            aap_potencia ~
            aap_potencia_anterior ~
            aap_tecnologia ~
            aap_tecnologia_anterior ~
            aap_brazo ~
            aap_brazo_anterior ~
            aap_collarin ~
            aap_collarin_anterior =>
        ReporteDireccionDato(
          aatc_id,
          aatc_id_anterior,
          aama_id,
          aama_id_anterior,
          aamo_id,
          aamo_id_anterior,
          aaco_id,
          aaco_id_anterior,
          aap_potencia,
          aap_potencia_anterior,
          aap_tecnologia,
          aap_tecnologia_anterior,
          aap_brazo,
          aap_brazo_anterior,
          aap_collarin,
          aap_collarin_anterior,
          tipo_id,
          tipo_id_anterior,
          aap_poste_altura,
          aap_poste_altura_anterior,
          aap_poste_propietario,
          aap_poste_propietario_anterior
        )
    }
  }

}

object ReporteDireccionDatoAdicional {

  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val datoWrites = new Writes[ReporteDireccionDatoAdicional] {
    def writes(dato: ReporteDireccionDatoAdicional) = Json.obj(
      "aacu_id_anterior" -> dato.aacu_id_anterior,
      "aacu_id" -> dato.aacu_id,
      "aaus_id_anterior" -> dato.aaus_id_anterior,
      "aaus_id" -> dato.aaus_id,
      "medi_id_anterior" -> dato.medi_id_anterior,
      "medi_id" -> dato.medi_id,
      "tran_id_anterior" -> dato.tran_id_anterior,
      "tran_id" -> dato.tran_id,
      "aap_apoyo_anterior" -> dato.aap_apoyo_anterior,
      "aap_apoyo" -> dato.aap_apoyo,
      "aap_lat_anterior" -> dato.aap_lat_anterior,
      "aap_lat" -> dato.aap_lat,
      "aap_lng_anterior" -> dato.aap_lng_anterior,
      "aap_lng" -> dato.aap_lng
    )
  }

  implicit val datoRead: Reads[ReporteDireccionDatoAdicional] = (
    (__ \ "aacu_id_anterior").readNullable[scala.Long] and
      (__ \ "aacu_id").readNullable[scala.Long] and
      (__ \ "aaus_id_anterior").readNullable[scala.Long] and
      (__ \ "aaus_id").readNullable[scala.Long] and
      (__ \ "medi_id_anterior").readNullable[scala.Long] and
      (__ \ "medi_id").readNullable[scala.Long] and
      (__ \ "tran_id_anterior").readNullable[scala.Long] and
      (__ \ "tran_id").readNullable[scala.Long] and
      (__ \ "aap_apoyo_anterior").readNullable[String] and
      (__ \ "aap_apoyo").readNullable[String] and
      (__ \ "aap_lat_anterior").readNullable[String] and
      (__ \ "aap_lat").readNullable[String] and
      (__ \ "aap_lng_anterior").readNullable[String] and
      (__ \ "aap_lng").readNullable[String]
  )(ReporteDireccionDatoAdicional.apply _)

  val _set = {
    get[Option[scala.Long]]("aacu_id_anterior") ~
      get[Option[scala.Long]]("aacu_id") ~
      get[Option[scala.Long]]("aaus_id_anterior") ~
      get[Option[scala.Long]]("aaus_id") ~
      get[Option[scala.Long]]("medi_id_anterior") ~
      get[Option[scala.Long]]("medi_id") ~
      get[Option[scala.Long]]("tran_id_anterior") ~
      get[Option[scala.Long]]("tran_id") ~
      get[Option[String]]("aap_apoyo_anterior") ~
      get[Option[String]]("aap_apoyo") ~
      get[Option[String]]("aap_lat_anterior") ~
      get[Option[String]]("aap_lat") ~
      get[Option[String]]("aap_lng_anterior") ~
      get[Option[String]]("aap_lng") map {
      case aacu_id_anterior ~
            aacu_id ~
            aaus_id_anterior ~
            aaus_id ~
            medi_id_anterior ~
            medi_id ~
            tran_id_anterior ~
            tran_id ~
            aap_apoyo_anterior ~
            aap_apoyo ~
            aap_lat_anterior ~
            aap_lat ~
            aap_lng_anterior ~
            aap_lng =>
        ReporteDireccionDatoAdicional(
          aacu_id_anterior,
          aacu_id,
          aaus_id_anterior,
          aaus_id,
          medi_id_anterior,
          medi_id,
          tran_id_anterior,
          tran_id,
          aap_apoyo_anterior,
          aap_apoyo,
          aap_lat_anterior,
          aap_lat,
          aap_lng_anterior,
          aap_lng
        )
    }
  }

}

object ReporteDireccion {

  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val direccionWrites = new Writes[ReporteDireccion] {
    def writes(direccion: ReporteDireccion) = Json.obj(
      "repo_id" -> direccion.repo_id,
      "aap_id" -> direccion.aap_id,
      "even_direccion" -> direccion.even_direccion,
      "barr_id" -> direccion.barr_id,
      "even_direccion_anterior" -> direccion.even_direccion_anterior,
      "barr_id_anterior" -> direccion.barr_id_anterior,
      "even_id" -> direccion.even_id,
      "even_estado" -> direccion.even_estado,
      "even_horaini" -> direccion.even_horaini,
      "even_horafin" -> direccion.even_horafin,
      "tire_id" -> direccion.tire_id,
      "coau_codigo" -> direccion.coau_codigo,
      "aap_fechatoma" -> direccion.aap_fechatoma,
      "dato" -> direccion.dato,
      "dato_adicional" -> direccion.dato_adicional,
      "fotos" -> direccion.fotos
    )
  }

  implicit val direccionRead: Reads[ReporteDireccion] = (
    (__ \ "repo_id").readNullable[scala.Long] and
      (__ \ "aap_id").readNullable[scala.Long] and
      (__ \ "even_direccion").readNullable[String] and
      (__ \ "barr_id").readNullable[scala.Long] and
      (__ \ "even_direccion_anterior").readNullable[String] and
      (__ \ "barr_id_anterior").readNullable[scala.Long] and
      (__ \ "even_id").readNullable[Int] and
      (__ \ "even_estado").readNullable[Int] and
      (__ \ "even_horaini").readNullable[String] and
      (__ \ "even_horafin").readNullable[String] and
      (__ \ "tire_id").readNullable[scala.Long] and
      (__ \ "coau_codigo").readNullable[String] and
      (__ \ "aap_fechatoma").readNullable[DateTime] and
      (__ \ "dato").readNullable[ReporteDireccionDato] and
      (__ \ "dato_adicional").readNullable[ReporteDireccionDatoAdicional] and
      (__ \ "foto").readNullable[List[ReporteDireccionFoto]]
  )(ReporteDireccion.apply _)

  val reporteDireccionSet = {
    get[Option[scala.Long]]("repo_id") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("even_direccion") ~
      get[Option[scala.Long]]("barr_id") ~
      get[Option[String]]("even_direccion_anterior") ~
      get[Option[scala.Long]]("barr_id_anterior") ~
      get[Option[Int]]("even_id") ~
      get[Option[Int]]("even_estado") ~
      get[Option[String]]("even_horaini") ~
      get[Option[String]]("even_horafin") ~
      get[Option[scala.Long]]("tire_id") ~
      get[Option[String]]("coau_codigo") ~
      get[Option[DateTime]]("aap_fechatoma") map {
      case repo_id ~ aap_id ~ even_direccion ~ barr_id ~ even_direccion_anterior ~ barr_id_anterior ~ even_id ~ even_estado ~ even_horaini ~ even_horafin ~ tire_id ~ coau_codigo ~ aap_fechatoma =>
        ReporteDireccion(
          repo_id,
          aap_id,
          even_direccion,
          barr_id,
          even_direccion_anterior,
          barr_id_anterior,
          even_id,
          even_estado,
          even_horaini,
          even_horafin,
          tire_id,
          coau_codigo,
          aap_fechatoma,
          None,
          None,
          None
        )
    }
  }

}

object Reporte {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  val _set = {
    get[Option[scala.Long]]("repo_id") ~
      get[Option[scala.Long]]("tireuc_id") ~
      get[Option[scala.Long]]("reti_id") ~
      get[Option[scala.Long]]("repo_consecutivo") ~
      get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[String]]("repo_direccion") ~
      get[Option[String]]("repo_nombre") ~
      get[Option[String]]("repo_telefono") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("repo_horainicio") ~
      get[Option[String]]("repo_horafin") ~
      get[Option[String]]("repo_reportetecnico") ~
      get[Option[String]]("repo_descripcion") ~
      get[Option[String]]("repo_subrepoconsecutivo") ~
      get[Option[scala.Long]]("rees_id") ~
      get[Option[scala.Long]]("orig_id") ~
      get[Option[scala.Long]]("barr_id") ~
      get[Option[scala.Long]]("empr_id") ~
      get[Option[scala.Long]]("tiba_id") ~
      get[Option[scala.Long]]("usua_id") map {
      case repo_id ~
            tireuc_id ~
            reti_id ~
            repo_consecutivo ~
            repo_fecharecepcion ~
            repo_direccion ~
            repo_nombre ~
            repo_telefono ~
            repo_fechasolucion ~
            repo_horainicio ~
            repo_horafin ~
            repo_reportetecnico ~
            repo_descripcion ~
            repo_subrepoconsecutivo ~
            rees_id ~
            orig_id ~
            barr_id ~
            empr_id ~
            tiba_id ~
            usua_id =>
        Reporte(
          repo_id,
          tireuc_id,
          reti_id,
          repo_consecutivo,
          repo_fecharecepcion,
          repo_direccion,
          repo_nombre,
          repo_telefono,
          repo_fechasolucion,
          repo_horainicio,
          repo_horafin,
          repo_reportetecnico,
          repo_descripcion,
          repo_subrepoconsecutivo,
          rees_id,
          orig_id,
          barr_id,
          empr_id,
          tiba_id,
          usua_id,
          None,
          None,
          None,
          None,
          None
        )
    }
  }
}

object Vencido {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val reporteWrites = new Writes[Vencido] {
    def writes(reporte: Vencido) = Json.obj(
      "repo_id" -> reporte.repo_id,
      "reti_id" -> reporte.reti_id,
      "repo_consecutivo" -> reporte.repo_consecutivo,
      "repo_fecharecepcion" -> reporte.repo_fecharecepcion,
      "fecha_limite" -> reporte.fecha_limite,
      "horas" -> reporte.horas,
      "repo_direccion" -> reporte.repo_direccion,
      "repo_nombre" -> reporte.repo_nombre,
      "repo_telefono" -> reporte.repo_telefono,
      "repo_descripcion" -> reporte.repo_descripcion,
      "rees_id" -> reporte.rees_id,
      "orig_descripcion" -> reporte.orig_descripcion,
      "barr_descripcion" -> reporte.barr_descripcion,
      "cuad_descripcion" -> reporte.cuad_descripcion
    )
  }

  implicit val reporteReads: Reads[Vencido] = (
    (__ \ "repo_id").readNullable[scala.Long] and
      (__ \ "reti_id").readNullable[scala.Long] and
      (__ \ "repo_consecutivo").readNullable[scala.Long] and
      (__ \ "repo_fecharecepcion").readNullable[DateTime] and
      (__ \ "fecha_limite").readNullable[DateTime] and
      (__ \ "horas").readNullable[scala.Int] and
      (__ \ "repo_direccion").readNullable[String] and
      (__ \ "repo_nombre").readNullable[String] and
      (__ \ "repo_telefono").readNullable[String] and
      (__ \ "repo_descripcion").readNullable[String] and
      (__ \ "rees_id").readNullable[scala.Long] and
      (__ \ "orig_descripcion").readNullable[String] and
      (__ \ "barr_descripcion").readNullable[String] and
      (__ \ "cuad_descripcion").readNullable[String]
  )(Vencido.apply _)

  val _set = {
    get[Option[scala.Long]]("repo_id") ~
      get[Option[scala.Long]]("reti_id") ~
      get[Option[scala.Long]]("repo_consecutivo") ~
      get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("fecha_limite") ~
      get[Option[Int]]("horas") ~
      get[Option[String]]("repo_direccion") ~
      get[Option[String]]("repo_nombre") ~
      get[Option[String]]("repo_telefono") ~
      get[Option[String]]("repo_descripcion") ~
      get[Option[scala.Long]]("rees_id") ~
      get[Option[String]]("orig_descripcion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("cuad_descripcion") map {
      case repo_id ~
            reti_id ~
            repo_consecutivo ~
            repo_fecharecepcion ~
            fecha_limite ~
            horas ~
            repo_direccion ~
            repo_nombre ~
            repo_telefono ~
            repo_descripcion ~
            rees_id ~
            orig_descripcion ~
            barr_descripcion ~
            cuad_descripcion =>
        Vencido(
          repo_id,
          reti_id,
          repo_consecutivo,
          repo_fecharecepcion,
          fecha_limite,
          horas,
          repo_direccion,
          repo_nombre,
          repo_telefono,
          repo_descripcion,
          rees_id,
          orig_descripcion,
          barr_descripcion,
          cuad_descripcion
        )
    }
  }
}

/// AQUI
object ReporteWeb {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val reporteWrites = new Writes[ReporteWeb] {
    def writes(reporte: ReporteWeb) = Json.obj(
      "repo_consecutivo" -> reporte.repo_consecutivo,
      "repo_fecharecepcion" -> reporte.repo_fecharecepcion,
      "repo_fechadigitacion" -> reporte.repo_fechadigitacion,
      "repo_direccion" -> reporte.repo_direccion,
      "barr_id" -> reporte.barr_id,
      "barr_descripcion" -> reporte.barr_descripcion,
      "repo_nombre" -> reporte.repo_nombre,
      "repo_telefono" -> reporte.repo_telefono,
      "repo_email" -> reporte.repo_email,
      "repo_descripcion" -> reporte.repo_descripcion,
      "rees_id" -> reporte.rees_id,
      "rees_descripcion" -> reporte.rees_descripcion,
      "repo_reportetecnico" -> reporte.repo_reportetecnico
    )
  }

  implicit val reporteReads: Reads[ReporteWeb] = (
    (__ \ "repo_consecutivo").readNullable[scala.Long] and
      (__ \ "repo_fecharecepcion").readNullable[DateTime] and
      (__ \ "repo_fechadigitacion").readNullable[DateTime] and
      (__ \ "repo_direccion").readNullable[String] and
      (__ \ "barr_id").readNullable[scala.Long] and
      (__ \ "barr_descripcion").readNullable[String] and
      (__ \ "repo_nombre").readNullable[String] and
      (__ \ "repo_telefono").readNullable[String] and
      (__ \ "repo_email").readNullable[String] and
      (__ \ "repo_descripcion").readNullable[String] and
      (__ \ "rees_id").readNullable[scala.Long] and
      (__ \ "rees_descripcion").readNullable[String] and
      (__ \ "repo_reportetecnico").readNullable[String]
  )(ReporteWeb.apply _)

  val _set = {
    get[Option[scala.Long]]("repo_consecutivo") ~
      get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[String]]("repo_direccion") ~
      get[Option[scala.Long]]("barr_id") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("repo_nombre") ~
      get[Option[String]]("repo_telefono") ~
      get[Option[String]]("repo_email") ~
      get[Option[String]]("repo_descripcion") ~
      get[Option[scala.Long]]("reporte.rees_id") ~
      get[Option[String]]("rees_descripcion") ~
      get[Option[String]]("repo_reportetecnico") map {
      case repo_consecutivo ~
            repo_fecharecepcion ~
            repo_fechadigitacion ~
            repo_direccion ~
            barr_id ~
            barr_descripcion ~
            repo_nombre ~
            repo_telefono ~
            repo_email ~
            repo_descripcion ~
            rees_id ~
            rees_descripcion ~
            repo_reportetecnico =>
        ReporteWeb(
          repo_consecutivo,
          repo_fecharecepcion,
          repo_fechadigitacion,
          repo_direccion,
          barr_id,
          barr_descripcion,
          repo_nombre,
          repo_telefono,
          repo_email,
          repo_descripcion,
          rees_id,
          rees_descripcion,
          repo_reportetecnico
        )
    }
  }
}

/// AQUI

object ReporteConsulta {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val rcWrites = new Writes[ReporteConsulta] {
    def writes(reporte: ReporteConsulta) = Json.obj(
      "repo_id" -> reporte.repo_id,
      "reti_id" -> reporte.reti_id,
      "repo_consecutivo" -> reporte.repo_consecutivo,
      "repo_fecharecepcion" -> reporte.repo_fecharecepcion,
      "repo_direccion" -> reporte.repo_direccion,
      "repo_nombre" -> reporte.repo_nombre,
      "repo_telefono" -> reporte.repo_telefono,
      "repo_reportetecnico" -> reporte.repo_reportetecnico,
      "repo_descripcion" -> reporte.repo_descripcion,
      "rees_id" -> reporte.rees_id,
      "orig_id" -> reporte.orig_id,
      "barr_id" -> reporte.barr_id,
      "empr_id" -> reporte.empr_id,
      "tiba_id" -> reporte.tiba_id,
      "usua_id" -> reporte.usua_id,
      "adicional" -> reporte.adicional
    )
  }

  implicit val rcReads: Reads[ReporteConsulta] = (
    (__ \ "repo_id").readNullable[scala.Long] and
      (__ \ "reti_id").readNullable[scala.Long] and
      (__ \ "repo_consecutivo").readNullable[scala.Long] and
      (__ \ "repo_fecharecepcion").readNullable[DateTime] and
      (__ \ "repo_direccion").readNullable[String] and
      (__ \ "repo_nombre").readNullable[String] and
      (__ \ "repo_telefono").readNullable[String] and
      (__ \ "repo_reportetecnico").readNullable[String] and
      (__ \ "repo_descripcion").readNullable[String] and
      (__ \ "rees_id").readNullable[scala.Long] and
      (__ \ "orig_id").readNullable[scala.Long] and
      (__ \ "barr_id").readNullable[scala.Long] and
      (__ \ "empr_id").readNullable[scala.Long] and
      (__ \ "tiba_id").readNullable[scala.Long] and
      (__ \ "usua_id").readNullable[scala.Long] and
      (__ \ "adicional").readNullable[ReporteAdicional]
  )(ReporteConsulta.apply _)
}

object ReporteTipo {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val tipoWrites = new Writes[ReporteTipo] {
    def writes(tipo: ReporteTipo) = Json.obj(
      "reti_id" -> tipo.reti_id,
      "reti_descripcion" -> tipo.reti_descripcion,
      "reti_consecutivo" -> tipo.reti_consecutivo,
      "reti_estado" -> tipo.reti_estado,
      "usua_id" -> tipo.usua_id
    )
  }

  implicit val tipoRead: Reads[ReporteTipo] = (
    (__ \ "reti_id").readNullable[scala.Long] and
      (__ \ "reti_descripcion").read[String] and
      (__ \ "reti_consecutivo").read[scala.Long] and
      (__ \ "reti_estado").read[Int] and
      (__ \ "usua_id").read[scala.Long]
  )(ReporteTipo.apply _)

  val repoTipoSet = {
    get[Option[scala.Long]]("reti_id") ~
      get[String]("reti_descripcion") ~
      get[scala.Long]("reti_consecutivo") ~
      get[Int]("reti_estado") ~
      get[scala.Long]("usua_id") map {
      case reti_id ~ reti_descripcion ~ reti_consecutivo ~ reti_estado ~ usua_id =>
        ReporteTipo(
          reti_id,
          reti_descripcion,
          reti_consecutivo,
          reti_estado,
          usua_id
        )
    }
  }
}

object ReporteEstado {

  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val estadoWrites = new Writes[ReporteEstado] {
    def writes(estado: ReporteEstado) = Json.obj(
      "rees_id" -> estado.rees_id,
      "rees_descripcion" -> estado.rees_descripcion,
      "rees_estado" -> estado.rees_estado,
      "usua_id" -> estado.usua_id
    )
  }

  implicit val estadoReads: Reads[ReporteEstado] = (
    (__ \ "rees_id").readNullable[scala.Long] and
      (__ \ "rees_descripcion").read[String] and
      (__ \ "rees_estado").read[Int] and
      (__ \ "usua_id").read[scala.Long]
  )(ReporteEstado.apply _)

  val oEstado = {
    get[Option[scala.Long]]("rees_id") ~
      get[String]("rees_descripcion") ~
      get[Int]("rees_estado") ~
      get[scala.Long]("usua_id") map {
      case rees_id ~ rees_descripcion ~ rees_estado ~ usua_id =>
        new ReporteEstado(rees_id, rees_descripcion, rees_estado, usua_id)
    }
  }

}

object RepoElemento {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val repelemWrites = new Writes[RepoElemento] {
    def writes(repelem: RepoElemento) = Json.obj(
      "rere_id" -> repelem.rere_id,
      "rere_descripcion" -> repelem.rere_descripcion,
      "rere_estado" -> repelem.rere_estado,
      "usua_id" -> repelem.usua_id
    )
  }

  implicit val repelemReads: Reads[RepoElemento] = (
    (__ \ "rere_id").readNullable[scala.Long] and
      (__ \ "rere_descripcion").readNullable[String] and
      (__ \ "rere_estado").readNullable[Int] and
      (__ \ "usua_id").readNullable[scala.Long]
  )(RepoElemento.apply _)

  val repoElementoSet = {
    get[Option[scala.Long]]("repoelemento.rere_id") ~
      get[Option[String]]("repoelemento.rere_descripcion") ~
      get[Option[Int]]("repoelemento.rere_estado") ~
      get[Option[scala.Long]]("repoelemento.usua_id") map {
      case rere_id ~ rere_descripcion ~ rere_estado ~ usua_id =>
        new RepoElemento(rere_id, rere_descripcion, rere_estado, usua_id)
    }
  }
}

class ReporteRepository @Inject()(
    dbapi: DBApi,
    eventoService: EventoRepository,
    elementoService: ElementoRepository,
    empresaService: EmpresaRepository,
    usuarioService: UsuarioRepository,
    medidorService: MedidorRepository,
    transformadorService: TransformadorRepository,
    controlReporteService: ControlReporteRepository,
    transformadorReporteService: TransformadorReporteRepository,
    medidorReporteService: MedidorReporteRepository,
    cuadrillaService: CuadrillaRepository,
    aapService: AapRepository
)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

  /**
    *  Validar Reporte Diligenciado
    *  @param reti_id
    *  @param repo_consecutivo
    */
  def validarReporteDiligenciado(
      reti_id: scala.Long,
      repo_consecutivo: Int,
      empr_id: scala.Long
  ): (Boolean, String) = {
    db.withConnection { implicit connection =>
      val lista = ListBuffer[String]()
      if (reti_id > 1) {
        val reportes = SQL(
          """SELECT r.repo_consecutivo FROM siap.reporte r 
                              LEFT JOIN siap.barrio b ON b.barr_id = r.barr_id
                              LEFT JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id 
                              WHERE r.reti_id = {reti_id} and r.repo_consecutivo < {repo_consecutivo} and r.rees_id in (1,2) and r.empr_id = {empr_id}
                              AND tb.tiba_id <> {tiba_id} ORDER BY r.repo_consecutivo ASC"""
        ).on(
            'reti_id -> reti_id,
            'repo_consecutivo -> repo_consecutivo,
            'empr_id -> empr_id,
            'tiba_id -> 2
          )
          .as(SqlParser.scalar[Int] *)
        reportes.map { r =>
          lista += r.toString()
        }
        (lista.length < 1, lista.mkString(","))
      } else {
        (true, lista.mkString(","))
      }
    }
  }
  /*
   *
   */
  def validarCodigo(
      elem_id: scala.Long,
      codigo: String,
      empr_id: scala.Long
  ): String = {
    db.withConnection { implicit connection =>
      var resultado: String = ""
      val tiel_id =
        SQL("""SELECT tiel_id FROM siap.elemento WHERE elem_id = {elem_id}""")
          .on(
            'elem_id -> elem_id
          )
          .as(SqlParser.scalar[scala.Long].single)
      val conteo1: scala.Long = SQL(
        """SELECT COUNT(*) AS conteo FROM siap.reporte_evento r
                           INNER JOIN siap.reporte s ON s.repo_id = r.repo_id
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_retirado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}"""
      ).on(
          'tiel_id -> tiel_id,
          'codigo -> codigo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)
      val conteo2: scala.Long = SQL(
        """SELECT COUNT(*) AS conteo FROM siap.reporte_evento r
                           INNER JOIN siap.reporte s ON s.repo_id = r.repo_id
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_instalado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}"""
      ).on(
          'tiel_id -> tiel_id,
          'codigo -> codigo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)
      val conteo3: scala.Long = SQL(
        """SELECT COUNT(*) AS conteo FROM siap.obra_evento r
                           INNER JOIN siap.obra s ON s.obra_id = r.obra_id            
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_retirado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}"""
      ).on(
          'tiel_id -> tiel_id,
          'codigo -> codigo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)
      val conteo4: scala.Long = SQL(
        """SELECT COUNT(*) AS conteo FROM siap.obra_evento r
                           INNER JOIN siap.obra s ON s.obra_id = r.obra_id            
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_instalado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}"""
      ).on(
          'tiel_id -> tiel_id,
          'codigo -> codigo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)

      val conteo5: scala.Long = SQL(
        """SELECT COUNT(*) AS conteo FROM siap.control_reporte_evento r
                           INNER JOIN siap.control_reporte s ON s.repo_id = r.repo_id            
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_retirado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}"""
      ).on(
          'tiel_id -> tiel_id,
          'codigo -> codigo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)

      val conteo6: scala.Long = SQL(
        """SELECT COUNT(*) AS conteo FROM siap.control_reporte_evento r
                           INNER JOIN siap.control_reporte s ON s.repo_id = r.repo_id            
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_instalado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}"""
      ).on(
          'tiel_id -> tiel_id,
          'codigo -> codigo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)

      if (conteo1 > 0 || conteo3 > 0 || conteo5 > 0) {
        resultado = resultado + "1"
      } else {
        resultado = resultado + "0"
      }
      if (conteo2 > 0 || conteo4 > 0 || conteo6 > 0) {
        resultado = resultado + "1"
      } else {
        resultado = resultado + "0"
      }
      resultado
    }
  }

  /**
    * Recuperar total de registros
    * @param empr_id: scala.Long
    * @return total
    */
  def cuenta(empr_id: scala.Long): scala.Long = {
    db.withConnection { implicit connection =>
      val result = SQL(
        "SELECT COUNT(*) AS c FROM siap.reporte WHERE empr_id = {empr_id} and rees_id < 9"
      ).on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)
      result
    }
  }

  /**
    Recuperar todos los Reporte de una empresa
    @param empr_id: scala.Long
    @param page_size: scala.Long
    @param current_page: scala.Long
    @return Future[Iterable[Reporte]]
    */
  def todos(
      page_size: scala.Long,
      current_page: scala.Long,
      empr_id: scala.Long,
      orderby: String,
      filter: String
  ): Future[Iterable[Reporte]] =
    Future[Iterable[Reporte]] {
      db.withConnection { implicit connection =>
        var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
        var query: String = """SELECT *
                                          FROM siap.reporte r 
                                          LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                                          WHERE r.empr_id = {empr_id}
                                          and r.rees_id < 9 """
        if (!filter.isEmpty) {
          query = query + " and " + filter
        }
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY r.rees_id ASC, r.repo_id DESC"
        }
        query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        val reps = SQL(query)
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(Reporte._set *)
        /*reps.map { r =>
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1
                """
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)

          val eventos = SQL(
            """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)

          val novedades = SQL(
            """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteNovedad._set *)

          val direcciones = SQL(
            """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
            dat match {
              case None =>
                dat = Some(
                  new ReporteDireccionDato(
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
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
                )
              case Some(dat) => None
            }
            var adi = SQL(
              """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDatoAdicional._set.singleOpt)
            adi match {
              case None =>
                adi = Some(
                  new ReporteDireccionDatoAdicional(
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
                    None
                  )
                )
              case Some(adi) => None
            }
            var fotos =
              SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
                .on(
                  'repo_id -> r.repo_id,
                  'aap_id -> d.aap_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteDireccionFoto._set *)
            var _listFoto = new ListBuffer[ReporteDireccionFoto]()
            fotos.map { f =>
              _listFoto += f
            }
            val direccion = d.copy(
              dato = dat,
              dato_adicional = adi,
              fotos = Some(_listFoto.toList)
            )
            _listDireccion += direccion
          }
          val reporte = new Reporte(
            r.repo_id,
            r.tireuc_id,
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_fechasolucion,
            r.repo_horainicio,
            r.repo_horafin,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.repo_subrepoconsecutivo,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            None, // adicional,
            None, // Some(meams),
            None, // Some(eventos),
            None, // Some(_listDireccion.toList),
            None //Some(novedades)
          )
          _list += reporte
        } */
        //_list
        reps.toList
      }
    }

  /**
    * Recuperar todos los Unidad activas
    */
  def reportes(empr_id: scala.Long): Future[Iterable[Reporte]] =
    Future[Iterable[Reporte]] {
      db.withConnection { implicit connection =>
        var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
        val reps = SQL(
          """SELECT * FROM siap.reporte r 
                                          WHERE r.empr_id = {empr_id} 
                                          and r.rees_id < 9 ORDER BY r.reti_id ASC, r.repo_consecutivo ASC"""
        ).on(
            'empr_id -> empr_id
          )
          .as(Reporte._set *)
        reps.map { r =>
          val eventos = SQL(
            """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val novedades = SQL(
            """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteNovedad._set *)
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val direcciones = SQL(
            """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
            dat match {
              case None =>
                dat = Some(
                  new ReporteDireccionDato(
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
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
                )
              case Some(dat) => None
            }
            var adi = SQL(
              """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDatoAdicional._set.singleOpt)
            adi match {
              case None =>
                adi = Some(
                  new ReporteDireccionDatoAdicional(
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
                    None
                  )
                )
              case Some(adi) => None
            }
            var fotos =
              SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
                .on(
                  'repo_id -> r.repo_id,
                  'aap_id -> d.aap_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteDireccionFoto._set *)
            var _listFoto = new ListBuffer[ReporteDireccionFoto]()
            fotos.map { f =>
              _listFoto += f
            }
            val direccion = d.copy(
              dato = dat,
              dato_adicional = adi,
              fotos = Some(_listFoto.toList)
            )
            _listDireccion += direccion
          }
          val reporte = new Reporte(
            r.repo_id,
            r.tireuc_id,
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_fechasolucion,
            r.repo_horainicio,
            r.repo_horafin,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.repo_subrepoconsecutivo,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            adicional,
            Some(meams),
            Some(eventos),
            Some(_listDireccion.toList),
            Some(novedades)
          )
          _list += reporte
        }
        _list
      }
    }

  /**
    * Recuperar todos los Unidad activas
    */
  def reportesporaap(
      aap_id: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] =
    Future[Iterable[Reporte]] {
      db.withConnection { implicit connection =>
        var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
        val reps = SQL(
          """SELECT DISTINCT r.* FROM siap.reporte r
                          LEFT JOIN siap.reporte_direccion rd ON rd.repo_id = r.repo_id
                          LEFT JOIN siap.reporte_evento re ON re.repo_id = r.repo_id
                          WHERE rd.aap_id = {aap_id} or re.aap_id = {aap_id} and r.empr_id = {empr_id} ORDER BY r.repo_id"""
        ).on(
            'aap_id -> aap_id,
            'empr_id -> empr_id
          )
          .as(Reporte._set *)
        reps.map { r =>
          val eventos = SQL(
            """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val novedades = SQL(
            """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteNovedad._set *)
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val direcciones = SQL(
            """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
            dat match {
              case None =>
                dat = Some(
                  new ReporteDireccionDato(
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
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
                )
              case Some(dat) => None
            }
            var adi = SQL(
              """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDatoAdicional._set.singleOpt)
            adi match {
              case None =>
                adi = Some(
                  new ReporteDireccionDatoAdicional(
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
                    None
                  )
                )
              case Some(adi) => None
            }
            var fotos =
              SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
                .on(
                  'repo_id -> r.repo_id,
                  'aap_id -> d.aap_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteDireccionFoto._set *)
            var _listFoto = new ListBuffer[ReporteDireccionFoto]()
            fotos.map { f =>
              _listFoto += f
            }
            val direccion = d.copy(
              dato = dat,
              dato_adicional = adi,
              fotos = Some(_listFoto.toList)
            )
            _listDireccion += direccion
          }
          val reporte = new Reporte(
            r.repo_id,
            r.tireuc_id,
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_fechasolucion,
            r.repo_horainicio,
            r.repo_horafin,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.repo_subrepoconsecutivo,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            adicional,
            Some(meams),
            Some(eventos),
            Some(_listDireccion.toList),
            Some(novedades)
          )
          _list += reporte
        }
        _list
      }
    }

  def siap_reporte_vencido(empr_id: scala.Long): Future[Iterable[Vencido]] =
    Future[Iterable[Vencido]] {
      db.withConnection { implicit connection =>
        SQL(
          """select o.repo_id, o.repo_fecharecepcion, o.reti_id, o.repo_consecutivo, o.fecha_limite, TRUNC(EXTRACT(epoch FROM o.horas)/3600)::Int as horas, o.orig_descripcion, o.repo_nombre, o.repo_telefono, o.repo_direccion, o.barr_descripcion, o.repo_descripcion, o.rees_id, c.cuad_descripcion
         from
         (select r.repo_id, r.repo_fecharecepcion, r.reti_id, r.repo_consecutivo,  
         ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval) as fecha_limite,
         (current_timestamp - ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval)) as horas, 
         o.orig_descripcion, r.repo_nombre, r.repo_telefono, r.repo_direccion, b.barr_descripcion, r.repo_descripcion, r.rees_id from siap.reporte r
        inner join siap.reporte_tipo rt on rt.reti_id = r.reti_id
        left join siap.barrio b on b.barr_id = r.barr_id
        left join siap.origen o on o.orig_id = r.orig_id
        where r.rees_id = 1 and r.empr_id = {empr_id}
        ) o
        left join siap.ordentrabajo_reporte otr on otr.repo_id = o.repo_id
        left join siap.ordentrabajo t on t.ortr_id = otr.ortr_id
        left join siap.cuadrilla c on c.cuad_id = t.cuad_id
        where o.horas > 1 * '1s' :: interval
        order by o.reti_id, o.repo_consecutivo"""
        ).on('empr_id -> empr_id).as(Vencido._set *)
      }
    }

  /**
    * Recuperar un Reporte dado su repo_id
    * @param repo_consecutivo: Int
    * @param empr_id: scala.Long
    */
  def buscarPorConsecutivoWeb(
      repo_consecutivo: Int,
      empr_id: scala.Long
  ): Option[ReporteWeb] = {
    db.withConnection { implicit connection =>
      val r = SQL(
        """SELECT DISTINCT * FROM (
            SELECT * FROM siap.reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
            UNION ALL
            SELECT * FROM siap.control_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.control_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
            UNION ALL
            SELECT * FROM siap.transformador_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.transformador_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
        ) as reporte
        """
      ).on(
          'repo_consecutivo -> repo_consecutivo,
          'empr_id -> empr_id
        )
        .as(ReporteWeb._set.singleOpt)
      r
    }
  }

  /**
    * Recuperar un Reporte dado su repo_id
    * @param repo_id: scala.Long
    */
  def buscarPorId(repo_id: scala.Long): Option[Reporte] = {
    db.withConnection { implicit connection =>
      val r = SQL("""SELECT * FROM siap.reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.repo_id = {repo_id}""")
        .on(
          'repo_id -> repo_id
        )
        .as(Reporte._set.singleOpt)

      r match {
        case Some(r) =>
          val eventos = SQL(
            """SELECT re1.* FROM siap.reporte_evento re1
                WHERE re1.repo_id = {repo_id} and re1.even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> repo_id
            )
            .as(scalar[scala.Long].*)
          val novedades = SQL(
            """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteNovedad._set *)
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id} 
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
          ).on(
              'repo_id -> repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val direcciones = SQL(
            """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
            dat match {
              case None =>
                dat = Some(
                  new ReporteDireccionDato(
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
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
                )
              case Some(dat) => None
            }
            var adi = SQL(
              """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDatoAdicional._set.singleOpt)
            adi match {
              case None =>
                adi = Some(
                  new ReporteDireccionDatoAdicional(
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
                    None
                  )
                )
              case Some(adi) => None
            }
            var fotos =
              SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
                .on(
                  'repo_id -> r.repo_id,
                  'aap_id -> d.aap_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteDireccionFoto._set *)
            var _listFoto = new ListBuffer[ReporteDireccionFoto]()
            fotos.map { f =>
              _listFoto += f
            }
            val direccion = d.copy(
              dato = dat,
              dato_adicional = adi,
              fotos = Some(_listFoto.toList)
            )
            _listDireccion += direccion
          }
          val reporte = new Reporte(
            r.repo_id,
            r.tireuc_id,
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_fechasolucion,
            r.repo_horainicio,
            r.repo_horafin,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.repo_subrepoconsecutivo,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            adicional,
            Some(meams),
            Some(eventos),
            Some(_listDireccion.toList),
            Some(novedades)
          )
          println("Reporte:  " + reporte)
          Some(reporte)

        case None => None
      }
    }
  }

  /**
    * Recuperar un Reporte dado su repo_consecutivo
    * @param reti_id: scala.Long
    * @param repo_consecutivo: scala.Long
    * @param empr_id: scala.Long
    */
  def buscarPorConsecutivo(
      reti_id: scala.Long,
      repo_consecutivo: scala.Long,
      empr_id: scala.Long
  ): Option[Reporte] = {
    db.withConnection { implicit connection =>
      val r = SQL(
        """SELECT DISTINCT * FROM (
            SELECT * FROM siap.reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
            UNION ALL
            SELECT * FROM siap.control_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.control_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
            UNION ALL
            SELECT * FROM siap.transformador_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.transformador_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
            UNION ALL
            SELECT * FROM siap.medidor_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.medidor_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9
        ) as reporte"""
      ).on(
          'reti_id -> reti_id,
          'repo_consecutivo -> repo_consecutivo,
          'empr_id -> empr_id
        )
        .as(Reporte._set.singleOpt)

      r match {
        case Some(r) =>
          val eventos = SQL(
            """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val novedades = SQL(
            """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteNovedad._set *)
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val direcciones = SQL(
            """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
            dat match {
              case None =>
                dat = Some(
                  new ReporteDireccionDato(
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
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
                )
              case Some(dat) => None
            }
            var adi = SQL(
              """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDatoAdicional._set.singleOpt)
            adi match {
              case None =>
                adi = Some(
                  new ReporteDireccionDatoAdicional(
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
                    None
                  )
                )
              case Some(adi) => None
            }
            var fotos =
              SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
                .on(
                  'repo_id -> r.repo_id,
                  'aap_id -> d.aap_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteDireccionFoto._set *)
            var _listFoto = new ListBuffer[ReporteDireccionFoto]()
            fotos.map { f =>
              _listFoto += f
            }
            val direccion = d.copy(
              dato = dat,
              dato_adicional = adi,
              fotos = Some(_listFoto.toList)
            )
            _listDireccion += direccion
          }
          val reporte = new Reporte(
            r.repo_id,
            r.tireuc_id,
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_fechasolucion,
            r.repo_horainicio,
            r.repo_horafin,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.repo_subrepoconsecutivo,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            adicional,
            Some(meams),
            Some(eventos),
            Some(_listDireccion.toList),
            Some(novedades)
          )
          Some(reporte)

        case None => None
      }
    }
  }

  /**
    * Recuperar un Reporte dado su repo_consecutivo
    * @param reti_id: scala.Long
    * @param repo_consecutivo: scala.Long
    * @param empr_id: scala.Long
    */
  def buscarPorConsecutivoMovil(
      reti_id: scala.Long,
      repo_consecutivo: scala.Long,
      empr_id: scala.Long
  ): Option[Reporte] = {
    db.withConnection { implicit connection =>
      val r = SQL(
        """SELECT * FROM siap.reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.rees_id in (1,2) AND (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE"""
      ).on(
          'reti_id -> reti_id,
          'repo_consecutivo -> repo_consecutivo
          //'empr_id -> empr_id
        )
        .as(Reporte._set.singleOpt)

      r match {
        case Some(r) =>
          val eventos = SQL(
            """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val novedades = SQL(
            """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteNovedad._set *)
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val direcciones = SQL(
            """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
            dat match {
              case None =>
                dat = Some(
                  new ReporteDireccionDato(
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
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
                )
              case Some(dat) => None
            }
            var adi = SQL(
              """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
            ).on(
                'repo_id -> d.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .as(ReporteDireccionDatoAdicional._set.singleOpt)
            adi match {
              case None =>
                adi = Some(
                  new ReporteDireccionDatoAdicional(
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
                    None
                  )
                )
              case Some(adi) => None
            }
            var fotos =
              SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
                .on(
                  'repo_id -> r.repo_id,
                  'aap_id -> d.aap_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteDireccionFoto._set *)
            var _listFoto = new ListBuffer[ReporteDireccionFoto]()
            fotos.map { f =>
              _listFoto += f
            }
            val direccion = d.copy(
              dato = dat,
              dato_adicional = adi,
              fotos = Some(_listFoto.toList)
            )
            _listDireccion += direccion
          }
          val reporte = new Reporte(
            r.repo_id,
            Some(1),
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_fechasolucion,
            r.repo_horainicio,
            r.repo_horafin,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.repo_subrepoconsecutivo,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            adicional,
            Some(meams),
            Some(eventos),
            Some(_listDireccion.toList),
            Some(novedades)
          )
          Some(reporte)

        case None =>
          val r = SQL(
            """SELECT * FROM siap.control_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.control_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.rees_id in (1,2) AND (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE"""
          ).on(
              'reti_id -> reti_id,
              'repo_consecutivo -> repo_consecutivo
              //'empr_id -> empr_id
            )
            .as(Reporte._set.singleOpt)

          r match {
            case Some(r) =>
              val eventos = SQL(
                """SELECT * FROM siap.control_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
              ).on(
                  'repo_id -> r.repo_id
                )
                .as(Evento.eventoSet *)
              val meams = SQL(
                """SELECT m.meam_id FROM siap.control_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
              ).on(
                  'repo_id -> r.repo_id
                )
                .as(scalar[scala.Long].*)
              val novedades = List[ReporteNovedad]() /* SQL(
                """SELECT * FROM siap.control_reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
              ).on(
                  'repo_id -> r.repo_id,
                  'tireuc_id -> r.tireuc_id
                )
              .as(ReporteNovedad._set *) */
              val adicional = SQL(
                """SELECT * FROM siap.control_reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
              ).on(
                  'repo_id -> r.repo_id,
                  'tireuc_id -> r.tireuc_id
                )
                .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
              val direcciones = SQL(
                """SELECT * FROM siap.control_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
              ).on(
                  'repo_id -> r.repo_id
                )
                .as(ReporteDireccion.reporteDireccionSet *)
              var _listDireccion = new ListBuffer[ReporteDireccion]()
              direcciones.map { d =>
                var dat = SQL(
                  """SELECT * FROM siap.control_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
                ).on(
                    'repo_id -> d.repo_id,
                    'aap_id -> d.aap_id,
                    'even_id -> d.even_id
                  )
                  .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
                dat match {
                  case None =>
                    dat = Some(
                      new ReporteDireccionDato(
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
                        None,
                        None,
                        None,
                        None,
                        None,
                        None,
                        None
                      )
                    )
                  case Some(dat) => None
                }
                var adi = SQL(
                  """SELECT * FROM siap.control_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
                ).on(
                    'repo_id -> d.repo_id,
                    'aap_id -> d.aap_id,
                    'even_id -> d.even_id
                  )
                  .as(ReporteDireccionDatoAdicional._set.singleOpt)
                adi match {
                  case None =>
                    adi = Some(
                      new ReporteDireccionDatoAdicional(
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
                        None
                      )
                    )
                  case Some(adi) => None
                }
                var fotos =
                  SQL("""
                  SELECT * FROM siap.control_reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                  """)
                    .on(
                      'repo_id -> r.repo_id,
                      'aap_id -> d.aap_id,
                      'tireuc_id -> r.tireuc_id
                    )
                    .as(ReporteDireccionFoto._set *)
                var _listFoto = new ListBuffer[ReporteDireccionFoto]()
                fotos.map { f =>
                  _listFoto += f
                }
                val direccion = d.copy(
                  dato = dat,
                  dato_adicional = adi,
                  fotos = Some(_listFoto.toList)
                )
                _listDireccion += direccion
              }
              val reporte = new Reporte(
                r.repo_id,
                Some(2),
                r.reti_id,
                r.repo_consecutivo,
                r.repo_fecharecepcion,
                r.repo_direccion,
                r.repo_nombre,
                r.repo_telefono,
                r.repo_fechasolucion,
                r.repo_horainicio,
                r.repo_horafin,
                r.repo_reportetecnico,
                r.repo_descripcion,
                r.repo_subrepoconsecutivo,
                r.rees_id,
                r.orig_id,
                r.barr_id,
                r.empr_id,
                r.tiba_id,
                r.usua_id,
                adicional,
                Some(meams),
                Some(eventos),
                Some(_listDireccion.toList),
                Some(novedades)
              )
              Some(reporte)

            case None =>
              val r = SQL(
                """SELECT * FROM siap.transformador_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.transformador_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.rees_id in (1,2) AND (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE"""
              ).on(
                  'reti_id -> reti_id,
                  'repo_consecutivo -> repo_consecutivo
                  //'empr_id -> empr_id
                )
                .as(Reporte._set.singleOpt)

              r match {
                case Some(r) =>
                  val eventos = SQL(
                    """SELECT * FROM siap.transformador_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
                  ).on(
                      'repo_id -> r.repo_id
                    )
                    .as(Evento.eventoSet *)
                  val meams = SQL(
                    """SELECT m.meam_id FROM siap.transformador_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
                  ).on(
                      'repo_id -> r.repo_id
                    )
                    .as(scalar[scala.Long].*)
                  val novedades = List[ReporteNovedad]()

                  val adicional = SQL(
                    """SELECT * FROM siap.transformador_reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
                  ).on(
                      'repo_id -> r.repo_id,
                      'tireuc_id -> r.tireuc_id
                    )
                    .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
                  val direcciones = SQL(
                    """SELECT * FROM siap.transformador_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
                  ).on(
                      'repo_id -> r.repo_id
                    )
                    .as(ReporteDireccion.reporteDireccionSet *)
                  var _listDireccion = new ListBuffer[ReporteDireccion]()
                  direcciones.map { d =>
                    var dat = SQL(
                      """SELECT * FROM siap.transformador_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
                    ).on(
                        'repo_id -> d.repo_id,
                        'aap_id -> d.aap_id,
                        'even_id -> d.even_id
                      )
                      .as(
                        ReporteDireccionDato.reporteDireccionDatoSet.singleOpt
                      )
                    dat match {
                      case None =>
                        dat = Some(
                          new ReporteDireccionDato(
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
                            None,
                            None,
                            None,
                            None,
                            None,
                            None,
                            None
                          )
                        )
                      case Some(dat) => None
                    }
                    var adi = SQL(
                      """SELECT * FROM siap.transformador_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
                    ).on(
                        'repo_id -> d.repo_id,
                        'aap_id -> d.aap_id,
                        'even_id -> d.even_id
                      )
                      .as(ReporteDireccionDatoAdicional._set.singleOpt)
                    adi match {
                      case None =>
                        adi = Some(
                          new ReporteDireccionDatoAdicional(
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
                            None
                          )
                        )
                      case Some(adi) => None
                    }
                    var fotos =
                      SQL("""
                  SELECT * FROM siap.transformador_reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                  """).on(
                          'repo_id -> r.repo_id,
                          'aap_id -> d.aap_id,
                          'tireuc_id -> r.tireuc_id
                        )
                        .as(ReporteDireccionFoto._set *)
                    var _listFoto = new ListBuffer[ReporteDireccionFoto]()
                    fotos.map { f =>
                      _listFoto += f
                    }
                    val direccion = d.copy(
                      dato = dat,
                      dato_adicional = adi,
                      fotos = Some(_listFoto.toList)
                    )
                    _listDireccion += direccion
                  }
                  val reporte = new Reporte(
                    r.repo_id,
                    Some(3),
                    r.reti_id,
                    r.repo_consecutivo,
                    r.repo_fecharecepcion,
                    r.repo_direccion,
                    r.repo_nombre,
                    r.repo_telefono,
                    r.repo_fechasolucion,
                    r.repo_horainicio,
                    r.repo_horafin,
                    r.repo_reportetecnico,
                    r.repo_descripcion,
                    r.repo_subrepoconsecutivo,
                    r.rees_id,
                    r.orig_id,
                    r.barr_id,
                    r.empr_id,
                    r.tiba_id,
                    r.usua_id,
                    adicional,
                    Some(meams),
                    Some(eventos),
                    Some(_listDireccion.toList),
                    Some(novedades)
                  )
                  Some(reporte)

                case None =>
                  val r = SQL(
                    """SELECT * FROM siap.medidor_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.medidor_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.rees_id in (1,2) AND (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE"""
                  ).on(
                      'reti_id -> reti_id,
                      'repo_consecutivo -> repo_consecutivo
                      //'empr_id -> empr_id
                    )
                    .as(Reporte._set.singleOpt)

                  r match {
                    case Some(r) =>
                      val eventos = SQL(
                        """SELECT * FROM siap.medidor_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
                      ).on(
                          'repo_id -> r.repo_id
                        )
                        .as(Evento.eventoSet *)
                      val meams = SQL(
                        """SELECT m.meam_id FROM siap.medidor_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
                      ).on(
                          'repo_id -> r.repo_id
                        )
                        .as(scalar[scala.Long].*)
                      val novedades = List[ReporteNovedad]()
                      val adicional = SQL(
                        """SELECT * FROM siap.medidor_reporte_adicional ra
                           LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                           LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                           WHERE ra.repo_id = {repo_id}
             			        ORDER BY ot.ortr_fecha DESC
				                  LIMIT 1 """
                      ).on(
                          'repo_id -> r.repo_id,
                          'tireuc_id -> r.tireuc_id
                        )
                        .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
                      val direcciones = SQL(
                        """SELECT * FROM siap.transformador_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
                      ).on(
                          'repo_id -> r.repo_id
                        )
                        .as(ReporteDireccion.reporteDireccionSet *)
                      var _listDireccion = new ListBuffer[ReporteDireccion]()
                      direcciones.map { d =>
                        var dat = SQL(
                          """SELECT * FROM siap.transformador_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
                        ).on(
                            'repo_id -> d.repo_id,
                            'aap_id -> d.aap_id,
                            'even_id -> d.even_id
                          )
                          .as(
                            ReporteDireccionDato.reporteDireccionDatoSet.singleOpt
                          )
                        dat match {
                          case None =>
                            dat = Some(
                              new ReporteDireccionDato(
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
                                None,
                                None,
                                None,
                                None,
                                None,
                                None,
                                None
                              )
                            )
                          case Some(dat) => None
                        }

                        var adi = SQL(
                          """SELECT * FROM siap.transformador_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
                        ).on(
                            'repo_id -> d.repo_id,
                            'aap_id -> d.aap_id,
                            'even_id -> d.even_id
                          )
                          .as(ReporteDireccionDatoAdicional._set.singleOpt)
                        adi match {
                          case None =>
                            adi = Some(
                              new ReporteDireccionDatoAdicional(
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
                                None
                              )
                            )
                          case Some(adi) => None
                        }
                        var fotos =
                          SQL("""
                          SELECT * FROM siap.transformador_reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                          """)
                            .on(
                              'repo_id -> r.repo_id,
                              'aap_id -> d.aap_id,
                              'tireuc_id -> r.tireuc_id
                            )
                            .as(ReporteDireccionFoto._set *)
                        var _listFoto = new ListBuffer[ReporteDireccionFoto]()
                        fotos.map { f =>
                          _listFoto += f
                        }
                        val direccion = d.copy(
                          dato = dat,
                          dato_adicional = adi,
                          fotos = Some(_listFoto.toList)
                        )
                        _listDireccion += direccion
                      }
                      val reporte = new Reporte(
                        r.repo_id,
                        Some(4),
                        r.reti_id,
                        r.repo_consecutivo,
                        r.repo_fecharecepcion,
                        r.repo_direccion,
                        r.repo_nombre,
                        r.repo_telefono,
                        r.repo_fechasolucion,
                        r.repo_horainicio,
                        r.repo_horafin,
                        r.repo_reportetecnico,
                        r.repo_descripcion,
                        r.repo_subrepoconsecutivo,
                        r.rees_id,
                        r.orig_id,
                        r.barr_id,
                        r.empr_id,
                        r.tiba_id,
                        r.usua_id,
                        adicional,
                        Some(meams),
                        Some(eventos),
                        Some(_listDireccion.toList),
                        Some(novedades)
                      )
                      Some(reporte)

                    case None => None
                  }
              }
          }
      }
    }
  }

  /**
    * Recuperar un Reporte dado su repo_consecutivo
    * @param reti_id: scala.Long
    * @param repo_consecutivo: scala.Long
    * @param empr_id: scala.Long
    */
  def buscarPorConsecutivoConsulta(
      reti_id: scala.Long,
      repo_consecutivo: scala.Long,
      empr_id: scala.Long
  ): Option[ReporteConsulta] = {
    db.withConnection { implicit connection =>
      val r = SQL(
        """SELECT * FROM siap.reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id <>9"""
      ).on(
          'reti_id -> reti_id,
          'repo_consecutivo -> repo_consecutivo,
          'empr_id -> empr_id
        )
        .as(Reporte._set.singleOpt)

      r match {
        case Some(r) =>
          val adicional = SQL(
            """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
          ).on(
              'repo_id -> r.repo_id,
              'tireuc_id -> r.tireuc_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val reporte = new ReporteConsulta(
            r.repo_id,
            r.reti_id,
            r.repo_consecutivo,
            r.repo_fecharecepcion,
            r.repo_direccion,
            r.repo_nombre,
            r.repo_telefono,
            r.repo_reportetecnico,
            r.repo_descripcion,
            r.rees_id,
            r.orig_id,
            r.barr_id,
            r.empr_id,
            r.tiba_id,
            r.usua_id,
            adicional
          )
          Some(reporte)
        case None => None
      }
    }
  }

  /**
    * Recuperar todos los Reporte dado su rango de fecha de recepcion
    * @param anho: Int
    * @param mes: Int
    * @param empr_id: scala.Long
    */
  def buscarPorRango(
      anho: Int,
      mes: Int,
      tireuc_id: Int,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
      var query: String =
        """SELECT *
                                          FROM siap.reporte r 
                                          LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                                          WHERE r.empr_id = {empr_id} and r.repo_fecharecepcion between {fecha_inicial} and {fecha_final}
                                          and tireuc_id = {tireuc_id} and r.rees_id < 9 ORDER BY r.rees_id, r.repo_fecharecepcion DESC """
      /*
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY r.rees_id ASC, r.repo_id DESC"
                    }
       */
      val fechaini = new DateTime(anho, mes, 1, 0, 0, 0, 0)
      val lastDay = fechaini.dayOfMonth().getMaximumValue()
      val fechafin = new DateTime(anho, mes, lastDay, 23, 59, 59, 999)
      val reps = SQL(query)
        .on(
          'empr_id -> empr_id,
          'fecha_inicial -> fechaini,
          'fecha_final -> fechafin,
          'tireuc_id -> tireuc_id
        )
        .as(Reporte._set *)
      // Bloquear esta línea para la carga inicial de busqueda
      /* reps.map { r =>
        val adicional = SQL(
          """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
        ).on(
            'repo_id -> r.repo_id,
            'tireuc_id -> r.tireuc_id
          )
          .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
        val eventos = SQL(
          """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} """
        ).on(
            'repo_id -> r.repo_id
          )
          .as(Evento.eventoSet *)
        val meams = SQL(
          """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(scalar[scala.Long].*)
        val novedades = SQL(
          """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
        ).on(
            'repo_id -> r.repo_id,
            'tireuc_id -> r.tireuc_id
          )
          .as(ReporteNovedad._set *)
        val direcciones = SQL(
          """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(ReporteDireccion.reporteDireccionSet *)
        var _listDireccion = new ListBuffer[ReporteDireccion]()
        direcciones.map { d =>
          var dat = SQL(
            """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
          dat match {
            case None =>
              dat = Some(
                new ReporteDireccionDato(
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
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                )
              )
            case Some(dat) => None
          }
          var adi = SQL(
            """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDatoAdicional._set.singleOpt)
          adi match {
            case None =>
              adi = Some(
                new ReporteDireccionDatoAdicional(
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
                  None
                )
              )
            case Some(adi) => None
          }
          var fotos =
            SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
              .on(
                'repo_id -> r.repo_id,
                'aap_id -> d.aap_id,
                'tireuc_id -> r.tireuc_id
              )
              .as(ReporteDireccionFoto._set *)
          var _listFoto = new ListBuffer[ReporteDireccionFoto]()
          fotos.map { f =>
            _listFoto += f
          }
          val direccion = d.copy(
            dato = dat,
            dato_adicional = adi,
            fotos = Some(_listFoto.toList)
          )
          _listDireccion += direccion
        }
        val reporte = new Reporte(
          r.repo_id,
          r.tireuc_id,
          r.reti_id,
          r.repo_consecutivo,
          r.repo_fecharecepcion,
          r.repo_direccion,
          r.repo_nombre,
          r.repo_telefono,
          r.repo_fechasolucion,
          r.repo_horainicio,
          r.repo_horafin,
          r.repo_reportetecnico,
          r.repo_descripcion,
          r.repo_subrepoconsecutivo,
          r.rees_id,
          r.orig_id,
          r.barr_id,
          r.empr_id,
          r.tiba_id,
          r.usua_id,
          adicional,
          None, //Some(meams),
          None, //Some(eventos),
          None, //Some(_listDireccion.toList)
          None
        )
        _list += reporte
      }*/
      // _list
      reps.toList
    }
  }

  /**
    * Recuperar todos los Reporte dado su rango de fecha de recepcion
    * @param anho: Int
    * @param mes: Int
    * @param empr_id: scala.Long
    */
  def buscarReportePorVarios(
      tireuc_id: Int,
      filtro: String,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
      var query: String =
        """SELECT *
              FROM siap.reporte r 
              LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
               WHERE r.empr_id = {empr_id} and tireuc_id = {tireuc_id} and r.rees_id < 9 
               and (repo_consecutivo::TEXT = {filtro1} or repo_nombre LIKE {filtro} or repo_telefono LIKE {filtro} or repo_direccion LIKE {filtro} or repo_descripcion LIKE {filtro})
               ORDER BY r.repo_fecharecepcion DESC """
      /*
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY r.rees_id ASC, r.repo_id DESC"
                    }
       */
      val numero = try {
        filtro.toInt
      } catch {
        case e: Exception => 0
      }
      val reps = SQL(query)
        .on(
          'empr_id -> empr_id,
          'tireuc_id -> tireuc_id,
          "filtro1" -> filtro,
          "filtro" -> (filtro + "%")
        )
        .as(Reporte._set *)
      // Bloquear esta línea para la carga inicial de busqueda
      /* reps.map { r =>
        val adicional = SQL(
          """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
        ).on(
            'repo_id -> r.repo_id,
            'tireuc_id -> r.tireuc_id
          )
          .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
        val eventos = SQL(
          """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} """
        ).on(
            'repo_id -> r.repo_id
          )
          .as(Evento.eventoSet *)
        val meams = SQL(
          """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(scalar[scala.Long].*)
        val novedades = SQL(
          """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
        ).on(
            'repo_id -> r.repo_id,
            'tireuc_id -> r.tireuc_id
          )
          .as(ReporteNovedad._set *)
        val direcciones = SQL(
          """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(ReporteDireccion.reporteDireccionSet *)
        var _listDireccion = new ListBuffer[ReporteDireccion]()
        direcciones.map { d =>
          var dat = SQL(
            """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
          dat match {
            case None =>
              dat = Some(
                new ReporteDireccionDato(
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
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                )
              )
            case Some(dat) => None
          }
          var adi = SQL(
            """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDatoAdicional._set.singleOpt)
          adi match {
            case None =>
              adi = Some(
                new ReporteDireccionDatoAdicional(
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
                  None
                )
              )
            case Some(adi) => None
          }
          var fotos =
            SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
              .on(
                'repo_id -> r.repo_id,
                'aap_id -> d.aap_id,
                'tireuc_id -> r.tireuc_id
              )
              .as(ReporteDireccionFoto._set *)
          var _listFoto = new ListBuffer[ReporteDireccionFoto]()
          fotos.map { f =>
            _listFoto += f
          }
          val direccion = d.copy(
            dato = dat,
            dato_adicional = adi,
            fotos = Some(_listFoto.toList)
          )
          _listDireccion += direccion
        }
        val reporte = new Reporte(
          r.repo_id,
          r.tireuc_id,
          r.reti_id,
          r.repo_consecutivo,
          r.repo_fecharecepcion,
          r.repo_direccion,
          r.repo_nombre,
          r.repo_telefono,
          r.repo_fechasolucion,
          r.repo_horainicio,
          r.repo_horafin,
          r.repo_reportetecnico,
          r.repo_descripcion,
          r.repo_subrepoconsecutivo,
          r.rees_id,
          r.orig_id,
          r.barr_id,
          r.empr_id,
          r.tiba_id,
          r.usua_id,
          adicional,
          None, //Some(meams),
          None, //Some(eventos),
          None, //Some(_listDireccion.toList)
          None
        )
        _list += reporte
      }*/
      // _list
      reps.toList
    }
  }

  /**
    * Recuperar todos los Reporte dado su rango de fecha de recepcion
    * @param anho: Int
    * @param mes: Int
    * @param empr_id: scala.Long
    */
  def buscarReportePorTipoConsectivo(
      tireuc_id: Int,
      reti_id: Int,
      repo_consecutivo: Int,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
      var query: String =
        """SELECT *
              FROM siap.reporte r 
              LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
               WHERE r.empr_id = {empr_id} and tireuc_id = {tireuc_id} and r.rees_id < 9 
               and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo}
               ORDER BY r.repo_fecharecepcion DESC """
      /*
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY r.rees_id ASC, r.repo_id DESC"
                    }
       */
      val reps = SQL(query)
        .on(
          'empr_id -> empr_id,
          'tireuc_id -> tireuc_id,
          "reti_id" -> reti_id,
          "repo_consecutivo" -> repo_consecutivo
        )
        .as(Reporte._set *)
      // Bloquear esta línea para la carga inicial de busqueda
      /* reps.map { r =>
        val adicional = SQL(
          """SELECT * FROM siap.reporte_adicional ra
                LEFT JOIN siap.ordentrabajo_reporte otr ON otr.repo_id = ra.repo_id and otr.tireuc_id = {tireuc_id}
                LEFT JOIN siap.ordentrabajo ot ON ot.ortr_id = otr.ortr_id
                WHERE ra.repo_id = {repo_id}
				        ORDER BY ot.ortr_fecha DESC
				        LIMIT 1 """
        ).on(
            'repo_id -> r.repo_id,
            'tireuc_id -> r.tireuc_id
          )
          .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
        val eventos = SQL(
          """SELECT * FROM siap.reporte_evento WHERE repo_id = {repo_id} """
        ).on(
            'repo_id -> r.repo_id
          )
          .as(Evento.eventoSet *)
        val meams = SQL(
          """SELECT m.meam_id FROM siap.reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(scalar[scala.Long].*)
        val novedades = SQL(
          """SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id} and rn.tireuc_id = {tireuc_id}"""
        ).on(
            'repo_id -> r.repo_id,
            'tireuc_id -> r.tireuc_id
          )
          .as(ReporteNovedad._set *)
        val direcciones = SQL(
          """SELECT * FROM siap.reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(ReporteDireccion.reporteDireccionSet *)
        var _listDireccion = new ListBuffer[ReporteDireccion]()
        direcciones.map { d =>
          var dat = SQL(
            """SELECT * FROM siap.reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
          dat match {
            case None =>
              dat = Some(
                new ReporteDireccionDato(
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
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                )
              )
            case Some(dat) => None
          }
          var adi = SQL(
            """SELECT * FROM siap.reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDatoAdicional._set.singleOpt)
          adi match {
            case None =>
              adi = Some(
                new ReporteDireccionDatoAdicional(
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
                  None
                )
              )
            case Some(adi) => None
          }
          var fotos =
            SQL("""
                SELECT * FROM siap.reporte_direccion_foto WHERE repo_id = {repo_id} and aap_id = {aap_id} and tireuc_id = {tireuc_id}
                """)
              .on(
                'repo_id -> r.repo_id,
                'aap_id -> d.aap_id,
                'tireuc_id -> r.tireuc_id
              )
              .as(ReporteDireccionFoto._set *)
          var _listFoto = new ListBuffer[ReporteDireccionFoto]()
          fotos.map { f =>
            _listFoto += f
          }
          val direccion = d.copy(
            dato = dat,
            dato_adicional = adi,
            fotos = Some(_listFoto.toList)
          )
          _listDireccion += direccion
        }
        val reporte = new Reporte(
          r.repo_id,
          r.tireuc_id,
          r.reti_id,
          r.repo_consecutivo,
          r.repo_fecharecepcion,
          r.repo_direccion,
          r.repo_nombre,
          r.repo_telefono,
          r.repo_fechasolucion,
          r.repo_horainicio,
          r.repo_horafin,
          r.repo_reportetecnico,
          r.repo_descripcion,
          r.repo_subrepoconsecutivo,
          r.rees_id,
          r.orig_id,
          r.barr_id,
          r.empr_id,
          r.tiba_id,
          r.usua_id,
          adicional,
          None, //Some(meams),
          None, //Some(eventos),
          None, //Some(_listDireccion.toList)
          None
        )
        _list += reporte
      }*/
      // _list
      reps.toList
    }
  }

  /**
    * Recuperar todos los Reporte dado su rango de fecha de solucion
    * @param fecha_inicial: DateTime
    * @param fecha_final: DateTime
    * @param empr_id: scala.Long
    */
  def buscarPorRangoFechaSolucion(
      fecha_inicial: DateTime,
      fecha_final: DateTime,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.reporte WHERE repo_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and empr_id = {empr_id}"
      ).on(
          'fecha_inicial -> fecha_inicial,
          'fecha_final -> fecha_final,
          'empr_id -> empr_id
        )
        .as(Reporte._set *)
    }
  }

  /**
    * Recuperar todos los Reporte dado su rango de fecha de solucion
    * @param fecha_inicial: DateTime
    * @param fecha_final: DateTime
    * @param empr_id: scala.Long
    */
  def buscarReportesModernizacionPorFechaSolucion(
      fecha_corte: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    val fecha_solucion = new DateTime(fecha_corte)
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.reporte WHERE repo_fechasolucion = {fecha_corte} and empr_id = {empr_id}"
      ).on(
          'fecha_corte -> fecha_solucion,
          'empr_id -> empr_id
        )
        .as(Reporte._set *)
    }
  }

  /**
    * Recuperar un Reporte dado su rees_id
    * @param rees_id: Int
    * @param empr_id: scala.Long
    */
  def buscarPorEstado(
      rees_id: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.reporte WHERE rees_id = {rees_id} and empr_id = {empr_id}"
      ).on(
          'rees_id -> rees_id,
          'empr_id -> empr_id
        )
        .as(Reporte._set *)
    }
  }

  /**
    * Recuperar un Reporte dado su acti_id
    * @param acti_id: scala.Long
    * @param empr_id: scala.Long
    */
  def buscarPorActividad(
      acti_id: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.reporte WHERE acti_id = {acti_id} and empr_id = {empr_id}"
      ).on(
          'acti_id -> acti_id,
          'empr_id -> empr_id
        )
        .as(Reporte._set *)
    }
  }

  /**
    * Recuperar un Reporte dado su orig_id
    * @param orig_id: scala.Long
    * @param empr_id: scala.Long
    */
  def buscarPorOrigen(
      orig_id: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.reporte WHERE orig_id = {orig_id} and empr_id = {empr_id}"
      ).on(
          'orig_id -> orig_id,
          'empr_id -> empr_id
        )
        .as(Reporte._set *)
    }
  }

  /**
    * Recuperar un Reporte dada una busqueda multiple
    * @param criteria: Map[String, Object]
    */
  /**
    * TODO: manejar el filtro avanzado
    */
  def buscarAvanzado(
      filtro: scala.collection.mutable.Map[String, Object]
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.reporte")
        .as(Reporte._set *)
    }
  }

  /**
    * Crear Reporte
    */
  def crear(reporte: Reporte): Future[(scala.Long, scala.Long)] =
    Future[(scala.Long, scala.Long)] {
      db.withConnection { implicit connection =>
        val fecha: LocalDate =
          new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime =
          new LocalDateTime(Calendar.getInstance().getTimeInMillis())
        val consec = consecutivo(reporte.reti_id.get)
        if (consec > 0) {
          val id: scala.Long = SQL(
            "INSERT INTO siap.reporte (tireuc_id, repo_fecharecepcion, repo_direccion, repo_nombre, repo_telefono, repo_fechasolucion, repo_horainicio, repo_horafin, repo_reportetecnico, repo_descripcion, rees_id, orig_id, barr_id, empr_id, tiba_id, usua_id, reti_id, repo_consecutivo) VALUES ({tireuc_id}, {repo_fecharecepcion}, {repo_direccion}, {repo_nombre}, {repo_telefono}, {repo_fechasolucion}, {repo_horainicio}, {repo_horafin}, {repo_reportetecnico}, {repo_descripcion}, {rees_id}, {orig_id}, {barr_id}, {empr_id}, {tiba_id}, {usua_id}, {reti_id}, {repo_consecutivo})"
          ).on(
              'tireuc_id -> reporte.tireuc_id,
              'repo_fecharecepcion -> reporte.repo_fecharecepcion,
              'repo_direccion -> reporte.repo_direccion,
              'repo_nombre -> reporte.repo_nombre,
              'repo_telefono -> reporte.repo_telefono,
              'repo_fechasolucion -> reporte.repo_fechasolucion,
              'repo_reportetecnico -> reporte.repo_reportetecnico,
              'orig_id -> reporte.orig_id,
              'barr_id -> reporte.barr_id,
              'usua_id -> reporte.usua_id,
              'empr_id -> reporte.empr_id,
              'rees_id -> reporte.rees_id,
              'repo_descripcion -> reporte.repo_descripcion,
              'repo_horainicio -> reporte.repo_horainicio,
              'repo_horafin -> reporte.repo_horafin,
              'reti_id -> reporte.reti_id,
              'repo_consecutivo -> consec,
              'tiba_id -> reporte.tiba_id
            )
            .executeInsert()
            .get

          reporte.adicional.map { adicional =>
            SQL("""INSERT INTO siap.reporte_adicional (repo_id, 
                                                               repo_fechadigitacion, 
                                                               repo_tipo_expansion, 
                                                               repo_luminaria, 
                                                               repo_redes, 
                                                               repo_poste, 
                                                               repo_modificado, 
                                                               repo_subreporte, 
                                                               repo_subid, 
                                                               repo_email,
                                                               acti_id,
                                                               repo_codigo,
                                                               repo_apoyo,
                                                               urba_id,
                                                               muot_id,
                                                               medi_id,
                                                               tran_id,
                                                               medi_acta,
                                                               aaco_id_anterior,
                                                               aaco_id_nuevo) VALUES (
                                                                {repo_id}, 
                                                                {repo_fechadigitacion}, 
                                                                {repo_tipo_expansion}, 
                                                                {repo_luminaria}, 
                                                                {repo_redes}, 
                                                                {repo_poste}, 
                                                                {repo_modificado}, 
                                                                {repo_subreporte}, 
                                                                {repo_subid}, 
                                                                {repo_email},
                                                                {acti_id},
                                                                {repo_codigo},
                                                                {repo_apoyo},
                                                                {urba_id},
                                                                {muot_id},
                                                                {medi_id},
                                                                {tran_id},
                                                                {medi_acta},
                                                                {aaco_id_anterior},
                                                                {aaco_id_nuevo}
                                                               )""")
              .on(
                'repo_fechadigitacion -> adicional.repo_fechadigitacion,
                'repo_tipo_expansion -> adicional.repo_tipo_expansion,
                'repo_luminaria -> adicional.repo_luminaria,
                'repo_redes -> adicional.repo_redes,
                'repo_poste -> adicional.repo_poste,
                'repo_modificado -> hora,
                'repo_subreporte -> adicional.repo_subreporte,
                'repo_subid -> adicional.repo_subid,
                'repo_email -> adicional.repo_email,
                'acti_id -> adicional.acti_id,
                'repo_codigo -> adicional.repo_codigo,
                'repo_apoyo -> adicional.repo_apoyo,
                'urba_id -> adicional.urba_id,
                'muot_id -> adicional.muot_id,
                'medi_id -> adicional.medi_id,
                'tran_id -> adicional.tran_id,
                'medi_acta -> adicional.medi_acta,
                'aaco_id_anterior -> adicional.aaco_id_anterior,
                'aaco_id_nuevo -> adicional.aaco_id_nuevo,
                'repo_id -> id
              )
              .executeInsert()
          }

          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> id,
              'audi_campo -> "repo_id",
              'audi_valorantiguo -> "",
              'audi_valornuevo -> id,
              'audi_evento -> "I"
            )
            .executeInsert()

          (id, consec)
        } else {
          (0, 0)
        }
      }
    }

  /**
    * Crear Reporte
    */
  def crearFromMovil(
      reporte: Reporte,
      uuid: String,
      ip_address: String
  ): Future[(scala.Long, scala.Long)] =
    Future[(scala.Long, scala.Long)] {
      db.withTransaction { implicit connection =>
        // verificar que no exista el reporte
        val _existe = SQL(
          """SELECT COUNT(*) FROM siap.reporte_desde_movil WHERE repo_idx = {repo_idx}::uuid"""
        ).on(
            'repo_idx -> uuid
          )
          .as(scalar[scala.Long].single) > 0
        if (!_existe) {
          val fecha: LocalDate =
            new LocalDate(Calendar.getInstance().getTimeInMillis())
          val hora: LocalDateTime =
            new LocalDateTime(Calendar.getInstance().getTimeInMillis())
          val consec = consecutivo(reporte.reti_id.get)
          if (consec > 0) {
            val id: scala.Long = SQL(
              "INSERT INTO siap.reporte (tireuc_id, repo_fecharecepcion, repo_direccion, repo_nombre, repo_telefono, repo_fechasolucion, repo_horainicio, repo_horafin, repo_reportetecnico, repo_descripcion, rees_id, orig_id, barr_id, empr_id, tiba_id, usua_id, reti_id, repo_consecutivo) VALUES ({tireuc_id}, {repo_fecharecepcion}, {repo_direccion}, {repo_nombre}, {repo_telefono}, {repo_fechasolucion}, {repo_horainicio}, {repo_horafin}, {repo_reportetecnico}, {repo_descripcion}, {rees_id}, {orig_id}, {barr_id}, {empr_id}, {tiba_id}, {usua_id}, {reti_id}, {repo_consecutivo})"
            ).on(
                'tireuc_id -> reporte.tireuc_id,
                'repo_fecharecepcion -> reporte.repo_fecharecepcion,
                'repo_direccion -> reporte.repo_direccion,
                'repo_nombre -> reporte.repo_nombre,
                'repo_telefono -> reporte.repo_telefono,
                'repo_fechasolucion -> reporte.repo_fechasolucion,
                'repo_reportetecnico -> reporte.repo_reportetecnico,
                'orig_id -> reporte.orig_id,
                'barr_id -> reporte.barr_id,
                'usua_id -> reporte.usua_id,
                'empr_id -> reporte.empr_id,
                'rees_id -> reporte.rees_id,
                'repo_descripcion -> reporte.repo_descripcion,
                'repo_horainicio -> reporte.repo_horainicio,
                'repo_horafin -> reporte.repo_horafin,
                'reti_id -> reporte.reti_id,
                'repo_consecutivo -> consec,
                'tiba_id -> reporte.tiba_id
              )
              .executeInsert()
              .get

            reporte.adicional.map { adicional =>
              SQL("""INSERT INTO siap.reporte_adicional (repo_id, 
                                                               repo_fechadigitacion, 
                                                               repo_tipo_expansion, 
                                                               repo_luminaria, 
                                                               repo_redes, 
                                                               repo_poste, 
                                                               repo_modificado, 
                                                               repo_subreporte, 
                                                               repo_subid, 
                                                               repo_email,
                                                               acti_id,
                                                               repo_codigo,
                                                               repo_apoyo,
                                                               urba_id,
                                                               muot_id,
                                                               medi_id,
                                                               tran_id,
                                                               medi_acta,
                                                               aaco_id_anterior,
                                                               aaco_id_nuevo) VALUES (
                                                                {repo_id}, 
                                                                {repo_fechadigitacion}, 
                                                                {repo_tipo_expansion}, 
                                                                {repo_luminaria}, 
                                                                {repo_redes}, 
                                                                {repo_poste}, 
                                                                {repo_modificado}, 
                                                                {repo_subreporte}, 
                                                                {repo_subid}, 
                                                                {repo_email},
                                                                {acti_id},
                                                                {repo_codigo},
                                                                {repo_apoyo},
                                                                {urba_id},
                                                                {muot_id},
                                                                {medi_id},
                                                                {tran_id},
                                                                {medi_acta},
                                                                {aaco_id_anterior},
                                                                {aaco_id_nuevo}
                                                               )""")
                .on(
                  'repo_fechadigitacion -> adicional.repo_fechadigitacion,
                  'repo_tipo_expansion -> adicional.repo_tipo_expansion,
                  'repo_luminaria -> adicional.repo_luminaria,
                  'repo_redes -> adicional.repo_redes,
                  'repo_poste -> adicional.repo_poste,
                  'repo_modificado -> hora,
                  'repo_subreporte -> adicional.repo_subreporte,
                  'repo_subid -> adicional.repo_subid,
                  'repo_email -> adicional.repo_email,
                  'acti_id -> adicional.acti_id,
                  'repo_codigo -> adicional.repo_codigo,
                  'repo_apoyo -> adicional.repo_apoyo,
                  'urba_id -> adicional.urba_id,
                  'muot_id -> adicional.muot_id,
                  'medi_id -> adicional.medi_id,
                  'tran_id -> adicional.tran_id,
                  'medi_acta -> adicional.medi_acta,
                  'aaco_id_anterior -> adicional.aaco_id_anterior,
                  'aaco_id_nuevo -> adicional.aaco_id_nuevo,
                  'repo_id -> id
                )
                .executeInsert()
            }

            // Adicionar a la orden de trabajo del día
            // Buscar Cuadrilla usando el usuario
            val _cuad_id = SQL(
              """SELECT c1.cuad_id FROM siap.cuadrilla c1
            INNER JOIN siap.cuadrilla_usuario cu1 ON cu1.cuad_id = c1.cuad_id
            INNER JOIN siap.usuario u1 ON u1.usua_id = cu1.usua_id
            WHERE u1.usua_id = {usua_id}"""
            ).on(
                "usua_id" -> reporte.usua_id
              )
              .as(SqlParser.scalar[scala.Long].singleOpt)
            println("Insertando reporte desde movil")
            SQL("""INSERT INTO siap.reporte_desde_movil VALUES (
              	{repo_idx}::uuid,
	              {tireuc_id},
	              {repo_id},
	              {repo_consecutivo},
	              {repo_fecha},
	              {cuad_id},
	              {repo_id_address},
	              {repo_registrado_en},
	              {usua_id},
	              {empr_id}
            )""")
              .on(
                'repo_idx -> uuid,
                'tireuc_id -> reporte.tireuc_id,
                'repo_id -> id,
                'repo_consecutivo -> consec,
                'repo_fecha -> reporte.repo_fecharecepcion,
                'cuad_id -> _cuad_id,
                'repo_id_address -> ip_address,
                'repo_registrado_en -> new DateTime(),
                'usua_id -> reporte.usua_id,
                'empr_id -> reporte.empr_id
              )
              .executeUpdate()

            _cuad_id match {
              case Some(cuad_id) =>
                // Buscar Orden de Trabajo
                var ortr_id = SQL(
                  """SELECT ortr_id FROM siap.ordentrabajo ot1 WHERE ot1.cuad_id = {cuad_id} and ot1.ortr_fecha = {fecha}"""
                ).on(
                    "cuad_id" -> cuad_id,
                    "fecha" -> fecha
                  )
                  .as(SqlParser.int("ortr_id").singleOpt)

                val cantidad = SQL(
                  """SELECT COUNT(*) FROM siap.ordentrabajo_reporte WHERE ortr_id = {ortr_id}"""
                ).on(
                    "ortr_id" -> ortr_id
                  )
                  .as(SqlParser.scalar[scala.Long].single)
                SQL(
                  """INSERT INTO siap.ordentrabajo_reporte (ortr_id, repo_id, even_id, even_estado, tireuc_id) VALUES ({ortr_id}, {repo_id}, {even_id}, {even_estado}, {tireuc_id})"""
                ).on(
                    'ortr_id -> ortr_id,
                    'repo_id -> id,
                    'even_id -> (cantidad + 1),
                    'even_estado -> 1,
                    'tireuc_id -> 1
                  )
                  .executeInsert()
              case None => None
            }
            // Adicionar auditoría
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> id,
                'audi_campo -> "repo_id",
                'audi_valorantiguo -> "",
                'audi_valornuevo -> id,
                'audi_evento -> "I"
              )
              .executeInsert()

            (id, consec)
          } else {
            (0, 0)
          }
        } else { 
          (0, 0) 
        }
      }
    }

    /*
    *
    *  Consultar un uuid desde movil para verificar si ya exite
    *  */
    def consultarReporteMovil(uuid: String) = {
      val resultData = db.withConnection { implicit connection =>
        val _parser = get[Option[Int]]("repo_id") ~ get[Option[Int]]("repo_consecutivo") map {
          case a ~ b => (a,b)
        }
        println("Consultando Resporte desde Movil")
        SQL("""SELECT repo_id, repo_consecutivo FROM siap.reporte_desde_movil WHERE repo_idx = {repo_idx}::uuid""")
        .on(
          'repo_idx -> uuid
        ).as(_parser.singleOpt)
      }
      println("result: " + resultData)
      resultData
    }

  /**
    * convertir
    *
    * @param reporte
    * @return Boolean
    */
  def convertir(id: scala.Long): scala.Long = {
    db.withTransaction { implicit connection =>
      var _id: scala.Long = 0
      val reporte = SQL("SELECT * FROM siap.reporte WHERE repo_id = {repo_id}")
        .on('repo_id -> id)
        .as(Reporte._set.single)
      // Validar si previamente fue convertido
      val control = SQL(
        "SELECT * FROM siap.control_reporte WHERE repo_consecutivo = {repo_consecutivo} and reti_id = {reti_id}"
      ).on(
          'repo_consecutivo -> reporte.repo_consecutivo,
          'reti_id -> reporte.reti_id
        )
        .as(Reporte._set.singleOpt)
      control match {
        case None =>
          val queryReporte =
            """INSERT INTO siap.control_reporte (
                      tireuc_id,
                      repo_fecharecepcion, 
                      repo_direccion, 
                      repo_nombre, 
                      repo_telefono, 
                      repo_fechasolucion, 
                      repo_reportetecnico, 
                      orig_id, 
                      barr_id, 
                      usua_id, 
                      empr_id, 
                      rees_id, 
                      repo_descripcion, 
                      repo_horainicio, 
                      repo_horafin, 
                      reti_id, 
                      repo_consecutivo, 
                      repo_subrepoconsecutivo,
                      tiba_id, 
                      barr_id_anterior) 
                      SELECT 
                      2,
                      repo_fecharecepcion, 
                      repo_direccion, 
                      repo_nombre, 
                      repo_telefono, 
                      repo_fechasolucion, 
                      repo_reportetecnico, 
                      orig_id, 
                      barr_id, 
                      usua_id, 
                      empr_id, 
                      rees_id, 
                      repo_descripcion, 
                      repo_horainicio, 
                      repo_horafin, 
                      reti_id, 
                      repo_consecutivo, 
                      repo_subrepoconsecutivo,
                      tiba_id, 
                      barr_id_anterior
                      FROM siap.reporte r 
                      WHERE r.repo_consecutivo = {repo_consecutivo} and r.reti_id = {reti_id}"""
          val queryAdicional =
            """INSERT INTO siap.control_reporte_adicional SELECT {id},
                               ra.repo_fechadigitacion,
                               ra.repo_tipo_expansion,
                               ra.repo_luminaria,
                               ra.repo_redes,
                               ra.repo_poste,
                               ra.repo_modificado,
                               ra.repo_subreporte,
                               ra.acti_id,
                               ra.repo_subid,
                               ra.repo_email,
                               ra.repo_codigo,
                               ra.repo_apoyo,
                               ra.urba_id,
                               ra.muot_id,
                               ra.aaco_id_anterior,
                               ra.aaco_id_nuevo,
                               ra.medi_id,
                               ra.tran_id,
                               ra.medi_acta
                              FROM siap.reporte_adicional ra 
                              WHERE ra.repo_id = {repo_id}
                              """
          _id = SQL(queryReporte)
            .on(
              'repo_consecutivo -> reporte.repo_consecutivo,
              'reti_id -> reporte.reti_id
            )
            .executeInsert()
            .get

          if (_id > 0) {
            SQL(queryAdicional)
              .on('repo_id -> reporte.repo_id, 'id -> _id)
              .executeUpdate()
          }

        case Some(c) =>
          _id = c.repo_id.get
          SQL(
            """UPDATE siap.control_reporte SET rees_id = 1 WHERE repo_id = {repo_id}"""
          ).on(
              'repo_id -> c.repo_id
            )
            .executeUpdate()
      }
      // Eliminar de Reporte y Reporte Adicional
      SQL("""UPDATE siap.reporte SET rees_id = 9 WHERE repo_id = {repo_id}""")
        .on(
          'repo_id -> reporte.repo_id
        )
        .executeUpdate()
      _id
    }
  }

  /**
    * Actualizar Parcial Reporte
    */
  def actualizarParcial(reporte: Reporte): Future[Boolean] =
    Future {

      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      var upd_repo = db.withConnection { implicit connection =>
        var upd1 = SQL("""UPDATE siap.reporte SET  
              repo_direccion = {repo_direccion}, 
              repo_nombre = {repo_nombre}, 
              repo_telefono = {repo_telefono},
              repo_descripcion = {repo_descripcion}, 
              rees_id = {rees_id}, 
              orig_id = {orig_id}, 
              barr_id = {barr_id}, 
              empr_id = {empr_id},
              usua_id = {usua_id} 
              WHERE repo_id = {repo_id}""")
          .on(
            'repo_direccion -> reporte.repo_direccion,
            'repo_nombre -> reporte.repo_nombre,
            'repo_telefono -> reporte.repo_telefono,
            'orig_id -> reporte.orig_id,
            'barr_id -> reporte.barr_id,
            'usua_id -> reporte.usua_id,
            'empr_id -> reporte.empr_id,
            'rees_id -> reporte.rees_id,
            'repo_descripcion -> reporte.repo_descripcion,
            'repo_id -> reporte.repo_id
          )
          .executeUpdate() > 0
        var upd2 = reporte.adicional match {
          case Some(adicional) =>
            SQL("""UPDATE siap.reporte_adicional SET
                    repo_tipo_expansion = {repo_tipo_expansion}, 
                    repo_luminaria = {repo_luminaria},
                    repo_redes = {repo_redes},
                    repo_poste = {repo_poste},
                    repo_email = {repo_email},
                    acti_id = {acti_id},
                    repo_codigo = {repo_codigo},
                    repo_apoyo = {repo_apoyo},
                    urba_id = {urba_id},
                    muot_id = {muot_id},
                    medi_id = {medi_id},
                    tran_id = {tran_id},
                    medi_acta = {medi_acta},
                    aaco_id_anterior = {aaco_id_anterior},
                    aaco_id_nuevo = {aaco_id_nuevo}
                  WHERE repo_id = {repo_id}""")
              .on(
                'repo_tipo_expansion -> adicional.repo_tipo_expansion,
                'repo_luminaria -> adicional.repo_luminaria,
                'repo_redes -> adicional.repo_redes,
                'repo_poste -> adicional.repo_poste,
                'repo_email -> adicional.repo_email,
                'acti_id -> adicional.acti_id,
                'repo_codigo -> adicional.repo_codigo,
                'repo_apoyo -> adicional.repo_apoyo,
                'urba_id -> adicional.urba_id,
                'muot_id -> adicional.muot_id,
                'medi_id -> adicional.medi_id,
                'tran_id -> adicional.tran_id,
                'medi_acta -> adicional.medi_acta,
                'aaco_id_anterior -> adicional.aaco_id_anterior,
                'aaco_id_nuevo -> adicional.aaco_id_nuevo,
                'repo_id -> adicional.repo_id
              )
              .executeUpdate() > 0

          case None => true
        }
        var ins = SQL(
          "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
        ).on(
            'audi_fecha -> fecha,
            'audi_hora -> hora,
            'usua_id -> reporte.usua_id,
            'audi_tabla -> "reporte",
            'audi_uid -> reporte.repo_id,
            'audi_campo -> "repo_id",
            'audi_valorantiguo -> "",
            'audi_valornuevo -> reporte.repo_id,
            'audi_evento -> "A"
          )
          .executeInsert()

        upd1 && upd2
      }
      upd_repo

    }

  /**
    * Actualizar Reporte
    * @param reporte: Reporte
    */
  def actualizar(
      reporte: Reporte,
      coau_tipo: Int,
      coau_codigo: String
  ): Boolean = {
    val reporte_ant: Option[Reporte] = buscarPorId(reporte.repo_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      var historia =
        scala.collection.mutable.Map[scala.Long, ElementoHistoria]()
      val result: Boolean = SQL(
        "UPDATE siap.reporte SET repo_direccion = {repo_direccion}, repo_nombre = {repo_nombre}, repo_telefono = {repo_telefono}, repo_fechasolucion = {repo_fechasolucion}, repo_horainicio = {repo_horainicio}, repo_horafin = {repo_horafin}, repo_reportetecnico = {repo_reportetecnico}, repo_descripcion = {repo_descripcion}, repo_subrepoconsecutivo = {repo_subrepoconsecutivo}, rees_id = {rees_id}, orig_id = {orig_id}, barr_id = {barr_id}, empr_id = {empr_id}, tiba_id = {tiba_id}, usua_id = {usua_id} WHERE repo_id = {repo_id}"
      ).on(
          'repo_id -> reporte.repo_id,
          'repo_direccion -> reporte.repo_direccion,
          'repo_nombre -> reporte.repo_nombre,
          'repo_telefono -> reporte.repo_telefono,
          'repo_fechasolucion -> reporte.repo_fechasolucion,
          'repo_horainicio -> reporte.repo_horainicio,
          'repo_horafin -> reporte.repo_horafin,
          'repo_reportetecnico -> reporte.repo_reportetecnico,
          'repo_descripcion -> reporte.repo_descripcion,
          'repo_subrepoconsecutivo -> reporte.repo_subrepoconsecutivo,
          'rees_id -> 3,
          'orig_id -> reporte.orig_id,
          'barr_id -> reporte.barr_id,
          'empr_id -> reporte.empr_id,
          'tiba_id -> reporte.tiba_id,
          'usua_id -> reporte.usua_id
        )
        .executeUpdate() > 0

      // actualizar reporte adicional
      reporte.adicional.map { adicional =>
        val hayAdicional: Boolean = SQL(
          """UPDATE siap.reporte_adicional SET repo_fechadigitacion = {repo_fechadigitacion}, 
                    repo_tipo_expansion = {repo_tipo_expansion}, repo_luminaria = {repo_luminaria}, 
                    repo_redes = {repo_redes}, repo_poste = {repo_poste}, repo_modificado = {repo_modificado}, 
                    repo_subreporte = {repo_subreporte}, repo_subid = {repo_subid}, repo_email = {repo_email}, 
                    acti_id = {acti_id}, repo_codigo = {repo_codigo}, repo_apoyo = {repo_apoyo}, 
                    urba_id = {urba_id}, muot_id = {muot_id}, medi_id = {medi_id}, tran_id = {tran_id}, 
                    medi_acta = {medi_acta}, aaco_id_anterior = {aaco_id_anterior}, aaco_id_nuevo = {aaco_id_nuevo} WHERE repo_id = {repo_id}"""
        ).on(
            'repo_fechadigitacion -> adicional.repo_fechadigitacion,
            'repo_tipo_expansion -> adicional.repo_tipo_expansion,
            'repo_luminaria -> adicional.repo_luminaria,
            'repo_redes -> adicional.repo_redes,
            'repo_poste -> adicional.repo_poste,
            'repo_modificado -> hora,
            'repo_subreporte -> adicional.repo_subreporte,
            'repo_subid -> adicional.repo_subid,
            'repo_email -> adicional.repo_email,
            'acti_id -> adicional.acti_id,
            'repo_codigo -> adicional.repo_codigo,
            'repo_apoyo -> adicional.repo_apoyo,
            'urba_id -> adicional.urba_id,
            'muot_id -> adicional.muot_id,
            'medi_id -> adicional.medi_id,
            'tran_id -> adicional.tran_id,
            'medi_acta -> adicional.medi_acta,
            'aaco_id_anterior -> adicional.aaco_id_anterior,
            'aaco_id_nuevo -> adicional.aaco_id_nuevo,
            'repo_id -> reporte.repo_id
          )
          .executeUpdate() > 0
        if (!hayAdicional) {
          SQL("""INSERT INTO siap.reporte_adicional (repo_id, 
                                                               repo_fechadigitacion, 
                                                               repo_tipo_expansion, 
                                                               repo_luminaria, 
                                                               repo_redes, 
                                                               repo_poste, 
                                                               repo_modificado, 
                                                               repo_subreporte, 
                                                               repo_email,
                                                               repo_subid, 
                                                               acti_id,
                                                               repo_codigo,
                                                               repo_apoyo,
                                                               urba_id,
                                                               muot_id) VALUES (
                                                                {repo_id}, 
                                                                {repo_fechadigitacion}, 
                                                                {repo_tipo_expansion}, 
                                                                {repo_luminaria}, 
                                                                {repo_redes}, 
                                                                {repo_poste}, 
                                                                {repo_modificado}, 
                                                                {repo_subreporte}, 
                                                                {repo_subid}, 
                                                                {repo_email},
                                                                {acti_id},
                                                                {repo_codigo},
                                                                {repo_apoyo},
                                                                {urba_id},
                                                                {muot_id}
                                                               )""")
            .on(
              'repo_fechadigitacion -> adicional.repo_fechadigitacion,
              'repo_tipo_expansion -> adicional.repo_tipo_expansion,
              'repo_luminaria -> adicional.repo_luminaria,
              'repo_redes -> adicional.repo_redes,
              'repo_poste -> adicional.repo_poste,
              'repo_modificado -> hora,
              'repo_subreporte -> adicional.repo_subreporte,
              'repo_subid -> adicional.repo_subid,
              'repo_email -> adicional.repo_email,
              'acti_id -> adicional.acti_id,
              'repo_codigo -> adicional.repo_codigo,
              'repo_apoyo -> adicional.repo_apoyo,
              'urba_id -> adicional.urba_id,
              'muot_id -> adicional.muot_id,
              'repo_id -> reporte.repo_id
            )
            .executeInsert()
        }
      }
      // Creación Actualizacion de Novedades
      reporte.novedades.map { novedades =>
        for (n <- novedades) {
          val novedadActualizado = SQL(
            """UPDATE siap.reporte_novedad SET 
                                           nove_id = {nove_id}, 
                                           reno_horaini = {reno_horaini}, 
                                           reno_horafin = {reno_horafin}, 
                                           reno_observacion = {reno_observacion},
                                           even_estado = {even_estado}
                                          WHERE tireuc_id = {tireuc_id} AND repo_id = {repo_id} AND even_id = {even_id}"""
          ).on(
              'tireuc_id -> reporte.tireuc_id,
              'repo_id -> reporte.repo_id,
              'even_id -> n.even_id,
              'nove_id -> n.nove_id,
              'reno_horaini -> n.reno_horaini,
              'reno_horafin -> n.reno_horafin,
              'reno_observacion -> n.reno_observacion,
              'even_estado -> n.even_estado
            )
            .executeUpdate() > 0
          if (!novedadActualizado) {
            if (n.even_estado.get < 8) {
              val novedadInsertado = SQL(""" INSERT INTO siap.reporte_novedad (
                                             tireuc_id, 
                                             repo_id, 
                                             even_id, 
                                             nove_id, 
                                             reno_horaini, 
                                             reno_horafin, 
                                             reno_observacion, 
                                             even_estado
                                           ) VALUES (
                                             {tireuc_id},
                                             {repo_id},
                                             {even_id},
                                             {nove_id},
                                             {reno_horaini},
                                             {reno_horafin},
                                             {reno_observacion},
                                             {even_estado}
                                           )
                                    """)
                .on(
                  'tireuc_id -> reporte.tireuc_id,
                  'repo_id -> reporte.repo_id,
                  'even_id -> n.even_id,
                  'nove_id -> n.nove_id,
                  'reno_horaini -> n.reno_horaini,
                  'reno_horafin -> n.reno_horafin,
                  'reno_observacion -> n.reno_observacion,
                  'even_estado -> n.even_estado
                )
                .executeUpdate() > 0
            }
          }
        }
      }

      // Proceso de Creación de Luminarias Nuevas por Expansión Tipo III
      if (reporte.reti_id.get == 2 && reporte.adicional.get.repo_tipo_expansion.get != 4) {
        reporte.direcciones.map { direcciones =>
          for (d <- direcciones) {
            if (d.aap_id != None) {
              var aap_elemento: AapElemento = new AapElemento(
                d.aap_id,
                None,
                None,
                None,
                None,
                None,
                None,
                reporte.reti_id,
                reporte.repo_consecutivo.map(_.toInt)
              )
              var aap: Aap = new Aap(
                d.aap_id,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                reporte.empr_id.get,
                reporte.repo_fechasolucion,
                reporte.repo_fechasolucion,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                None,
                reporte.usua_id.get,
                Some(aap_elemento),
                None
              )
              var aap_adicional: AapAdicional = new AapAdicional(
                d.aap_id,
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
                None
              )
              val aapOption =
                aapService.buscarPorId(d.aap_id.get, reporte.empr_id.get)
              var activo = new Activo(
                Some(aap),
                None,
                None,
                Some(aap_adicional),
                Some(aap_elemento),
                None,
                Some(1)
              )
              aapOption match {
                case None =>
                  if (d.even_estado.get < 9) {
                    aapService.creardirecto(
                      activo,
                      reporte.empr_id.get,
                      reporte.usua_id.get
                    )
                  }
                case (a) => aap = a.get
              }

              // Fin Proceso de Creación de Luminarias Nuevas por Expansión Tipo I,II,III,V
            }
          }
        }
      }

      SQL("""DELETE FROM siap.reporte_evento WHERE repo_id = {repo_id}""")
        .on('repo_id -> reporte.repo_id)
        .executeUpdate()

      reporte.eventos.map { eventos =>
        for (e <- eventos) {
          if (e.aap_id != None) {
            var elemento: Elemento = null
            var bombillo_retirado = None: Option[String]
            var bombillo_instalado = None: Option[String]
            var balasto_retirado = None: Option[String]
            var balasto_instalado = None: Option[String]
            var arrancador_retirado = None: Option[String]
            var arrancador_instalado = None: Option[String]
            var condensador_retirado = None: Option[String]
            var condensador_instalado = None: Option[String]
            var fotocelda_retirado = None: Option[String]
            var fotocelda_instalado = None: Option[String]

            e.elem_id match {
              case None => None
              case Some(elem_id) =>
                elemento = elementoService.buscarPorId(elem_id).get
            }
            // Actualizar Evento si ya Existe
            var estado = 0
            e.even_estado match {
              case Some(1) => estado = 2
              case Some(2) => estado = 2
              case Some(8) => estado = 9
              case Some(9) => estado = 9
              case _       => estado = 2
            }
            var eventoActualizado: Boolean = false
            var eventoInsertado: Boolean = false
            /*println("empr_id: " + reporte.empr_id)
            println("repo_id: " + reporte.repo_id)
            println("aap_id: " + e.aap_id)
            println("even_id: " + e.even_id)*/
            eventoActualizado = SQL(
              """UPDATE siap.reporte_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                elem_id = {elem_id},
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id},
                                                                unit_id = {unit_id}
                                                            WHERE empr_id = {empr_id} and repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}
                                                        """
            ).on(
                'even_fecha -> hora,
                'elem_id -> e.elem_id,
                'even_codigo_retirado -> e.even_codigo_retirado,
                'even_cantidad_retirado -> e.even_cantidad_retirado,
                'even_codigo_instalado -> e.even_codigo_instalado,
                'even_cantidad_instalado -> e.even_cantidad_instalado,
                'usua_id -> reporte.usua_id,
                'even_estado -> estado,
                'empr_id -> reporte.empr_id,
                'repo_id -> reporte.repo_id,
                'aap_id -> e.aap_id,
                'even_id -> e.even_id,
                'unit_id -> e.unit_id
              )
              .executeUpdate() > 0
            if (!eventoActualizado) {
              eventoInsertado = SQL(
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
                                    even_id,
                                    unit_id) VALUES (
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
                                    {even_id},
                                    {unit_id})"""
              ).on(
                  "even_fecha" -> hora,
                  "even_codigo_instalado" -> e.even_codigo_instalado,
                  "even_cantidad_instalado" -> e.even_cantidad_instalado,
                  "even_codigo_retirado" -> e.even_codigo_retirado,
                  "even_cantidad_retirado" -> e.even_cantidad_retirado,
                  "even_estado" -> estado,
                  "aap_id" -> e.aap_id,
                  "repo_id" -> reporte.repo_id,
                  "elem_id" -> e.elem_id,
                  "usua_id" -> e.usua_id,
                  "empr_id" -> reporte.empr_id,
                  "even_id" -> e.even_id,
                  "unit_id" -> e.unit_id
                )
                .executeUpdate() > 0
            }
            if ((eventoActualizado || eventoInsertado) && (estado != 9)) {
              // validar elemento y actualizar aap_elemento
              elemento.tiel_id match {
                case Some(1) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo}, reti_id = {reti_id} , repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo_retirado -> e.even_codigo_retirado,
                      'aap_bombillo_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> e.even_codigo_retirado,
                        'aap_bombillo_instalado -> e.even_codigo_instalado,
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(2) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto_retirado -> e.even_codigo_retirado,
                      'aap_balasto_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> e.even_codigo_retirado,
                        'aap_balasto_instalado -> e.even_codigo_instalado,
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(3) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador_retirado -> e.even_codigo_retirado,
                      'aap_arrancador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> e.even_codigo_retirado,
                        'aap_arrancador_instalado -> e.even_codigo_instalado,
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(4) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador_retirado -> e.even_codigo_retirado,
                      'aap_condensador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> e.even_codigo_retirado,
                        'aap_condensador_instalado -> e.even_codigo_instalado,
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(5) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda_retirado -> e.even_codigo_retirado,
                      'aap_fotocelda_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> e.even_codigo_retirado,
                        'aap_fotocelda_instalado -> e.even_codigo_instalado,
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case _ => None
              }
            }
          }
        }
      }

      reporte.direcciones.map { direcciones =>
        for (d <- direcciones) {
          if (d.aap_id != None) {
            var dirActualizado: Boolean = false
            var dirInsertado: Boolean = false
            var datoActualizado: Boolean = false
            var datoInsertado: Boolean = false
            var datoadicionalInsertado: Boolean = false
            var datoadicionalActualizado: Boolean = false
            val aap =
              aapService.buscarPorId(d.aap_id.get, reporte.empr_id.get).get

            var estado = 0
            d.even_estado match {
              case Some(1) => estado = 2
              case Some(2) => estado = 2
              case Some(8) => estado = 9
              case Some(9) => estado = 9
              case _       => estado = 2
            }
            // Direccion
            dirActualizado = SQL(
              """UPDATE siap.reporte_direccion SET
                                                even_direccion = {even_direccion},
                                                barr_id = {barr_id},
                                                even_direccion_anterior = {even_direccion_anterior},
                                                barr_id_anterior = {barr_id_anterior},
                                                even_estado = {even_estado},
                                                tire_id = {tire_id},
                                                even_horaini = {even_horaini},
                                                even_horafin = {even_horafin}
                                            WHERE
                                                repo_id = {repo_id} and
                                                aap_id = {aap_id} and
                                                even_id = {even_id} """
            ).on(
                'even_direccion -> d.even_direccion,
                'barr_id -> d.barr_id,
                'even_direccion_anterior -> d.even_direccion_anterior,
                'barr_id_anterior -> d.barr_id_anterior,
                'even_estado -> estado,
                'tire_id -> d.tire_id,
                'repo_id -> reporte.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id,
                'even_horaini -> d.even_horaini,
                'even_horafin -> d.even_horafin
              )
              .executeUpdate() > 0

            if (!dirActualizado) {
              dirInsertado = SQL(
                """INSERT INTO siap.reporte_direccion (repo_id, aap_id, even_direccion, barr_id, even_id, even_direccion_anterior, barr_id_anterior, even_estado, tire_id, even_horaini, even_horafin) VALUES ({repo_id}, {aap_id}, {even_direccion}, {barr_id}, {even_id}, {even_direccion_anterior}, {barr_id_anterior}, {even_estado}, {tire_id}, {even_horaini}, {even_horafin})"""
              ).on(
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_direccion -> d.even_direccion,
                  'barr_id -> d.barr_id,
                  'even_id -> d.even_id,
                  'even_horaini -> d.even_horaini,
                  'even_horafin -> d.even_horafin,
                  'tire_id -> d.tire_id,
                  'even_direccion_anterior -> aap.aap_direccion,
                  'barr_id_anterior -> aap.barr_id,
                  'even_estado -> estado
                )
                .executeUpdate() > 0
            }
            // Fin Direccion
            // aap elemento historia
            val updated: Boolean = SQL(
              """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
            ).on(
                'aap_balasto_retirado -> Option
                  .empty[String],
                'aap_balasto_instalado -> Option
                  .empty[String],
                'aap_id -> d.aap_id,
                'aael_fecha -> reporte.repo_fechasolucion,
                'reti_id -> reporte.reti_id,
                'repo_consecutivo -> reporte.repo_consecutivo,
                'empr_id -> reporte.empr_id
              )
              .executeUpdate() > 0
            if (!updated) {
              SQL(
                """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
              ).on(
                  'aap_bombillo_retirado -> Option
                    .empty[String],
                  'aap_bombillo_instalado -> Option
                    .empty[String],
                  'aap_balasto_retirado -> Option
                    .empty[String],
                  'aap_balasto_instalado -> Option
                    .empty[String],
                  'aap_arrancador_retirado -> Option
                    .empty[String],
                  'aap_arrancador_instalado -> Option
                    .empty[String],
                  'aap_condensador_retirado -> Option
                    .empty[String],
                  'aap_condensador_instalado -> Option
                    .empty[String],
                  'aap_fotocelda_retirado -> Option
                    .empty[String],
                  'aap_fotocelda_instalado -> Option
                    .empty[String],
                  'aap_id -> d.aap_id,
                  'aael_fecha -> reporte.repo_fechasolucion,
                  'reti_id -> reporte.reti_id,
                  'repo_consecutivo -> reporte.repo_consecutivo,
                  'empr_id -> reporte.empr_id
                )
                .executeUpdate()
            }
            // Fin aap elemento historia
            // Direccion Dato
            datoActualizado = SQL(
              """UPDATE siap.reporte_direccion_dato SET
                            aatc_id = {aatc_id},
                            aatc_id_anterior = {aatc_id_anterior},
                            aama_id = {aama_id},
                            aama_id_anterior = {aama_id_anterior},
                            aamo_id = {aamo_id},
                            aamo_id_anterior = {aamo_id_anterior},
                            aaco_id = {aaco_id},
                            aaco_id_anterior = {aaco_id_anterior},
                            aap_potencia = {aap_potencia},
                            aap_potencia_anterior = {aap_potencia_anterior},
                            aap_tecnologia = {aap_tecnologia},
                            aap_tecnologia_anterior = {aap_tecnologia_anterior},
                            aap_brazo = {aap_brazo},
                            aap_brazo_anterior = {aap_brazo_anterior},
                            aap_collarin = {aap_collarin_anterior},
                            tipo_id = {tipo_id},
                            tipo_id_anterior = {tipo_id_anterior},
                            aap_poste_altura = {aap_poste_altura},
                            aap_poste_altura_anterior = {aap_poste_altura_anterior},
                            aap_poste_propietario = {aap_poste_propietario},
                            aap_poste_propietario_anterior = {aap_poste_propietario_anterior}
                        WHERE
                            repo_id = {repo_id} and 
                            aap_id = {aap_id} and
                            even_id = {even_id}
                        """
            ).on(
                'aatc_id -> d.dato.get.aatc_id,
                'aatc_id_anterior -> d.dato.get.aatc_id_anterior,
                'aama_id -> d.dato.get.aama_id,
                'aama_id_anterior -> d.dato.get.aama_id_anterior,
                'aamo_id -> d.dato.get.aamo_id,
                'aamo_id_anterior -> d.dato.get.aamo_id_anterior,
                'aaco_id -> d.dato.get.aaco_id,
                'aaco_id_anterior -> d.dato.get.aaco_id_anterior,
                'aap_potencia -> d.dato.get.aap_potencia,
                'aap_potencia_anterior -> d.dato.get.aap_potencia_anterior,
                'aap_tecnologia -> d.dato.get.aap_tecnologia,
                'aap_tecnologia_anterior -> d.dato.get.aap_tecnologia_anterior,
                'aap_brazo -> d.dato.get.aap_brazo,
                'aap_brazo_anterior -> d.dato.get.aap_brazo_anterior,
                'aap_collarin -> d.dato.get.aap_collarin,
                'aap_collarin_anterior -> d.dato.get.aap_collarin_anterior,
                'tipo_id -> d.dato.get.tipo_id,
                'tipo_id_anterior -> d.dato.get.tipo_id_anterior,
                'aap_poste_altura -> d.dato.get.aap_poste_altura,
                'aap_poste_altura_anterior -> d.dato.get.aap_poste_altura_anterior,
                'aap_poste_propietario -> d.dato.get.aap_poste_propietario,
                'aap_poste_propietario_anterior -> d.dato.get.aap_poste_propietario_anterior,
                'repo_id -> reporte.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .executeUpdate() > 0
            if (!datoActualizado) {
              datoInsertado = SQL("""INSERT INTO siap.reporte_direccion_dato (
                                repo_id,
                                even_id,
                                aap_id,
                                aatc_id,
                                aatc_id_anterior,
                                aama_id,
                                aama_id_anterior,
                                aamo_id,
                                aamo_id_anterior,
                                aaco_id,
                                aaco_id_anterior,
                                aap_potencia,
                                aap_potencia_anterior,
                                aap_tecnologia,
                                aap_tecnologia_anterior,
                                aap_brazo,
                                aap_brazo_anterior,
                                aap_collarin,
                                aap_collarin_anterior,
                                tipo_id,
                                tipo_id_anterior,
                                aap_poste_altura,
                                aap_poste_altura_anterior,
                                aap_poste_propietario,
                                aap_poste_propietario_anterior
                                ) VALUES (
                                    {repo_id},
                                    {even_id},
                                    {aap_id},
                                    {aatc_id},
                                    {aatc_id_anterior},
                                    {aama_id},
                                    {aama_id_anterior},
                                    {aamo_id},
                                    {aamo_id_anterior},
                                    {aaco_id},
                                    {aaco_id_anterior},
                                    {aap_potencia},
                                    {aap_potencia_anterior},
                                    {aap_tecnologia},
                                    {aap_tecnologia_anterior},
                                    {aap_brazo},
                                    {aap_brazo_anterior},
                                    {aap_collarin},
                                    {aap_collarin_anterior},
                                    {tipo_id},
                                    {tipo_id_anterior},
                                    {aap_poste_altura},
                                    {aap_poste_altura_anterior},
                                    {aap_poste_propietario},
                                    {aap_poste_propietario_anterior}
                                )
                            """)
                .on(
                  'aatc_id -> d.dato.get.aatc_id,
                  'aatc_id_anterior -> d.dato.get.aatc_id_anterior,
                  'aama_id -> d.dato.get.aama_id,
                  'aama_id_anterior -> d.dato.get.aama_id_anterior,
                  'aamo_id -> d.dato.get.aamo_id,
                  'aamo_id_anterior -> d.dato.get.aamo_id_anterior,
                  'aaco_id -> d.dato.get.aaco_id,
                  'aaco_id_anterior -> d.dato.get.aaco_id_anterior,
                  'aap_potencia -> d.dato.get.aap_potencia,
                  'aap_potencia_anterior -> d.dato.get.aap_potencia_anterior,
                  'aap_tecnologia -> d.dato.get.aap_tecnologia,
                  'aap_tecnologia_anterior -> d.dato.get.aap_tecnologia_anterior,
                  'aap_brazo -> d.dato.get.aap_brazo,
                  'aap_brazo_anterior -> d.dato.get.aap_brazo_anterior,
                  'aap_collarin -> d.dato.get.aap_collarin,
                  'aap_collarin_anterior -> d.dato.get.aap_collarin_anterior,
                  'tipo_id -> d.dato.get.tipo_id,
                  'tipo_id_anterior -> d.dato.get.tipo_id_anterior,
                  'aap_poste_altura -> d.dato.get.aap_poste_altura,
                  'aap_poste_altura_anterior -> d.dato.get.aap_poste_altura_anterior,
                  'aap_poste_propietario -> d.dato.get.aap_poste_propietario,
                  'aap_poste_propietario_anterior -> d.dato.get.aap_poste_propietario_anterior,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_id -> d.even_id
                )
                .executeUpdate() > 0
            }
            // Fin Direccion Dato
            // Direccion Dato Adicional
            datoadicionalActualizado = SQL(
              """UPDATE siap.reporte_direccion_dato_adicional SET 
                                aacu_id_anterior = {aacu_id_anterior},
                                aacu_id = {aacu_id},
                                aaus_id_anterior = {aaus_id_anterior},
                                aaus_id = {aaus_id},
                                medi_id_anterior = {medi_id_anterior},
                                medi_id = {medi_id},
                                tran_id_anterior = {tran_id_anterior},
                                tran_id = {tran_id},
                                aap_apoyo = {aap_apoyo},
                                aap_apoyo_anterior = {aap_apoyo_anterior},
                                aap_lat = {aap_lat},
                                aap_lat_anterior = {aap_lat_anterior},
                                aap_lng = {aap_lng},
                                aap_lng_anterior = {aap_lng_anterior}
                                WHERE
                                    repo_id = {repo_id} and
                                    even_id = {even_id} and
                                    aap_id = {aap_id}
                                """
            ).on(
                'aacu_id_anterior -> d.dato_adicional.get.aacu_id_anterior,
                'aacu_id -> d.dato_adicional.get.aacu_id,
                'aaus_id_anterior -> d.dato_adicional.get.aaus_id_anterior,
                'aaus_id -> d.dato_adicional.get.aaus_id,
                'medi_id_anterior -> d.dato_adicional.get.medi_id_anterior,
                'medi_id -> d.dato_adicional.get.medi_id,
                'tran_id_anterior -> d.dato_adicional.get.tran_id_anterior,
                'tran_id -> d.dato_adicional.get.tran_id,
                'aap_apoyo -> d.dato_adicional.get.aap_apoyo,
                'aap_apoyo_anterior -> d.dato_adicional.get.aap_apoyo,
                'aap_lat -> d.dato_adicional.get.aap_lat,
                'aap_lat_anterior -> d.dato_adicional.get.aap_lat_anterior,
                'aap_lng -> d.dato_adicional.get.aap_lng,
                'aap_lng_anterior -> d.dato_adicional.get.aap_lng_anterior,
                'repo_id -> reporte.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .executeUpdate() > 0
            if (!datoadicionalActualizado) {
              datoadicionalInsertado = SQL(
                """INSERT INTO siap.reporte_direccion_dato_adicional (
                                    repo_id,
                                    even_id,
                                    aap_id,
                                    aacu_id_anterior,
                                    aacu_id,
                                    aaus_id_anterior,
                                    aaus_id,
                                    medi_id_anterior,
                                    medi_id,
                                    tran_id_anterior,
                                    tran_id,
                                    aap_apoyo,
                                    aap_apoyo_anterior,
                                    aap_lat,
                                    aap_lat_anterior,
                                    aap_lng,
                                    aap_lng_anterior) 
                                VALUES (
                                    {repo_id},
                                    {even_id},
                                    {aap_id},
                                    {aacu_id_anterior},
                                    {aacu_id},
                                    {aaus_id_anterior},
                                    {aaus_id},
                                    {medi_id_anterior},
                                    {medi_id},
                                    {tran_id_anterior},
                                    {tran_id},
                                    {aap_apoyo},
                                    {aap_apoyo_anterior},
                                    {aap_lat},
                                    {aap_lat_anterior},
                                    {aap_lng},
                                    {aap_lng_anterior}
                                )
                                """
              ).on(
                  'aacu_id_anterior -> d.dato_adicional.get.aacu_id_anterior,
                  'aacu_id -> d.dato_adicional.get.aacu_id,
                  'aaus_id_anterior -> d.dato_adicional.get.aaus_id_anterior,
                  'aaus_id -> d.dato_adicional.get.aaus_id,
                  'medi_id_anterior -> d.dato_adicional.get.medi_id_anterior,
                  'medi_id -> d.dato_adicional.get.medi_id,
                  'tran_id_anterior -> d.dato_adicional.get.tran_id_anterior,
                  'tran_id -> d.dato_adicional.get.tran_id,
                  'aap_apoyo -> d.dato_adicional.get.aap_apoyo,
                  'aap_apoyo_anterior -> d.dato_adicional.get.aap_apoyo,
                  'aap_lat -> d.dato_adicional.get.aap_lat,
                  'aap_lat_anterior -> d.dato_adicional.get.aap_lat_anterior,
                  'aap_lng -> d.dato_adicional.get.aap_lng,
                  'aap_lng_anterior -> d.dato_adicional.get.aap_lng_anterior,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_id -> d.even_id
                )
                .executeUpdate() > 0
            }
            // Fin Direccion Dato Adicional
            d.dato match {
              case Some(dato) =>
                dato.aatc_id match {
                  case Some(aatc_id) =>
                    println("Actualizando Tipo Luminaria")
                    SQL(
                      "UPDATE siap.aap SET aatc_id = {aatc_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aatc_id -> aatc_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aama_id match {
                  case Some(aama_id) =>
                    println("Actualizando Marca Luminaria")
                    SQL(
                      "UPDATE siap.aap SET aama_id = {aama_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aama_id -> aama_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aamo_id match {
                  case Some(aamo_id) =>
                    println("Actualizando Modelo Luminaria")
                    SQL(
                      "UPDATE siap.aap SET aamo_id = {aamo_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aamo_id -> aamo_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aap_potencia match {
                  case Some(aap_potencia) =>
                    println("Actualizando Tipo Luminaria")
                    SQL(
                      "UPDATE siap.aap_adicional SET aap_potencia = {aap_potencia} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_potencia -> aap_potencia,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aap_tecnologia match {
                  case Some(aap_tecnologia) =>
                    println("Actualizando Tecnologia")
                    SQL(
                      "UPDATE siap.aap_adicional SET aap_tecnologia = {aap_tecnologia} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_tecnologia -> aap_tecnologia,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aap_brazo match {
                  case Some(aap_brazo) =>
                    println("Actualizando Brazo")
                    SQL(
                      "UPDATE siap.aap_adicional SET aap_brazo = {aap_brazo} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_brazo -> aap_brazo,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aap_collarin match {
                  case Some(aap_collarin) =>
                    println("Actualizando Collarin")
                    SQL(
                      "UPDATE siap.aap_adicional SET aap_collarin = {aap_collarin} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_collarin -> aap_collarin,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.tipo_id match {
                  case Some(tipo_id) =>
                    println("Actualizando Tipo Poste")
                    SQL(
                      "UPDATE siap.aap_adicional SET tipo_id = {tipo_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'tipo_id -> tipo_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aap_poste_altura match {
                  case Some(aap_poste_altura) =>
                    println("Actualizando Poste Altura")
                    SQL(
                      "UPDATE siap.aap_adicional SET aap_poste_altura = {aap_poste_altura} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_poste_altura -> aap_poste_altura,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aap_poste_propietario match {
                  case Some(aap_poste_propietario) =>
                    println("Actualizando Poste Propietario")
                    SQL(
                      "UPDATE siap.aap_adicional SET aap_poste_propietario = {aap_poste_propietario} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_poste_propietario -> aap_poste_propietario,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato.aaco_id match {
                  case Some(aaco_id) =>
                    println("Actualizando Tipo Medida")
                    SQL(
                      "UPDATE siap.aap SET aaco_id = {aaco_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aaco_id -> aaco_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                    // si se conecta en aforo, eliminar la conexión con medidor
                    if (aaco_id == 1) {
                      println("Borrando Relacion con el Medidor")
                      SQL(
                        "DELETE FROM siap.aap_medidor WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                      ).on(
                          'aap_id -> d.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                    }
                    if (aaco_id == 3) {
                      println("Cambiando estado a RETIRADA")
                      SQL(
                        "UPDATE siap.aap SET esta_id = {esta_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                      ).on(
                          'esta_id -> 2,
                          'aap_id -> d.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                    }
                    if (aaco_id == 1 || aaco_id == 2) {
                      println("Cambiando estado a ACTIVA")
                      SQL(
                        "UPDATE siap.aap SET esta_id = {esta_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                      ).on(
                          'esta_id -> 1,
                          'aap_id -> d.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                    }
                  case None => false
                }
              case None => false
            }
            d.dato_adicional match {
              case Some(dato_adicional) =>
                dato_adicional.aacu_id match {
                  case Some(aacu_id) =>
                    println("Actualizando Cuenta Alumbrado")
                    SQL(
                      "UPDATE siap.aap SET aacu_id = {aacu_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aacu_id -> aacu_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato_adicional.aaus_id match {
                  case Some(aaus_id) =>
                    println("Actualizando Uso")
                    SQL(
                      "UPDATE siap.aap SET aaus_id = {aaus_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aaus_id -> aaus_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }
                dato_adicional.medi_id match {
                  case Some(medi_id) =>
                    println("Actualizando Medidor")
                    val actualizar = SQL(
                      "UPDATE siap.aap_medidor SET medi_id = {medi_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'medi_id -> medi_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                    if (!actualizar) {
                      val insertado = SQL(
                        "INSERT INTO siap.aap_medidor (aap_id, empr_id, medi_id) VALUES ({aap_id}, {empr_id}, {medi_id});"
                      ).on(
                          'medi_id -> medi_id,
                          'aap_id -> d.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate() > 0
                    }
                  case None => false
                }
                dato_adicional.tran_id match {
                  case Some(tran_id) =>
                    println("Actualizando Transformador")
                    val actualizar = SQL(
                      "UPDATE siap.aap_transformador SET tran_id = {tran_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'tran_id -> tran_id,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                    if (!actualizar) {
                      val insertado = SQL(
                        "INSERT INTO siap.aap_transformador (aap_id, empr_id, tran_id) VALUES ({aap_id}, {empr_id}, {tran_id});"
                      ).on(
                          'tran_id -> tran_id,
                          'aap_id -> d.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate() > 0
                    }
                  case None => false
                }

                dato_adicional.aap_apoyo match {
                  case Some(aap_apoyo) =>
                    println("Actualizando Código de Apoyo")
                    val actualizar = SQL(
                      "UPDATE siap.aap SET aap_apoyo = {aap_apoyo} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_apoyo -> aap_apoyo,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }

                dato_adicional.aap_lat match {
                  case Some(aap_lat) =>
                    println("Actualizando Latitud")
                    val actualizar = SQL(
                      "UPDATE siap.aap SET aap_lat = {aap_lat} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_lat -> aap_lat,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }

                dato_adicional.aap_lng match {
                  case Some(aap_lng) =>
                    println("Actualizando Código de Apoyo")
                    val actualizar = SQL(
                      "UPDATE siap.aap SET aap_lng = {aap_lng} WHERE aap_id = {aap_id} and empr_id = {empr_id}"
                    ).on(
                        'aap_lng -> aap_lng,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate() > 0
                  case None => false
                }

              case None => false
            }
            // actualizar direccion de la luminaria y datos adicionales
            // Actualizar direccion sin importar el tipo de reporte
            // if (reporte.reti_id.get == 1 || reporte.reti_id.get == 2 || reporte.reti_id.get == 3) {
            val res = SQL(
              """UPDATE siap.aap SET aap_direccion = {aap_direccion}, barr_id = {barr_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
            ).on(
                'aap_direccion -> d.even_direccion,
                'barr_id -> d.barr_id,
                'aap_id -> d.aap_id,
                'empr_id -> reporte.empr_id
              )
              .executeUpdate() > 0
            // }
            reporte.reti_id match {
              case Some(8) =>
                SQL(
                  """UPDATE siap.aap SET aaco_id = 3 WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
                ).on(
                    'aap_id -> d.aap_id,
                    'empr_id -> reporte.empr_id
                  )
                  .executeUpdate()
                println("tipo retiro: " + d.tire_id)
                d.tire_id match {
                  case Some(3) =>
                    SQL(
                      """UPDATE siap.aap SET esta_id = 9 WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
                    ).on(
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  case _ =>
                    SQL(
                      """UPDATE siap.aap SET esta_id = 2 WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
                    ).on(
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                }
              case _ => None
            }
          } // Fin d.aap_id != null
        } // Fin for direcciones
      } // Fin direcciones map

      //
      // guardar medio ambiente
      SQL(
        """DELETE FROM siap.reporte_medioambiente WHERE repo_id = {repo_id}"""
      ).on(
          'repo_id -> reporte.repo_id
        )
        .execute()

      reporte.meams.map { meams =>
        for (m <- meams) {
          SQL(
            """INSERT INTO siap.reporte_medioambiente (repo_id, meam_id) VALUES ({repo_id}, {meam_id})"""
          ).on(
              'repo_id -> reporte.repo_id.get,
              'meam_id -> m
            )
            .executeInsert()
        }
      }
      //
      // Registrar Código de Autorización
      if (!coau_codigo.trim.isEmpty) {
        SQL(
          """INSERT INTO siap.reporte_codigo_autorizacion (repo_id, coau_codigo, empr_id) VALUES ({repo_id}, {coau_codigo}, {empr_id})"""
        ).on(
            'repo_id -> reporte.repo_id,
            'coau_codigo -> coau_codigo,
            'empr_id -> reporte.empr_id
          )
          .executeInsert()

        SQL(
          """UPDATE siap.codigo_autorizacion SET coau_fechauso = {coau_fechauso}, coau_usua_id = {usua_id}, coau_estado = {coau_estado} WHERE coau_tipo = {coau_tipo} and coau_codigo = {coau_codigo} and empr_id = {empr_id}"""
        ).on(
            'coau_fechauso -> hora,
            'usua_id -> reporte.usua_id,
            'coau_estado -> 1,
            'empr_id -> reporte.empr_id,
            'coau_tipo -> coau_tipo,
            'coau_codigo -> coau_codigo
          )
          .executeUpdate()
      }

      if (reporte_ant != None) {
        if (reporte_ant.get.repo_fecharecepcion != reporte.repo_fecharecepcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_fecharecepcion",
              'audi_valorantiguo -> reporte_ant.get.repo_fecharecepcion,
              'audi_valornuevo -> reporte.repo_fecharecepcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_direccion != reporte.repo_direccion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_direccion",
              'audi_valorantiguo -> reporte_ant.get.repo_direccion,
              'audi_valornuevo -> reporte.repo_direccion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_nombre != reporte.repo_nombre) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_nombre",
              'audi_valorantiguo -> reporte_ant.get.repo_nombre,
              'audi_valornuevo -> reporte.repo_nombre,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_telefono != reporte.repo_telefono) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_telefono",
              'audi_valorantiguo -> reporte_ant.get.repo_telefono,
              'audi_valornuevo -> reporte.repo_telefono,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_fechasolucion != reporte.repo_fechasolucion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_fechasolucion",
              'audi_valorantiguo -> reporte_ant.get.repo_fechasolucion,
              'audi_valornuevo -> reporte.repo_fechasolucion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_horainicio != reporte.repo_horainicio) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_horainicio",
              'audi_valorantiguo -> reporte_ant.get.repo_horainicio,
              'audi_valornuevo -> reporte.repo_horainicio,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_horafin != reporte.repo_horafin) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_horafin",
              'audi_valorantiguo -> reporte_ant.get.repo_horafin,
              'audi_valornuevo -> reporte.repo_horafin,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.repo_reportetecnico != reporte.repo_reportetecnico) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "repo_reportetecnico",
              'audi_valorantiguo -> reporte_ant.get.repo_reportetecnico,
              'audi_valornuevo -> reporte.repo_reportetecnico,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.rees_id != reporte.rees_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.rees_id,
              'audi_campo -> "rees_id",
              'audi_valorantiguo -> reporte_ant.get.rees_id,
              'audi_valornuevo -> reporte.rees_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.orig_id != reporte.orig_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "orig_id",
              'audi_valorantiguo -> reporte_ant.get.orig_id,
              'audi_valornuevo -> reporte.orig_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.barr_id != reporte.barr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "barr_id",
              'audi_valorantiguo -> reporte_ant.get.barr_id,
              'audi_valornuevo -> reporte.barr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.empr_id != reporte.empr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "empr_id",
              'audi_valorantiguo -> reporte_ant.get.empr_id,
              'audi_valornuevo -> reporte.empr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (reporte_ant.get.usua_id != reporte.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> reporte.usua_id,
              'audi_tabla -> "reporte",
              'audi_uid -> reporte.repo_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> reporte_ant.get.usua_id,
              'audi_valornuevo -> reporte.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
      }

      result
    }
  }

  /**
    * Actualizar Reporte
    * @param reporte: Reporte
    */
  def actualizarMaterial(
      reporte: Reporte
  ): Boolean = {
    val reporte_ant: Option[Reporte] = buscarPorId(reporte.repo_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      var historia =
        scala.collection.mutable.Map[scala.Long, ElementoHistoria]()

      SQL("""DELETE FROM siap.reporte_evento WHERE repo_id = {repo_id}""")
        .on('repo_id -> reporte.repo_id)
        .executeUpdate()

      reporte.eventos.map { eventos =>
        for (e <- eventos) {
          if (e.aap_id != None) {
            var elemento: Elemento = null
            var bombillo_retirado = None: Option[String]
            var bombillo_instalado = None: Option[String]
            var balasto_retirado = None: Option[String]
            var balasto_instalado = None: Option[String]
            var arrancador_retirado = None: Option[String]
            var arrancador_instalado = None: Option[String]
            var condensador_retirado = None: Option[String]
            var condensador_instalado = None: Option[String]
            var fotocelda_retirado = None: Option[String]
            var fotocelda_instalado = None: Option[String]

            e.elem_id match {
              case None => None
              case Some(elem_id) =>
                elemento = elementoService.buscarPorId(elem_id).get
            }
            // Actualizar Evento si ya Existe
            var estado = 0
            e.even_estado match {
              case Some(1) => estado = 2
              case Some(2) => estado = 2
              case Some(8) => estado = 9
              case Some(9) => estado = 9
              case _       => estado = 2
            }
            var eventoActualizado: Boolean = false
            var eventoInsertado: Boolean = false
            /*println("empr_id: " + reporte.empr_id)
            println("repo_id: " + reporte.repo_id)
            println("aap_id: " + e.aap_id)
            println("even_id: " + e.even_id)*/
            eventoActualizado = SQL(
              """UPDATE siap.reporte_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                elem_id = {elem_id},
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id},
                                                                unit_id = {unit_id}
                                                            WHERE empr_id = {empr_id} and repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}
                                                        """
            ).on(
                'even_fecha -> hora,
                'elem_id -> e.elem_id,
                'even_codigo_retirado -> e.even_codigo_retirado,
                'even_cantidad_retirado -> e.even_cantidad_retirado,
                'even_codigo_instalado -> e.even_codigo_instalado,
                'even_cantidad_instalado -> e.even_cantidad_instalado,
                'usua_id -> reporte.usua_id,
                'even_estado -> estado,
                'empr_id -> reporte.empr_id,
                'repo_id -> reporte.repo_id,
                'aap_id -> e.aap_id,
                'even_id -> e.even_id,
                'unit_id -> e.unit_id
              )
              .executeUpdate() > 0
            if (!eventoActualizado) {
              eventoInsertado = SQL(
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
                                    even_id,
                                    unit_id) VALUES (
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
                                    {even_id},
                                    {unit_id})"""
              ).on(
                  "even_fecha" -> hora,
                  "even_codigo_instalado" -> e.even_codigo_instalado,
                  "even_cantidad_instalado" -> e.even_cantidad_instalado,
                  "even_codigo_retirado" -> e.even_codigo_retirado,
                  "even_cantidad_retirado" -> e.even_cantidad_retirado,
                  "even_estado" -> estado,
                  "aap_id" -> e.aap_id,
                  "repo_id" -> reporte.repo_id,
                  "elem_id" -> e.elem_id,
                  "usua_id" -> e.usua_id,
                  "empr_id" -> reporte.empr_id,
                  "even_id" -> e.even_id,
                  "unit_id" -> e.unit_id
                )
                .executeUpdate() > 0
            }
            if ((eventoActualizado || eventoInsertado) && (estado != 9)) {
              // validar elemento y actualizar aap_elemento
              elemento.tiel_id match {
                case Some(1) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo}, reti_id = {reti_id} , repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo_retirado -> e.even_codigo_retirado,
                      'aap_bombillo_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> e.even_codigo_retirado,
                        'aap_bombillo_instalado -> e.even_codigo_instalado,
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(2) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto_retirado -> e.even_codigo_retirado,
                      'aap_balasto_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> e.even_codigo_retirado,
                        'aap_balasto_instalado -> e.even_codigo_instalado,
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(3) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador_retirado -> e.even_codigo_retirado,
                      'aap_arrancador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> e.even_codigo_retirado,
                        'aap_arrancador_instalado -> e.even_codigo_instalado,
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(4) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador_retirado -> e.even_codigo_retirado,
                      'aap_condensador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> e.even_codigo_retirado,
                        'aap_condensador_instalado -> e.even_codigo_instalado,
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(5) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda_retirado -> e.even_codigo_retirado,
                      'aap_fotocelda_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> e.even_codigo_retirado,
                        'aap_fotocelda_instalado -> e.even_codigo_instalado,
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case _ => None
              }
            }
          }
        }
      }

      true
    }
  }

  def actualizarMovil(
      reporte: Reporte,
      ip_address: String
  ): Boolean = {
    reporte.repo_id match {
      case Some(repo_id) =>
        if (repo_id > -1) {
          reporte.tireuc_id match {
            case Some(1) => actualizarMovilLuminaria(reporte)
            case Some(2) =>
              controlReporteService.actualizarMovilControl(reporte)
            case Some(3) =>
              transformadorReporteService.actualizarMovilTransformador(reporte)
            case Some(4) => medidorReporteService.actualizarMovilMedidor(reporte)
            case _       => false
          }
          insertarSincronizacionMovil(reporte, ip_address)
        } else {
          false
        }
      case None => false
    }
  }

  /**
    * Sincronizacion de Reporte
    * @param reporte: Reporte
    * @param ip_address: String
    */
  def insertarSincronizacionMovil(
      reporte: Reporte,
      ip_address: String
  ): Boolean = {
    var result = false

    try {
      db.withTransaction { implicit connection =>
        val fecha: LocalDate =
          new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime =
          new LocalDateTime(Calendar.getInstance().getTimeInMillis())
        val _uuid = java.util.UUID.randomUUID

        var _parseOrtr = get[Option[Int]]("ortr_id") ~ get[Option[Int]](
          "cuad_id"
        ) map {
          case ortr_id ~ cuad_id => (ortr_id, cuad_id)
        }
        val _ortr =
          SQL("""select o1.ortr_id, o1.cuad_id  from siap.reporte r1
              inner join siap.ordentrabajo_reporte or1 on or1.repo_id = r1.repo_id and or1.even_estado < 8
              inner join siap.ordentrabajo o1 on o1.ortr_id = or1.ortr_id and o1.otes_id < 8
             where r1.repo_id = {repo_id} and o1.ortr_fecha = {fecha_solucion}
             order by o1.ortr_fecha desc
             limit 1""")
            .on(
              'repo_id -> reporte.repo_id,
              'fecha_solucion -> reporte.repo_fechasolucion
            )
            .as(_parseOrtr.singleOpt)
            .getOrElse((None, None))

        val _ortr_id: Int = _ortr._1 match {
          case Some(ortr_id: Int) => ortr_id
          case None               => -1
        }

        val _cuad_id: Int = _ortr._2 match {
          case Some(cuad_id: Int) => cuad_id
          case None               => -1
        }

        val _reporteInsertado = SQL(
          """INSERT INTO siap.sincronizacion_reporte (
                repo_id,  
                repo_fecharecepcion, 
                repo_direccion, 
                repo_nombre, 
                repo_telefono, 
                repo_fechasolucion,
                repo_reportetecnico,
                orig_id,
                barr_id,
                usua_id,
                empr_id,
                rees_id,
                repo_descripcion,
                repo_horainicio,
                repo_horafin,
                reti_id, 
                repo_consecutivo,
                tiba_id, 
                tireuc_id,
                repo_subrepoconsecutivo,
                ortr_id, 
                cuad_id, 
                ip_address, 
                repo_idx, 
                repo_fecha_sync) VALUES (
                  {repo_id},
                  {repo_fecharecepcion}, 
                  {repo_direccion}, 
                  {repo_nombre}, 
                  {repo_telefono}, 
                  {repo_fechasolucion}, 
                  {repo_reportetecnico},
                  {orig_id},
                  {barr_id},
                  {usua_id},
                  {empr_id},
                  {rees_id},
                  {repo_descripcion},
                  {repo_horainicio}, 
                  {repo_horafin}, 
                  {reti_id},                  
                  {repo_consecutivo},                   
                  {tiba_id},                   
                  {tireuc_id}, 
                  {repo_subrepoconsecutivo},
                  {ortr_id}, 
                  {cuad_id}, 
                  {ip_address}, 
                  {repo_idx}::uuid, 
                  {repo_fecha_sync})"""
        ).on(
            'repo_id -> reporte.repo_id,
            'tireuc_id -> reporte.tireuc_id,
            'repo_fecharecepcion -> reporte.repo_fecharecepcion,
            'repo_direccion -> reporte.repo_direccion,
            'repo_nombre -> reporte.repo_nombre,
            'repo_telefono -> reporte.repo_telefono,
            'repo_fechasolucion -> reporte.repo_fechasolucion,
            'repo_reportetecnico -> reporte.repo_reportetecnico,
            'orig_id -> reporte.orig_id,
            'barr_id -> reporte.barr_id,
            'usua_id -> reporte.usua_id,
            'empr_id -> reporte.empr_id,
            'rees_id -> reporte.rees_id,
            'repo_descripcion -> reporte.repo_descripcion,
            'repo_horainicio -> reporte.repo_horainicio,
            'repo_horafin -> reporte.repo_horafin,
            'reti_id -> reporte.reti_id,
            'repo_consecutivo -> reporte.repo_consecutivo,
            'tiba_id -> reporte.tiba_id,
            'repo_subrepoconsecutivo -> reporte.repo_subrepoconsecutivo,
            'ortr_id -> _ortr_id,
            'cuad_id -> _cuad_id,
            'ip_address -> ip_address,
            'repo_idx -> _uuid,
            'repo_fecha_sync -> hora
          )
          .executeUpdate() > 0

        // actualizar reporte adicional
        reporte.adicional.map { adicional =>
          val hayAdicional: Boolean =
            SQL("""INSERT INTO siap.sincronizacion_reporte_adicional (repo_id, 
                                                               repo_fechadigitacion, 
                                                               repo_tipo_expansion, 
                                                               repo_luminaria, 
                                                               repo_redes, 
                                                               repo_poste, 
                                                               repo_modificado, 
                                                               repo_subreporte, 
                                                               repo_email,
                                                               repo_subid, 
                                                               acti_id,
                                                               repo_codigo,
                                                               repo_apoyo,
                                                               urba_id,
                                                               muot_id,
                                                               repo_idx) VALUES (
                                                                {repo_id}, 
                                                                {repo_fechadigitacion}, 
                                                                {repo_tipo_expansion}, 
                                                                {repo_luminaria}, 
                                                                {repo_redes}, 
                                                                {repo_poste}, 
                                                                {repo_modificado}, 
                                                                {repo_subreporte}, 
                                                                {repo_email},
                                                                {repo_subid}, 
                                                                {acti_id},
                                                                {repo_codigo},
                                                                {repo_apoyo},
                                                                {urba_id},
                                                                {muot_id},
                                                                {repo_idx}::uuid
                                                               )""")
              .on(
                'repo_fechadigitacion -> adicional.repo_fechadigitacion,
                'repo_tipo_expansion -> adicional.repo_tipo_expansion,
                'repo_luminaria -> adicional.repo_luminaria,
                'repo_redes -> adicional.repo_redes,
                'repo_poste -> adicional.repo_poste,
                'repo_modificado -> hora,
                'repo_subreporte -> adicional.repo_subreporte,
                'repo_subid -> Option.empty[scala.Long],
                'repo_email -> adicional.repo_email,
                'acti_id -> adicional.acti_id,
                'repo_codigo -> adicional.repo_codigo,
                'repo_apoyo -> adicional.repo_apoyo,
                'urba_id -> adicional.urba_id,
                'muot_id -> adicional.muot_id,
                'repo_id -> reporte.repo_id,
                'repo_idx -> _uuid
              )
              .executeUpdate() > 0
        }
        reporte.novedades.map { novedades =>
          for (n <- novedades) {
            if (n.even_estado.get < 8) {
              val novedadInsertado = SQL(
                """ INSERT INTO siap.sincronizacion_reporte_novedad (
                                             tireuc_id, 
                                             repo_id, 
                                             even_id, 
                                             nove_id, 
                                             reno_horaini, 
                                             reno_horafin, 
                                             reno_observacion, 
                                             even_estado,
                                             repo_idx
                                           ) VALUES (
                                             {tireuc_id},
                                             {repo_id},
                                             {even_id},
                                             {nove_id},
                                             {reno_horaini},
                                             {reno_horafin},
                                             {reno_observacion},
                                             {even_estado},
                                             {repo_idx}::uuid
                                           )
                                    """
              ).on(
                  'tireuc_id -> reporte.tireuc_id,
                  'repo_id -> reporte.repo_id,
                  'even_id -> n.even_id,
                  'nove_id -> n.nove_id,
                  'reno_horaini -> n.reno_horaini,
                  'reno_horafin -> n.reno_horafin,
                  'reno_observacion -> n.reno_observacion,
                  'even_estado -> n.even_estado,
                  'repo_idx -> _uuid
                )
                .executeUpdate() > 0
            }
          }
        }

        reporte.eventos.map { eventos =>
          for (e <- eventos) {
            if (e.aap_id != None) {
              var eventoInsertado = SQL(
                """INSERT INTO siap.sincronizacion_reporte_evento (even_fecha, 
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
                                    even_id,
                                    unit_id,
                                    repo_idx) VALUES (
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
                                    {even_id},
                                    {unit_id},
                                    {repo_idx}::uuid
                                    )"""
              ).on(
                  "even_fecha" -> hora,
                  "even_codigo_instalado" -> e.even_codigo_instalado,
                  "even_cantidad_instalado" -> e.even_cantidad_instalado,
                  "even_codigo_retirado" -> e.even_codigo_retirado,
                  "even_cantidad_retirado" -> e.even_cantidad_retirado,
                  "even_estado" -> e.even_estado,
                  "aap_id" -> e.aap_id,
                  "repo_id" -> reporte.repo_id,
                  "elem_id" -> e.elem_id,
                  "usua_id" -> e.usua_id,
                  "empr_id" -> reporte.empr_id,
                  "even_id" -> e.even_id,
                  "unit_id" -> e.unit_id,
                  "repo_idx" -> _uuid
                )
                .executeUpdate() > 0
            }
          }
        }

        reporte.direcciones.map { direcciones =>
          for (d <- direcciones) {
            if (d.aap_id != null) {
              val dirInsertado = SQL(
                """INSERT INTO siap.sincronizacion_reporte_direccion (repo_id, aap_id, even_direccion, barr_id, even_id, even_direccion_anterior, barr_id_anterior, even_estado, tire_id, even_horaini, even_horafin, repo_idx) VALUES ({repo_id}, {aap_id}, {even_direccion}, {barr_id}, {even_id}, {even_direccion_anterior}, {barr_id_anterior}, {even_estado}, {tire_id}, {even_horaini}, {even_horafin}, {repo_idx}::uuid)"""
              ).on(
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_direccion -> d.even_direccion,
                  'barr_id -> d.barr_id,
                  'even_id -> d.even_id,
                  'even_horaini -> d.even_horaini,
                  'even_horafin -> d.even_horafin,
                  'tire_id -> d.tire_id,
                  'even_direccion_anterior -> d.even_direccion_anterior,
                  'barr_id_anterior -> d.barr_id_anterior,
                  'even_estado -> d.even_estado,
                  'repo_idx -> _uuid
                )
                .executeUpdate() > 0
              val datoInsertado = SQL(
                """INSERT INTO siap.sincronizacion_reporte_direccion_dato (
                                repo_id,
                                even_id,
                                aap_id,
                                aatc_id,
                                aatc_id_anterior,
                                aama_id,
                                aama_id_anterior,
                                aamo_id,
                                aamo_id_anterior,
                                aaco_id,
                                aaco_id_anterior,
                                aap_potencia,
                                aap_potencia_anterior,
                                aap_tecnologia,
                                aap_tecnologia_anterior,
                                aap_brazo,
                                aap_brazo_anterior,
                                aap_collarin,
                                aap_collarin_anterior,
                                tipo_id,
                                tipo_id_anterior,
                                aap_poste_altura,
                                aap_poste_altura_anterior,
                                aap_poste_propietario,
                                aap_poste_propietario_anterior,
                                repo_idx
                                ) VALUES (
                                    {repo_id},
                                    {even_id},
                                    {aap_id},
                                    {aatc_id},
                                    {aatc_id_anterior},
                                    {aama_id},
                                    {aama_id_anterior},
                                    {aamo_id},
                                    {aamo_id_anterior},
                                    {aaco_id},
                                    {aaco_id_anterior},
                                    {aap_potencia},
                                    {aap_potencia_anterior},
                                    {aap_tecnologia},
                                    {aap_tecnologia_anterior},
                                    {aap_brazo},
                                    {aap_brazo_anterior},
                                    {aap_collarin},
                                    {aap_collarin_anterior},
                                    {tipo_id},
                                    {tipo_id_anterior},
                                    {aap_poste_altura},
                                    {aap_poste_altura_anterior},
                                    {aap_poste_propietario},
                                    {aap_poste_propietario_anterior},
                                    {repo_idx}::uuid
                                )
                            """
              ).on(
                  'aatc_id -> d.dato.get.aatc_id,
                  'aatc_id_anterior -> d.dato.get.aatc_id_anterior,
                  'aama_id -> d.dato.get.aama_id,
                  'aama_id_anterior -> d.dato.get.aama_id_anterior,
                  'aamo_id -> d.dato.get.aamo_id,
                  'aamo_id_anterior -> d.dato.get.aamo_id_anterior,
                  'aaco_id -> d.dato.get.aaco_id,
                  'aaco_id_anterior -> d.dato.get.aaco_id_anterior,
                  'aap_potencia -> d.dato.get.aap_potencia,
                  'aap_potencia_anterior -> d.dato.get.aap_potencia_anterior,
                  'aap_tecnologia -> d.dato.get.aap_tecnologia,
                  'aap_tecnologia_anterior -> d.dato.get.aap_tecnologia_anterior,
                  'aap_brazo -> d.dato.get.aap_brazo,
                  'aap_brazo_anterior -> d.dato.get.aap_brazo_anterior,
                  'aap_collarin -> d.dato.get.aap_collarin,
                  'aap_collarin_anterior -> d.dato.get.aap_collarin_anterior,
                  'tipo_id -> d.dato.get.tipo_id,
                  'tipo_id_anterior -> d.dato.get.tipo_id_anterior,
                  'aap_poste_altura -> d.dato.get.aap_poste_altura,
                  'aap_poste_altura_anterior -> d.dato.get.aap_poste_altura_anterior,
                  'aap_poste_propietario -> d.dato.get.aap_poste_propietario,
                  'aap_poste_propietario_anterior -> d.dato.get.aap_poste_propietario_anterior,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_id -> d.even_id,
                  'repo_idx -> _uuid
                )
                .executeUpdate() > 0
              // Fin Direccion Dato
              // Direccion Dato Adicional
              val datoadicionalInsertado = SQL(
                """INSERT INTO siap.sincronizacion_reporte_direccion_dato_adicional (
                                    repo_id,
                                    even_id,
                                    aap_id,
                                    aacu_id_anterior,
                                    aacu_id,
                                    aaus_id_anterior,
                                    aaus_id,
                                    medi_id_anterior,
                                    medi_id,
                                    tran_id_anterior,
                                    tran_id,
                                    aap_apoyo,
                                    aap_apoyo_anterior,
                                    aap_lat,
                                    aap_lat_anterior,
                                    aap_lng,
                                    aap_lng_anterior,
                                    repo_idx) 
                                VALUES (
                                    {repo_id},
                                    {even_id},
                                    {aap_id},
                                    {aacu_id_anterior},
                                    {aacu_id},
                                    {aaus_id_anterior},
                                    {aaus_id},
                                    {medi_id_anterior},
                                    {medi_id},
                                    {tran_id_anterior},
                                    {tran_id},
                                    {aap_apoyo},
                                    {aap_apoyo_anterior},
                                    {aap_lat},
                                    {aap_lat_anterior},
                                    {aap_lng},
                                    {aap_lng_anterior},
                                    {repo_idx}::uuid
                                )
                                """
              ).on(
                  'aacu_id_anterior -> d.dato_adicional.get.aacu_id_anterior,
                  'aacu_id -> d.dato_adicional.get.aacu_id,
                  'aaus_id_anterior -> d.dato_adicional.get.aaus_id_anterior,
                  'aaus_id -> d.dato_adicional.get.aaus_id,
                  'medi_id_anterior -> d.dato_adicional.get.medi_id_anterior,
                  'medi_id -> d.dato_adicional.get.medi_id,
                  'tran_id_anterior -> d.dato_adicional.get.tran_id_anterior,
                  'tran_id -> d.dato_adicional.get.tran_id,
                  'aap_apoyo -> d.dato_adicional.get.aap_apoyo,
                  'aap_apoyo_anterior -> d.dato_adicional.get.aap_apoyo,
                  'aap_lat -> d.dato_adicional.get.aap_lat,
                  'aap_lat_anterior -> d.dato_adicional.get.aap_lat_anterior,
                  'aap_lng -> d.dato_adicional.get.aap_lng,
                  'aap_lng_anterior -> d.dato_adicional.get.aap_lng_anterior,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_id -> d.even_id,
                  'repo_idx -> _uuid
                )
                .executeUpdate() > 0
              // Procesando Fotos
              d.fotos.map { fotos =>
                for (f <- fotos) {
                  var refoData = if (!f.refo_data.isEmpty) {
                    "reporte_" + reporte.tireuc_id.getOrElse(1).toString + "_" + reporte.repo_id.get.toString + "_aap_" + d.aap_id.get.toString + "_image_" + f.refo_id.get.toString + ".jpg"
                  } else { "" }
                  val fotoInsertada = SQL(
                    """
                    INSERT INTO siap.sincronizacion_reporte_direccion_foto (
                        repo_id,
                        tireuc_id,
                        aap_id,
                        refo_id,
                        refo_tipo,
                        refo_data,
                        repo_idx
                    ) VALUES (
                        {repo_id},
                        {tireuc_id},
                        {aap_id},
                        {refo_id},
                        {refo_tipo},
                        {refo_data},
                        {repo_idx}::uuid
                    )
                """
                  ).on(
                      'repo_id -> reporte.repo_id,
                      'tireuc_id -> reporte.tireuc_id,
                      'aap_id -> d.aap_id,
                      'refo_id -> f.refo_id,
                      'refo_tipo -> f.refo_tipo,
                      'refo_data -> refoData,
                      'repo_idx -> _uuid
                    )
                    .executeUpdate() > 0
                }
              }
              // Fin Direccion Dato Adicional
              // actualizar direccion de la luminaria y datos adicionales
              // Actualizar direccion sin importar el tipo de reporte
              // if (reporte.reti_id.get == 1 || reporte.reti_id.get == 2 || reporte.reti_id.get == 3) {
            } // Fin d.aap_id != null
          } // Fin for direcciones
        } // Fin direcciones map

        reporte.meams.map { meams =>
          for (m <- meams) {
            SQL(
              """INSERT INTO siap.sincronizacion_reporte_medioambiente (repo_id, meam_id, repo_idx) VALUES ({repo_id}, {meam_id}, {repo_idx}::uuid)"""
            ).on(
                'repo_id -> reporte.repo_id.get,
                'meam_id -> m,
                'repo_idx -> _uuid
              )
              .executeInsert()
          }
        }
        result = true
      }
    } catch {
      case e: Exception => {
        println("Guardando Reporte Movil Luminaria Error: " + e.getMessage())
        result = false
      }
    }
    result
  }

  def actualizarMovilMedidor(reporte: Reporte): Boolean = {
    true
  }

  /**
    * Actualizar Reporte
    * @param reporte: Reporte
    */
  def actualizarMovilLuminaria(
      reporte: Reporte
  ): Boolean = {
    val reporte_ant: Option[Reporte] = buscarPorId(reporte.repo_id.get)
    // val valida = validarOrdenDeTrabajo(reporte.repo_id.get)
    var result = false
    // if (true) {
    println("Actualizando Reporte Movil")
    try {
      db.withTransaction { implicit connection =>
        val fecha: LocalDate =
          new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime =
          new LocalDateTime(Calendar.getInstance().getTimeInMillis())
        val fecha_solucion: Option[DateTime] = reporte_ant match {
          case Some(r) =>
            r.repo_fechasolucion match {
              case Some(f) => Some(f)
              case None => {
                if (reporte.direcciones.get.length > 0) {
                  Some(DateTime.now())
                } else {
                  None
                }
              }
            }
          case None => None
        }

        // if (reporte.repo_reportetecnico.getOrElse("") != "") {
        result = SQL(
          "UPDATE siap.reporte SET repo_direccion = {repo_direccion}, repo_nombre = {repo_nombre}, repo_telefono = {repo_telefono}, repo_fechasolucion = {repo_fechasolucion}, repo_horainicio = {repo_horainicio}, repo_horafin = {repo_horafin}, repo_reportetecnico = {repo_reportetecnico}, repo_descripcion = {repo_descripcion}, repo_subrepoconsecutivo = {repo_subrepoconsecutivo}, rees_id = {rees_id}, orig_id = {orig_id}, barr_id = {barr_id}, empr_id = {empr_id}, tiba_id = {tiba_id}, usua_id = {usua_id} WHERE repo_id = {repo_id}"
        ).on(
            'repo_id -> reporte.repo_id,
            'repo_direccion -> reporte.repo_direccion,
            'repo_nombre -> reporte.repo_nombre,
            'repo_telefono -> reporte.repo_telefono,
            'repo_fechasolucion -> fecha_solucion,
            'repo_horainicio -> reporte.repo_horainicio,
            'repo_horafin -> reporte.repo_horafin,
            'repo_reportetecnico -> reporte.repo_reportetecnico,
            'repo_descripcion -> reporte.repo_descripcion,
            'repo_subrepoconsecutivo -> reporte.repo_subrepoconsecutivo,
            'rees_id -> 2,
            'orig_id -> reporte.orig_id,
            'barr_id -> reporte.barr_id,
            'empr_id -> reporte.empr_id,
            'tiba_id -> reporte.tiba_id,
            'usua_id -> reporte.usua_id
          )
          .executeUpdate() > 0

        // actualizar reporte adicional
        reporte.adicional.map { adicional =>
          val hayAdicional: Boolean = SQL(
            """UPDATE siap.reporte_adicional SET repo_fechadigitacion = {repo_fechadigitacion}, 
                    repo_tipo_expansion = {repo_tipo_expansion}, repo_luminaria = {repo_luminaria}, 
                    repo_redes = {repo_redes}, repo_poste = {repo_poste}, repo_modificado = {repo_modificado}, 
                    repo_subreporte = {repo_subreporte}, repo_subid = {repo_subid}, repo_email = {repo_email}, 
                    acti_id = {acti_id}, repo_codigo = {repo_codigo}, repo_apoyo = {repo_apoyo}, 
                    urba_id = {urba_id}, muot_id = {muot_id}, medi_id = {medi_id}, tran_id = {tran_id}, 
                    medi_acta = {medi_acta}, aaco_id_anterior = {aaco_id_anterior}, aaco_id_nuevo = {aaco_id_nuevo} WHERE repo_id = {repo_id}"""
          ).on(
              'repo_fechadigitacion -> adicional.repo_fechadigitacion,
              'repo_tipo_expansion -> adicional.repo_tipo_expansion,
              'repo_luminaria -> adicional.repo_luminaria,
              'repo_redes -> adicional.repo_redes,
              'repo_poste -> adicional.repo_poste,
              'repo_modificado -> hora,
              'repo_subreporte -> adicional.repo_subreporte,
              'repo_subid -> Option.empty[scala.Long],
              'repo_email -> adicional.repo_email,
              'acti_id -> adicional.acti_id,
              'repo_codigo -> adicional.repo_codigo,
              'repo_apoyo -> adicional.repo_apoyo,
              'urba_id -> adicional.urba_id,
              'muot_id -> adicional.muot_id,
              'medi_id -> adicional.medi_id,
              'tran_id -> adicional.tran_id,
              'medi_acta -> adicional.medi_acta,
              'aaco_id_anterior -> adicional.aaco_id_anterior,
              'aaco_id_nuevo -> adicional.aaco_id_nuevo,
              'repo_id -> reporte.repo_id
            )
            .executeUpdate() > 0
          if (!hayAdicional) {
            SQL("""INSERT INTO siap.reporte_adicional (repo_id, 
                                                               repo_fechadigitacion, 
                                                               repo_tipo_expansion, 
                                                               repo_luminaria, 
                                                               repo_redes, 
                                                               repo_poste, 
                                                               repo_modificado, 
                                                               repo_subreporte, 
                                                               repo_email,
                                                               repo_subid, 
                                                               acti_id,
                                                               repo_codigo,
                                                               repo_apoyo,
                                                               urba_id,
                                                               muot_id) VALUES (
                                                                {repo_id}, 
                                                                {repo_fechadigitacion}, 
                                                                {repo_tipo_expansion}, 
                                                                {repo_luminaria}, 
                                                                {repo_redes}, 
                                                                {repo_poste}, 
                                                                {repo_modificado}, 
                                                                {repo_subreporte}, 
                                                                {repo_email},
                                                                {repo_subid}, 
                                                                {acti_id},
                                                                {repo_codigo},
                                                                {repo_apoyo},
                                                                {urba_id},
                                                                {muot_id}
                                                               )""")
              .on(
                'repo_fechadigitacion -> adicional.repo_fechadigitacion,
                'repo_tipo_expansion -> adicional.repo_tipo_expansion,
                'repo_luminaria -> adicional.repo_luminaria,
                'repo_redes -> adicional.repo_redes,
                'repo_poste -> adicional.repo_poste,
                'repo_modificado -> hora,
                'repo_subreporte -> adicional.repo_subreporte,
                'repo_subid -> Option.empty[scala.Long],
                'repo_email -> adicional.repo_email,
                'acti_id -> adicional.acti_id,
                'repo_codigo -> adicional.repo_codigo,
                'repo_apoyo -> adicional.repo_apoyo,
                'urba_id -> adicional.urba_id,
                'muot_id -> adicional.muot_id,
                'repo_id -> reporte.repo_id
              )
              .executeInsert()
          }
        }
        // Creación Actualizacion de Novedades
        SQL(
          """DELETE FROM siap.reporte_novedad rn1 WHERE rn1.repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .executeUpdate()
        reporte.novedades.map { novedades =>
          for (n <- novedades) {
            val novedadActualizado = SQL(
              """UPDATE siap.reporte_novedad SET 
                                           nove_id = {nove_id}, 
                                           reno_horaini = {reno_horaini}, 
                                           reno_horafin = {reno_horafin}, 
                                           reno_observacion = {reno_observacion},
                                           even_estado = {even_estado}
                                          WHERE repo_id = {repo_id} AND even_id = {even_id}"""
            ).on(
                'repo_id -> reporte.repo_id,
                'even_id -> n.even_id,
                'nove_id -> n.nove_id,
                'reno_horaini -> n.reno_horaini,
                'reno_horafin -> n.reno_horafin,
                'reno_observacion -> n.reno_observacion,
                'even_estado -> n.even_estado
              )
              .executeUpdate() > 0
            if (!novedadActualizado) {
              if (n.even_estado.get < 8) {
                val novedadInsertado = SQL(
                  """ INSERT INTO siap.reporte_novedad (
                                             tireuc_id, 
                                             repo_id, 
                                             even_id, 
                                             nove_id, 
                                             reno_horaini, 
                                             reno_horafin, 
                                             reno_observacion, 
                                             even_estado
                                           ) VALUES (
                                             {tireuc_id},
                                             {repo_id},
                                             {even_id},
                                             {nove_id},
                                             {reno_horaini},
                                             {reno_horafin},
                                             {reno_observacion},
                                             {even_estado}
                                           )
                                    """
                ).on(
                    'tireuc_id -> reporte.tireuc_id,
                    'repo_id -> reporte.repo_id,
                    'even_id -> n.even_id,
                    'nove_id -> n.nove_id,
                    'reno_horaini -> n.reno_horaini,
                    'reno_horafin -> n.reno_horafin,
                    'reno_observacion -> n.reno_observacion,
                    'even_estado -> n.even_estado
                  )
                  .executeUpdate() > 0
              }
            }
          }
        }

        /// Proceso Suspendido
        // Proceso de Creación de Luminarias Nuevas por Expansión Tipo III
        /*       if (reporte.reti_id.get == 2 && reporte.adicional.get.repo_tipo_expansion.get != 4 && reporte.rees_id.get == 4) {
        reporte.direcciones.map { direcciones =>
          for (d <- direcciones) {
            if (d.aap_id != None) {
              var aap_elemento: AapElemento = new AapElemento(
                d.aap_id,
                None,
                None,
                None,
                None,
                None,
                None,
                reporte.reti_id,
                reporte.repo_consecutivo.map(_.toInt)
              )
              var aap: Aap = new Aap(
                d.aap_id,
                d.dato_adicional match { case Some(d) => d.aap_apoyo case None => None },
                None,
                d.even_direccion,
                d.dato_adicional match { case Some(d) => d.aap_lat case None => None },
                d.dato_adicional match { case Some(d) => d.aap_lng case None => None },
                d.barr_id,
                None,
                reporte.empr_id.get,
                reporte.repo_fechasolucion,
                reporte.repo_fechasolucion,
                None,
                d.dato match { case Some(d) => d.aama_id case None => None },
                d.dato_adicional match { case Some(d) => d.aacu_id case None => None },
                None,
                d.dato match { case Some(d) => d.aatc_id case None => None },
                d.dato match { case Some(d) => d.aamo_id case None => None },
                d.dato_adicional match { case Some(d) => d.aaus_id case None => None },
                d.dato match { case Some(d) => d.aaco_id case None => None },
                reporte.usua_id.get,
                Some(aap_elemento),
                None
              )
              var aap_adicional: AapAdicional = new AapAdicional(
                d.aap_id,
                d.dato match { case Some(d) => d.tipo_id case None => None },
                d.dato match { case Some(d) => d.aap_poste_altura case None => None },
                d.dato match { case Some(d) => d.aap_brazo case None => None },
                d.dato match { case Some(d) => d.aap_collarin case None => None },
                d.dato match { case Some(d) => d.aap_potencia case None => None },
                d.dato match { case Some(d) => d.aap_tecnologia case None => None },
                None,
                None,
                d.dato match { case Some(d) => d.aap_poste_propietario case None => None },
                None,
                None
              )
              val aapOption =
                aapService.buscarPorId(d.aap_id.get, reporte.empr_id.get)
              var activo = new Activo(
                Some(aap),
                None,
                None,
                Some(aap_adicional),
                Some(aap_elemento),
                None,
                Some(1)
              )
              aapOption match {
                case None =>
                  if (d.even_estado.get == 1) {
                    println("Creando Luminaria: " + d.aap_id)
                    aapService.creardirecto(
                      activo,
                      reporte.empr_id.get,
                      reporte.usua_id.get
                    )
                  }
                case (a) => {
                  // aapService.actualizarDirecto(activo, reporte.empr_id.get, reporte.usua_id.get)
                }
              }

              // Fin Proceso de Creación de Luminarias Nuevas por Expansión Tipo I,II,III,V
            }
          }
        }
      }
         */ /// Fin Proceso Suspendido

        SQL(
          """DELETE FROM siap.reporte_evento re1 WHERE re1.repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .executeUpdate()
        reporte.eventos.map { eventos =>
          for (e <- eventos) {
            if (e.aap_id != None) {
              var elemento: Elemento = null
              var bombillo_retirado = None: Option[String]
              var bombillo_instalado = None: Option[String]
              var balasto_retirado = None: Option[String]
              var balasto_instalado = None: Option[String]
              var arrancador_retirado = None: Option[String]
              var arrancador_instalado = None: Option[String]
              var condensador_retirado = None: Option[String]
              var condensador_instalado = None: Option[String]
              var fotocelda_retirado = None: Option[String]
              var fotocelda_instalado = None: Option[String]

              e.elem_id match {
                case None => None
                case Some(elem_id) =>
                  elemento = elementoService.buscarPorId(elem_id).get
              }
              // Actualizar Evento si ya Existe
              var estado = 0
              e.even_estado match {
                case Some(1) => estado = 2
                case Some(2) => estado = 2
                case Some(8) => estado = 9
                case Some(9) => estado = 9
                case _       => estado = 2
              }
              var eventoActualizado: Boolean = false
              var eventoInsertado: Boolean = false
              eventoActualizado = SQL(
                """UPDATE siap.reporte_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id},
                                                                unit_id = {unit_id}
                                                            WHERE repo_id = {repo_id} and aap_id = {aap_id} and elem_id = {elem_id} AND even_id = {even_id}
                                                        """
              ).on(
                  'even_fecha -> hora,
                  'elem_id -> e.elem_id,
                  'even_codigo_retirado -> e.even_codigo_retirado,
                  'even_cantidad_retirado -> e.even_cantidad_retirado,
                  'even_codigo_instalado -> e.even_codigo_instalado,
                  'even_cantidad_instalado -> e.even_cantidad_instalado,
                  'usua_id -> reporte.usua_id,
                  'even_estado -> estado,
                  'empr_id -> reporte.empr_id,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> e.aap_id,
                  'unit_id -> e.unit_id,
                  'even_id -> e.even_id
                )
                .executeUpdate() > 0
              if (!eventoActualizado) {
                eventoInsertado = SQL(
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
                                    even_id,
                                    unit_id) VALUES (
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
                                    {even_id},
                                    {unit_id})"""
                ).on(
                    "even_fecha" -> hora,
                    "even_codigo_instalado" -> e.even_codigo_instalado,
                    "even_cantidad_instalado" -> e.even_cantidad_instalado,
                    "even_codigo_retirado" -> e.even_codigo_retirado,
                    "even_cantidad_retirado" -> e.even_cantidad_retirado,
                    "even_estado" -> estado,
                    "aap_id" -> e.aap_id,
                    "repo_id" -> reporte.repo_id,
                    "elem_id" -> e.elem_id,
                    "usua_id" -> e.usua_id,
                    "empr_id" -> reporte.empr_id,
                    "even_id" -> e.even_id,
                    "unit_id" -> e.unit_id
                  )
                  .executeUpdate() > 0
              }
              if ((eventoActualizado || eventoInsertado) && (estado != 9)) {
                // validar elemento y actualizar aap_elemento
                elemento.tiel_id match {
                  case Some(1) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo}, reti_id = {reti_id} , repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate() */
                  /*                   val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo_retirado -> e.even_codigo_retirado,
                      'aap_bombillo_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0 */
                  /*                   if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> e.even_codigo_retirado,
                        'aap_bombillo_instalado -> e.even_codigo_instalado,
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  } */
                  case Some(2) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto_retirado -> e.even_codigo_retirado,
                      'aap_balasto_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> e.even_codigo_retirado,
                        'aap_balasto_instalado -> e.even_codigo_instalado,
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case Some(3) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador_retirado -> e.even_codigo_retirado,
                      'aap_arrancador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> e.even_codigo_retirado,
                        'aap_arrancador_instalado -> e.even_codigo_instalado,
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case Some(4) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador_retirado -> e.even_codigo_retirado,
                      'aap_condensador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> e.even_codigo_retirado,
                        'aap_condensador_instalado -> e.even_codigo_instalado,
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case Some(5) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda_retirado -> e.even_codigo_retirado,
                      'aap_fotocelda_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> e.even_codigo_retirado,
                        'aap_fotocelda_instalado -> e.even_codigo_instalado,
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case _ => None
                }
              }
            }
          }
        }

        SQL(
          """DELETE FROM siap.reporte_direccion rd1 WHERE rd1.repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .executeUpdate()
        SQL(
          """DELETE FROM siap.reporte_direccion_dato rdd1 WHERE rdd1.repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .executeUpdate()
        SQL(
          """DELETE FROM siap.reporte_direccion_dato_adicional rdda1 WHERE rdda1.repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .executeUpdate()
        SQL(
          """DELETE FROM siap.reporte_direccion_foto rdf1 WHERE rdf1.repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .executeUpdate()
        reporte.direcciones.map { direcciones =>
          for (d <- direcciones) {
            if (d.aap_id != None && d.aap_id.get.toInt > 0) {
              var dirActualizado: Boolean = false
              var dirInsertado: Boolean = false
              var datoActualizado: Boolean = false
              var datoInsertado: Boolean = false
              var datoadicionalInsertado: Boolean = false
              var datoadicionalActualizado: Boolean = false
              val aap = aapService.buscarPorId(
                d.aap_id.get,
                reporte.empr_id.get
              ) match {
                case Some(a) => a
                case None => {
                  val app = Aap.empty
                  val a = app.copy(aap_id = d.aap_id)
                  a
                }
              }

              var estado = 0
              d.even_estado match {
                case Some(1) => estado = 2
                case Some(2) => estado = 2
                case Some(8) => estado = 9
                case Some(9) => estado = 9
                case _       => estado = 2
              }
              // Direccion
              dirActualizado = SQL(
                """UPDATE siap.reporte_direccion SET
                                                even_direccion = {even_direccion},
                                                barr_id = {barr_id},
                                                even_direccion_anterior = {even_direccion_anterior},
                                                barr_id_anterior = {barr_id_anterior},
                                                even_estado = {even_estado},
                                                tire_id = {tire_id},
                                                even_horaini = {even_horaini},
                                                even_horafin = {even_horafin}
                                            WHERE
                                                repo_id = {repo_id} and
                                                aap_id = {aap_id} """
              ).on(
                  'even_direccion -> d.even_direccion,
                  'barr_id -> d.barr_id,
                  'even_direccion_anterior -> d.even_direccion_anterior,
                  'barr_id_anterior -> d.barr_id_anterior,
                  'even_estado -> estado,
                  'tire_id -> d.tire_id,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_horaini -> d.even_horaini,
                  'even_horafin -> d.even_horafin
                )
                .executeUpdate() > 0

              if (!dirActualizado) {
                dirInsertado = SQL(
                  """INSERT INTO siap.reporte_direccion (repo_id, aap_id, even_direccion, barr_id, even_id, even_direccion_anterior, barr_id_anterior, even_estado, tire_id, even_horaini, even_horafin) VALUES ({repo_id}, {aap_id}, {even_direccion}, {barr_id}, {even_id}, {even_direccion_anterior}, {barr_id_anterior}, {even_estado}, {tire_id}, {even_horaini}, {even_horafin})"""
                ).on(
                    'repo_id -> reporte.repo_id,
                    'aap_id -> d.aap_id,
                    'even_direccion -> d.even_direccion,
                    'barr_id -> d.barr_id,
                    'even_id -> d.even_id,
                    'even_horaini -> d.even_horaini,
                    'even_horafin -> d.even_horafin,
                    'tire_id -> d.tire_id,
                    'even_direccion_anterior -> aap.aap_direccion,
                    'barr_id_anterior -> aap.barr_id,
                    'even_estado -> estado
                  )
                  .executeUpdate() > 0
              }
              // Fin Direccion
              // Direccion Dato
              datoActualizado = SQL(
                """UPDATE siap.reporte_direccion_dato SET
                            aatc_id = {aatc_id},
                            aatc_id_anterior = {aatc_id_anterior},
                            aama_id = {aama_id},
                            aama_id_anterior = {aama_id_anterior},
                            aamo_id = {aamo_id},
                            aamo_id_anterior = {aamo_id_anterior},
                            aaco_id = {aaco_id},
                            aaco_id_anterior = {aaco_id_anterior},
                            aap_potencia = {aap_potencia},
                            aap_potencia_anterior = {aap_potencia_anterior},
                            aap_tecnologia = {aap_tecnologia},
                            aap_tecnologia_anterior = {aap_tecnologia_anterior},
                            aap_brazo = {aap_brazo},
                            aap_brazo_anterior = {aap_brazo_anterior},
                            aap_collarin = {aap_collarin_anterior},
                            tipo_id = {tipo_id},
                            tipo_id_anterior = {tipo_id_anterior},
                            aap_poste_altura = {aap_poste_altura},
                            aap_poste_altura_anterior = {aap_poste_altura_anterior},
                            aap_poste_propietario = {aap_poste_propietario},
                            aap_poste_propietario_anterior = {aap_poste_propietario_anterior}
                        WHERE
                            repo_id = {repo_id} and 
                            aap_id = {aap_id}
                        """
              ).on(
                  'aatc_id -> d.dato.get.aatc_id,
                  'aatc_id_anterior -> d.dato.get.aatc_id_anterior,
                  'aama_id -> d.dato.get.aama_id,
                  'aama_id_anterior -> d.dato.get.aama_id_anterior,
                  'aamo_id -> d.dato.get.aamo_id,
                  'aamo_id_anterior -> d.dato.get.aamo_id_anterior,
                  'aaco_id -> d.dato.get.aaco_id,
                  'aaco_id_anterior -> d.dato.get.aaco_id_anterior,
                  'aap_potencia -> d.dato.get.aap_potencia,
                  'aap_potencia_anterior -> d.dato.get.aap_potencia_anterior,
                  'aap_tecnologia -> d.dato.get.aap_tecnologia,
                  'aap_tecnologia_anterior -> d.dato.get.aap_tecnologia_anterior,
                  'aap_brazo -> d.dato.get.aap_brazo,
                  'aap_brazo_anterior -> d.dato.get.aap_brazo_anterior,
                  'aap_collarin -> d.dato.get.aap_collarin,
                  'aap_collarin_anterior -> d.dato.get.aap_collarin_anterior,
                  'tipo_id -> d.dato.get.tipo_id,
                  'tipo_id_anterior -> d.dato.get.tipo_id_anterior,
                  'aap_poste_altura -> d.dato.get.aap_poste_altura,
                  'aap_poste_altura_anterior -> d.dato.get.aap_poste_altura_anterior,
                  'aap_poste_propietario -> d.dato.get.aap_poste_propietario,
                  'aap_poste_propietario_anterior -> d.dato.get.aap_poste_propietario_anterior,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id
                )
                .executeUpdate() > 0
              if (!datoActualizado) {
                datoInsertado = SQL(
                  """INSERT INTO siap.reporte_direccion_dato (
                                repo_id,
                                even_id,
                                aap_id,
                                aatc_id,
                                aatc_id_anterior,
                                aama_id,
                                aama_id_anterior,
                                aamo_id,
                                aamo_id_anterior,
                                aaco_id,
                                aaco_id_anterior,
                                aap_potencia,
                                aap_potencia_anterior,
                                aap_tecnologia,
                                aap_tecnologia_anterior,
                                aap_brazo,
                                aap_brazo_anterior,
                                aap_collarin,
                                aap_collarin_anterior,
                                tipo_id,
                                tipo_id_anterior,
                                aap_poste_altura,
                                aap_poste_altura_anterior,
                                aap_poste_propietario,
                                aap_poste_propietario_anterior
                                ) VALUES (
                                    {repo_id},
                                    {even_id},
                                    {aap_id},
                                    {aatc_id},
                                    {aatc_id_anterior},
                                    {aama_id},
                                    {aama_id_anterior},
                                    {aamo_id},
                                    {aamo_id_anterior},
                                    {aaco_id},
                                    {aaco_id_anterior},
                                    {aap_potencia},
                                    {aap_potencia_anterior},
                                    {aap_tecnologia},
                                    {aap_tecnologia_anterior},
                                    {aap_brazo},
                                    {aap_brazo_anterior},
                                    {aap_collarin},
                                    {aap_collarin_anterior},
                                    {tipo_id},
                                    {tipo_id_anterior},
                                    {aap_poste_altura},
                                    {aap_poste_altura_anterior},
                                    {aap_poste_propietario},
                                    {aap_poste_propietario_anterior}
                                )
                            """
                ).on(
                    'aatc_id -> d.dato.get.aatc_id,
                    'aatc_id_anterior -> d.dato.get.aatc_id_anterior,
                    'aama_id -> d.dato.get.aama_id,
                    'aama_id_anterior -> d.dato.get.aama_id_anterior,
                    'aamo_id -> d.dato.get.aamo_id,
                    'aamo_id_anterior -> d.dato.get.aamo_id_anterior,
                    'aaco_id -> d.dato.get.aaco_id,
                    'aaco_id_anterior -> d.dato.get.aaco_id_anterior,
                    'aap_potencia -> d.dato.get.aap_potencia,
                    'aap_potencia_anterior -> d.dato.get.aap_potencia_anterior,
                    'aap_tecnologia -> d.dato.get.aap_tecnologia,
                    'aap_tecnologia_anterior -> d.dato.get.aap_tecnologia_anterior,
                    'aap_brazo -> d.dato.get.aap_brazo,
                    'aap_brazo_anterior -> d.dato.get.aap_brazo_anterior,
                    'aap_collarin -> d.dato.get.aap_collarin,
                    'aap_collarin_anterior -> d.dato.get.aap_collarin_anterior,
                    'tipo_id -> d.dato.get.tipo_id,
                    'tipo_id_anterior -> d.dato.get.tipo_id_anterior,
                    'aap_poste_altura -> d.dato.get.aap_poste_altura,
                    'aap_poste_altura_anterior -> d.dato.get.aap_poste_altura_anterior,
                    'aap_poste_propietario -> d.dato.get.aap_poste_propietario,
                    'aap_poste_propietario_anterior -> d.dato.get.aap_poste_propietario_anterior,
                    'repo_id -> reporte.repo_id,
                    'aap_id -> d.aap_id,
                    'even_id -> d.even_id
                  )
                  .executeUpdate() > 0
              }
              // Fin Direccion Dato
              // Direccion Dato Adicional
              var medi_id =
                medidorService.buscarPorAap(aap.aap_id.get, aap.empr_id) match {
                  case Some(m) => m.medi_id
                  case None    => None
                }
              var tran_id = transformadorService.buscarPorId(
                aap.aap_id.get,
                aap.empr_id
              ) match {
                case Some(t) => t.aap_id
                case None    => None
              }
              var aacu_id: Option[Int] =
                SQL("""
              select acu1.aacu_id from siap.aap a1
              left join siap.aap_cuentaap acu1 on
	            acu1.aacu_aaco = a1.aaco_id and cast(a1.aaus_id as char) in (select regexp_split_to_table(acu1.aacu_aaus,',') from siap.aap_cuentaap acu2
              where acu2.aacu_id = acu1.aacu_id)
              where a1.aap_id = {aap_id}
            """).on('aap_id -> aap.aap_id.get)
                  .as(SqlParser.scalar[Int].singleOpt) match {
                  case Some(a) => Some(a)
                  case None    => Some(-1)
                }
              datoadicionalActualizado = SQL(
                """UPDATE siap.reporte_direccion_dato_adicional SET 
                                aacu_id_anterior = {aacu_id_anterior},
                                aacu_id = {aacu_id},
                                aaus_id_anterior = {aaus_id_anterior},
                                aaus_id = {aaus_id},
                                medi_id_anterior = {medi_id_anterior},
                                medi_id = {medi_id},
                                tran_id_anterior = {tran_id_anterior},
                                tran_id = {tran_id},
                                aap_apoyo = {aap_apoyo},
                                aap_apoyo_anterior = {aap_apoyo_anterior},
                                aap_lat = {aap_lat},
                                aap_lat_anterior = {aap_lat_anterior},
                                aap_lng = {aap_lng},
                                aap_lng_anterior = {aap_lng_anterior}
                                WHERE
                                    repo_id = {repo_id} and
                                    aap_id = {aap_id}
                                """
              ).on(
                  'aacu_id_anterior -> d.dato_adicional.get.aacu_id_anterior,
                  'aacu_id -> aacu_id,
                  'aaus_id_anterior -> d.dato_adicional.get.aaus_id_anterior,
                  'aaus_id -> d.dato_adicional.get.aaus_id,
                  'medi_id_anterior -> medi_id, //d.dato_adicional.get.medi_id_anterior,
                  'medi_id -> medi_id, //d.dato_adicional.get.medi_id,
                  'tran_id_anterior -> tran_id, //d.dato_adicional.get.tran_id_anterior,
                  'tran_id -> tran_id, //d.dato_adicional.get.tran_id,
                  'aap_apoyo -> d.dato_adicional.get.aap_apoyo,
                  'aap_apoyo_anterior -> d.dato_adicional.get.aap_apoyo,
                  'aap_lat -> d.dato_adicional.get.aap_lat,
                  'aap_lat_anterior -> d.dato_adicional.get.aap_lat_anterior,
                  'aap_lng -> d.dato_adicional.get.aap_lng,
                  'aap_lng_anterior -> d.dato_adicional.get.aap_lng_anterior,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id
                )
                .executeUpdate() > 0
              if (!datoadicionalActualizado) {
                datoadicionalInsertado = SQL(
                  """INSERT INTO siap.reporte_direccion_dato_adicional (
                                    repo_id,
                                    even_id,
                                    aap_id,
                                    aacu_id_anterior,
                                    aacu_id,
                                    aaus_id_anterior,
                                    aaus_id,
                                    medi_id_anterior,
                                    medi_id,
                                    tran_id_anterior,
                                    tran_id,
                                    aap_apoyo,
                                    aap_apoyo_anterior,
                                    aap_lat,
                                    aap_lat_anterior,
                                    aap_lng,
                                    aap_lng_anterior) 
                                VALUES (
                                    {repo_id},
                                    {even_id},
                                    {aap_id},
                                    {aacu_id_anterior},
                                    {aacu_id},
                                    {aaus_id_anterior},
                                    {aaus_id},
                                    {medi_id_anterior},
                                    {medi_id},
                                    {tran_id_anterior},
                                    {tran_id},
                                    {aap_apoyo},
                                    {aap_apoyo_anterior},
                                    {aap_lat},
                                    {aap_lat_anterior},
                                    {aap_lng},
                                    {aap_lng_anterior}
                                )
                                """
                ).on(
                    'aacu_id_anterior -> d.dato_adicional.get.aacu_id_anterior,
                    'aacu_id -> d.dato_adicional.get.aacu_id,
                    'aaus_id_anterior -> d.dato_adicional.get.aaus_id_anterior,
                    'aaus_id -> d.dato_adicional.get.aaus_id,
                    'medi_id_anterior -> medi_id, // d.dato_adicional.get.medi_id_anterior,
                    'medi_id -> medi_id, //d.dato_adicional.get.medi_id,
                    'tran_id_anterior -> tran_id, //d.dato_adicional.get.tran_id_anterior,
                    'tran_id -> tran_id, // d.dato_adicional.get.tran_id,
                    'aap_apoyo -> d.dato_adicional.get.aap_apoyo,
                    'aap_apoyo_anterior -> d.dato_adicional.get.aap_apoyo,
                    'aap_lat -> d.dato_adicional.get.aap_lat,
                    'aap_lat_anterior -> d.dato_adicional.get.aap_lat_anterior,
                    'aap_lng -> d.dato_adicional.get.aap_lng,
                    'aap_lng_anterior -> d.dato_adicional.get.aap_lng_anterior,
                    'repo_id -> reporte.repo_id,
                    'aap_id -> d.aap_id,
                    'even_id -> d.even_id
                  )
                  .executeUpdate() > 0
              }
              // Procesando Fotos
              d.fotos.map { fotos =>
                for (f <- fotos) {
                  var refoData = if (!f.refo_data.isEmpty) {
                    "reporte_" + reporte.tireuc_id.getOrElse(1).toString + "_" + reporte.repo_id.get.toString + "_aap_" + d.aap_id.get.toString + "_image_" + f.refo_id.get.toString + ".jpg"
                  } else { "" }
                  val fotoActualizada = SQL(
                    """UPDATE siap.reporte_direccion_foto SET 
                                    refo_data = {refo_data}
                                WHERE
                                    repo_id = {repo_id} and
                                    tireuc_id = {tireuc_id} and
                                    aap_id = {aap_id} and
                                    refo_id = {refo_id} and
                                    refo_tipo = {refo_tipo}
                                """
                  ).on(
                      'repo_id -> reporte.repo_id,
                      'tireuc_id -> reporte.tireuc_id,
                      'aap_id -> d.aap_id,
                      'refo_id -> f.refo_id,
                      'refo_tipo -> f.refo_tipo,
                      'refo_data ->  refoData
                    )
                    .executeUpdate() > 0
                  if (!fotoActualizada) {
                    val fotoInsertada = SQL(
                      """
                    INSERT INTO siap.reporte_direccion_foto (
                        repo_id,
                        tireuc_id,
                        aap_id,
                        refo_id,
                        refo_tipo,
                        refo_data
                    ) VALUES (
                        {repo_id},
                        {tireuc_id},
                        {aap_id},
                        {refo_id},
                        {refo_tipo},
                        {refo_data}
                    )
                """
                    ).on(
                        'repo_id -> reporte.repo_id,
                        'tireuc_id -> reporte.tireuc_id,
                        'aap_id -> d.aap_id,
                        'refo_id -> f.refo_id,
                        'refo_tipo -> f.refo_tipo,
                        'refo_data -> refoData
                      )
                      .executeUpdate() > 0
                  }
                }
              }

              // Fin Direccion Dato Adicional
              // actualizar direccion de la luminaria y datos adicionales
              // Actualizar direccion sin importar el tipo de reporte
              // if (reporte.reti_id.get == 1 || reporte.reti_id.get == 2 || reporte.reti_id.get == 3) {
            } // Fin d.aap_id != null
          } // Fin for direcciones
        } // Fin direcciones map

        //
        // guardar medio ambiente
        SQL(
          """DELETE FROM siap.reporte_medioambiente WHERE repo_id = {repo_id}"""
        ).on(
            'repo_id -> reporte.repo_id
          )
          .execute()

        reporte.meams.map { meams =>
          for (m <- meams) {
            SQL(
              """INSERT INTO siap.reporte_medioambiente (repo_id, meam_id) VALUES ({repo_id}, {meam_id})"""
            ).on(
                'repo_id -> reporte.repo_id.get,
                'meam_id -> m
              )
              .executeInsert()
          }
        }

        val operaciones = reporte.direcciones match {
          case Some(d) => d.length
          case None    => 0
        }

        val materiales = reporte.eventos match {
          case Some(e) => e.length
          case None    => 0
        }
        //
        /*  val repoSyncUpdated = SQL("""UPDATE siap.reporte_sincronizacion SET resi_operaciones = {resi_operaciones}, resi_material = {resi_material}, resi_ultimo_sync = {resi_ultimo_sync} WHERE tireuc_id = {tireuc_id} and repo_id = {repo_id}""").on(
          'resi_ultimo_sync -> new DateTime(),
          'resi_operaciones -> operaciones,
          'resi_material -> materiales,
          'tireuc_id -> reporte.tireuc_id,
          'repo_id -> reporte.repo_id.get
        ).executeUpdate() > 0 */
        val repoSyncUpdated = false

        if (!repoSyncUpdated) {
          result = SQL(
            """INSERT INTO siap.reporte_sincronizacion VALUES ({tireuc_id}, {repo_id}, {reti_id}, {repo_consecutivo}, {resi_ultimo_sync}, {resi_operaciones}, {resi_material})"""
          ).on(
              'tireuc_id -> reporte.tireuc_id,
              'repo_id -> reporte.repo_id,
              'reti_id -> reporte.reti_id,
              'repo_consecutivo -> reporte.repo_consecutivo,
              'resi_ultimo_sync -> DateTime.now(),
              'resi_operaciones -> operaciones,
              'resi_material -> materiales
            )
            .executeUpdate() > 0
        }
        if (reporte_ant != None) {
          if (reporte_ant.get.repo_fecharecepcion != reporte.repo_fecharecepcion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_fecharecepcion",
                'audi_valorantiguo -> reporte_ant.get.repo_fecharecepcion,
                'audi_valornuevo -> reporte.repo_fecharecepcion,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_direccion != reporte.repo_direccion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_direccion",
                'audi_valorantiguo -> reporte_ant.get.repo_direccion,
                'audi_valornuevo -> reporte.repo_direccion,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_nombre != reporte.repo_nombre) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_nombre",
                'audi_valorantiguo -> reporte_ant.get.repo_nombre,
                'audi_valornuevo -> reporte.repo_nombre,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_telefono != reporte.repo_telefono) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_telefono",
                'audi_valorantiguo -> reporte_ant.get.repo_telefono,
                'audi_valornuevo -> reporte.repo_telefono,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_fechasolucion != reporte.repo_fechasolucion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_fechasolucion",
                'audi_valorantiguo -> reporte_ant.get.repo_fechasolucion,
                'audi_valornuevo -> reporte.repo_fechasolucion,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_horainicio != reporte.repo_horainicio) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_horainicio",
                'audi_valorantiguo -> reporte_ant.get.repo_horainicio,
                'audi_valornuevo -> reporte.repo_horainicio,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_horafin != reporte.repo_horafin) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_horafin",
                'audi_valorantiguo -> reporte_ant.get.repo_horafin,
                'audi_valornuevo -> reporte.repo_horafin,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.repo_reportetecnico != reporte.repo_reportetecnico) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "repo_reportetecnico",
                'audi_valorantiguo -> reporte_ant.get.repo_reportetecnico,
                'audi_valornuevo -> reporte.repo_reportetecnico,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.rees_id != reporte.rees_id) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.rees_id,
                'audi_campo -> "rees_id",
                'audi_valorantiguo -> reporte_ant.get.rees_id,
                'audi_valornuevo -> reporte.rees_id,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.orig_id != reporte.orig_id) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "orig_id",
                'audi_valorantiguo -> reporte_ant.get.orig_id,
                'audi_valornuevo -> reporte.orig_id,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.barr_id != reporte.barr_id) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "barr_id",
                'audi_valorantiguo -> reporte_ant.get.barr_id,
                'audi_valornuevo -> reporte.barr_id,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.empr_id != reporte.empr_id) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "empr_id",
                'audi_valorantiguo -> reporte_ant.get.empr_id,
                'audi_valornuevo -> reporte.empr_id,
                'audi_evento -> "A"
              )
              .executeInsert()
          }

          if (reporte_ant.get.usua_id != reporte.usua_id) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "usua_id",
                'audi_valorantiguo -> reporte_ant.get.usua_id,
                'audi_valornuevo -> reporte.usua_id,
                'audi_evento -> "A"
              )
              .executeInsert()
          }
        }
        //}

        result = true
      // } // Fin del If de repo_reportetecnico
      }
    } catch {
      case e: Exception => {
        println("Guardando Reporte Movil Luminaria Error: " + e.getMessage())
        result = false
      }
    }
    result
  }

  def actualizarMaterialMovil(
      eventos: List[Evento],
      repo_id: Long
  ): Boolean = {
    var result = false
    val reporte_ant: Option[Reporte] = buscarPorId(repo_id)
    println("Actualizando Reporte Material Movil")
    reporte_ant match {
      case Some(reporte) =>
        db.withConnection { implicit connection =>
          val fecha: LocalDate =
            new LocalDate(Calendar.getInstance().getTimeInMillis())
          val hora: LocalDateTime =
            new LocalDateTime(Calendar.getInstance().getTimeInMillis())

          // Proceso de Creación de Luminarias Nuevas por Expansión Tipo III

          for (e <- eventos) {
            if (e.aap_id != None) {
              var elemento: Elemento = null
              var bombillo_retirado = None: Option[String]
              var bombillo_instalado = None: Option[String]
              var balasto_retirado = None: Option[String]
              var balasto_instalado = None: Option[String]
              var arrancador_retirado = None: Option[String]
              var arrancador_instalado = None: Option[String]
              var condensador_retirado = None: Option[String]
              var condensador_instalado = None: Option[String]
              var fotocelda_retirado = None: Option[String]
              var fotocelda_instalado = None: Option[String]

              e.elem_id match {
                case None => None
                case Some(elem_id) =>
                  elemento = elementoService.buscarPorId(elem_id).get
              }
              // Actualizar Evento si ya Existe
              var estado = 0
              e.even_estado match {
                case Some(1) => estado = 2
                case Some(2) => estado = 2
                case Some(8) => estado = 9
                case Some(9) => estado = 9
                case _       => estado = 2
              }
              var eventoActualizado: Boolean = false
              var eventoInsertado: Boolean = false
              eventoActualizado = SQL(
                """UPDATE siap.reporte_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id},
                                                                unit_id = {unit_id}
                                                            WHERE repo_id = {repo_id} and aap_id = {aap_id} and elem_id = {elem_id}
                                                        """
              ).on(
                  'even_fecha -> hora,
                  'elem_id -> e.elem_id,
                  'even_codigo_retirado -> e.even_codigo_retirado,
                  'even_cantidad_retirado -> e.even_cantidad_retirado,
                  'even_codigo_instalado -> e.even_codigo_instalado,
                  'even_cantidad_instalado -> e.even_cantidad_instalado,
                  'usua_id -> reporte.usua_id,
                  'even_estado -> estado,
                  'empr_id -> reporte.empr_id,
                  'repo_id -> reporte.repo_id,
                  'aap_id -> e.aap_id,
                  'unit_id -> e.unit_id
                )
                .executeUpdate() > 0
              if (!eventoActualizado) {
                eventoInsertado = SQL(
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
                                    even_id,
                                    unit_id) VALUES (
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
                                    {even_id},
                                    {unit_id})"""
                ).on(
                    "even_fecha" -> hora,
                    "even_codigo_instalado" -> e.even_codigo_instalado,
                    "even_cantidad_instalado" -> e.even_cantidad_instalado,
                    "even_codigo_retirado" -> e.even_codigo_retirado,
                    "even_cantidad_retirado" -> e.even_cantidad_retirado,
                    "even_estado" -> estado,
                    "aap_id" -> e.aap_id,
                    "repo_id" -> reporte.repo_id,
                    "elem_id" -> e.elem_id,
                    "usua_id" -> e.usua_id,
                    "empr_id" -> reporte.empr_id,
                    "even_id" -> e.even_id,
                    "unit_id" -> e.unit_id
                  )
                  .executeUpdate() > 0
              }
              if ((eventoActualizado || eventoInsertado) && (estado != 9)) {
                // validar elemento y actualizar aap_elemento
                elemento.tiel_id match {
                  case Some(1) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo}, reti_id = {reti_id} , repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate() */
                  /*                   val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo_retirado -> e.even_codigo_retirado,
                      'aap_bombillo_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0 */
                  /*                   if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> e.even_codigo_retirado,
                        'aap_bombillo_instalado -> e.even_codigo_instalado,
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  } */
                  case Some(2) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto_retirado -> e.even_codigo_retirado,
                      'aap_balasto_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> e.even_codigo_retirado,
                        'aap_balasto_instalado -> e.even_codigo_instalado,
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case Some(3) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador_retirado -> e.even_codigo_retirado,
                      'aap_arrancador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> e.even_codigo_retirado,
                        'aap_arrancador_instalado -> e.even_codigo_instalado,
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case Some(4) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador_retirado -> e.even_codigo_retirado,
                      'aap_condensador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> e.even_codigo_retirado,
                        'aap_condensador_instalado -> e.even_codigo_instalado,
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case Some(5) =>
                  /*                   SQL(
                    """UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    //.executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda_retirado -> e.even_codigo_retirado,
                      'aap_fotocelda_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    //.executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> e.even_codigo_retirado,
                        'aap_fotocelda_instalado -> e.even_codigo_instalado,
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      //.executeUpdate()
                  }
                   */
                  case _ => None
                }
              }
            }
          }

          if (reporte_ant != None) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
            ).on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> reporte.usua_id,
                'audi_tabla -> "reporte_evento",
                'audi_uid -> reporte.repo_id,
                'audi_campo -> "evento",
                'audi_valorantiguo -> "",
                'audi_valornuevo -> "",
                'audi_evento -> "A"
              )
              .executeInsert()
          }
          result = true
        }
      case None => None
    }
    result
  }

  /**
    *
    *
    *
    */
  def actualizarElemento(
      reporte: Reporte,
      coau_tipo: Int,
      coau_codigo: String
  ): Boolean = {
    val reporte_ant: Option[Reporte] = buscarPorId(reporte.repo_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())

      SQL("""DELETE FROM siap.reporte_evento WHERE repo_id = {repo_id}""")
        .on('repo_id -> reporte.repo_id)
        .executeUpdate()

      reporte.eventos.map { eventos =>
        for (e <- eventos) {
          if (e.aap_id != None) {
            var elemento: Elemento = null
            var bombillo_retirado = None: Option[String]
            var bombillo_instalado = None: Option[String]
            var balasto_retirado = None: Option[String]
            var balasto_instalado = None: Option[String]
            var arrancador_retirado = None: Option[String]
            var arrancador_instalado = None: Option[String]
            var condensador_retirado = None: Option[String]
            var condensador_instalado = None: Option[String]
            var fotocelda_retirado = None: Option[String]
            var fotocelda_instalado = None: Option[String]

            e.elem_id match {
              case None => None
              case Some(elem_id) =>
                elemento = elementoService.buscarPorId(elem_id).get
            }
            // Actualizar Evento si ya Existe
            var estado = 0
            e.even_estado match {
              case Some(1) => estado = 2
              case Some(2) => estado = 2
              case Some(8) => estado = 9
              case Some(9) => estado = 9
              case _       => estado = 2
            }
            var eventoActualizado: Boolean = false
            var eventoInsertado: Boolean = false
            /*println("empr_id: " + reporte.empr_id)
            println("repo_id: " + reporte.repo_id)
            println("aap_id: " + e.aap_id)
            println("even_id: " + e.even_id)*/
            eventoActualizado = SQL(
              """UPDATE siap.reporte_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                elem_id = {elem_id},
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id},
                                                                unit_id = {unit_id}
                                                            WHERE empr_id = {empr_id} and repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}
                                                        """
            ).on(
                'even_fecha -> hora,
                'elem_id -> e.elem_id,
                'even_codigo_retirado -> e.even_codigo_retirado,
                'even_cantidad_retirado -> e.even_cantidad_retirado,
                'even_codigo_instalado -> e.even_codigo_instalado,
                'even_cantidad_instalado -> e.even_cantidad_instalado,
                'usua_id -> reporte.usua_id,
                'even_estado -> estado,
                'empr_id -> reporte.empr_id,
                'repo_id -> reporte.repo_id,
                'aap_id -> e.aap_id,
                'even_id -> e.even_id,
                'unit_id -> e.unit_id
              )
              .executeUpdate() > 0
            if (!eventoActualizado) {
              eventoInsertado = SQL(
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
                                    even_id,
                                    unit_id) VALUES (
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
                                    {even_id},
                                    {unit_id})"""
              ).on(
                  "even_fecha" -> hora,
                  "even_codigo_instalado" -> e.even_codigo_instalado,
                  "even_cantidad_instalado" -> e.even_cantidad_instalado,
                  "even_codigo_retirado" -> e.even_codigo_retirado,
                  "even_cantidad_retirado" -> e.even_cantidad_retirado,
                  "even_estado" -> estado,
                  "aap_id" -> e.aap_id,
                  "repo_id" -> reporte.repo_id,
                  "elem_id" -> e.elem_id,
                  "usua_id" -> e.usua_id,
                  "empr_id" -> reporte.empr_id,
                  "even_id" -> e.even_id,
                  "unit_id" -> e.unit_id
                )
                .executeUpdate() > 0
            }
            if ((eventoActualizado || eventoInsertado) && (estado != 9)) {
              // validar elemento y actualizar aap_elemento
              elemento.tiel_id match {
                case Some(1) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo}, reti_id = {reti_id} , repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo_retirado -> e.even_codigo_retirado,
                      'aap_bombillo_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                      .on(
                        'aap_bombillo_retirado -> e.even_codigo_retirado,
                        'aap_bombillo_instalado -> e.even_codigo_instalado,
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(2) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto_retirado -> e.even_codigo_retirado,
                      'aap_balasto_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> e.even_codigo_retirado,
                        'aap_balasto_instalado -> e.even_codigo_instalado,
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(3) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador_retirado -> e.even_codigo_retirado,
                      'aap_arrancador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> e.even_codigo_retirado,
                        'aap_arrancador_instalado -> e.even_codigo_instalado,
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(4) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador_retirado -> e.even_codigo_retirado,
                      'aap_condensador_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> e.even_codigo_retirado,
                        'aap_condensador_instalado -> e.even_codigo_instalado,
                        'aap_fotocelda_retirado -> "",
                        'aap_fotocelda_instalado -> "",
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case Some(5) =>
                  SQL(
                    """UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda_retirado -> e.even_codigo_retirado,
                      'aap_fotocelda_instalado -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """)
                      .on(
                        'aap_bombillo_retirado -> "",
                        'aap_bombillo_instalado -> "",
                        'aap_balasto_retirado -> "",
                        'aap_balasto_instalado -> "",
                        'aap_arrancador_retirado -> "",
                        'aap_arrancador_instalado -> "",
                        'aap_condensador_retirado -> "",
                        'aap_condensador_instalado -> "",
                        'aap_fotocelda_retirado -> e.even_codigo_retirado,
                        'aap_fotocelda_instalado -> e.even_codigo_instalado,
                        'aap_id -> e.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case _ => None
              }
            }
          }
        }
      }
    }
    true
  }

  /**
    * Eliminar Reporte
    */
  def borrar(repo_id: scala.Long, usua_id: scala.Long): Boolean = {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: scala.Long =
        SQL("UPDATE siap.reporte SET rees_id = 9 WHERE repo_id = {repo_id}")
          .on(
            'repo_id -> repo_id
          )
          .executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "reporte",
          'audi_uid -> repo_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> "",
          'audi_evento -> "E"
        )
        .executeInsert()

      count > 0
    }
  }

  def actualizarHistoria(empr_id: scala.Long): Future[Boolean] = {
    reportes(empr_id).map { reportes =>
      reportes.foreach { reporte =>
        println(
          "procesando reporte: " + reporte.repo_id + "->" + reporte.reti_id + "->" + reporte.repo_consecutivo
        )
        reporte.direcciones.foreach { direcciones =>
          direcciones.foreach { direccion =>
            db.withConnection { implicit connection =>
              direccion.aap_id match {
                case Some(aap_id) =>
                  val updated: Boolean = SQL(
                    """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto_retirado -> Option
                        .empty[String],
                      'aap_balasto_instalado -> Option
                        .empty[String],
                      'aap_id -> direccion.aap_id,
                      'aael_fecha -> reporte.repo_fechasolucion,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo,
                      'empr_id -> reporte.empr_id
                    )
                    .executeUpdate() > 0
                  if (!updated) {
                    SQL(
                      """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
                    ).on(
                        'aap_bombillo_retirado -> Option
                          .empty[String],
                        'aap_bombillo_instalado -> Option
                          .empty[String],
                        'aap_balasto_retirado -> Option
                          .empty[String],
                        'aap_balasto_instalado -> Option
                          .empty[String],
                        'aap_arrancador_retirado -> Option
                          .empty[String],
                        'aap_arrancador_instalado -> Option
                          .empty[String],
                        'aap_condensador_retirado -> Option
                          .empty[String],
                        'aap_condensador_instalado -> Option
                          .empty[String],
                        'aap_fotocelda_retirado -> Option
                          .empty[String],
                        'aap_fotocelda_instalado -> Option
                          .empty[String],
                        'aap_id -> direccion.aap_id,
                        'aael_fecha -> reporte.repo_fechasolucion,
                        'reti_id -> reporte.reti_id,
                        'repo_consecutivo -> reporte.repo_consecutivo,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  }
                case None => None
              }
            }
          }
        }
        reporte.eventos.foreach { eventos =>
          eventos.foreach { e =>
            println("procesando evento aap_id: " + e.aap_id)
            e.aap_id match {
              case Some(aap_id) =>
                println("procesando aap: " + e.aap_id)
                var elemento: Option[Elemento] = None
                var bombillo_retirado: Option[String] = None
                var bombillo_instalado: Option[String] = None
                var balasto_retirado: Option[String] = None
                var balasto_instalado: Option[String] = None
                var arrancador_retirado: Option[String] = None
                var arrancador_instalado: Option[String] = None
                var condensador_retirado: Option[String] = None
                var condensador_instalado: Option[String] = None
                var fotocelda_retirado: Option[String] = None
                var fotocelda_instalado: Option[String] = None

                elemento = elementoService.buscarPorId(e.elem_id.get)

                elemento match {
                  case Some(elemento) =>
                    // Actualizar Evento si ya Existe
                    var estado = 0
                    estado = e.even_estado match {
                      case Some(1) => 2
                      case Some(2) => 2
                      case Some(8) => 9
                      case Some(9) => 9
                      case _       => 2
                    }
                    if (estado != 9) {
                      // validar elemento y actualizar aap_elemento
                      println("elemento tipo:" + elemento.tiel_id)
                      db.withConnection { implicit connection =>
                        elemento.tiel_id match {
                          case Some(1) =>
                            SQL(
                              """UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                            ).on(
                                'aap_bombillo -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate()
                            val updated: Boolean = SQL(
                              """UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                            ).on(
                                'aap_bombillo_retirado -> e.even_codigo_retirado,
                                'aap_bombillo_instalado -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'aael_fecha -> reporte.repo_fechasolucion,
                                'reti_id -> reporte.reti_id,
                                'repo_consecutivo -> reporte.repo_consecutivo,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate() > 0
                            if (!updated) {
                              SQL("""INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )
                                                """)
                                .on(
                                  'aap_bombillo_retirado -> e.even_codigo_retirado,
                                  'aap_bombillo_instalado -> e.even_codigo_instalado,
                                  'aap_balasto_retirado -> Option.empty[String],
                                  'aap_balasto_instalado -> Option
                                    .empty[String],
                                  'aap_arrancador_retirado -> Option
                                    .empty[String],
                                  'aap_arrancador_instalado -> Option
                                    .empty[String],
                                  'aap_condensador_retirado -> Option
                                    .empty[String],
                                  'aap_condensador_instalado -> Option
                                    .empty[String],
                                  'aap_fotocelda_retirado -> Option
                                    .empty[String],
                                  'aap_fotocelda_instalado -> Option
                                    .empty[String],
                                  'aap_id -> e.aap_id,
                                  'aael_fecha -> reporte.repo_fechasolucion,
                                  'reti_id -> reporte.reti_id,
                                  'repo_consecutivo -> reporte.repo_consecutivo,
                                  'empr_id -> reporte.empr_id
                                )
                                .executeUpdate()
                            }
                          case Some(2) =>
                            SQL(
                              """UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto} where aap_id = {aap_id} and empr_id = {empr_id}"""
                            ).on(
                                'aap_balasto -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate()
                            val updated: Boolean = SQL(
                              """UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                            ).on(
                                'aap_balasto_retirado -> e.even_codigo_retirado,
                                'aap_balasto_instalado -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'aael_fecha -> reporte.repo_fechasolucion,
                                'reti_id -> reporte.reti_id,
                                'repo_consecutivo -> reporte.repo_consecutivo,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate() > 0
                            if (!updated) {
                              SQL(
                                """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
                              ).on(
                                  'aap_bombillo_retirado -> Option
                                    .empty[String],
                                  'aap_bombillo_instalado -> Option
                                    .empty[String],
                                  'aap_balasto_retirado -> e.even_codigo_retirado,
                                  'aap_balasto_instalado -> e.even_codigo_instalado,
                                  'aap_arrancador_retirado -> Option
                                    .empty[String],
                                  'aap_arrancador_instalado -> Option
                                    .empty[String],
                                  'aap_condensador_retirado -> Option
                                    .empty[String],
                                  'aap_condensador_instalado -> Option
                                    .empty[String],
                                  'aap_fotocelda_retirado -> Option
                                    .empty[String],
                                  'aap_fotocelda_instalado -> Option
                                    .empty[String],
                                  'aap_id -> e.aap_id,
                                  'aael_fecha -> reporte.repo_fechasolucion,
                                  'reti_id -> reporte.reti_id,
                                  'repo_consecutivo -> reporte.repo_consecutivo,
                                  'empr_id -> reporte.empr_id
                                )
                                .executeUpdate()
                            }
                          case Some(3) =>
                            SQL(
                              """UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador} where aap_id = {aap_id} and empr_id = {empr_id}"""
                            ).on(
                                'aap_arrancador -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate()
                            val updated: Boolean = SQL(
                              """UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                            ).on(
                                'aap_arrancador_retirado -> e.even_codigo_retirado,
                                'aap_arrancador_instalado -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'aael_fecha -> reporte.repo_fechasolucion,
                                'reti_id -> reporte.reti_id,
                                'repo_consecutivo -> reporte.repo_consecutivo,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate() > 0
                            if (!updated) {
                              SQL(
                                """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
                              ).on(
                                  'aap_bombillo_retirado -> Option
                                    .empty[String],
                                  'aap_bombillo_instalado -> Option
                                    .empty[String],
                                  'aap_balasto_retirado -> Option.empty[String],
                                  'aap_balasto_instalado -> Option
                                    .empty[String],
                                  'aap_arrancador_retirado -> e.even_codigo_retirado,
                                  'aap_arrancador_instalado -> e.even_codigo_instalado,
                                  'aap_condensador_retirado -> Option
                                    .empty[String],
                                  'aap_condensador_instalado -> Option
                                    .empty[String],
                                  'aap_fotocelda_retirado -> Option
                                    .empty[String],
                                  'aap_fotocelda_instalado -> Option
                                    .empty[String],
                                  'aap_id -> e.aap_id,
                                  'aael_fecha -> reporte.repo_fechasolucion,
                                  'reti_id -> reporte.reti_id,
                                  'repo_consecutivo -> reporte.repo_consecutivo,
                                  'empr_id -> reporte.empr_id
                                )
                                .executeUpdate()
                            }
                          case Some(4) =>
                            SQL(
                              """UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador} where aap_id = {aap_id} and empr_id = {empr_id}"""
                            ).on(
                                'aap_condensador -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate()
                            val updated: Boolean = SQL(
                              """UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                            ).on(
                                'aap_condensador_retirado -> e.even_codigo_retirado,
                                'aap_condensador_instalado -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'aael_fecha -> reporte.repo_fechasolucion,
                                'reti_id -> reporte.reti_id,
                                'repo_consecutivo -> reporte.repo_consecutivo,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate() > 0
                            if (!updated) {
                              SQL(
                                """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
                              ).on(
                                  'aap_bombillo_retirado -> Option
                                    .empty[String],
                                  'aap_bombillo_instalado -> Option
                                    .empty[String],
                                  'aap_balasto_retirado -> Option.empty[String],
                                  'aap_balasto_instalado -> Option
                                    .empty[String],
                                  'aap_arrancador_retirado -> Option
                                    .empty[String],
                                  'aap_arrancador_instalado -> Option
                                    .empty[String],
                                  'aap_condensador_retirado -> e.even_codigo_retirado,
                                  'aap_condensador_instalado -> e.even_codigo_instalado,
                                  'aap_fotocelda_retirado -> Option
                                    .empty[String],
                                  'aap_fotocelda_instalado -> Option
                                    .empty[String],
                                  'aap_id -> e.aap_id,
                                  'aael_fecha -> reporte.repo_fechasolucion,
                                  'reti_id -> reporte.reti_id,
                                  'repo_consecutivo -> reporte.repo_consecutivo,
                                  'empr_id -> reporte.empr_id
                                )
                                .executeUpdate()
                            }
                          case Some(5) => {
                            SQL(
                              """UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda} where aap_id = {aap_id} and empr_id = {empr_id}"""
                            ).on(
                                'aap_fotocelda -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate()
                            val updated: Boolean = SQL(
                              """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                            ).on(
                                'aap_fotocelda_retirado -> e.even_codigo_retirado,
                                'aap_fotocelda_instalado -> e.even_codigo_instalado,
                                'aap_id -> e.aap_id,
                                'aael_fecha -> reporte.repo_fechasolucion,
                                'reti_id -> reporte.reti_id,
                                'repo_consecutivo -> reporte.repo_consecutivo,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate() > 0
                            if (!updated) {
                              SQL(
                                """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
                              ).on(
                                  'aap_bombillo_retirado -> Option
                                    .empty[String],
                                  'aap_bombillo_instalado -> Option
                                    .empty[String],
                                  'aap_balasto_retirado -> Option.empty[String],
                                  'aap_balasto_instalado -> Option
                                    .empty[String],
                                  'aap_arrancador_retirado -> Option
                                    .empty[String],
                                  'aap_arrancador_instalado -> Option
                                    .empty[String],
                                  'aap_condensador_retirado -> Option
                                    .empty[String],
                                  'aap_condensador_instalado -> Option
                                    .empty[String],
                                  'aap_fotocelda_retirado -> e.even_codigo_retirado,
                                  'aap_fotocelda_instalado -> e.even_codigo_instalado,
                                  'aap_id -> e.aap_id,
                                  'aael_fecha -> reporte.repo_fechasolucion,
                                  'reti_id -> reporte.reti_id,
                                  'repo_consecutivo -> reporte.repo_consecutivo,
                                  'empr_id -> reporte.empr_id
                                )
                                .executeUpdate()
                            }
                          }
                          case _ => {
                            val updated: Boolean = SQL(
                              """UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and repo_consecutivo = {repo_consecutivo} and empr_id = {empr_id}"""
                            ).on(
                                'aap_fotocelda_retirado -> "",
                                'aap_fotocelda_instalado -> "",
                                'aap_id -> e.aap_id,
                                'aael_fecha -> reporte.repo_fechasolucion,
                                'reti_id -> reporte.reti_id,
                                'repo_consecutivo -> reporte.repo_consecutivo,
                                'empr_id -> reporte.empr_id
                              )
                              .executeUpdate() > 0
                            if (!updated) {
                              SQL(
                                """INSERT INTO siap.aap_elemento_historia (
                                                    aap_id,
                                                    aael_fecha,
                                                    aap_bombillo_retirado,
                                                    aap_bombillo_instalado,
                                                    aap_balasto_retirado,
                                                    aap_balasto_instalado,
                                                    aap_arrancador_retirado,
                                                    aap_arrancador_instalado,
                                                    aap_condensador_retirado,
                                                    aap_condensador_instalado,
                                                    aap_fotocelda_retirado,
                                                    aap_fotocelda_instalado,
                                                    reti_id,
                                                    repo_consecutivo,
                                                    empr_id
                                                    )
                                                    VALUES (
                                                    {aap_id},
                                                    {aael_fecha},
                                                    {aap_bombillo_retirado},
                                                    {aap_bombillo_instalado},
                                                    {aap_balasto_retirado},
                                                    {aap_balasto_instalado},
                                                    {aap_arrancador_retirado},
                                                    {aap_arrancador_instalado},
                                                    {aap_condensador_retirado},
                                                    {aap_condensador_instalado},
                                                    {aap_fotocelda_retirado},
                                                    {aap_fotocelda_instalado},
                                                    {reti_id},
                                                    {repo_consecutivo},
                                                    {empr_id}
                                                    )                                                    
                                                """
                              ).on(
                                  'aap_bombillo_retirado -> Option
                                    .empty[String],
                                  'aap_bombillo_instalado -> Option
                                    .empty[String],
                                  'aap_balasto_retirado -> Option.empty[String],
                                  'aap_balasto_instalado -> Option
                                    .empty[String],
                                  'aap_arrancador_retirado -> Option
                                    .empty[String],
                                  'aap_arrancador_instalado -> Option
                                    .empty[String],
                                  'aap_condensador_retirado -> Option
                                    .empty[String],
                                  'aap_condensador_instalado -> Option
                                    .empty[String],
                                  'aap_fotocelda_retirado -> Option
                                    .empty[String],
                                  'aap_fotocelda_instalado -> Option
                                    .empty[String],
                                  'aap_id -> e.aap_id,
                                  'aael_fecha -> reporte.repo_fechasolucion,
                                  'reti_id -> reporte.reti_id,
                                  'repo_consecutivo -> reporte.repo_consecutivo,
                                  'empr_id -> reporte.empr_id
                                )
                                .executeUpdate()
                            }
                          }
                        }
                      }
                    }
                  case None => println("Elemento en Blanco")
                } // elemento match
              case None => println("evento sin aap_id")
            }
            println("eventos procesados")
          }
        }
      }
      true
    }
  }

  /**
    * estado
    */
  def estados(): Future[Iterable[ReporteEstado]] =
    Future[Iterable[ReporteEstado]] {
      db.withConnection { implicit connection =>
        SQL("""SELECT * FROM siap.reporte_estado WHERE rees_estado < 9""").as(
          ReporteEstado.oEstado *
        )
      }
    }

  /**
    * tipos
    */
  def tipos(): Future[Iterable[ReporteTipo]] = Future[Iterable[ReporteTipo]] {
    db.withConnection { implicit connection =>
      SQL(
        """SELECT * FROM siap.reporte_tipo WHERE reti_estado < 9 ORDER BY reti_id"""
      ).as(ReporteTipo.repoTipoSet *)
    }
  }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def imprimir(repo_id: scala.Long, empr_id: scala.Long): Array[Byte] = {
    var os = Array[Byte]()
    val reporte: Option[Reporte] = buscarPorId(repo_id)
    db.withConnection { implicit connection =>
      empresaService.buscarPorId(empr_id).map { empresa =>
        try {
          var compiledFile = REPORT_DEFINITION_PATH + "siap_reporte.jasper";
          reporte match {
            case Some(r) =>
              if (reporte.get.reti_id.get == 6) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_modernizacion.jasper"
              }
              if (reporte.get.reti_id.get == 2) {
                if (reporte.get.adicional.get.repo_tipo_expansion.get.toInt == 3) {
                  compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_expansion_tipo_III.jasper"
                } else {
                  compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_expansion.jasper"
                }
              } else if (reporte.get.reti_id.get == 8) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_retiro.jasper"
              } else if (reporte.get.reti_id.get == 3) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_reubicacion.jasper"
              } else if (reporte.get.reti_id.get == 9) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_cambio_medida.jasper"
              }
            case None => None
          }
          var reportParams = new HashMap[String, java.lang.Object]()
          reportParams.put("REPO_ID", new java.lang.Long(repo_id.longValue()))
          reportParams.put("EMPRESA", empresa.empr_descripcion)
          reportParams.put("DIRECCION", empresa.empr_direccion)
          reportParams.put("CONCESION", empresa.empr_concesion.get)
          os = JasperRunManager
            .runReportToPdf(compiledFile, reportParams, connection)
        } catch {
          case e: Exception => e.printStackTrace();
        }
      }
    }
    os
  }

  /**
    *  imprimir Formato en Blanco
    * @param reti_id: scala.Long
    * @return OutputStream
    */
  def formato(reti_id: scala.Long, empr_id: scala.Long): Array[Byte] = {
    var os = Array[Byte]()
    db.withConnection { implicit connection =>
      val tipoReporte: ReporteTipo = SQL(
        """SELECT * FROM siap.reporte_tipo WHERE reti_id = {reti_id}"""
      ).on('reti_id -> reti_id).as(ReporteTipo.repoTipoSet.single)
      empresaService.buscarPorId(empr_id).map { empresa =>
        try {
          var compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_blanco.jasper";
          if (reti_id == 2 || reti_id == 6) {
            compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_expansion_blanco.jasper";
          } else if (reti_id == 8) {
            compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_retiro.jasper";
          } else if (reti_id == 3) {
            compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_reubicacion.jasper";
          }
          var reportParams = new HashMap[String, java.lang.Object]()
          reportParams.put("EMPRESA", empresa.empr_descripcion)
          reportParams.put("DIRECCION", empresa.empr_direccion)
          reportParams.put("CONCESION", empresa.empr_concesion.get)
          reportParams.put("reti_descripcion", tipoReporte.reti_descripcion)
          os = JasperRunManager
            .runReportToPdf(compiledFile, reportParams, connection)
        } catch {
          case e: Exception => e.printStackTrace();
        }
      }
    }
    os
  }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def imprimirRelacion(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: scala.Long,
      usua_id: scala.Long,
      formato: String
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 999)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      val format = new SimpleDateFormat("yyyy-MM-dd")
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_relacion.jasper"

      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      reportParams.put("EMPR_ID", new java.lang.Long(empr_id.longValue()))
      formato match {
        case "pdf" =>
          os = JasperRunManager.runReportToPdf(
            compiledFile,
            reportParams,
            connection
          )
        case "xls" => {
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 9))
            _listMerged += CellRange((1, 1), (0, 9))
            _listMerged += CellRange((2, 2), (0, 9))
            _listMerged += CellRange((3, 3), (0, 9))
            _listMerged.toList
          }
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var columnstyle = {
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 0, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 1, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 2, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 3, width = new Width(50, WidthUnit.Character))
            _listColumn.toList
          }
          val sheet1 = Sheet(
            name = "HojaT",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONCESIÓN ALUMBRADO PÚBLICO")
              val title2Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(empresa.empr_descripcion)
              val title3Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONTROL ATENCIÓN A USUARIOS")
              val title4Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("OPERACION Y MANTENIMIENTO PENDIENTE")
              val headerRow = com.norbitltd.spoiwo.model
                .Row(style = CellStyle(font = Font(bold = true)))
                .withCellValues(
                  "Número de Reporte",
                  "Tipo de Reporte",
                  "Tipo de Elemento",
                  "Fecha Recepción",
                  "Fecha Solución",
                  "Persona/Entidad Usuario",
                  "Dirección",
                  "Barrio",
                  "Teléfono",
                  "Medio",
                  "Daño Reportado",
                  "Cuadrilla",
                  "Pendiente"
                )
              var j = 2
              var query =
                """
                	select r.* from 
				(select distinct on (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'LUMINARIA' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion,
        case 
						when (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE THEN 'X'
						else ''
					end as estado 
				           from siap.reporte r 
                        left join siap.reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id and ot.ortr_fecha = {fecha_final} and ot.otes_id < 8
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fecharecepcion between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE and r.empr_id = {empr_id}
                   union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'CONTROL' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion,
                   case 
						        when (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE THEN 'X'
						        else ''
					         end as estado 
                   from siap.control_reporte r 
                        left join siap.control_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id and ot.ortr_fecha = {fecha_final} and ot.otes_id < 8
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fecharecepcion between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE and r.empr_id = {empr_id}
				           union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'TRANSFORMADOR' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion,
                   case 
						        when (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE THEN 'X'
						        else ''
					         end as estado 
                   from siap.transformador_reporte r 
                        left join siap.transformador_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id and ot.ortr_fecha = {fecha_final} and ot.otes_id < 8
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fecharecepcion between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE and r.empr_id = {empr_id}
				           union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'TRANSFORMADOR' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion,
                   case 
						        when (coalesce(r.repo_reportetecnico, '') = '') IS NOT FALSE THEN 'X'
						        else ''
					         end as estado 
                   from siap.medidor_reporte r 
                        left join siap.medidor_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id and ot.ortr_fecha = {fecha_final} and ot.otes_id < 8
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fecharecepcion between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE and r.empr_id = {empr_id}
                   union all
                   select DISTINCT ON (r.obra_consecutivo) r.obra_consecutivo, r.obra_fecharecepcion, r.obra_direccion, r.barr_id, r.obra_telefono, r.obra_nombre, 'OBRA' as tipo_inventario, o.orig_descripcion, 'OBRA' as reti_descripcion, r.obra_descripcion as acti_descripcion, b.barr_descripcion, ((r.obra_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.obra_fecharecepcion and (r.obra_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion,
                   case 
						        when (coalesce(r.obra_reportetecnico, '') = '') IS NOT FALSE THEN 'X'
						        else ''
					         end as estado 
                   from siap.obra r 
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_obra otr on otr.obra_id = r.obra_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id and ot.ortr_fecha = {fecha_final} and ot.otes_id < 8
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.obra_fecharecepcion between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) and (coalesce(trim(r.obra_reportetecnico), '') = '') IS NOT FALSE and r.empr_id = {empr_id}
                    ) r
                  ORDER BY r.reti_descripcion ASC, r.repo_consecutivo ASC 
              """
              val resultSet =
                SQL(query)
                  .on(
                    'fecha_inicial -> new DateTime(fi.getTimeInMillis),
                    'fecha_final -> new DateTime(ff.getTimeInMillis),
                    'empr_id -> empr_id
                  )
                  .as(Pendiente._set *)
              val rows = resultSet.map {
                i =>
                  j += 1
                  com.norbitltd.spoiwo.model.Row(
                    NumericCell(
                      i.repo_consecutivo match {
                        case Some(value) => value
                        case None        => 0
                      },
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.reti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.tire_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.repo_fecharecepcion match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(3),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.fecha_limite match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(4),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_nombre match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_direccion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.barr_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(7),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_telefono match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(8),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.orig_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(9),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.acti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(10),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.cuad_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(11),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.estado match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(12),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }

              title1Row :: title2Row :: title3Row :: title4Row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var sos: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1).writeToOutputStream(sos)
          os = sos.toByteArray
        }
      }

      os
    }
  }

  def imprimirRelacionFiltrado(
      cuad_id: Long,
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: scala.Long,
      usua_id: scala.Long,
      formato: String
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 999)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      val format = new SimpleDateFormat("yyyy-MM-dd")
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_ordentrabajo_cuadrilla_reporte_relacion.jasper"

      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      reportParams.put("EMPR_ID", new java.lang.Long(empr_id.longValue()))
      reportParams.put("CUAD_ID", new java.lang.Long(cuad_id.longValue()))
      formato match {
        case "pdf" =>
          os = JasperRunManager.runReportToPdf(
            compiledFile,
            reportParams,
            connection
          )
        case "xls" => {
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 9))
            _listMerged += CellRange((1, 1), (0, 9))
            _listMerged += CellRange((2, 2), (0, 9))
            _listMerged += CellRange((3, 3), (0, 9))
            _listMerged.toList
          }
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var columnstyle = {
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 0, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 1, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 2, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 3, width = new Width(50, WidthUnit.Character))
            _listColumn.toList
          }
          val sheet1 = Sheet(
            name = "HojaT",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONCESIÓN ALUMBRADO PÚBLICO")
              val title2Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(empresa.empr_descripcion)
              val title3Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONTROL ATENCIÓN A USUARIOS")
              val title4Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("OPERACION Y MANTENIMIENTO PENDIENTE")
              val headerRow = com.norbitltd.spoiwo.model
                .Row(style = CellStyle(font = Font(bold = true)))
                .withCellValues(
                  "Número de Reporte",
                  "Tipo de Reporte",
                  "Tipo de Elemento",
                  "Fecha Recepción",
                  "Fecha Solución",
                  "Persona/Entidad Usuario",
                  "Dirección",
                  "Barrio",
                  "Teléfono",
                  "Medio",
                  "Daño Reportado",
                  "Cuadrilla"
                )
              var j = 2
              var query =
                """
                	select r.* from 
				(select distinct on (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'LUMINARIA' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion 
				           from siap.reporte r 
                        left join siap.reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where ot.ortr_fecha between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) /* and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE */ and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
                   union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'CONTROL' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion 
                   from siap.control_reporte r 
                        left join siap.control_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where ot.ortr_fecha between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) /* and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE */ and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
				           union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'TRANSFORMADOR' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion 
                   from siap.transformador_reporte r 
                        left join siap.transformador_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where ot.ortr_fecha between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) /* and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE */ and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
				           union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'MEDIDOR' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion 
                   from siap.medidor_reporte r 
                        left join siap.medidor_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where ot.ortr_fecha between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) /* and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT FALSE */ and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
                   union all
                   select DISTINCT ON (r.obra_consecutivo) r.obra_consecutivo, r.obra_fecharecepcion, r.obra_direccion, r.barr_id, r.obra_telefono, r.obra_nombre, 'OBRA' as tipo_inventario, o.orig_descripcion, 'OBRA' as reti_descripcion, r.obra_descripcion as acti_descripcion, b.barr_descripcion, ((r.obra_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.obra_fecharecepcion and (r.obra_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion 
                   from siap.obra r 
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_obra otr on otr.obra_id = r.obra_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where ot.ortr_fecha between {fecha_inicial} and {fecha_final} and r.rees_id in (1,2) /* and (coalesce(trim(r.obra_reportetecnico), '') = '') IS NOT FALSE */ and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
                    ) r
                  ORDER BY r.reti_descripcion ASC, r.repo_consecutivo ASC              
              """
              val resultSet =
                SQL(query)
                  .on(
                    'fecha_inicial -> new DateTime(fi.getTimeInMillis),
                    'fecha_final -> new DateTime(ff.getTimeInMillis),
                    'empr_id -> empr_id,
                    'cuad_id -> cuad_id
                  )
                  .as(Pendiente._set *)
              val rows = resultSet.map {
                i =>
                  j += 1
                  com.norbitltd.spoiwo.model.Row(
                    NumericCell(
                      i.repo_consecutivo match {
                        case Some(value) => value
                        case None        => 0
                      },
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.reti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.tire_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.repo_fecharecepcion match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(3),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.fecha_limite match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(4),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_nombre match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_direccion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.barr_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(7),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_telefono match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(8),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.orig_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(9),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.acti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(10),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.cuad_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(11),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }

              title1Row :: title2Row :: title3Row :: title4Row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var sos: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1).writeToOutputStream(sos)
          os = sos.toByteArray
        }
      }

      os
    }
  }

  def imprimirEjecutado(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: scala.Long,
      usua_id: scala.Long,
      formato: String
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 999)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      val format = new SimpleDateFormat("yyyy-MM-dd")
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_ejecutado.jasper"

      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      reportParams.put("EMPR_ID", new java.lang.Long(empr_id.longValue()))
      formato match {
        case "pdf" =>
          os = JasperRunManager.runReportToPdf(
            compiledFile,
            reportParams,
            connection
          )
        case "xls" => {
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 9))
            _listMerged += CellRange((1, 1), (0, 9))
            _listMerged += CellRange((2, 2), (0, 9))
            _listMerged += CellRange((3, 3), (0, 9))
            _listMerged.toList
          }
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var columnstyle = {
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 0, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 1, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 2, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 3, width = new Width(50, WidthUnit.Character))
            _listColumn.toList
          }
          val sheet1 = Sheet(
            name = "HojaT",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONCESIÓN ALUMBRADO PÚBLICO")
              val title2Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(empresa.empr_descripcion)
              val title3Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONTROL ATENCIÓN A USUARIOS")
              val title4Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(
                  "OPERACION Y MANTENIMIENTO EJECUTADO" /*  - " + (tipo match {
                    case "1" => "LUMINARIA"
                    case "2" => "CONTROL"
                    case "3" => "CANALIZACION"
                    case "4" => "POSTES"
                    case "5" => "REDES"
                    case "6" => "TRANSFORMADOR"
                    case "7" => "MEDIDOR"
                  }) */
                )
              val headerRow = com.norbitltd.spoiwo.model
                .Row(style = CellStyle(font = Font(bold = true)))
                .withCellValues(
                  "Número de Reporte",
                  "Tipo de Reporte",
                  "Tipo de Elemento",
                  "Fecha Recepción",
                  "Fecha Solución",
                  "Persona/Entidad Usuario",
                  "Dirección",
                  "Barrio",
                  "Teléfono",
                  "Medio",
                  "Daño Reportado",
                  "Cuadrilla"
                )
              var j = 2
              var query =
                """
                	select r.* from 
				(select r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'LUMINARIA' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, r.repo_fechasolucion as fecha_limite,  c.cuad_descripcion 
				           from siap.reporte r 
                        left join siap.reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id}
                   union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'CONTROL' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, r.repo_fechasolucion as fecha_limite,  c.cuad_descripcion 
                   from siap.control_reporte r 
                        left join siap.control_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id}
				           union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'TRANSFORMADOR' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, r.repo_fechasolucion  as fecha_limite,  c.cuad_descripcion 
                   from siap.transformador_reporte r 
                        left join siap.transformador_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id}
                   union all
                   select DISTINCT ON (r.obra_consecutivo) r.obra_consecutivo, r.obra_fecharecepcion, r.obra_direccion, r.barr_id, r.obra_telefono, r.obra_nombre, 'OBRA' as tipo_inventario, o.orig_descripcion, 'OBRA' as reti_descripcion, r.obra_descripcion as acti_descripcion, b.barr_descripcion, r.obra_fechasolucion as repo_fechasolucion,  c.cuad_descripcion 
                   from siap.obra r 
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_obra otr on otr.obra_id = r.obra_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.obra_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.obra_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id}				 	                                                
                    ) r
                  ORDER BY r.reti_descripcion ASC, r.repo_consecutivo ASC              
              """
              val resultSet =
                SQL(query)
                  .on(
                    'fecha_inicial -> new DateTime(fi.getTimeInMillis),
                    'fecha_final -> new DateTime(ff.getTimeInMillis),
                    'empr_id -> empr_id
                  )
                  .as(Pendiente._set *)
              val rows = resultSet.map {
                i =>
                  j += 1
                  com.norbitltd.spoiwo.model.Row(
                    NumericCell(
                      i.repo_consecutivo match {
                        case Some(value) => value
                        case None        => 0
                      },
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.reti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.tire_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.repo_fecharecepcion match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(3),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.fecha_limite match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(4),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_nombre match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_direccion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.barr_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(7),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_telefono match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(8),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.orig_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(9),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.acti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(10),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.cuad_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(11),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }

              title1Row :: title2Row :: title3Row :: title4Row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var sos: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1).writeToOutputStream(sos)
          os = sos.toByteArray
        }
      }

      os
    }
  }

  def imprimirEjecutadoFiltrado(
      cuad_id: Long,
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: scala.Long,
      usua_id: scala.Long,
      formato: String
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    val cuadrilla = cuadrillaService.buscarPorId(cuad_id).get
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 999)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      val format = new SimpleDateFormat("yyyy-MM-dd")
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_ejecutado.jasper"

      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      reportParams.put("EMPR_ID", new java.lang.Long(empr_id.longValue()))
      formato match {
        case "pdf" =>
          os = JasperRunManager.runReportToPdf(
            compiledFile,
            reportParams,
            connection
          )
        case "xls" => {
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 9))
            _listMerged += CellRange((1, 1), (0, 9))
            _listMerged += CellRange((2, 2), (0, 9))
            _listMerged += CellRange((3, 3), (0, 9))
            _listMerged += CellRange((4, 4), (0, 9))
            _listMerged.toList
          }
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var columnstyle = {
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 0, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 1, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 2, width = new Width(50, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 3, width = new Width(50, WidthUnit.Character))
            _listColumn.toList
          }
          val sheet1 = Sheet(
            name = "HojaT",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONCESIÓN ALUMBRADO PÚBLICO")
              val title2Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(empresa.empr_descripcion)
              val title3Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues("CONTROL ATENCIÓN A USUARIOS")
              val title4Row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(
                  "OPERACION Y MANTENIMIENTO EJECUTADO" /*  - " + (tipo match {
                    case "1" => "LUMINARIA"
                    case "2" => "CONTROL"
                    case "3" => "CANALIZACION"
                    case "4" => "POSTES"
                    case "5" => "REDES"
                    case "6" => "TRANSFORMADOR"
                    case "7" => "MEDIDOR"
                  }) */
                )
              val title5row = com.norbitltd.spoiwo.model
                .Row(
                  style = CellStyle(
                    font = Font(bold = true),
                    horizontalAlignment = HA.Center
                  )
                )
                .withCellValues(cuadrilla.cuad_descripcion)
              val headerRow = com.norbitltd.spoiwo.model
                .Row(style = CellStyle(font = Font(bold = true)))
                .withCellValues(
                  "Número de Reporte",
                  "Tipo de Reporte",
                  "Tipo de Elemento",
                  "Fecha Recepción",
                  "Fecha Solución",
                  "Persona/Entidad Usuario",
                  "Dirección",
                  "Barrio",
                  "Teléfono",
                  "Medio",
                  "Daño Reportado",
                  "Cuadrilla"
                )
              var j = 2
              var query =
                """
                	select r.* from 
				          (select r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'LUMINARIA' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, r.repo_fechasolucion as fecha_limite,  c.cuad_descripcion 
				           from siap.reporte r 
                        left join siap.reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
                   union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'CONTROL' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, r.repo_fechasolucion as fecha_limite,  c.cuad_descripcion 
                   from siap.control_reporte r 
                        left join siap.control_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
				           union all
                   select DISTINCT ON (r.repo_consecutivo) r.repo_consecutivo, r.repo_fecharecepcion, r.repo_direccion, r.barr_id, r.repo_telefono, r.repo_nombre, 'TRANSFORMADOR' as tire_descripcion, o.orig_descripcion, rt.reti_descripcion, t.acti_descripcion, b.barr_descripcion, r.repo_fechasolucion  as fecha_limite,  c.cuad_descripcion 
                   from siap.transformador_reporte r 
                        left join siap.transformador_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.tireuc_id = r.tireuc_id and otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.repo_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id} and c.cuad_id = {cuad_id}
                   union all
                   select DISTINCT ON (r.obra_consecutivo) r.obra_consecutivo, r.obra_fecharecepcion, r.obra_direccion, r.barr_id, r.obra_telefono, r.obra_nombre, 'OBRA' as tipo_inventario, o.orig_descripcion, 'OBRA' as reti_descripcion, r.obra_descripcion as acti_descripcion, b.barr_descripcion, r.obra_fechasolucion as repo_fechasolucion,  c.cuad_descripcion 
                   from siap.obra r 
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_obra otr on otr.obra_id = r.obra_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.obra_fechasolucion between {fecha_inicial} and {fecha_final} and r.rees_id in (2,3) and (coalesce(trim(r.obra_reportetecnico), '') = '') IS NOT TRUE  and r.empr_id = {empr_id}	and c.cuad_id = {cuad_id}			 	                                                
                    ) r
                  ORDER BY r.reti_descripcion ASC, r.repo_consecutivo ASC            
              """
              val resultSet =
                SQL(query)
                  .on(
                    'fecha_inicial -> new DateTime(fi.getTimeInMillis),
                    'fecha_final -> new DateTime(ff.getTimeInMillis),
                    'empr_id -> empr_id,
                    'cuad_id -> cuad_id
                  )
                  .as(Pendiente._set *)
              val rows = resultSet.map {
                i =>
                  j += 1
                  com.norbitltd.spoiwo.model.Row(
                    NumericCell(
                      i.repo_consecutivo match {
                        case Some(value) => value
                        case None        => 0
                      },
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.reti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.tire_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.repo_fecharecepcion match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(3),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.fecha_limite match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(4),
                      style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("YYYY-MM-DD HH:mm")
                        )
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_nombre match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_direccion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.barr_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(7),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_telefono match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(8),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.orig_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(9),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.acti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(10),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.cuad_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(11),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }

              title1Row :: title2Row :: title3Row :: title4Row :: title5row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var sos: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1).writeToOutputStream(sos)
          os = sos.toByteArray
        }
      }

      os
    }
  }

  def consecutivo(reti_id: scala.Long): scala.Long = {
    db.withConnection { implicit connection =>
      val count: scala.Long = SQL(
        "UPDATE siap.reporte_tipo SET reti_consecutivo = reti_consecutivo + 1 WHERE reti_id = {reti_id}"
      ).on('reti_id -> reti_id).executeUpdate()
      if (count > 0) {
        val consec = SQL(
          "SELECT reti_consecutivo FROM siap.reporte_tipo WHERE reti_id = {reti_id}"
        ).on(
            'reti_id -> reti_id
          )
          .as(SqlParser.scalar[scala.Long].single)
        consec
      } else {
        0
      }
    }
  }

  def reporteSinOrdenTrabajo(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: scala.Long
  ): Future[List[
    (
        Option[String],
        Option[String],
        Option[Int],
        Option[DateTime],
        Option[DateTime],
        Option[Int],
        Option[Int]
    )
  ]] = Future {
    var fi = Calendar.getInstance()
    var ff = Calendar.getInstance()
    fi.setTimeInMillis(fecha_inicial)
    ff.setTimeInMillis(fecha_final)
    fi.set(Calendar.MILLISECOND, 0)
    fi.set(Calendar.SECOND, 0)
    fi.set(Calendar.MINUTE, 0)
    fi.set(Calendar.HOUR, 0)

    ff.set(Calendar.MILLISECOND, 999)
    ff.set(Calendar.SECOND, 59)
    ff.set(Calendar.MINUTE, 59)
    ff.set(Calendar.HOUR, 23)
    val _parser =
      get[Option[String]]("tipo_operacion") ~
        get[Option[String]]("reti_descripcion") ~
        get[Option[Int]]("repo_consecutivo") ~
        get[Option[DateTime]]("repo_fecharecepcion") ~
        get[Option[DateTime]]("repo_fechasolucion") ~
        get[Option[Int]]("ortr_consecutivo") ~
        get[Option[Int]]("operaciones") map {
        case a1 ~
              b1 ~
              c1 ~
              d1 ~
              e1 ~
              f1 ~
              g1 =>
          (a1, b1, c1, d1, e1, f1, g1)
      }
    db.withConnection { implicit connection =>
      SQL(
        """select * from (SELECT r1.repo_id, 'LUMINARIA' AS tipo_operacion, rt1.reti_descripcion, r1.repo_consecutivo, r1.repo_fecharecepcion, r1.repo_fechasolucion, 
                      (select o1.ortr_consecutivo from siap.ordentrabajo_reporte or2 inner join siap.ordentrabajo o1 on o1.ortr_id = or2.ortr_id where or2.repo_id = r1.repo_id and or2.tireuc_id = 1
                      order by o1.ortr_consecutivo limit 1) as ortr_consecutivo,
                      (SELECT COUNT(*) 
                        FROM siap.reporte_direccion rd1 
                        WHERE rd1.repo_id = r1.repo_id) AS operaciones
                FROM siap.reporte r1
                INNER JOIN siap.reporte_tipo rt1 ON rt1.reti_id = r1.reti_id
               WHERE r1.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final}
                      AND r1.empr_id = {empr_id} and r1.rees_id < 8
union all
SELECT r1.repo_id, 'CONTROL' AS tipo_operacion, rt1.reti_descripcion, r1.repo_consecutivo, r1.repo_fecharecepcion, r1.repo_fechasolucion, 
                      (select o1.ortr_consecutivo from siap.ordentrabajo_reporte or2 inner join siap.ordentrabajo o1 on o1.ortr_id = or2.ortr_id where or2.repo_id = r1.repo_id and or2.tireuc_id = 2
                      order by o1.ortr_consecutivo limit 1) as ortr_consecutivo,
                      (SELECT COUNT(*) 
                        FROM siap.reporte_direccion rd1 
                        WHERE rd1.repo_id = r1.repo_id) AS operaciones
                FROM siap.control_reporte r1
                INNER JOIN siap.reporte_tipo rt1 ON rt1.reti_id = r1.reti_id
               WHERE r1.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final}
                      AND r1.empr_id = {empr_id} and r1.rees_id < 8
union all 
SELECT r1.repo_id, 'TRANSFORMADOR' AS tipo_operacion, rt1.reti_descripcion, r1.repo_consecutivo, r1.repo_fecharecepcion, r1.repo_fechasolucion, 
                      (select o1.ortr_consecutivo from siap.ordentrabajo_reporte or2 inner join siap.ordentrabajo o1 on o1.ortr_id = or2.ortr_id where or2.repo_id = r1.repo_id and or2.tireuc_id = 3
                      order by o1.ortr_consecutivo limit 1) as ortr_consecutivo,
                      (SELECT COUNT(*) 
                        FROM siap.reporte_direccion rd1 
                        WHERE rd1.repo_id = r1.repo_id) AS operaciones
                FROM siap.transformador_reporte r1
                INNER JOIN siap.reporte_tipo rt1 ON rt1.reti_id = r1.reti_id
               WHERE r1.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final}
                      AND r1.empr_id = {empr_id} and r1.rees_id < 8
union all
SELECT r1.repo_id, 'MEDIDOR' AS tipo_operacion, rt1.reti_descripcion, r1.repo_consecutivo, r1.repo_fecharecepcion, r1.repo_fechasolucion, 
                      (select o1.ortr_consecutivo from siap.ordentrabajo_reporte or2 inner join siap.ordentrabajo o1 on o1.ortr_id = or2.ortr_id where or2.repo_id = r1.repo_id and or2.tireuc_id = 4
                      order by o1.ortr_consecutivo limit 1) as ortr_consecutivo,
                      (SELECT COUNT(*) 
                        FROM siap.reporte_direccion rd1 
                        WHERE rd1.repo_id = r1.repo_id) AS operaciones
                FROM siap.medidor_reporte r1
                INNER JOIN siap.reporte_tipo rt1 ON rt1.reti_id = r1.reti_id
               WHERE r1.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final}
                      AND r1.empr_id = {empr_id} and r1.rees_id < 8
) as o
where o.ortr_consecutivo is null
order by o.repo_consecutivo"""
      ).on(
          'fecha_inicial -> new DateTime(fi.getTimeInMillis),
          'fecha_final -> new DateTime(ff.getTimeInMillis),
          'empr_id -> empr_id
        )
        .as(_parser.*)
    }
  }

  def getReportesParaCierreDirecto(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long
  ): Future[
    List[(Int, Int, Int, String, Int, java.util.Date, String, String)]
  ] =
    Future {
      val resultSet = db.withConnection { implicit connection =>
        val _parser = int("tireuc_id") ~ int("repo_id") ~ int("reti_id") ~
          str("reti_descripcion") ~ int("repo_consecutivo") ~ date(
          "repo_fecharecepcion"
        ) ~
          str("repo_fechasolucion") ~ str("orig_descripcion") map {
          case tireuc_id ~ repo_id ~ reti_id ~ reti_descripcion ~ repo_consecutivo ~ repo_fecharecepcion ~ repo_fechasolucion ~ orig_descripcion =>
            (
              tireuc_id,
              repo_id,
              reti_id,
              reti_descripcion,
              repo_consecutivo,
              repo_fecharecepcion,
              repo_fechasolucion,
              orig_descripcion
            )
        }
        val _query =
          """select * from (
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar),
  o1.orig_descripcion
from siap.reporte r1
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id and (rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null)
left join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and (rdd1.aap_potencia = rdd1.aap_potencia_anterior or rdd1.aap_potencia_anterior is null) and
	(rdd1.aap_tecnologia = rdd1.aap_tecnologia_anterior or rdd1.aap_tecnologia_anterior is null)
inner join siap.origen o1 on o1.orig_id = r1.orig_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id <> 2 and r1.rees_id = 2	
  and r1.repo_reportetecnico > '' and r1.repo_horainicio > '' and r1.repo_horafin > ''
union all 
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar),
  o1.orig_descripcion
from siap.reporte r1
inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id 
inner join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
inner join siap.reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
inner join siap.origen o1 on o1.orig_id = r1.orig_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior 
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
left join siap.aap_conexion ac1 on ac1.aaco_id = rdd1.aaco_id_anterior
left join siap.aap_conexion ac2 on ac2.aaco_id = rdd1.aaco_id
left join siap.aap_cuentaap acu1 on acu1.aacu_id = rdda1.aacu_id_anterior 
left join siap.aap_cuentaap acu2 on acu2.aacu_id = rdda1.aacu_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id = 2 and r1.rees_id = 2 and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null) and
	(rdd1.aaco_id = rdd1.aaco_id_anterior or rdd1.aaco_id_anterior is null) and
	(rdd1.aap_tecnologia <> rdd1.aap_tecnologia_anterior or rdd1.aap_tecnologia_anterior is null) and
	(rdd1.aap_potencia = rdd1.aap_potencia_anterior or rdd1.aap_potencia_anterior is null)
  and r1.repo_reportetecnico > '' and r1.repo_horainicio > '' and r1.repo_horafin > ''
union all
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar),
  o1.orig_descripcion
from siap.control_reporte r1
inner join siap.control_reporte_direccion rd1 on rd1.repo_id = r1.repo_id 
left join siap.control_reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
left join siap.control_reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
inner join siap.origen o1 on o1.orig_id = r1.orig_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior 
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.rees_id = 2 and
	((rd1.even_direccion = rd1.even_direccion_anterior or rd1.even_direccion_anterior is null or rd1.even_direccion_anterior = '') and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null)
	)
  and r1.repo_reportetecnico > '' and r1.repo_horainicio > '' and r1.repo_horafin > ''
union all 
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar),
  o1.orig_descripcion
from siap.transformador_reporte r1
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
inner join siap.transformador_reporte_direccion rd1 on rd1.repo_id = r1.repo_id 
left join siap.transformador_reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
left join siap.transformador_reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
inner join siap.origen o1 on o1.orig_id = r1.orig_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior 
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.rees_id = 2 and
	((rd1.even_direccion = rd1.even_direccion_anterior or rd1.even_direccion_anterior is null or rd1.even_direccion_anterior = '') and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null)
	)
  and r1.repo_reportetecnico > '' and r1.repo_horainicio > '' and r1.repo_horafin > ''
) o
order by o.reti_descripcion, o.repo_consecutivo
      """
        /* """
select * from (
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar)
from
  siap.reporte r1
  left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
  left join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
where
  r1.rees_id in (1,2) and (coalesce(r1.repo_reportetecnico, '') = '') IS FALSE
group by 1,2,3,4,5,6,7
having count(rd1.*) < 1
union all
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar)
from siap.reporte r1
inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
inner join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
inner join siap.reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
left join siap.aap_conexion ac1 on ac1.aaco_id = rdd1.aaco_id_anterior
left join siap.aap_conexion ac2 on ac2.aaco_id = rdd1.aaco_id
left join siap.aap_tipo_carcasa atcrdd1 on atcrdd1.aatc_id = rdd1.aatc_id_anterior
left join siap.aap_tipo_carcasa atcrdd2 on atcrdd2.aatc_id = rdd1.aatc_id
left join siap.aap_marca amardd1 on amardd1.aama_id = rdd1.aama_id_anterior
left join siap.aap_marca amardd2 on amardd2.aama_id = rdd1.aama_id
left join siap.aap_modelo amordd1 on amordd1.aamo_id = rdd1.aamo_id_anterior
left join siap.aap_modelo amordd2 on amordd2.aamo_id = rdd1.aamo_id
left join siap.aap_cuentaap acu1 on acu1.aacu_id = rdda1.aacu_id_anterior
left join siap.aap_cuentaap acu2 on acu2.aacu_id = rdda1.aacu_id
left join siap.aap_uso aus1 on aus1.aaus_id = rdda1.aaus_id_anterior
left join siap.aap_uso aus2 on aus2.aaus_id = rdda1.aaus_id
left join siap.medidor m1 on m1.medi_id = rdda1.medi_id_anterior
left join siap.medidor m2 on m2.medi_id = rdda1.medi_id
left join siap.transformador t1 on t1.aap_id = rdda1.tran_id_anterior
left join siap.transformador t2 on t2.aap_id = rdda1.tran_id
left join siap.tipo_poste tp1 on tp1.tipo_id = rdd1.tipo_id_anterior
left join siap.tipo_poste tp2 on tp2.tipo_id = rdd1.tipo_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id = 1 and r1.rees_id = 2 and
	((rd1.even_direccion = rd1.even_direccion_anterior or rd1.even_direccion_anterior is null or rd1.even_direccion_anterior = '') and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null) and
	(rdda1.aap_apoyo = rdda1.aap_apoyo_anterior or rdda1.aap_apoyo_anterior is null or rdda1.aap_apoyo_anterior = '') and
	(rdd1.aaco_id = rdd1.aaco_id_anterior or rdd1.aaco_id_anterior is null) and
	(rdd1.aatc_id = rdd1.aatc_id_anterior or rdd1.aatc_id_anterior is null) and
	(rdd1.aama_id = rdd1.aama_id_anterior or rdd1.aama_id_anterior is null) and
	(rdd1.aamo_id = rdd1.aamo_id_anterior or rdd1.aamo_id_anterior is null) and
	(rdd1.aap_potencia = rdd1.aap_potencia_anterior or rdd1.aap_potencia_anterior is null) and
	(rdd1.aap_tecnologia = rdd1.aap_tecnologia_anterior or rdd1.aap_tecnologia_anterior is null) and
	(rdd1.aap_brazo = rdd1.aap_brazo_anterior or rdd1.aap_brazo_anterior is null) and
	(rdd1.aap_collarin = rdd1.aap_collarin_anterior or rdd1.aap_collarin_anterior is null) and
	(rdd1.aap_poste_altura = rdd1.aap_poste_altura_anterior or rdd1.aap_poste_altura_anterior is null) and
	(rdd1.aap_poste_propietario = rdd1.aap_poste_propietario_anterior or rdd1.aap_poste_propietario_anterior is null) and
	(rdda1.aacu_id = rdda1.aacu_id_anterior or rdda1.aacu_id_anterior is null) and
	(rdda1.aaus_id = rdda1.aaus_id_anterior or rdda1.aaus_id_anterior is null) and
	(rdda1.medi_id = rdda1.medi_id_anterior or rdda1.medi_id_anterior = -1 or rdda1.medi_id_anterior is null) and
	(rdda1.tran_id = rdda1.tran_id_anterior or rdda1.tran_id_anterior = -1 or rdda1.tran_id_anterior is null) and
	(rdd1.tipo_id = rdd1.tipo_id_anterior or rdd1.tipo_id_anterior is null)
	)
union all
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar)
from siap.reporte r1
inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
inner join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
inner join siap.reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
left join siap.aap_conexion ac1 on ac1.aaco_id = rdd1.aaco_id_anterior
left join siap.aap_conexion ac2 on ac2.aaco_id = rdd1.aaco_id
left join siap.aap_tipo_carcasa atcrdd1 on atcrdd1.aatc_id = rdd1.aatc_id_anterior
left join siap.aap_tipo_carcasa atcrdd2 on atcrdd2.aatc_id = rdd1.aatc_id
left join siap.aap_marca amardd1 on amardd1.aama_id = rdd1.aama_id_anterior
left join siap.aap_marca amardd2 on amardd2.aama_id = rdd1.aama_id
left join siap.aap_modelo amordd1 on amordd1.aamo_id = rdd1.aamo_id_anterior
left join siap.aap_modelo amordd2 on amordd2.aamo_id = rdd1.aamo_id
left join siap.aap_cuentaap acu1 on acu1.aacu_id = rdda1.aacu_id_anterior
left join siap.aap_cuentaap acu2 on acu2.aacu_id = rdda1.aacu_id
left join siap.aap_uso aus1 on aus1.aaus_id = rdda1.aaus_id_anterior
left join siap.aap_uso aus2 on aus2.aaus_id = rdda1.aaus_id
left join siap.medidor m1 on m1.medi_id = rdda1.medi_id_anterior
left join siap.medidor m2 on m2.medi_id = rdda1.medi_id
left join siap.transformador t1 on t1.aap_id = rdda1.tran_id_anterior
left join siap.transformador t2 on t2.aap_id = rdda1.tran_id
left join siap.tipo_poste tp1 on tp1.tipo_id = rdd1.tipo_id_anterior
left join siap.tipo_poste tp2 on tp2.tipo_id = rdd1.tipo_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id = 6 and r1.rees_id = 2 and
	((rd1.even_direccion = rd1.even_direccion_anterior or rd1.even_direccion_anterior is null or rd1.even_direccion_anterior = '') and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null) and
	(rdda1.aap_apoyo = rdda1.aap_apoyo_anterior or rdda1.aap_apoyo_anterior is null or rdda1.aap_apoyo_anterior = '') and
	(rdd1.aaco_id = rdd1.aaco_id_anterior or rdd1.aaco_id_anterior is null) and
	(rdd1.aatc_id <> rdd1.aatc_id_anterior or rdd1.aatc_id_anterior is null) and
	(rdd1.aama_id <> rdd1.aama_id_anterior or rdd1.aama_id_anterior is null) and
	(rdd1.aamo_id <> rdd1.aamo_id_anterior or rdd1.aamo_id_anterior is null) and
	(rdd1.aap_tecnologia <> rdd1.aap_tecnologia_anterior or rdd1.aap_tecnologia_anterior is null) and
	(rdd1.aap_brazo = rdd1.aap_brazo_anterior or rdd1.aap_brazo_anterior is null) and
	(rdd1.aap_collarin = rdd1.aap_collarin_anterior or rdd1.aap_collarin_anterior is null) and
	(rdd1.aap_poste_altura = rdd1.aap_poste_altura_anterior or rdd1.aap_poste_altura_anterior is null) and
	(rdd1.aap_poste_propietario = rdd1.aap_poste_propietario_anterior or rdd1.aap_poste_propietario_anterior is null) and
	(rdda1.aacu_id = rdda1.aacu_id_anterior or rdda1.aacu_id_anterior is null) and
	(rdda1.aaus_id = rdda1.aaus_id_anterior or rdda1.aaus_id_anterior is null) and
	(rdda1.medi_id = rdda1.medi_id_anterior or rdda1.medi_id_anterior = -1 or rdda1.medi_id_anterior is null) and
	(rdda1.tran_id = rdda1.tran_id_anterior or rdda1.tran_id_anterior = -1 or rdda1.tran_id_anterior is null) and
	(rdd1.tipo_id = rdd1.tipo_id_anterior or rdd1.tipo_id_anterior is null)
	)
union all
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar)
from siap.control_reporte r1
inner join siap.control_reporte_direccion rd1 on rd1.repo_id = r1.repo_id
left join siap.control_reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
left join siap.control_reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id = 1 and r1.rees_id = 2 and
	((rd1.even_direccion = rd1.even_direccion_anterior or rd1.even_direccion_anterior is null or rd1.even_direccion_anterior = '') and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null) and
	(rdda1.aap_lat = rdda1.aap_lat_anterior or rdda1.aap_lat_anterior = '' or rdda1.aap_lat_anterior is null) and
	(rdda1.aap_lng = rdda1.aap_lng_anterior or rdda1.aap_lng_anterior = '' or rdda1.aap_lng_anterior is null)
	)
union all
select distinct
  r1.tireuc_id,
  r1.repo_id,
  r1.reti_id,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  cast(r1.repo_fechasolucion as varchar)
from siap.transformador_reporte r1
inner join siap.transformador_reporte_direccion rd1 on rd1.repo_id = r1.repo_id
left join siap.transformador_reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
left join siap.transformador_reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
where r1.empr_id = {empr_id} and
	r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id = 1 and r1.rees_id = 2 and
	((rd1.even_direccion = rd1.even_direccion_anterior or rd1.even_direccion_anterior is null or rd1.even_direccion_anterior = '') and
	(rd1.barr_id = rd1.barr_id_anterior or rd1.barr_id_anterior is null) and
	(rdda1.aap_lat = rdda1.aap_lat_anterior or rdda1.aap_lat_anterior = '' or rdda1.aap_lat_anterior is null) and
	(rdda1.aap_lng = rdda1.aap_lng_anterior or rdda1.aap_lng_anterior = '' or rdda1.aap_lng_anterior is null)
	)
) o
order by o.reti_descripcion, o.repo_consecutivo""" */

        SQL(_query)
          .on(
            "fecha_inicial" -> new DateTime(fecha_inicial),
            "fecha_final" -> new DateTime(fecha_final),
            "empr_id" -> empr_id
          )
          .as(_parser *)
      }
      println("ResultSet:" + resultSet)
      resultSet
    }

  def cerrarReporte(
      tireuc_id: scala.Long,
      repo_id: scala.Long,
      empr_id: scala.Long,
      user_id: scala.Long
  ) = {
    val result = tireuc_id match {
      case 1L =>
        val reporte = buscarPorId(repo_id)
        reporte match {
          case Some(r) => actualizar(r, 0, "")
          case None    => false
        }

      case 2L =>
        val reporte = controlReporteService.buscarPorId(repo_id)
        reporte match {
          case Some(r) => controlReporteService.actualizar(r, 0, "")
          case None    => false
        }

      case 3L =>
        val reporte = transformadorReporteService.buscarPorId(repo_id)
        reporte match {
          case Some(r) => transformadorReporteService.actualizar(r, 0, "")
          case None    => false
        }

    }
    result
  }

  def siap_informe_material_usado_por_reti(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): List[
    (
        Option[String],
        Option[String],
        Option[String],
        Option[String],
        Option[Double]
    )
  ] = {
    val resultSet = db.withConnection { implicit connection =>
      val _parser = {
        get[Option[String]]("reti_descripcion") ~
          get[Option[String]]("elem_codigo") ~
          get[Option[String]]("elem_descripcion") ~
          get[Option[String]]("elem_unidad") ~
          get[Option[Double]]("total") map {
          case reti_descripcion ~ elem_codigo ~ elem_descripcion ~ elem_unidad ~ even_cantidad_instalado =>
            (
              reti_descripcion,
              elem_codigo,
              elem_descripcion,
              elem_unidad,
              even_cantidad_instalado
            )
        }
      }
      val _query =
        """select rt1.reti_descripcion, e1.elem_codigo, e1.elem_descripcion, e1.elem_unidad, SUM(re1.even_cantidad_instalado) as total
                          from siap.reporte_evento re1
                          inner join siap.reporte r1 on r1.repo_id = re1.repo_id
                          inner join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
                          inner join siap.elemento e1 on e1.elem_id = re1.elem_id
                        where r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and re1.even_estado < 8
                        group by 1,2,3,4
                        order by 1"""
      SQL(_query)
        .on(
          "fecha_inicial" -> new DateTime(fecha_inicial),
          "fecha_final" -> new DateTime(fecha_final),
          "empr_id" -> empr_id
        )
        .as(_parser *)
    }
    resultSet.toList
  }

  def siap_informe_material_usado_por_reti_xlsx(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long,
      usua_id: scala.Long
  ) = {
    val empresa = empresaService.buscarPorId(empr_id)
    val usuario = usuarioService.buscarPorId(usua_id)
    val _resultSet =
      siap_informe_material_usado_por_reti(fecha_inicial, fecha_final, empr_id)

    var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
    var _listMerged = new ListBuffer[CellRange]()
    val df = DateTimeFormat.forPattern("yyyy-MM-dd")
    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val sheet = Sheet(
      name = "Material Utilizado",
      rows = {
        _listRow += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(empresa match {
            case Some(e) => e.empr_descripcion
            case None    => ""
          })
        _listRow += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "Material Utilizado en el Periodo " + df
              .print(fecha_inicial) + " al " + df.print(fecha_final)
          )
        _listRow += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("Fecha de Generacion:" + dtf.print(DateTime.now()))
        _listRow += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("Usuario:", usuario match {
            case Some(u) => u.usua_nombre + " " + u.usua_apellido
            case None    => ""
          })
        _listRow += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "Tipo de Operación",
            "Código Material",
            "Descripción Material",
            "Unidad",
            "Cantidad Utilizada"
          )
        _resultSet.map {
          i =>
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues(
                i._1 match {
                  case Some(s) => s
                  case None    => ""
                },
                i._2 match {
                  case Some(s) => s
                  case None    => ""
                },
                i._3 match {
                  case Some(s) => s
                  case None    => ""
                },
                i._4 match {
                  case Some(s) => s
                  case None    => ""
                },
                i._5 match {
                  case Some(s) => s
                  case None    => 0.0
                }
              )
        }
        _listRow.toList
      },
      mergedRegions = {
        _listMerged += CellRange((0, 0), (0, 4))
        _listMerged += CellRange((1, 1), (0, 4))
        _listMerged += CellRange((2, 2), (0, 4))
        _listMerged += CellRange((3, 3), (0, 4))
        _listMerged.toList
      },
      columns = {
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(30, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(30, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(40, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(15, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(20, WidthUnit.Character))
        _listColumn.toList
      }
    )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(sheet).writeToOutputStream(os)
    println("Stream Listo")
    os.toByteArray
  }
}
