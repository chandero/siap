package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream, FileInputStream}
import java.util.{Map, HashMap, Date}
import java.lang.Long
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.util.UUID.randomUUID

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

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, double, date, scalar, flatten}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.immutable.List
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import scala.util.Random

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
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
import org.apache.poi.common.usermodel.HyperlinkType
// Utility
import utilities.Utility

case class Siap_resumen_material(
    elem_codigo: Option[String],
    elem_descripcion: Option[String],
    even_cantidad_retirado: Option[Double],
    even_cantidad_instalado: Option[Double]
)
case class Siap_resumen_material_reporte(
    elem_codigo: Option[String],
    elem_descripcion: Option[String],
    reti_descripcion: Option[String],
    even_cantidad_retirado: Option[Double],
    even_cantidad_instalado: Option[Double]
)
case class Siap_detallado_material(
    elem_codigo: Option[String],
    elem_descripcion: Option[String],
    reti_descripcion: Option[String],
    repo_numero: Option[scala.Long],
    repo_fechasolucion: Option[DateTime],
    even_codigo_retirado: Option[String],
    even_cantidad_retirado: Option[Double],
    even_codigo_instalado: Option[String],
    even_cantidad_instalado: Option[Double]
)
case class Siap_inventario_a(
    aap_id: Option[scala.Long],
    aap_apoyo: Option[String],
    esta_descripcion: Option[String],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    tiba_descripcion: Option[String],
    aaus_descripcion: Option[String],
    aap_modernizada: Option[Boolean],
    aap_modernizada_anho: Option[Int],
    aatc_descripcion: Option[String],
    aap_medidor: Option[Boolean],
    aaco_descripcion: Option[String],
    aama_descripcion: Option[String],
    aamo_descripcion: Option[String]
)
case class Siap_inventario_b(
    tipo_descripcion: Option[String],
    aap_poste_altura: Option[Double],
    aap_brazo: Option[String],
    aap_collarin: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aap_rte: Option[String],
    aap_poste_propietario: Option[String],
    aap_bombillo: Option[String],
    aap_balasto: Option[String],
    aap_arrancador: Option[String],
    aap_condensador: Option[String],
    aap_fotocelda: Option[String],
    medi_codigo: Option[String],
    medi_numero: Option[String],
    aacu_descripcion: Option[String],
    tran_codigo: Option[String],
    tran_numero: Option[String],
)

case class Inventario(
    aap_id: Option[scala.Long],
    aap_apoyo: Option[String],
    esta_descripcion: Option[String],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    tiba_descripcion: Option[String],
    aaus_descripcion: Option[String],
    aap_modernizada: Option[Boolean],
    aap_modernizada_anho: Option[Int],
    aatc_descripcion: Option[String],
    aap_medidor: Option[Boolean],
    aaco_descripcion: Option[String],
    aama_descripcion: Option[String],
    aamo_descripcion: Option[String],
    tipo_descripcion: Option[String],
    aap_poste_altura: Option[Double],
    aap_brazo: Option[String],
    aap_collarin: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aap_rte: Option[String],
    aap_poste_propietario: Option[String],
    aap_bombillo: Option[String],
    aap_balasto: Option[String],
    aap_arrancador: Option[String],
    aap_condensador: Option[String],
    aap_fotocelda: Option[String],
    medi_codigo: Option[String],
    medi_numero: Option[String],
    aacu_descripcion: Option[String],
    tran_codigo: Option[String],
    tran_numero: Option[String]
)

case class Siap_inventario(a: Siap_inventario_a, b: Siap_inventario_b)
case class Siap_disponibilidad(
    repo_fecharecepcion: Option[String],
    repo_fechasolucion: Option[String],
    repo_consecutivo: Option[scala.Double],
    aap_id: Option[scala.Long],
    aap_potencia: Option[Int],
    vereda: Option[String],
    festivos: Option[Int]
)
case class Siap_eficiencia(
    repo_direccion: Option[String],
    barr_descripcion: Option[String],
    repo_consecutivo: Option[scala.Double],
    aap_id: Option[scala.Long],
    repo_fecharecepcion: Option[String],
    repo_fechasolucion: Option[String],
    festivos: Option[Int]
)
case class Siap_visita_por_barrio(
    barr_descripcion: Option[String],
    visitas: Option[Int]
)
case class Siap_material_repetido(
    even_codigo_instalado: String,
    reti_descripcion: String,
    repo_consecutivo: Int,
    repo_fechasolucion: DateTime,
    aap_id: scala.Long,
    aap_direccion: String,
    barr_descripcion: String,
    tiel_descripcion: String
)

object Siap_visita_por_barrio {
  implicit val sdWrites = new Writes[Siap_visita_por_barrio] {
    def writes(sd: Siap_visita_por_barrio) = Json.obj(
      "barr_descripcion" -> sd.barr_descripcion,
      "visitas" -> sd.visitas
    )
  }

  val _set = {
    get[Option[String]]("barr_descripcion") ~
      get[Option[Int]]("visitas") map {
      case barr_descripcion ~
            visitas =>
        new Siap_visita_por_barrio(barr_descripcion, visitas)
    }
  }
}

object Siap_disponibilidad {
  implicit val sdWrites = new Writes[Siap_disponibilidad] {
    def writes(sd: Siap_disponibilidad) = Json.obj(
      "repo_fecharecepcion" -> sd.repo_fecharecepcion,
      "repo_fechasolucion" -> sd.repo_fechasolucion,
      "repo_consecutivo" -> sd.repo_consecutivo,
      "aap_id" -> sd.aap_id,
      "aap_potencia" -> sd.aap_potencia,
      "vereda" -> sd.vereda,
      "festivos" -> sd.festivos
    )
  }

  val siap_disponibilidad_set = {
    get[Option[String]]("repo_fecharecepcion") ~
      get[Option[String]]("repo_fechasolucion") ~
      get[Option[scala.Double]]("repo_consecutivo") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("vereda") ~
      get[Option[Int]]("festivos") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            repo_consecutivo ~
            aap_id ~
            aap_potencia ~
            vereda ~
            festivos =>
        new Siap_disponibilidad(
          repo_fecharecepcion,
          repo_fechasolucion,
          repo_consecutivo,
          aap_id,
          aap_potencia,
          vereda,
          festivos
        )
    }
  }
}

object Siap_eficiencia {
  implicit val sdWrites = new Writes[Siap_eficiencia] {
    def writes(sd: Siap_eficiencia) = Json.obj(
      "repo_direccion" -> sd.repo_direccion,
      "barr_descripcion" -> sd.barr_descripcion,
      "repo_consecutivo" -> sd.repo_consecutivo,
      "aap_id" -> sd.aap_id,
      "repo_fecharecepcion" -> sd.repo_fecharecepcion,
      "repo_fechasolucion" -> sd.repo_fechasolucion,
      "festivos" -> sd.festivos
    )
  }

  val _set = {
    get[Option[String]]("repo_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[scala.Double]]("repo_consecutivo") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("repo_fecharecepcion") ~
      get[Option[String]]("repo_fechasolucion") ~
      get[Option[Int]]("festivos") map {
      case repo_direccion ~
            barr_descripcion ~
            repo_consecutivo ~
            aap_id ~
            repo_fecharecepcion ~
            repo_fechasolucion ~
            festivos =>
        new Siap_eficiencia(
          repo_direccion,
          barr_descripcion,
          repo_consecutivo,
          aap_id,
          repo_fecharecepcion,
          repo_fechasolucion,
          festivos
        )
    }
  }
}

object Siap_inventario_a {
  implicit val siaWrites = new Writes[Siap_inventario_a] {
    def writes(sia: Siap_inventario_a) = Json.obj(
      "aap_id" -> sia.aap_id,
      "aap_apoyo" -> sia.aap_apoyo,
      "esta_descripcion" -> sia.esta_descripcion,
      "aap_direccion" -> sia.aap_direccion,
      "barr_descripcion" -> sia.barr_descripcion,
      "tiba_descripcion" -> sia.tiba_descripcion,
      "aaus_descripcion" -> sia.aaus_descripcion,
      "aap_modernizada" -> sia.aap_modernizada,
      "aap_modernizada_anho" -> sia.aap_modernizada_anho,
      "aatc_descripcion" -> sia.aatc_descripcion,
      "aap_medidor" -> sia.aap_medidor,
      "aaco_descripcion" -> sia.aaco_descripcion,
      "aama_descripcion" -> sia.aama_descripcion,
      "aamo_descripcion" -> sia.aamo_descripcion
    )
  }
}

object Siap_inventario_b {
  implicit val sibWrites = new Writes[Siap_inventario_b] {
    def writes(sib: Siap_inventario_b) = Json.obj(
      "tipo_descripcion" -> sib.tipo_descripcion,
      "aap_poste_altura" -> sib.aap_poste_altura,
      "aap_brazo" -> sib.aap_brazo,
      "aap_collarin" -> sib.aap_collarin,
      "aap_potencia" -> sib.aap_potencia,
      "aap_tecnologia" -> sib.aap_tecnologia,
      "aap_rte" -> sib.aap_rte,
      "aap_poste_propietario" -> sib.aap_poste_propietario,
      "aap_bombillo" -> sib.aap_bombillo,
      "aap_balasto" -> sib.aap_balasto,
      "aap_arrancador" -> sib.aap_arrancador,
      "aap_condensador" -> sib.aap_condensador,
      "aap_fotocelda" -> sib.aap_fotocelda,
      "medi_codigo" -> sib.medi_codigo,
      "medi_numero" -> sib.medi_numero,
      "aacu_descripcion" -> sib.aacu_descripcion,
      "tran_codigo" -> sib.tran_codigo,
      "tran_numero" -> sib.tran_numero
    )
  }
}

object Inventario {

  implicit val iWrites = new Writes[Inventario] {
    def writes(i: Inventario) = Json.obj(
      "codigo" -> i.aap_id,
      "apoyo" -> i.aap_apoyo,
      "direccion" -> i.aap_direccion,
      "barrio" -> i.barr_descripcion,
      "sector" -> i.tiba_descripcion,
      "uso" -> i.aaus_descripcion,
      "tipo" -> i.aatc_descripcion,
      "medidor" -> i.aap_medidor,
      "medida" -> i.aaco_descripcion,
      "marca" -> i.aama_descripcion,
      "modelo" -> i.aamo_descripcion,
      "potencia" -> i.aap_potencia,
      "tecnologia" -> i.aap_tecnologia
    )
  }
}

object Siap_inventario {
  implicit val siWrites = new Writes[Siap_inventario] {
    def writes(si: Siap_inventario) = Json.obj(
      "a" -> si.a,
      "b" -> si.b
    )
  }

  val Siap_inventario_set = {
    get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_apoyo") ~
      get[Option[String]]("esta_descripcion") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("tiba_descripcion") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[Boolean]]("aap_modernizada") ~
      get[Option[Int]]("aap_modernizada_anho") ~
      get[Option[String]]("aatc_descripcion") ~
      get[Option[Boolean]]("aap_medidor") ~
      get[Option[String]]("aaco_descripcion") ~
      get[Option[String]]("aama_descripcion") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[String]]("tipo_descripcion") ~
      get[Option[Double]]("aap_poste_altura") ~
      get[Option[String]]("aap_brazo") ~
      get[Option[String]]("aap_collarin") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aap_rte") ~
      get[Option[String]]("aap_poste_propietario") ~
      get[Option[String]]("aap_bombillo") ~
      get[Option[String]]("aap_balasto") ~
      get[Option[String]]("aap_arrancador") ~
      get[Option[String]]("aap_condensador") ~
      get[Option[String]]("aap_fotocelda") ~
      get[Option[String]]("medi_codigo") ~
      get[Option[String]]("medi_numero") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("tran_codigo") ~
      get[Option[String]]("tran_numero") map {
      case aap_id ~
            aap_apoyo ~
            esta_descripcion ~
            aap_direccion ~
            barr_descripcion ~
            tiba_descripcion ~
            aaus_descripcion ~
            aap_modernizada ~
            aap_modernizada_anho ~
            aatc_descripcion ~
            aap_medidor ~
            aaco_descripcion ~
            aama_descripcion ~
            aamo_descripcion ~
            tipo_descripcion ~
            aap_poste_altura ~
            aap_brazo ~
            aap_collarin ~
            aap_potencia ~
            aap_tecnologia ~
            aap_rte ~
            aap_poste_propietario ~
            aap_bombillo ~
            aap_balasto ~
            aap_arrancador ~
            aap_condensador ~
            aap_fotocelda ~
            medi_codigo ~
            medi_numero ~
            aacu_descripcion ~
            tran_codigo ~
            tran_numero =>
        new Inventario(
          aap_id,
          aap_apoyo,
          esta_descripcion,
          aap_direccion,
          barr_descripcion,
          tiba_descripcion,
          aaus_descripcion,
          aap_modernizada,
          aap_modernizada_anho,
          aatc_descripcion,
          aap_medidor,
          aaco_descripcion,
          aama_descripcion,
          aamo_descripcion,
          tipo_descripcion,
          aap_poste_altura,
          aap_brazo,
          aap_collarin,
          aap_potencia,
          aap_tecnologia,
          aap_rte,
          aap_poste_propietario,
          aap_bombillo,
          aap_balasto,
          aap_arrancador,
          aap_condensador,
          aap_fotocelda,
          medi_codigo,
          medi_numero,
          aacu_descripcion,
          tran_codigo,
          tran_numero
        )
    }
  }
}

object Siap_resumen_material {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val srmWrites = new Writes[Siap_resumen_material] {
    def writes(srm: Siap_resumen_material) = Json.obj(
      "elem_codigo" -> srm.elem_codigo,
      "elem_descripcion" -> srm.elem_descripcion,
      "even_cantidad_retirado" -> srm.even_cantidad_retirado,
      "even_cantidad_instalado" -> srm.even_cantidad_instalado
    )
  }
}

object Siap_resumen_material_reporte {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val srmWrites = new Writes[Siap_resumen_material_reporte] {
    def writes(srm: Siap_resumen_material_reporte) = Json.obj(
      "elem_codigo" -> srm.elem_codigo,
      "elem_descripcion" -> srm.elem_descripcion,
      "reti_descripcion" -> srm.reti_descripcion,
      "even_cantidad_retirado" -> srm.even_cantidad_retirado,
      "even_cantidad_instalado" -> srm.even_cantidad_instalado
    )
  }
}

object Siap_detallado_material {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val srmWrites = new Writes[Siap_detallado_material] {
    def writes(srm: Siap_detallado_material) = Json.obj(
      "elem_codigo" -> srm.elem_codigo,
      "elem_descripcion" -> srm.elem_descripcion,
      "reti_descripcion" -> srm.reti_descripcion,
      "repo_numero" -> srm.repo_numero,
      "repo_fechasolucion" -> srm.repo_fechasolucion,
      "even_codigo_retirado" -> srm.even_codigo_retirado,
      "even_cantidad_retirado" -> srm.even_cantidad_retirado,
      "even_codigo_instalado" -> srm.even_codigo_instalado,
      "even_cantidad_instalado" -> srm.even_cantidad_instalado
    )
  }
}

object Siap_material_repetido {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val smrWrites = new Writes[Siap_material_repetido] {
    def writes(smr: Siap_material_repetido) = Json.obj(
      "even_codigo_instalado" -> smr.even_codigo_instalado,
      "reti_descripcion" -> smr.reti_descripcion,
      "repo_consecutivo" -> smr.repo_consecutivo,
      "repo_fechasolucion" -> smr.repo_fechasolucion,
      "aap_id" -> smr.aap_id,
      "aap_direccion" -> smr.aap_direccion,
      "barr_descripcion" -> smr.barr_descripcion,
      "tiel_descripcion" -> smr.tiel_descripcion
    )
  }

  val _set = {
    get[String]("even_codigo_instalado") ~
      get[String]("reti_descripcion") ~
      get[Int]("repo_consecutivo") ~
      get[DateTime]("repo_fechasolucion") ~
      get[Int]("aap_id") ~
      get[String]("aap_direccion") ~
      get[String]("barr_descripcion") ~
      get[String]("tiel_descripcion") map {
      case a ~ b ~ c ~ d ~ e ~ f ~ g ~ h =>
        new Siap_material_repetido(a, b, c, d, e, f, g, h)
    }
  }
}

case class Siap_detallado_expansion(
    repo_fechasolucion: Option[DateTime],
    repo_fechadigitacion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    repo_descripcion: Option[String],
    tipo_descripcion: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String],
    urba_descripcion: Option[String]
)

case class Siap_detallado_reubicacion(
    repo_fechasolucion: Option[DateTime],
    repo_fechadigitacion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion_anterior: Option[String],
    barr_descripcion_anterior: Option[String],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    aaus_descripcion_anterior: Option[String],
    aacu_descripcion_anterior: Option[String],
    aap_codigo: Option[String],
    aamo_descripcion_anterior: Option[String],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia_anterior: Option[String],
    aaco_descripcion_anterior: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aaco_descripcion: Option[String]
)

case class Siap_detallado_cambio_medida(
    repo_fechasolucion: Option[DateTime],
    repo_fechadigitacion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion_anterior: Option[String],
    barr_descripcion_anterior: Option[String],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    aaus_descripcion_anterior: Option[String],
    aacu_descripcion_anterior: Option[String],
    aamo_descripcion_anterior: Option[String],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia_anterior: Option[String],
    aaco_descripcion_anterior: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aaco_descripcion: Option[String],
    medi_codigo: Option[String],
    tran_codigo: Option[String]
)

case class Siap_detallado_modernizacion(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion_anterior: Option[String],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia_anterior: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String]
)

case class Siap_detallado_actualizacion(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion_anterior: Option[String],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia_anterior: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String]
)

case class Siap_detallado_reposicion(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion_anterior: Option[String],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia_anterior: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String]
)

case class Siap_detallado_repotenciacion(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion_anterior: Option[String],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia_anterior: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String]
)

case class Siap_detallado_retiro(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    repo_consecutivo: Option[Int],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String],
    reubicado: Option[String],
    tire_descripcion: Option[String]
)

case class Siap_retiro_reubicacion_a(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    repo_fechadigitacion: Option[DateTime],
    repo_consecutivo: Option[Int],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String],
    enbaja: Option[String]
)

case class Siap_retiro_reubicacion_b(
    repo_fecharecepcion: Option[DateTime],
    repo_fechasolucion: Option[DateTime],
    repo_fechadigitacion: Option[DateTime],
    reti_descripcion: Option[String],
    aap_rte: Option[String],
    aap_id: Option[scala.Long],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    vereda: Option[String],
    parque: Option[String],
    aaus_descripcion: Option[String],
    aacu_descripcion: Option[String],
    aamo_descripcion: Option[String],
    aap_potencia: Option[Int],
    aap_tecnologia: Option[String],
    aaco_descripcion: Option[String]
)

case class Siap_retiro_reubicacion(
    retiro: Option[Siap_retiro_reubicacion_a],
    reubicacion: Option[Siap_retiro_reubicacion_b]
)

case class Siap_consolidado(
    repo_consecutivo: Option[Int],
    repo_fecharecepcion: Option[DateTime],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_direccion: Option[String],
    barr_descripcion: Option[String],
    orig_descripcion: Option[String],
    acti_descripcion: Option[String],
    repo_descripcion: Option[String],
    repo_fechasolucion: Option[DateTime],
    repo_reportetecnico: Option[String]
)

object Siap_consolidado {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val scWrites = new Writes[Siap_consolidado] {
    def writes(sc: Siap_consolidado) = Json.obj(
      "repo_consecutivo" -> sc.repo_consecutivo,
      "repo_fecharecepcion" -> sc.repo_fecharecepcion,
      "repo_nombre" -> sc.repo_nombre,
      "repo_telefono" -> sc.repo_telefono,
      "repo_direccion" -> sc.repo_direccion,
      "barr_descripcion" -> sc.barr_descripcion,
      "orig_descripcion" -> sc.orig_descripcion,
      "acti_descripcion" -> sc.acti_descripcion,
      "repo_descripcion" -> sc.repo_descripcion,
      "repo_fechasolucion" -> sc.repo_fechasolucion,
      "repo_reportetecnico" -> sc.repo_reportetecnico
    )
  }

  val set = {
    get[Option[Int]]("repo_consecutivo") ~
      get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[String]]("repo_nombre") ~
      get[Option[String]]("repo_telefono") ~
      get[Option[String]]("repo_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("orig_descripcion") ~
      get[Option[String]]("acti_descripcion") ~
      get[Option[String]]("repo_descripcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("repo_reportetecnico") map {
      case repo_consecutivo ~
            repo_fecharecepcion ~
            repo_nombre ~
            repo_telefono ~
            repo_direccion ~
            barr_descripcion ~
            orig_descripcion ~
            acti_descripcion ~
            repo_descripcion ~
            repo_fechasolucion ~
            repo_reportetecnico =>
        new Siap_consolidado(
          repo_consecutivo,
          repo_fecharecepcion,
          repo_nombre,
          repo_telefono,
          repo_direccion,
          barr_descripcion,
          orig_descripcion,
          acti_descripcion,
          repo_descripcion,
          repo_fechasolucion,
          repo_reportetecnico
        )
    }
  }
}

object Siap_detallado_expansion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdeWrites = new Writes[Siap_detallado_expansion] {
    def writes(srm: Siap_detallado_expansion) = Json.obj(
      "repo_fechasolucion" -> srm.repo_fechasolucion,
      "repo_fechadigitacion" -> srm.repo_fechadigitacion,
      "aap_rte" -> srm.aap_rte,
      "aap_id" -> srm.aap_id,
      "aap_direccion" -> srm.aap_direccion,
      "barr_descripcion" -> srm.barr_descripcion,
      "vereda" -> srm.vereda,
      "parque" -> srm.parque,
      "aaus_descripcion" -> srm.aaus_descripcion,
      "aacu_descripcion" -> srm.aacu_descripcion,
      "repo_descripcion" -> srm.repo_descripcion,
      "tipo_descripcion" -> srm.tipo_descripcion,
      "aamo_descripcion" -> srm.aamo_descripcion,
      "aap_potencia" -> srm.aap_potencia,
      "aap_tecnologia" -> srm.aap_tecnologia,
      "aaco_descripcion" -> srm.aaco_descripcion,
      "urba_descripcion" -> srm.urba_descripcion
    )
  }

  val siap_detallado_expansion_set = {
    get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("repo_descripcion") ~
      get[Option[String]]("tipo_descripcion") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") ~
      get[Option[String]]("urba_descripcion") map {
      case repo_fechasolucion ~
            repo_fechadigitacion ~
            aap_rte ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            repo_descripcion ~
            tipo_descripcion ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion ~
            urba_descripcion =>
        new Siap_detallado_expansion(
          repo_fechasolucion,
          repo_fechadigitacion,
          aap_rte,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          repo_descripcion,
          tipo_descripcion,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion,
          urba_descripcion
        )
    }
  }
}

object Siap_detallado_reubicacion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdrWrites = new Writes[Siap_detallado_reubicacion] {
    def writes(srm: Siap_detallado_reubicacion) = Json.obj(
      "repo_fechasolucion" -> srm.repo_fechasolucion,
      "repo_fechadigitacion" -> srm.repo_fechadigitacion,
      "aap_rte" -> srm.aap_rte,
      "aap_id" -> srm.aap_id,
      "aap_direccion_anterior" -> srm.aap_direccion_anterior,
      "barr_descripcion_anterior" -> srm.barr_descripcion_anterior,
      "aap_direccion" -> srm.aap_direccion,
      "barr_descripcion" -> srm.barr_descripcion,
      "aaus_descripcion_anterior" -> srm.aaus_descripcion_anterior,
      "aacu_descripcion_anterior" -> srm.aacu_descripcion_anterior,
      "aap_codigo" -> srm.aap_codigo,
      "aamo_descripcion_anterior" -> srm.aamo_descripcion_anterior,
      "aap_potencia_anterior" -> srm.aap_potencia_anterior,
      "aap_tecnologia_anterior" -> srm.aap_tecnologia,
      "aaco_descripcion_anterior" -> srm.aaco_descripcion_anterior,
      "aamo_descripcion" -> srm.aamo_descripcion,
      "aap_potencia" -> srm.aap_potencia,
      "aap_tecnologia" -> srm.aap_tecnologia,
      "aaus_descripcion" -> srm.aaus_descripcion,
      "aacu_descripcion" -> srm.aacu_descripcion,
      "aaco_descripcion" -> srm.aaco_descripcion
    )
  }

  val siap_detallado_reubicacion_set = {
    get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion_anterior") ~
      get[Option[String]]("barr_descripcion_anterior") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("aaus_descripcion_anterior") ~
      get[Option[String]]("aacu_descripcion_anterior") ~
      get[Option[String]]("aap_codigo") ~
      get[Option[String]]("aamo_descripcion_anterior") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aaco_descripcion_anterior") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aaco_descripcion") map {
      case repo_fechasolucion ~
            repo_fechadigitacion ~
            aap_rte ~
            aap_id ~
            aap_direccion_anterior ~
            barr_descripcion_anterior ~
            aap_direccion ~
            barr_descripcion ~
            aaus_descripcion_anterior ~
            aacu_descripcion_anterior ~
            aap_codigo ~
            aamo_descripcion_anterior ~
            aap_potencia_anterior ~
            aap_tecnologia_anterior ~
            aaco_descripcion_anterior ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaus_descripcion ~
            aacu_descripcion ~
            aaco_descripcion =>
        new Siap_detallado_reubicacion(
          repo_fechasolucion,
          repo_fechadigitacion,
          aap_rte,
          aap_id,
          aap_direccion_anterior,
          barr_descripcion_anterior,
          aap_direccion,
          barr_descripcion,
          aaus_descripcion_anterior,
          aacu_descripcion_anterior,
          aap_codigo,
          aamo_descripcion_anterior,
          aap_potencia_anterior,
          aap_tecnologia_anterior,
          aaco_descripcion_anterior,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaus_descripcion,
          aacu_descripcion,
          aaco_descripcion
        )
    }
  }
}

object Siap_detallado_cambio_medida {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdrWrites = new Writes[Siap_detallado_cambio_medida] {
    def writes(srm: Siap_detallado_cambio_medida) = Json.obj(
      "repo_fechasolucion" -> srm.repo_fechasolucion,
      "repo_fechadigitacion" -> srm.repo_fechadigitacion,
      "aap_rte" -> srm.aap_rte,
      "aap_id" -> srm.aap_id,
      "aap_direccion_anterior" -> srm.aap_direccion_anterior,
      "barr_descripcion_anterior" -> srm.barr_descripcion_anterior,
      "aap_direccion" -> srm.aap_direccion,
      "barr_descripcion" -> srm.barr_descripcion,
      "aaus_descripcion_anterior" -> srm.aaus_descripcion_anterior,
      "aacu_descripcion_anterior" -> srm.aacu_descripcion_anterior,
      "aamo_descripcion_anterior" -> srm.aamo_descripcion_anterior,
      "aap_potencia_anterior" -> srm.aap_potencia_anterior,
      "aap_tecnologia_anterior" -> srm.aap_tecnologia,
      "aaco_descripcion_anterior" -> srm.aaco_descripcion_anterior,
      "aamo_descripcion" -> srm.aamo_descripcion,
      "aap_potencia" -> srm.aap_potencia,
      "aap_tecnologia" -> srm.aap_tecnologia,
      "aaus_descripcion" -> srm.aaus_descripcion,
      "aacu_descripcion" -> srm.aacu_descripcion,
      "aaco_descripcion" -> srm.aaco_descripcion,
      "medi_codigo" -> srm.medi_codigo,
      "tran_codigo" -> srm.tran_codigo
    )
  }

  val _set = {
    get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion_anterior") ~
      get[Option[String]]("barr_descripcion_anterior") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("aaus_descripcion_anterior") ~
      get[Option[String]]("aacu_descripcion_anterior") ~
      get[Option[String]]("aamo_descripcion_anterior") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aaco_descripcion_anterior") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aaco_descripcion") ~
      get[Option[String]]("medi_codigo") ~
      get[Option[String]]("tran_codigo") map {
      case repo_fechasolucion ~
            repo_fechadigitacion ~
            aap_rte ~
            aap_id ~
            aap_direccion_anterior ~
            barr_descripcion_anterior ~
            aap_direccion ~
            barr_descripcion ~
            aaus_descripcion_anterior ~
            aacu_descripcion_anterior ~
            aamo_descripcion_anterior ~
            aap_potencia_anterior ~
            aap_tecnologia_anterior ~
            aaco_descripcion_anterior ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaus_descripcion ~
            aacu_descripcion ~
            aaco_descripcion ~
            medi_codigo ~
            tran_codigo =>
        new Siap_detallado_cambio_medida(
          repo_fechasolucion,
          repo_fechadigitacion,
          aap_rte,
          aap_id,
          aap_direccion_anterior,
          barr_descripcion_anterior,
          aap_direccion,
          barr_descripcion,
          aaus_descripcion_anterior,
          aacu_descripcion_anterior,
          aamo_descripcion_anterior,
          aap_potencia_anterior,
          aap_tecnologia_anterior,
          aaco_descripcion_anterior,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaus_descripcion,
          aacu_descripcion,
          aaco_descripcion,
          medi_codigo,
          tran_codigo
        )
    }
  }
}

object Siap_detallado_modernizacion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdmWrites = new Writes[Siap_detallado_modernizacion] {
    def writes(sdm: Siap_detallado_modernizacion) = Json.obj(
      "repo_fecharecepcion" -> sdm.repo_fecharecepcion,
      "repo_fechasolucion" -> sdm.repo_fechasolucion,
      "aap_rte" -> sdm.aap_rte,
      "aap_id" -> sdm.aap_id,
      "aap_direccion" -> sdm.aap_direccion,
      "barr_descripcion" -> sdm.barr_descripcion,
      "aaus_descripcion" -> sdm.aaus_descripcion,
      "aacu_descripcion" -> sdm.aacu_descripcion,
      "aamo_descripcion_anterior" -> sdm.aamo_descripcion_anterior,
      "aap_potencia_anterior" -> sdm.aap_potencia_anterior,
      "aap_tecnologia_anterior" -> sdm.aap_tecnologia_anterior,
      "aamo_descripcion" -> sdm.aamo_descripcion,
      "aap_potencia" -> sdm.aap_potencia,
      "aap_tecnologia" -> sdm.aap_tecnologia,
      "aaco_descripcion" -> sdm.aaco_descripcion
    )
  }

  val siap_detallado_modernizacion_set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion_anterior") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            aap_rte ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion_anterior ~
            aap_potencia_anterior ~
            aap_tecnologia_anterior ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion =>
        new Siap_detallado_modernizacion(
          repo_fecharecepcion,
          repo_fechasolucion,
          aap_rte,
          aap_id,
          aap_direccion,
          barr_descripcion,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion_anterior,
          aap_potencia_anterior,
          aap_tecnologia_anterior,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion
        )

    }
  }
}

object Siap_detallado_actualizacion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdmWrites = new Writes[Siap_detallado_actualizacion] {
    def writes(sdm: Siap_detallado_actualizacion) = Json.obj(
      "repo_fecharecepcion" -> sdm.repo_fecharecepcion,
      "repo_fechasolucion" -> sdm.repo_fechasolucion,
      "aap_rte" -> sdm.aap_rte,
      "aap_id" -> sdm.aap_id,
      "aap_direccion" -> sdm.aap_direccion,
      "barr_descripcion" -> sdm.barr_descripcion,
      "vereda" -> sdm.vereda,
      "parque" -> sdm.parque,
      "aaus_descripcion" -> sdm.aaus_descripcion,
      "aacu_descripcion" -> sdm.aacu_descripcion,
      "aamo_descripcion_anterior" -> sdm.aamo_descripcion_anterior,
      "aap_potencia_anterior" -> sdm.aap_potencia_anterior,
      "aap_tecnologia_anterior" -> sdm.aap_tecnologia_anterior,
      "aamo_descripcion" -> sdm.aamo_descripcion,
      "aap_potencia" -> sdm.aap_potencia,
      "aap_tecnologia" -> sdm.aap_tecnologia,
      "aaco_descripcion" -> sdm.aaco_descripcion
    )
  }

  val siap_detallado_actualizacion_set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion_anterior") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            aap_rte ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion_anterior ~
            aap_potencia_anterior ~
            aap_tecnologia_anterior ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion =>
        new Siap_detallado_actualizacion(
          repo_fecharecepcion,
          repo_fechasolucion,
          aap_rte,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion_anterior,
          aap_potencia_anterior,
          aap_tecnologia_anterior,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion
        )

    }
  }
}

object Siap_detallado_reposicion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdmWrites = new Writes[Siap_detallado_reposicion] {
    def writes(sdm: Siap_detallado_reposicion) = Json.obj(
      "repo_fecharecepcion" -> sdm.repo_fecharecepcion,
      "repo_fechasolucion" -> sdm.repo_fechasolucion,
      "aap_rte" -> sdm.aap_rte,
      "aap_id" -> sdm.aap_id,
      "aap_direccion" -> sdm.aap_direccion,
      "barr_descripcion" -> sdm.barr_descripcion,
      "vereda" -> sdm.vereda,
      "parque" -> sdm.parque,
      "aaus_descripcion" -> sdm.aaus_descripcion,
      "aacu_descripcion" -> sdm.aacu_descripcion,
      "aamo_descripcion_anterior" -> sdm.aamo_descripcion_anterior,
      "aap_potencia_anterior" -> sdm.aap_potencia_anterior,
      "aap_tecnologia_anterior" -> sdm.aap_tecnologia_anterior,
      "aamo_descripcion" -> sdm.aamo_descripcion,
      "aap_potencia" -> sdm.aap_potencia,
      "aap_tecnologia" -> sdm.aap_tecnologia,
      "aaco_descripcion" -> sdm.aaco_descripcion
    )
  }

  val siap_detallado_reposicion_set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion_anterior") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            aap_rte ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion_anterior ~
            aap_potencia_anterior ~
            aap_tecnologia_anterior ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion =>
        new Siap_detallado_reposicion(
          repo_fecharecepcion,
          repo_fechasolucion,
          aap_rte,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion_anterior,
          aap_potencia_anterior,
          aap_tecnologia_anterior,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion
        )

    }
  }
}

object Siap_detallado_repotenciacion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdmWrites = new Writes[Siap_detallado_repotenciacion] {
    def writes(sdm: Siap_detallado_repotenciacion) = Json.obj(
      "repo_fecharecepcion" -> sdm.repo_fecharecepcion,
      "repo_fechasolucion" -> sdm.repo_fechasolucion,
      "aap_rte" -> sdm.aap_rte,
      "aap_id" -> sdm.aap_id,
      "aap_direccion" -> sdm.aap_direccion,
      "barr_descripcion" -> sdm.barr_descripcion,
      "vereda" -> sdm.vereda,
      "parque" -> sdm.parque,
      "aaus_descripcion" -> sdm.aaus_descripcion,
      "aacu_descripcion" -> sdm.aacu_descripcion,
      "aamo_descripcion_anterior" -> sdm.aamo_descripcion_anterior,
      "aap_potencia_anterior" -> sdm.aap_potencia_anterior,
      "aap_tecnologia_anterior" -> sdm.aap_tecnologia_anterior,
      "aamo_descripcion" -> sdm.aamo_descripcion,
      "aap_potencia" -> sdm.aap_potencia,
      "aap_tecnologia" -> sdm.aap_tecnologia,
      "aaco_descripcion" -> sdm.aaco_descripcion
    )
  }

  val siap_detallado_repotenciacion_set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion_anterior") ~
      get[Option[Int]]("aap_potencia_anterior") ~
      get[Option[String]]("aap_tecnologia_anterior") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            aap_rte ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion_anterior ~
            aap_potencia_anterior ~
            aap_tecnologia_anterior ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion =>
        new Siap_detallado_repotenciacion(
          repo_fecharecepcion,
          repo_fechasolucion,
          aap_rte,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion_anterior,
          aap_potencia_anterior,
          aap_tecnologia_anterior,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion
        )

    }
  }
}

object Siap_detallado_retiro {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdmWrites = new Writes[Siap_detallado_retiro] {
    def writes(sdm: Siap_detallado_retiro) = Json.obj(
      "repo_fecharecepcion" -> sdm.repo_fecharecepcion,
      "repo_fechasolucion" -> sdm.repo_fechasolucion,
      "repo_consecutivo" -> sdm.repo_consecutivo,
      "aap_id" -> sdm.aap_id,
      "aap_direccion" -> sdm.aap_direccion,
      "barr_descripcion" -> sdm.barr_descripcion,
      "vereda" -> sdm.vereda,
      "parque" -> sdm.parque,
      "aaus_descripcion" -> sdm.aaus_descripcion,
      "aacu_descripcion" -> sdm.aacu_descripcion,
      "aamo_descripcion" -> sdm.aamo_descripcion,
      "aap_potencia" -> sdm.aap_potencia,
      "aap_tecnologia" -> sdm.aap_tecnologia,
      "aaco_descripcion" -> sdm.aaco_descripcion,
      "reubicado" -> sdm.reubicado,
      "tire_descripcion" -> sdm.tire_descripcion
    )
  }

  val set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[Int]]("repo_consecutivo") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") ~
      get[Option[String]]("reubicado") ~
      get[Option[String]]("tire_descripcion") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            repo_consecutivo ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion ~
            reubicado ~
            tire_descripcion =>
        new Siap_detallado_retiro(
          repo_fecharecepcion,
          repo_fechasolucion,
          repo_consecutivo,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion,
          reubicado,
          tire_descripcion
        )

    }
  }
}

object Siap_retiro_reubicacion_a {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd HH:mm")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy/MM/dd HH:mm")

  implicit val sdmWrites = new Writes[Siap_retiro_reubicacion_a] {
    def writes(sdm: Siap_retiro_reubicacion_a) = Json.obj(
      "repo_fecharecepcion" -> sdm.repo_fecharecepcion,
      "repo_fechasolucion" -> sdm.repo_fechasolucion,
      "repo_fechadigitacion" -> sdm.repo_fechadigitacion,
      "repo_consecutivo" -> sdm.repo_consecutivo,
      "aap_id" -> sdm.aap_id,
      "aap_direccion" -> sdm.aap_direccion,
      "barr_descripcion" -> sdm.barr_descripcion,
      "vereda" -> sdm.vereda,
      "parque" -> sdm.parque,
      "aaus_descripcion" -> sdm.aaus_descripcion,
      "aacu_descripcion" -> sdm.aacu_descripcion,
      "aamo_descripcion" -> sdm.aamo_descripcion,
      "aap_potencia" -> sdm.aap_potencia,
      "aap_tecnologia" -> sdm.aap_tecnologia,
      "aaco_descripcion" -> sdm.aaco_descripcion,
      "enbaja" -> sdm.enbaja
    )
  }

  val set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[Int]]("repo_consecutivo") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") ~
      get[Option[String]]("enbaja") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            repo_fechadigitacion ~
            repo_consecutivo ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion ~
            enbaja =>
        new Siap_retiro_reubicacion_a(
          repo_fecharecepcion,
          repo_fechasolucion,
          repo_fechadigitacion,
          repo_consecutivo,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion,
          enbaja
        )

    }
  }
}

object Siap_retiro_reubicacion_b {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd HH:mm")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy/MM/dd HH:mm")

  implicit val sdrWrites = new Writes[Siap_retiro_reubicacion_b] {
    def writes(srm: Siap_retiro_reubicacion_b) = Json.obj(
      "repo_fecharecepcion" -> srm.repo_fecharecepcion,
      "repo_fechasolucion" -> srm.repo_fechasolucion,
      "repo_fechadigitacion" -> srm.repo_fechadigitacion,
      "reti_descripcion" -> srm.reti_descripcion,
      "aap_rte" -> srm.aap_rte,
      "aap_id" -> srm.aap_id,
      "aap_direccion" -> srm.aap_direccion,
      "barr_descripcion" -> srm.barr_descripcion,
      "vereda" -> srm.vereda,
      "parque" -> srm.parque,
      "aaus_descripcion" -> srm.aaus_descripcion,
      "aacu_descripcion" -> srm.aacu_descripcion,
      "aamo_descripcion" -> srm.aamo_descripcion,
      "aap_potencia" -> srm.aap_potencia,
      "aap_tecnologia" -> srm.aap_tecnologia,
      "aaco_descripcion" -> srm.aaco_descripcion
    )
  }

  val set = {
    get[Option[DateTime]]("repo_fecharecepcion") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[DateTime]]("repo_fechadigitacion") ~
      get[Option[String]]("reti_descripcion") ~
      get[Option[String]]("aap_rte") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("parque") ~
      get[Option[String]]("aaus_descripcion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[String]]("aamo_descripcion") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[String]]("aaco_descripcion") map {
      case repo_fecharecepcion ~
            repo_fechasolucion ~
            repo_fechadigitacion ~
            reti_descripcion ~
            aap_rte ~
            aap_id ~
            aap_direccion ~
            barr_descripcion ~
            vereda ~
            parque ~
            aaus_descripcion ~
            aacu_descripcion ~
            aamo_descripcion ~
            aap_potencia ~
            aap_tecnologia ~
            aaco_descripcion =>
        new Siap_retiro_reubicacion_b(
          repo_fecharecepcion,
          repo_fechasolucion,
          repo_fechadigitacion,
          reti_descripcion,
          aap_rte,
          aap_id,
          aap_direccion,
          barr_descripcion,
          vereda,
          parque,
          aaus_descripcion,
          aacu_descripcion,
          aamo_descripcion,
          aap_potencia,
          aap_tecnologia,
          aaco_descripcion
        )
    }
  }
}

object Siap_retiro_reubicacion {
  implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy/MM/dd")
  implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy/MM/dd")

  implicit val sdrWrites = new Writes[Siap_retiro_reubicacion] {
    def writes(srm: Siap_retiro_reubicacion) = Json.obj(
      "retiro" -> srm.retiro,
      "reubicacion" -> srm.reubicacion
    )
  }
}

class InformeRepository @Inject()(
    dbapi: DBApi,
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository,
    municipioService: MunicipioRepository
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

  private val siap_resumen_material_set = {
    get[Option[String]]("elem_codigo") ~
      get[Option[String]]("elem_descripcion") ~
      get[Option[Double]]("even_cantidad_retirado") ~
      get[Option[Double]]("even_cantidad_instalado") map {
      case elem_codigo ~
            elem_descripcion ~
            even_cantidad_retirado ~
            even_cantidad_instalado =>
        new Siap_resumen_material(
          elem_codigo,
          elem_descripcion,
          even_cantidad_retirado,
          even_cantidad_instalado
        )
    }
  }

  private val siap_resumen_material_reporte_set = {
    get[Option[String]]("elem_codigo") ~
      get[Option[String]]("elem_descripcion") ~
      get[Option[String]]("reti_descripcion") ~
      get[Option[Double]]("even_cantidad_retirado") ~
      get[Option[Double]]("even_cantidad_instalado") map {
      case elem_codigo ~
            elem_descripcion ~
            reti_descripcion ~
            even_cantidad_retirado ~
            even_cantidad_instalado =>
        new Siap_resumen_material_reporte(
          elem_codigo,
          elem_descripcion,
          reti_descripcion,
          even_cantidad_retirado,
          even_cantidad_instalado
        )
    }
  }

  private val siap_detallado_material_set = {
    get[Option[String]]("elem_codigo") ~
      get[Option[String]]("elem_descripcion") ~
      get[Option[String]]("reti_descripcion") ~
      get[Option[scala.Long]]("repo_consecutivo") ~
      get[Option[DateTime]]("repo_fechasolucion") ~
      get[Option[String]]("even_codigo_retirado") ~
      get[Option[Double]]("even_cantidad_retirado") ~
      get[Option[String]]("even_codigo_instalado") ~
      get[Option[Double]]("even_cantidad_instalado") map {
      case elem_codigo ~
            elem_descripcion ~
            reti_descripcion ~
            repo_consecutivo ~
            repo_fechasolucion ~
            even_codigo_retirado ~
            even_cantidad_retirado ~
            even_codigo_instalado ~
            even_cantidad_instalado =>
        new Siap_detallado_material(
          elem_codigo,
          elem_descripcion,
          reti_descripcion,
          repo_consecutivo,
          repo_fechasolucion,
          even_codigo_retirado,
          even_cantidad_retirado,
          even_codigo_instalado,
          even_cantidad_instalado
        )
    }
  }

  def siap_visita_por_barrio_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_visita_por_barrio]] =
    Future[Iterable[Siap_visita_por_barrio]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)

        SQL("""SELECT b.barr_descripcion, count(r) as visitas FROM
                siap.reporte r 
                LEFT JOIN siap.barrio b ON b.barr_id = r.barr_id
                WHERE r.repo_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and r.rees_id <> 9 and r.empr_id = {empr_id} and r.reti_id = 1
                GROUP BY b.barr_descripcion
                ORDER BY b.barr_descripcion
            """)
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_visita_por_barrio._set *)
      }
    }

  /**
    * return sql response to excel export
    * resumen material reporte
    */
  def siap_resumen_material_reporte_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_resumen_material_reporte]] =
    Future[Iterable[Siap_resumen_material_reporte]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)

        SQL("""SELECT e.elem_codigo, e.reti_id, e.elem_descripcion, e.reti_descripcion,
                           SUM(e.even_cantidad_retirado) as even_cantidad_retirado, 
                           SUM(e.even_cantidad_instalado) as even_cantidad_instalado
				    FROM 
(SELECT e.elem_codigo, p.reti_id, e.elem_descripcion, p.reti_descripcion,
                           t.even_cantidad_retirado as even_cantidad_retirado, 
                           t.even_cantidad_instalado as even_cantidad_instalado
                    FROM siap.reporte r
                    LEFT JOIN siap.reporte_adicional a on a.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_tipo p on p.reti_id = r.reti_id
                    LEFT JOIN siap.reporte_evento t on t.repo_id = r.repo_id
                    INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                    WHERE r.repo_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and r.rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
UNION ALL 
SELECT e.elem_codigo, 0 as reti_id, e.elem_descripcion,  CONCAT('OBRA', ' ', r.obra_nombre) as reti_descripcion,
                           t.even_cantidad_retirado as even_cantidad_retirado, 
                           t.even_cantidad_instalado as even_cantidad_instalado
                    FROM siap.obra r
                    LEFT JOIN siap.obra_evento t on t.obra_id = r.obra_id
                    INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                    WHERE r.obra_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and r.rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
) e
GROUP BY e.reti_id, e.elem_codigo, e.elem_descripcion, e.reti_descripcion
ORDER BY e.reti_id, e.elem_codigo""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(siap_resumen_material_reporte_set *)
      }
    }

  /**
    * return sql response to excel export
    * resumen material reporte
    */
  def siap_resumen_material_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_resumen_material]] =
    Future[Iterable[Siap_resumen_material]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)

        SQL("""SELECT e.elem_codigo, e.elem_descripcion,
                        SUM(e.even_cantidad_retirado) as even_cantidad_retirado, 
                        SUM(e.even_cantidad_instalado) as even_cantidad_instalado
						FROM 
                        (SELECT e.elem_codigo, e.elem_descripcion,
                        t.even_cantidad_retirado as even_cantidad_retirado, 
                        t.even_cantidad_instalado as even_cantidad_instalado
                        FROM siap.reporte r
                        LEFT JOIN siap.reporte_adicional a on a.repo_id = r.repo_id
                        LEFT JOIN siap.reporte_tipo p on p.reti_id = r.reti_id
                        LEFT JOIN siap.reporte_evento t on t.repo_id = r.repo_id
                        INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                        WHERE r.repo_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
                        UNION ALL
                        SELECT e.elem_codigo, e.elem_descripcion,
                        t.even_cantidad_retirado as even_cantidad_retirado, 
                        t.even_cantidad_instalado as even_cantidad_instalado
                        FROM siap.obra r
                        LEFT JOIN siap.obra_evento t on t.obra_id = r.obra_id
                        INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                        WHERE r.obra_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
                        ) e
                        GROUP BY e.elem_codigo, e.elem_descripcion
                        ORDER BY e.elem_codigo""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(siap_resumen_material_set *)
      }
    }

  /**
    * return sql response to excel export
    * resumen material reporte
    */
  def siap_detallado_material_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_material]] =
    Future[Iterable[Siap_detallado_material]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""SELECT e.elem_codigo, e.elem_descripcion, p.reti_descripcion, 
                          r.repo_consecutivo, 
                          r.repo_fechasolucion, 
                          t.even_codigo_retirado, t.even_cantidad_retirado, 
                          t.even_codigo_instalado, t.even_cantidad_instalado 
                    FROM siap.reporte r
                    LEFT JOIN siap.reporte_adicional a on a.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_tipo p on p.reti_id = r.reti_id
                    LEFT JOIN siap.reporte_evento t on t.repo_id = r.repo_id
                    INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                    WHERE r.repo_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and r.rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
                    UNION ALL
                    SELECT e.elem_codigo, e.elem_descripcion,  CONCAT('OBRA', ' ', r.obra_nombre) as reti_descripcion, 
                          r.obra_consecutivo as repo_consecutivo, 
                          r.obra_fechasolucion, 
                          t.even_codigo_retirado, t.even_cantidad_retirado, 
                          t.even_codigo_instalado, t.even_cantidad_instalado 
                    FROM siap.obra r
                    LEFT JOIN siap.obra_evento t on t.obra_id = r.obra_id
                    INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                    WHERE r.obra_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and r.rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
                    ORDER BY reti_descripcion, elem_codigo""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(siap_detallado_material_set *)
      }
    }

  /**
    * return sql response to excel export
    * resumen material municipio obra reporte
    */
    def siap_informe_detallado_material_muob_xls(
      muob_consecutivo: Int,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_material]] =
    Future[Iterable[Siap_detallado_material]] {
      db.withConnection { implicit connection =>
        /*
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        */
        SQL("""SELECT e.elem_codigo, e.elem_descripcion, p.reti_descripcion, 
                          r.repo_consecutivo, 
                          r.repo_fechasolucion, 
                          t.even_codigo_retirado, t.even_cantidad_retirado, 
                          t.even_codigo_instalado, t.even_cantidad_instalado 
                    FROM siap.reporte r
                    LEFT JOIN siap.reporte_adicional a on a.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_tipo p on p.reti_id = r.reti_id
                    LEFT JOIN siap.reporte_evento t on t.repo_id = r.repo_id
                    INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                    WHERE a.muot_id = {muot_id} and r.rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
                    UNION ALL
                    SELECT e.elem_codigo, e.elem_descripcion,  CONCAT('OBRA', ' ', r.obra_nombre) as reti_descripcion, 
                          r.obra_consecutivo as repo_consecutivo, 
                          r.obra_fechasolucion, 
                          t.even_codigo_retirado, t.even_cantidad_retirado, 
                          t.even_codigo_instalado, t.even_cantidad_instalado 
                    FROM siap.obra r
                    LEFT JOIN siap.obra_evento t on t.obra_id = r.obra_id
                    INNER JOIN siap.elemento e on e.elem_id = t.elem_id
                    WHERE r.muot_id = {muot_id} and r.rees_id <> 9 and t.even_estado <> 9 and r.empr_id = {empr_id}
                    ORDER BY reti_descripcion, elem_codigo""")
          .on(
            'muot_id -> muob_consecutivo,
            'empr_id -> empr_id
          ).as(siap_detallado_material_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_material(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { connection =>
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_detallado_material.jasper"
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPR_ID", Long.valueOf(empresa.empr_id.get))
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      os =
        JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
      os
    }
  }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_resumen_material_reporte(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { connection =>
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_resumen_material_reporte.jasper"
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPR_ID", Long.valueOf(empresa.empr_id.get))
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      os =
        JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
      os
    }
  }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_resumen_material(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { connection =>
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_resumen_material.jasper"
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPR_ID", Long.valueOf(empresa.empr_id.get))
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      os =
        JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
      os
    }
  }

  /**
    *  imprimir
    * @param fecha_toma: scala.Long
    * @param usua_id: scala.Long
    * @param empr_id: scala.Long
    * @return OutputStream
    */
  def siap_reporte_por_uso_xls(
      fecha_toma: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { connection =>
      //var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_por_cuenta.jasper"
      var ft = Calendar.getInstance()
      ft.setTimeInMillis(fecha_toma)
      ft.set(Calendar.MILLISECOND, 0)
      ft.set(Calendar.SECOND, 0)
      ft.set(Calendar.MINUTE, 0)
      ft.set(Calendar.HOUR, 0)

      var os = new ByteArrayOutputStream()
      var exporter = new JRXlsxExporter(); // initialize exporter
      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put("FECHA_TOMA", new java.sql.Date(ft.getTimeInMillis()))
      reportParams.put("EMPR_ID", Long.valueOf(empresa.empr_id.get))
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put(
        "USUARIO",
        usuario.usua_nombre + " " + usuario.usua_apellido
      )
      var jasperPrint =
        JasperFillManager.fillReport(compiledFile, reportParams, connection)
      exporter.setExporterInput(new SimpleExporterInput(jasperPrint)); // set compiled report as input
      exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(os)) // set output file via path with filename
      var configuration = new SimpleXlsxReportConfiguration();
      configuration.setOnePagePerSheet(false) // setup configuration
      configuration.setDetectCellType(true)
      configuration.setAutoFitPageHeight(true)
      exporter.setConfiguration(configuration) // set configuration
      exporter.exportReport()

      // os = JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
      os.toByteArray
    }
  }

  def siap_inventario_total(
      fecha_corte: scala.Long,
      empr_id: scala.Long
  ): scala.Long = {
    db.withConnection { implicit connection =>
      var result =
        SQL("""SELECT
	                  COUNT(*)
                    FROM siap.aap a
                    LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id
                    LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id
                    LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                    LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                    LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                    LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                    LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                    LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                    LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                    WHERE a.aap_fechatoma <= {fecha_corte} and a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id <> 9999999
                    """)
          .on(
            'fecha_corte -> new DateTime(fecha_corte),
            'empr_id -> empr_id
          )
          .as(SqlParser.scalar[scala.Long].single)
      result
    }
  }

  def siap_inventario(
      fecha_corte: scala.Long,
      empr_id: scala.Long,
      page_size: scala.Long,
      current_page: scala.Long
  ): Future[Iterable[Siap_inventario]] = Future[Iterable[Siap_inventario]] {
    var _list = new ListBuffer[Siap_inventario]()
    db.withConnection { implicit connection =>
      var resultSet =
        SQL("""SELECT
	                        a.aap_id,
                          a.aap_apoyo,
                          s.esta_descripcion,
	                        a.aap_direccion,
	                        b.barr_descripcion,
	                        tb.tiba_descripcion,
	                        us.aaus_descripcion,
	                        a.aap_modernizada,
	                        d.aap_modernizada_anho,
	                        tc.aatc_descripcion,
	                        a.aap_medidor,
	                        co.aaco_descripcion,
	                        ma.aama_descripcion,
	                        mo.aamo_descripcion,
	                        tp.tipo_descripcion,
	                        d.aap_poste_altura,
	                        d.aap_brazo,
	                        d.aap_collarin,
	                        d.aap_potencia,
	                        d.aap_tecnologia,
	                        d.aap_rte,
	                        d.aap_poste_propietario,
	                        e.aap_bombillo,
	                        e.aap_balasto,
	                        e.aap_arrancador,
	                        e.aap_condensador,
                          e.aap_fotocelda,
                          to_char(m.medi_id, '0000') as medi_codigo,
                          m.medi_numero,
                          (CASE WHEN a.aaco_id = 1 THEN cu.aacu_descripcion WHEN a.aaco_id = 2 THEN mcu.aacu_descripcion ELSE '' END) AS aacu_descripcion,
                          to_char(t.tran_id, '0000') as tran_codigo,
                          t.tran_numero
                    FROM siap.aap a
                    LEFT JOIN siap.estado s ON s.esta_id = a.esta_id
                    LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id AND d.empr_id = a.empr_id
                    LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id AND e.empr_id = a.empr_id
                    LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                    LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                    LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                    LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                    LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                    LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                    LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                    LEFT JOIN siap.aap_medidor am ON am.aap_id = a.aap_id AND am.empr_id = a.empr_id
                    LEFT JOIN siap.medidor m ON m.medi_id = am.medi_id and m.empr_id = am.empr_id
                    LEFT JOIN siap.aap_cuentaap mcu ON mcu.aacu_id = m.aacu_id and mcu.empr_id = m.empr_id
                    LEFT JOIN siap.aap_transformador at ON at.aap_id = a.aap_id and at.empr_id = a.empr_id
                    LEFT JOIN siap.transformador t ON t.tran_id = at.tran_id and t.empr_id = at.empr_id
                    WHERE a.aap_fechatoma <= {fecha_corte} and a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id <> 9999999
                    ORDER BY a.aap_id ASC LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)
                    """)
          .on(
            'fecha_corte -> new DateTime(fecha_corte),
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(Siap_inventario.Siap_inventario_set *)
      resultSet.map { i =>
        val a = new Siap_inventario_a(
          i.aap_id,
          i.aap_apoyo,
          i.esta_descripcion,
          i.aap_direccion,
          i.barr_descripcion,
          i.tiba_descripcion,
          i.aaus_descripcion,
          i.aap_modernizada,
          i.aap_modernizada_anho,
          i.aatc_descripcion,
          i.aap_medidor,
          i.aaco_descripcion,
          i.aama_descripcion,
          i.aamo_descripcion
        )
        val b = new Siap_inventario_b(
          i.tipo_descripcion,
          i.aap_poste_altura,
          i.aap_brazo,
          i.aap_collarin,
          i.aap_potencia,
          i.aap_tecnologia,
          i.aap_rte,
          i.aap_poste_propietario,
          i.aap_bombillo,
          i.aap_balasto,
          i.aap_arrancador,
          i.aap_condensador,
          i.aap_fotocelda,
          i.medi_codigo,
          i.medi_numero,
          i.aacu_descripcion,
          i.tran_codigo,
          i.tran_numero
        )
        val si = new Siap_inventario(a, b)
        _list += si
      }
      _list
    }
  }

  def siap_inventario_xls(fecha_corte: Long, empr_id: Long): Array[Byte] = {
    db.withConnection { implicit connection =>
      val dt = new DateTime(fecha_corte)
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val sheet = Sheet(
        name = "Inventario" + fmt.print(dt),
        rows = {
          val headerRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "Código",
              "Apoyo",
              "Estado",
              "Dirección",
              "Barrio",
              "Sector",
              "Uso",
              "Modernizada",
              "Año Modernizada",
              "Tipo Luminaria",
              "Medidor",
              "Tipo Medida",
              "Marca",
              "Modelo",
              "Poste",
              "Poste Altura",
              "Poste Propietario",
              "Brazo",
              "Collarin",
              "Potencia",
              "Tecnologia",
              "Reporte Técnico",
              "Bombillo",
              "Balasto",
              "Arrancador",
              "Condensador",
              "Fotocelda",
              "Medidor Código",
              "Medidor Número",
              "Cuenta Alumbrado",
              "Transformador Código",
              "Transformador Número"              
            )
          val resultSet =
            SQL("""SELECT
	                        a.aap_id,
                          a.aap_apoyo,
                          s.esta_descripcion,
	                        a.aap_direccion,
	                        b.barr_descripcion,
	                        tb.tiba_descripcion,
	                        us.aaus_descripcion,
	                        a.aap_modernizada,
	                        d.aap_modernizada_anho,
	                        tc.aatc_descripcion,
	                        a.aap_medidor,
	                        co.aaco_descripcion,
	                        ma.aama_descripcion,
	                        mo.aamo_descripcion,
	                        tp.tipo_descripcion,
	                        d.aap_poste_altura,
	                        d.aap_brazo,
	                        d.aap_collarin,
	                        d.aap_potencia,
	                        d.aap_tecnologia,
	                        d.aap_rte,
	                        d.aap_poste_propietario,
	                        e.aap_bombillo,
	                        e.aap_balasto,
	                        e.aap_arrancador,
	                        e.aap_condensador,
                          e.aap_fotocelda,
                          to_char(m.medi_id, '0000') as medi_codigo,
                          m.medi_numero,
                          (CASE WHEN a.aaco_id = 1 THEN cu.aacu_descripcion WHEN a.aaco_id = 2 THEN mcu.aacu_descripcion ELSE '' END) AS aacu_descripcion,
                          to_char(t.tran_id, '0000') as tran_codigo,
                          t.tran_numero
                    FROM siap.aap a
                    LEFT JOIN siap.estado s ON s.esta_id = a.esta_id
                    LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id AND d.empr_id = a.empr_id
                    LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id AND e.empr_id = a.empr_id
                    LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                    LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                    LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                    LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                    LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                    LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                    LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                    LEFT JOIN siap.aap_medidor am ON am.aap_id = a.aap_id AND am.empr_id = a.empr_id
                    LEFT JOIN siap.medidor m ON m.medi_id = am.medi_id and m.empr_id = am.empr_id
                    LEFT JOIN siap.aap_cuentaap mcu ON mcu.aacu_id = m.aacu_id and mcu.empr_id = m.empr_id
                    LEFT JOIN siap.aap_transformador at ON at.aap_id = a.aap_id and at.empr_id = a.empr_id
                    LEFT JOIN siap.transformador t ON t.tran_id = at.tran_id and t.empr_id = at.empr_id
                    WHERE a.aap_fechatoma <= {fecha_corte} and a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id <> 9999999
                    ORDER BY a.aap_id ASC
                    """)
              .on(
                'fecha_corte -> new DateTime(fecha_corte),
                'empr_id -> empr_id
              )
              .as(Siap_inventario.Siap_inventario_set *)
          val rows = resultSet.map {
            i =>
              com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  i.aap_id match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_apoyo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.esta_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_direccion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.barr_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tiba_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aaus_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_modernizada match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_modernizada_anho match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aatc_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_medidor match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aaco_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aama_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aamo_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tipo_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_poste_altura match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_poste_propietario match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_brazo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_collarin match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_potencia match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_tecnologia match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_rte match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_bombillo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_balasto match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_arrancador match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_condensador match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_fotocelda match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.medi_codigo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.medi_numero match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aacu_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tran_codigo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tran_numero match {
                    case Some(value) => value
                    case None        => ""
                  }               
                )
          }
          headerRow :: rows.toList
        }
      )
      println("Escribiendo en el Stream")
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      Workbook(sheet).writeToOutputStream(os)
      println("Stream Listo")
      os.toByteArray
    }
  }

  def siap_inventario_web(
      fecha_corte: Long,
      empr_id: Long
  ): Future[Iterable[Inventario]] = Future[Iterable[Inventario]] {
    db.withConnection { implicit connection =>
      val dt = new DateTime(fecha_corte)
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val resultSet =
        SQL("""SELECT
	                        a.aap_id as Codigo,
	                        a.aap_apoyo as Apoyo,
	                        a.aap_direccion as Direccion,
	                        b.barr_descripcion as Barrio,
	                        tb.tiba_descripcion as Sector,
	                        us.aaus_descripcion as Uso,
	                        a.aap_modernizada as Modernizada,
	                        d.aap_modernizada_anho as Anho_Modernizada,
	                        tc.aatc_descripcion as Tipo_Luminaria,
	                        a.aap_medidor as Medidor,
	                        co.aaco_descripcion as Tipo_Medida,
	                        ma.aama_descripcion as Marca,
	                        mo.aamo_descripcion as Modelo,
	                        cu.aacu_descripcion as Cuenta,
	                        tp.tipo_descripcion as Tipo_Poste,
                            d.aap_poste_altura as Poste_Altura,
                            d.aap_poste_propietario as Poste_Propietario,
	                        d.aap_potencia as Potencia,
	                        d.aap_tecnologia as Tecnologia,
                    FROM siap.aap a
                    LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id
                    LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id
                    LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                    LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                    LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                    LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                    LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                    LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                    LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                    WHERE a.aap_fechatoma <= {fecha_corte} and a.empr_id = {empr_id} and esta_id <> 9 and a.aap_id <> 9999999
                    ORDER BY a.aap_id ASC
                    """)
          .on(
            'fecha_corte -> new DateTime(fecha_corte),
            'empr_id -> empr_id
          )
          .as(Siap_inventario.Siap_inventario_set *)
      resultSet
    }
  }

  def siap_inventario_filtro_xls(
      fecha_corte: Long,
      empr_id: Long,
      orderby: String,
      filter: String
  ): Array[Byte] = {
    db.withConnection { implicit connection =>
      val dt = new DateTime(fecha_corte)
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val sheet = Sheet(
        name = "Inventario" + fmt.print(dt),
        rows = {
          val headerRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "Código",
              "Apoyo",
              "Estado",
              "Dirección",
              "Barrio",
              "Sector",
              "Uso",
              "Modernizada",
              "Año Modernizada",
              "Tipo Luminaria",
              "Medidor",
              "Tipo Medida",
              "Marca",
              "Modelo",
              "Poste",
              "Poste Altura",
              "Poste Propietario",
              "Brazo",
              "Collarin",
              "Potencia",
              "Tecnologia",
              "Reporte Técnico",
              "Bombillo",
              "Balasto",
              "Arrancador",
              "Condensador",
              "Fotocelda",
              "Medidor Código",
              "Medidor Número",
              "Cuenta Alumbrado",
              "Transformador Código",
              "Transformador Número"
            )
          var query = """SELECT
                          a.aap_id,
                          a.aap_apoyo,
                          s.esta_descripcion,
                          a.aap_direccion,
                          b.barr_descripcion,
                          tb.tiba_descripcion,
                          us.aaus_descripcion,
                          a.aap_modernizada,
                          d.aap_modernizada_anho,
                          tc.aatc_descripcion,
                          a.aap_medidor,
                          co.aaco_descripcion,
                          ma.aama_descripcion,
                          mo.aamo_descripcion,
                          cu.aacu_descripcion,
                          tp.tipo_descripcion,
                          d.aap_poste_altura,
                          d.aap_brazo,
                          d.aap_collarin,
                          d.aap_potencia,
                          d.aap_tecnologia,
                          d.aap_rte,
                          d.aap_poste_propietario,
                          e.aap_bombillo,
                          e.aap_balasto,
                          e.aap_arrancador,
                          e.aap_condensador,
                          e.aap_fotocelda,
                          to_char(m.medi_id, '0000') as medi_codigo,
                          m.medi_numero,
                          (CASE WHEN a.aaco_id = 1 THEN cu.aacu_descripcion WHEN a.aaco_id = 2 THEN mcu.aacu_descripcion ELSE '' END) AS aacu_descripcion,
                          to_char(t.tran_id, '0000') as tran_codigo,
                          t.tran_numero
                        FROM siap.aap a
                        LEFT JOIN siap.estado s ON s.esta_id = a.esta_id
                        LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id AND d.empr_id = a.empr_id
                        LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id AND e.empr_id = a.empr_id
                        LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                        LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                        LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                        LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                        LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                        LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                        LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                        LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                        LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                        LEFT JOIN siap.aap_medidor am ON am.aap_id = a.aap_id AND am.empr_id = a.empr_id
                        LEFT JOIN siap.medidor m ON m.medi_id = am.medi_id and m.empr_id = am.empr_id
                        LEFT JOIN siap.aap_cuentaap mcu ON mcu.aacu_id = m.aacu_id and mcu.empr_id = m.empr_id                    
                        LEFT JOIN siap.aap_transformador at ON at.aap_id = a.aap_id and at.empr_id = a.empr_id
                        LEFT JOIN siap.transformador t ON t.tran_id = at.tran_id and t.empr_id = at.empr_id
                        WHERE a.aap_fechatoma <= {fecha_corte} and a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id <> 9999999
                      """
          println("filtro a aplicar:" + filter)
          if (!filter.isEmpty) {
            query = query + " and " + filter
          }
          println("orderby a aplicar: " + orderby)
          if (!orderby.isEmpty) {
            query = query + s" ORDER BY $orderby"
          }  
          val resultSet =
            SQL(query)
              .on(
                'fecha_corte -> new DateTime(fecha_corte),
                'empr_id -> empr_id
              )
              .as(Siap_inventario.Siap_inventario_set *)
          val rows = resultSet.map {
            i =>
              com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  i.aap_id match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_apoyo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.esta_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_direccion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.barr_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tiba_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aaus_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_modernizada match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_modernizada_anho match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aatc_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_medidor match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aaco_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aama_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aamo_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tipo_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_poste_altura match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_poste_propietario match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_brazo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_collarin match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_potencia match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_tecnologia match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_rte match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_bombillo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_balasto match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_arrancador match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_condensador match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aap_fotocelda match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.medi_codigo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.medi_numero match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.aacu_descripcion match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tran_codigo match {
                    case Some(value) => value
                    case None        => ""
                  },
                  i.tran_numero match {
                    case Some(value) => value
                    case None        => ""
                  }
                )
          }
          headerRow :: rows.toList
        }
      )
      println("Escribiendo en el Stream")
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      Workbook(sheet).writeToOutputStream(os)
      println("Stream Listo")
      os.toByteArray
    }
  }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_expansion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_expansion]] =
    Future[Iterable[Siap_detallado_expansion]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL(
          """select distinct r.repo_fechasolucion, r.repo_fechasolucion as repo_fechadigitacion, r.repo_consecutivo::text as aap_rte, a.aap_id, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque, au.aaus_descripcion, ac.aacu_descripcion, r.repo_descripcion, tp.tipo_descripcion, case when tp.tipo_id = 1 then 'X' else '' end as concreto, case when tp.tipo_id = 2 then 'X'  else '' end as metalico, mo.aamo_descripcion, d.aap_potencia, d.aap_tecnologia, co.aaco_descripcion, case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo, u.urba_descripcion from siap.reporte r 
                    left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
                    left join siap.reporte_direccion e on e.repo_id = r.repo_id
                    left join siap.aap a on a.aap_id = e.aap_id
                    left join siap.aap_adicional d on d.aap_id = a.aap_id
                    left join siap.barrio b on b.barr_id = a.barr_id
                    left join siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    left join siap.aap_conexion co on co.aaco_id = a.aaco_id
                    left join siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                    left join siap.aap_uso au on au.aaus_id = a.aaus_id
                    left join siap.aap_cuentaap ac on ac.aacu_id = a.aacu_id
                    left join siap.urbanizadora u on u.urba_id = ra.urba_id
                    where r.reti_id = 2 and ra.repo_tipo_expansion <> 4 and r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.empr_id = {empr_id} and a.aap_id <> 9999999 and e.even_estado <> 9
                    order by r.repo_fechasolucion, r.repo_descripcion, a.aap_id"""
        ).on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_expansion.siap_detallado_expansion_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_reubicacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_reubicacion]] =
    Future[Iterable[Siap_detallado_reubicacion]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        /*
            SQL("""select distinct
                    r.repo_fechasolucion,
                    ra.repo_fechadigitacion,
                    a.aap_id,
                    e.even_direccion_anterior as aap_direccion_anterior,
                    rb.barr_descripcion as barr_descripcion_anterior,
                    a.aap_direccion,
                    b.barr_descripcion,
                    CONCAT(to_char(b.tiba_id, '00'),'-',to_char(b.barr_id, '000'),'-',FORMAT('%s', a.aap_id)) as aap_codigo,
                    mo.aamo_descripcion as aamo_descripcion_anterior,
                    d.aap_potencia as aap_potencia_anterior,
                    d.aap_tecnologia as aap_tecnologia_anterior,
                    mo.aamo_descripcion,
                    d.aap_potencia,
                    d.aap_tecnologia,
                    co.aaco_descripcion from siap.reporte r
                    left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
                    left join siap.reporte_direccion e on e.repo_id = r.repo_id
                    left join siap.aap a on a.aap_id = e.aap_id
                    left join siap.aap_adicional d on d.aap_id = a.aap_id
                    left join siap.barrio b on b.barr_id = a.barr_id
					left join siap.barrio rb on rb.barr_id = e.barr_id_anterior
                    left join siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    left join siap.aap_conexion co on co.aaco_id = a.aaco_id
                    where r.reti_id = 3 and r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.empr_id = {empr_id} and r.rees_id = 3 and a.aap_id <> 9999999
                    order by r.repo_fechasolucion, a.aap_id""").on(
                    'fecha_inicial -> fi.getTime(),
                    'fecha_final -> ff.getTime(),
                    'empr_id -> empr_id
                    ).as(Siap_detallado_reubicacion.siap_detallado_reubicacion_set *)
         */
        SQL("""select distinct 
                    r.repo_fechasolucion,
                    ra.repo_fechadigitacion, 
                    r.repo_consecutivo::text as aap_rte,
                    a.aap_id, 
                    e.even_direccion_anterior as aap_direccion_anterior, 
                    rb.barr_descripcion as barr_descripcion_anterior,
                    case when rb.tiba_id = 2 then 'X' else '' end as vereda_anterior, case when rb.tiba_id = 5 then 'X' else '' end as parque_anterior,                    
                    a.aap_direccion,
                    b.barr_descripcion,
                    case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
                    aua.aaus_descripcion as aaus_descripcion_anterior,
                    aca.aacu_descripcion as aacu_descripcion_anterior,
                    a.aap_id::text as aap_codigo,
                    mo.aamo_descripcion as aamo_descripcion_anterior, 
                    d.aap_potencia_anterior, 
                    d.aap_tecnologia_anterior,
                    (SELECT co.aaco_descripcion
                    FROM siap.reporte r
                    LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_direccion d on d.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_direccion_dato dd on dd.repo_id = r.repo_id and dd.even_id = d.even_id and dd.aap_id = d.aap_id
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = dd.aaco_id_anterior
                    WHERE r.reti_id = 8 AND r.empr_id = {empr_id} AND r.rees_id = 3 and d.even_estado <> 9 and d.aap_id = a.aap_id
                    ORDER BY r.repo_fechasolucion desc, r.repo_consecutivo, a.aap_id
                    LIMIT 1) as aaco_descripcion_anterior,
                    mo.aamo_descripcion, 
                    d.aap_potencia,
                    d.aap_tecnologia,
                    au.aaus_descripcion,
                    ac.aacu_descripcion, 
                    co.aaco_descripcion,                   
                    case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo
                    from siap.reporte r 
                    left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
                    left join siap.reporte_direccion e on e.repo_id = r.repo_id
                    left join siap.reporte_direccion_dato d on d.repo_id = e.repo_id and d.even_id = e.even_id
					left join siap.reporte_direccion_dato_adicional da on da.repo_id = d.repo_id and da.even_id = d.even_id
                    left join siap.aap a on a.aap_id = e.aap_id
                    left join siap.aap_adicional ad on ad.aap_id = a.aap_id
                    left join siap.barrio b on b.barr_id = a.barr_id
					left join siap.barrio rb on rb.barr_id = e.barr_id_anterior
                    left join siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                    left join siap.aap_conexion co on co.aaco_id = d.aaco_id
                    left join siap.aap_conexion coa on coa.aaco_id = d.aaco_id
                    left join siap.aap_uso aua on aua.aaus_id = da.aaus_id_anterior
                    left join siap.aap_cuentaap aca on aca.aacu_id = da.aacu_id_anterior                   
                    left join siap.aap_uso au on au.aaus_id = da.aaus_id
                    left join siap.aap_cuentaap ac on ac.aacu_id = da.aacu_id                     
                    where r.reti_id = 3 and r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.empr_id = {empr_id} and r.rees_id = 3 and a.aap_id <> 9999999 and e.even_estado <> 9
                    order by r.repo_fechasolucion, a.aap_id""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_reubicacion.siap_detallado_reubicacion_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
    def siap_detallado_cambio_medida_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
    ): Future[Iterable[Siap_detallado_cambio_medida]] =
    Future[Iterable[Siap_detallado_cambio_medida]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""select distinct
            r.repo_fechasolucion,
            ra.repo_fechadigitacion,
            a.aap_id,
            r.repo_consecutivo::text as aap_rte,
            rd.even_direccion_anterior as aap_direccion_anterior,
            ba.barr_descripcion as barr_descripcion_anterior,
            rd.even_direccion as aap_direccion,
            b.barr_descripcion,
            coa.aaco_descripcion as aaco_descripcion_anterior,
            moa.aamo_descripcion as aamo_descripcion_anterior,
            rdd.aap_potencia_anterior as aap_potencia_anterior,
            rdd.aap_tecnologia_anterior as aap_tecnologia_anterior,
            aua.aaus_descripcion as aaus_descripcion_anterior,
            aca.aacu_descripcion as aacu_descripcion_anterior,                    
            mo.aamo_descripcion,
            rdd.aap_potencia,
            rdd.aap_tecnologia,
            au.aaus_descripcion,
            ac.aacu_descripcion,
            co.aaco_descripcion,
            m.medi_numero,
            t.tran_numero from siap.reporte r
            left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
            left join siap.reporte_direccion rd on rd.repo_id = r.repo_id
            left join siap.reporte_direccion_dato rdd on rdd.repo_id = rd.repo_id and rdd.even_id = rd.even_id and rdd.aap_id = rd.aap_id
            left join siap.reporte_direccion_dato_adicional rdda on rdda.repo_id = rd.repo_id and rdda.even_id = rd.even_id and rdda.aap_id = rd.aap_id
            left join siap.aap a on a.aap_id = rd.aap_id
            left join siap.aap_adicional d on d.aap_id = a.aap_id
            left join siap.barrio b on b.barr_id = rd.barr_id
            left join siap.barrio ba on ba.barr_id = rd.barr_id_anterior
            left join siap.aap_cuentaap aca on aca.aacu_id = rdda.aacu_id_anterior
            left join siap.aap_cuentaap ac on ac.aacu_id = rdda.aacu_id
            left join siap.aap_modelo moa on moa.aamo_id = rdd.aamo_id_anterior
            left join siap.aap_modelo mo on mo.aamo_id = rdd.aamo_id
            left join siap.aap_conexion coa on coa.aaco_id = rdd.aaco_id_anterior
            left join siap.aap_conexion co on co.aaco_id = rdd.aaco_id
            left join siap.medidor m on m.medi_id = ra.medi_id
            left join siap.transformador t on t.tran_id = ra.tran_id
            left join siap.aap_uso aua on aua.aaus_id = rdda.aaus_id_anterior
            left join siap.aap_uso au on au.aaus_id = rdda.aaus_id                    
            where r.reti_id = 9 and r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.empr_id = {empr_id} and r.rees_id = 3 and a.aap_id <> 9999999
            order by r.repo_fechasolucion, a.aap_id""").on(
                    'fecha_inicial -> fi.getTime(),
                    'fecha_final -> ff.getTime(),
                    'empr_id -> empr_id
                    ).as(Siap_detallado_cambio_medida._set *)
      }
    }



  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_modernizacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_modernizacion]] =
    Future[Iterable[Siap_detallado_modernizacion]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""SELECT  
		                r.repo_fecharecepcion, 
                        r.repo_fechasolucion, 
                        r.repo_consecutivo::text as aap_rte,
		                a.aap_id, 
		                a.aap_direccion, 
                        b.barr_descripcion,
                        au.aaus_descripcion,
                        ac.aacu_descripcion,
		                mo1.aamo_descripcion as aamo_descripcion_anterior, 
		                d.aap_potencia_anterior, 
		                d.aap_tecnologia_anterior,
		                mo2.aamo_descripcion,
		                d.aap_potencia,
		                d.aap_tecnologia,
                        case when co.aaco_id = 1 then 'X' else ' ' end as aforo,
                        co.aaco_descripcion
                    FROM siap.reporte r
                    LEFT JOIN siap.reporte_direccion rd on rd.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_direccion_dato d on d.repo_id = rd.repo_id and d.even_id = rd.even_id
                    LEFT JOIN siap.reporte_direccion_dato_adicional da on da.repo_id = d.repo_id and da.even_id = d.even_id
                    LEFT JOIN siap.aap a on a.aap_id = d.aap_id
                    LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                    LEFT JOIN siap.aap_modelo mo1 on mo1.aamo_id = d.aamo_id_anterior
                    LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = d.aamo_id
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                    LEFT JOIN siap.aap_uso au on au.aaus_id = da.aaus_id
                    LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = da.aacu_id                    
                    WHERE r.reti_id = 6 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 and r.rees_id = 3 and rd.even_estado < 8
                    ORDER BY r.repo_fechasolucion, a.aap_id""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_modernizacion.siap_detallado_modernizacion_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_actualizacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_actualizacion]] =
    Future[Iterable[Siap_detallado_actualizacion]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""SELECT distinct 
		            r.repo_fecharecepcion, 
                    r.repo_fechasolucion, 
                    r.repo_consecutivo::text as aap_rte,
		            a.aap_id, 
		            a.aap_direccion, 
                    b.barr_descripcion,
                    au.aaus_descripcion,
                    ac.aacu_descripcion,
		            case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
		            mo1.aamo_descripcion as aamo_descripcion_anterior, 
		            d.aap_potencia_anterior, 
		            d.aap_tecnologia_anterior,
		            mo2.aamo_descripcion,
		            d.aap_potencia,
		            d.aap_tecnologia,
                    case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo,
                    co.aaco_descripcion
                   FROM siap.reporte r
                   LEFT JOIN siap.reporte_direccion e ON e.repo_id = r.repo_id
                   LEFT JOIN siap.reporte_direccion_dato d on d.repo_id = e.repo_id
                   LEFT JOIN siap.reporte_direccion_dato_adicional da on da.repo_id = d.repo_id and da.even_id = d.even_id                   
                   LEFT JOIN siap.aap a on a.aap_id = d.aap_id
                   LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                   LEFT JOIN siap.aap_modelo mo1 on mo1.aamo_id = d.aamo_id_anterior
                   LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = d.aamo_id
                   LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                   LEFT JOIN siap.aap_uso au on au.aaus_id = da.aaus_id
                   LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = da.aacu_id
                   WHERE r.reti_id = 5 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 and r.rees_id = 3 and e.even_id <> 9
                   ORDER BY r.repo_fechasolucion, a.aap_id""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_actualizacion.siap_detallado_actualizacion_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_reposicion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_reposicion]] =
    Future[Iterable[Siap_detallado_reposicion]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""SELECT distinct
		            r.repo_fecharecepcion,
                    r.repo_fechasolucion,
                    r.repo_consecutivo::text as aap_rte,
		            a.aap_id,
		            a.aap_direccion,
                    b.barr_descripcion,
                    au.aaus_descripcion,
                    ac.aacu_descripcion,
		            case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
		            mo1.aamo_descripcion as aamo_descripcion_anterior,
		            d.aap_potencia_anterior,
		            d.aap_tecnologia_anterior,
		            mo2.aamo_descripcion,
		            d.aap_potencia,
		            d.aap_tecnologia,
                    case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo,
                    co.aaco_descripcion
                   FROM siap.reporte r
                   LEFT JOIN siap.reporte_direccion e ON e.repo_id = r.repo_id
                   LEFT JOIN siap.reporte_direccion_dato d on d.repo_id = e.repo_id
                   LEFT JOIN siap.reporte_direccion_dato_adicional da on da.repo_id = d.repo_id and da.even_id = d.even_id
                   LEFT JOIN siap.aap a on a.aap_id = d.aap_id
                   LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                   LEFT JOIN siap.aap_modelo mo1 on mo1.aamo_id = d.aamo_id_anterior
                   LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = d.aamo_id
                   LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                   LEFT JOIN siap.aap_uso au on au.aaus_id = da.aaus_id
                   LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = da.aacu_id                   
                   WHERE r.reti_id = 7 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 and r.rees_id = 3
                   ORDER BY r.repo_fechasolucion, a.aap_id""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_reposicion.siap_detallado_reposicion_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_repotenciacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_reposicion]] =
    Future[Iterable[Siap_detallado_reposicion]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""SELECT distinct
		            r.repo_fecharecepcion, 
                    r.repo_fechasolucion, 
                    r.repo_consecutivo::text as aap_rte,
		            a.aap_id, 
		            a.aap_direccion, 
                    b.barr_descripcion, 
                    au.aaus_descripcion,
                    ac.aacu_descripcion,                    
		            case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
		            mo1.aamo_descripcion as aamo_descripcion_anterior, 
		            d.aap_potencia_anterior, 
		            d.aap_tecnologia_anterior,
		            mo2.aamo_descripcion,
		            d.aap_potencia,
		            d.aap_tecnologia,
                    case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo,
                    co.aaco_descripcion
                   FROM siap.reporte r
                   LEFT JOIN siap.reporte_direccion e ON e.repo_id = r.repo_id
                   LEFT JOIN siap.reporte_direccion_dato d on d.repo_id = e.repo_id
                   LEFT JOIN siap.reporte_direccion_dato_adicional da on da.repo_id = d.repo_id and da.even_id = d.even_id
                   LEFT JOIN siap.aap a on a.aap_id = d.aap_id
                   LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                   LEFT JOIN siap.aap_modelo mo1 on mo1.aamo_id = d.aamo_id_anterior
                   LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = d.aamo_id
                   LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                   LEFT JOIN siap.aap_uso au on au.aaus_id = da.aaus_id
                   LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = da.aacu_id                   
                   WHERE r.reti_id = 4 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 and r.rees_id = 3
                   ORDER BY r.repo_fechasolucion, a.aap_id""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_reposicion.siap_detallado_reposicion_set *)
      }
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_detallado_retiro_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_detallado_retiro]] =
    Future[Iterable[Siap_detallado_retiro]] {
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        SQL("""SELECT distinct r.repo_fecharecepcion, 
                    r.repo_fechasolucion,
                    r.repo_consecutivo,
                    a.aap_id, 
                    d.even_direccion_anterior as aap_direccion, 
                    b.barr_descripcion, 
                    au.aaus_descripcion,
                    ac.aacu_descripcion,                    
                    case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
                    mo2.aamo_descripcion,
                    dd.aap_potencia_anterior as aap_potencia,
                    dd.aap_tecnologia_anterior as aap_tecnologia,
                    case when dd.aaco_id_anterior = 2 then 'X' else '' end as medidor, case when dd.aaco_id_anterior = 1 then 'X' else ' ' end as aforo,
                    case when a.aaco_id <> 3 then case when tr.tire_descripcion = 'REUBICACION' then 'REUBICADA' else 'REPUESTA' end end as reubicado,
                    co.aaco_descripcion,
                    tr.tire_descripcion
                FROM siap.reporte r
                LEFT JOIN siap.reporte_direccion d on d.repo_id = r.repo_id
                LEFT JOIN siap.reporte_direccion_dato dd on dd.repo_id = r.repo_id and dd.even_id = d.even_id
                LEFT JOIN siap.reporte_direccion_dato_adicional da on da.repo_id = d.repo_id and da.even_id = d.even_id                
                INNER JOIN siap.aap a on a.aap_id = d.aap_id
                INNER JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
                LEFT JOIN siap.barrio b on b.barr_id = d.barr_id_anterior
                LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = dd.aamo_id_anterior
                LEFT JOIN siap.aap_conexion co on co.aaco_id = dd.aaco_id_anterior
                LEFT JOIN siap.aap_uso au on au.aaus_id = da.aaus_id
                LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = da.aacu_id
                LEFT JOIN siap.tiporetiro tr on tr.tire_id = d.tire_id
                WHERE r.reti_id = 8 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND r.rees_id = 3 AND d.even_estado <> 9
                ORDER BY r.repo_fechasolucion, r.repo_consecutivo, a.aap_id""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_detallado_retiro.set *)
      }
    }

  def siap_detallado_retiro_reubicacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_retiro_reubicacion]] =
    Future[Iterable[Siap_retiro_reubicacion]] {
      var _listData = new ListBuffer[Siap_retiro_reubicacion]()
      db.withConnection { implicit connection =>
        var fi = Calendar.getInstance()
        var ff = Calendar.getInstance()
        fi.setTimeInMillis(fecha_inicial)
        ff.setTimeInMillis(fecha_final)
        fi.set(Calendar.MILLISECOND, 0)
        fi.set(Calendar.SECOND, 0)
        fi.set(Calendar.MINUTE, 0)
        fi.set(Calendar.HOUR, 0)

        ff.set(Calendar.MILLISECOND, 59)
        ff.set(Calendar.SECOND, 59)
        ff.set(Calendar.MINUTE, 59)
        ff.set(Calendar.HOUR, 23)
        val retiros =
          SQL("""SELECT distinct r.repo_fecharecepcion, 
                    r.repo_fechasolucion,
                    ra.repo_modificado as repo_fechadigitacion,
                    r.repo_consecutivo,
                    a.aap_id, 
                    d.even_direccion_anterior as aap_direccion, 
                    b.barr_descripcion, 
                    case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
                    au.aaus_descripcion,
                    ac.aacu_descripcion,
                    mo2.aamo_descripcion,
                    ad.aap_potencia,
                    ad.aap_tecnologia,
                    case when dd.aaco_id_anterior = 2 then 'X' else '' end as medidor, case when dd.aaco_id_anterior = 1 then 'X' else ' ' end as aforo,
                    co.aaco_descripcion,
                    case when a.esta_id = 9 then 'DADA DE BAJA' else '' end as enbaja
                FROM siap.reporte r
                LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
                LEFT JOIN siap.reporte_direccion d on d.repo_id = r.repo_id
                LEFT JOIN siap.reporte_direccion_dato dd on dd.repo_id = r.repo_id and dd.even_id = d.even_id
                LEFT JOIN siap.reporte_direccion_dato_adicional dda on dda.repo_id = r.repo_id and dda.even_id = d.even_id and dda.aap_id = d.aap_id
                INNER JOIN siap.aap a on a.aap_id = d.aap_id
                INNER JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
                LEFT JOIN siap.barrio b on b.barr_id = d.barr_id_anterior
                LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = a.aamo_id
                LEFT JOIN siap.aap_conexion co on co.aaco_id = dd.aaco_id_anterior
                LEFT JOIN siap.aap_uso au on au.aaus_id = dda.aaus_id_anterior
                LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = dda.aacu_id_anterior
                WHERE r.reti_id = 8 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND r.rees_id = 3 and d.even_estado <> 9
                ORDER BY r.repo_fechasolucion, r.repo_consecutivo, a.aap_id""")
            .on(
              'fecha_inicial -> fi.getTime(),
              'fecha_final -> ff.getTime(),
              'empr_id -> empr_id
            )
            .as(Siap_retiro_reubicacion_a.set *)
        retiros.map { retiro =>
          val reubicacion = SQL(
            """select r.repo_fecharecepcion,
                                            r.repo_fechasolucion,
                                            ra.repo_modificado as repo_fechadigitacion,
                                            rt.reti_descripcion,
                                            r.repo_consecutivo::text as aap_rte,
                                            a.aap_id, 
                                            e.even_direccion as aap_direccion,
                                            b.barr_descripcion,
                                            case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
                                            au.aaus_descripcion,
                                            ac.aacu_descripcion,
                                            mo.aamo_descripcion, 
                                            d.aap_potencia, 
                                            d.aap_tecnologia,
                                            case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo,
                                            co.aaco_descripcion
                                            from siap.reporte r 
                                            left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
                                            left join siap.reporte_direccion e on e.repo_id = r.repo_id
                                            left join siap.reporte_direccion_dato ed on ed.repo_id = e.repo_id and ed.even_id = e.even_id and ed.aap_id = e.aap_id
                                            left join siap.reporte_direccion_dato_adicional eda on eda.repo_id = e.repo_id and eda.even_id = e.even_id and eda.aap_id = e.aap_id
                                            left join siap.reporte_tipo rt ON rt.reti_id = r.reti_id
                                            left join siap.aap a on a.aap_id = e.aap_id
                                            left join siap.aap_adicional d on d.aap_id = a.aap_id
                                            left join siap.barrio b on b.barr_id = e.barr_id
                                            left join siap.aap_modelo mo on mo.aamo_id = ed.aamo_id
                                            left join siap.aap_conexion co on co.aaco_id = ed.aaco_id
                                            left join siap.aap_uso au on au.aaus_id = eda.aaus_id
                                            LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = eda.aacu_id
                                            where r.reti_id IN (3,7) and r.rees_id = 3 and r.empr_id = {empr_id} and a.aap_id = {aap_id} and e.even_estado <> 9
                                            ORDER BY r.repo_fechasolucion desc LIMIT 1"""
          ).on(
              'fecha_inicial -> fi.getTime(),
              'fecha_final -> ff.getTime(),
              'empr_id -> empr_id,
              'aap_id -> retiro.aap_id
            )
            .as(Siap_retiro_reubicacion_b.set.singleOpt)
          val retiro_reubicacion =
            new Siap_retiro_reubicacion(Some(retiro), reubicacion)
          _listData += retiro_reubicacion
        }

        val reubicaciones = SQL(
                                  """select r.repo_fecharecepcion,
                                            r.repo_fechasolucion,
                                            ra.repo_modificado as repo_fechadigitacion,
                                            rt.reti_descripcion,
                                            r.repo_consecutivo::text as aap_rte,
                                            a.aap_id, 
                                            e.even_direccion as aap_direccion,
                                            b.barr_descripcion,
                                            case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
                                            au.aaus_descripcion,
                                            ac.aacu_descripcion,
                                            mo.aamo_descripcion, 
                                            d.aap_potencia, 
                                            d.aap_tecnologia,
                                            case when co.aaco_id = 2 then 'X' else '' end as medidor, case when co.aaco_id = 1 then 'X' else ' ' end as aforo,
                                            co.aaco_descripcion
                                            from siap.reporte r 
                                            left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
                                            left join siap.reporte_direccion e on e.repo_id = r.repo_id
                                            left join siap.reporte_direccion_dato ed on ed.repo_id = e.repo_id and ed.even_id = e.even_id and ed.aap_id = e.aap_id
                                            left join siap.reporte_direccion_dato_adicional eda on eda.repo_id = e.repo_id and eda.even_id = e.even_id and eda.aap_id = e.aap_id
                                            left join siap.reporte_tipo rt ON rt.reti_id = r.reti_id
                                            left join siap.aap a on a.aap_id = e.aap_id
                                            left join siap.aap_adicional d on d.aap_id = a.aap_id
                                            left join siap.barrio b on b.barr_id = e.barr_id
                                            left join siap.aap_modelo mo on mo.aamo_id = ed.aamo_id
                                            left join siap.aap_conexion co on co.aaco_id = ed.aaco_id
                                            left join siap.aap_uso au on au.aaus_id = eda.aaus_id
                                            LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = eda.aacu_id
                                            where r.reti_id IN (3,7) and r.rees_id = 3 and r.repo_fechasolucion between {fecha_inicial} AND {fecha_final} and r.empr_id = {empr_id} and e.even_estado <> 9"""
        ).on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id
          )
          .as(Siap_retiro_reubicacion_b.set *)
        reubicaciones.map { reubicacion =>
          val retiro =
            SQL("""SELECT r.repo_fecharecepcion, 
                    r.repo_fechasolucion,
                    ra.repo_modificado as repo_fechadigitacion,
                    r.repo_consecutivo,
                    a.aap_id, 
                    d.even_direccion_anterior as aap_direccion, 
                    b.barr_descripcion, 
                    case when b.tiba_id = 2 then 'X' else '' end as vereda, case when b.tiba_id = 5 then 'X' else '' end as parque,
                    au.aaus_descripcion,
                    ac.aacu_descripcion,
                    mo2.aamo_descripcion,
                    ad.aap_potencia,
                    ad.aap_tecnologia,
                    case when dd.aaco_id_anterior = 2 then 'X' else '' end as medidor, case when dd.aaco_id_anterior = 1 then 'X' else ' ' end as aforo,
                    co.aaco_descripcion,
                    case when a.esta_id = 9 then 'DADA DE BAJA' else '' end as enbaja
                    FROM siap.reporte r
                    LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_direccion d on d.repo_id = r.repo_id
                    LEFT JOIN siap.reporte_direccion_dato dd on dd.repo_id = r.repo_id and dd.even_id = d.even_id and dd.aap_id = d.aap_id
                    LEFT JOIN siap.reporte_direccion_dato_adicional dda on dda.repo_id = r.repo_id and dda.even_id = d.even_id and dda.aap_id = d.aap_id
                    INNER JOIN siap.aap a on a.aap_id = d.aap_id
                    INNER JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
                    LEFT JOIN siap.barrio b on b.barr_id = d.barr_id_anterior
                    LEFT JOIN siap.aap_modelo mo2 on mo2.aamo_id = dd.aamo_id_anterior
                    LEFT JOIN siap.aap_conexion co on co.aaco_id = dd.aaco_id_anterior
                    LEFT JOIN siap.aap_uso au on au.aaus_id = dda.aaus_id_anterior
                    LEFT JOIN siap.aap_cuentaap ac on ac.aacu_id = dda.aacu_id_anterior
                    WHERE r.reti_id = 8 AND r.empr_id = {empr_id} AND r.rees_id = 3 and d.even_estado <> 9 and d.aap_id = {aap_id}
                    ORDER BY r.repo_fechasolucion desc, r.repo_consecutivo, a.aap_id
                    LIMIT 1""")
              .on(
                'fecha_inicial -> fi.getTime(),
                'fecha_final -> ff.getTime(),
                'empr_id -> empr_id,
                'aap_id -> reubicacion.aap_id
              )
              .as(Siap_retiro_reubicacion_a.set.singleOpt)
          var ret = new Siap_retiro_reubicacion_a(
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
          retiro match {
            case Some(r) => ret = r
            case None    => val retiro_reubicacion = new Siap_retiro_reubicacion(Some(ret), Some(reubicacion))
                            _listData += retiro_reubicacion
          }

        }

      }
      _listData.toList.distinct
    }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_disponibilidad_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      wt: Double,
      empr_id: scala.Long
  ): Array[Byte] = {
    import Height._
    db.withConnection { implicit connection =>
      val empresa = empresaService.buscarPorId(empr_id)
      empresa match {
        case Some(empresa) =>
          val municipio = municipioService.buscarPorId(empresa.muni_id)
          val fmt = DateTimeFormat.forPattern("yyyyMMdd")
          val format = new SimpleDateFormat("yyyy-MM-dd")
          val fi = new DateTime(fecha_inicial)
          val ff = new DateTime(fecha_final)
          val dias = ff.dayOfMonth()
          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var _listMerged = new ListBuffer[CellRange]()
          val resultSet = SQL(
            """select distinct 
                            r.repo_fecharecepcion::text, r.repo_fechasolucion::text, r.repo_consecutivo, v.aap_id, d.aap_potencia, case when b.tiba_id = 2 then 'X' else Null end as vereda,
                           (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion AND r.repo_fechasolucion) AS festivos
                           from siap.reporte r
                           inner join siap.reporte_evento v on v.repo_id = r.repo_id
                           inner join siap.aap a on a.aap_id = v.aap_id
                           left join siap.aap_adicional d on d.aap_id = a.aap_id
                           left join siap.barrio b on b.barr_id = a.barr_id
                           where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.reti_id = 1 and r.rees_id = 3 and r.empr_id = {empr_id}
                           and a.aap_id <> 9999999
                           order by r.repo_fechasolucion::text asc"""
          ).on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'empr_id -> empr_id
            )
            .as(Siap_disponibilidad.siap_disponibilidad_set *)

          val sheet1 = Sheet(
            name = "HojaT",
            rows = {
              val titleRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa.empr_descripcion)
              val headerRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  "Fecha Del Reporte",
                  "Fecha de Ejecución",
                  "Reporte",
                  "Código de Luminaria",
                  "Potencia",
                  "HSSi",
                  "Tope Horas Apagada",
                  "Días",
                  "Vereda",
                  "Fecha Solución Vereda",
                  "Días Dif",
                  "Horas Dif"
                )
              var j = 2
              val rows = resultSet.map {
                i =>
                  j += 1
                  com.norbitltd.spoiwo.model.Row(
                    DateCell(
                      i.repo_fecharecepcion match {
                        case Some(value) => format.parse(value)
                      },
                      Some(0),
                      style = Some(
                        CellStyle(dataFormat = CellDataFormat("YYYY-MM-DD"))
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      i.vereda match {
                        case Some(v) =>
                          i.repo_fecharecepcion match {
                            case Some(value) => format.parse(value)
                          }
                        case None =>
                          i.repo_fechasolucion match {
                            case Some(value) => format.parse(value)
                          }
                      },
                      Some(1),
                      style = Some(
                        CellStyle(dataFormat = CellDataFormat("YYYY-MM-DD"))
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell( 
                      i.repo_consecutivo.get,
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell(
                      i.aap_id match { case Some(value) => value },
                      Some(3),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell(
                      i.aap_potencia match { case Some(value) => value },
                      Some(4),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "IF(A" + j + "=B" + j + ",12,(B" + j + "-A" + j + ")*12)",
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "IF(F" + j + ">=48,F" + j + ",0)",
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "IF(F" + j + ">=48,B" + j + "-A" + j + ")",
                      Some(7),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.vereda match {
                        case Some(value) => "X"
                        case None        => ""
                      },
                      Some(8),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.vereda match {
                        case Some(value) =>
                          i.repo_fechasolucion match {
                            case Some(value) => value
                          }
                        case None => ""
                      },
                      Some(9),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "IF(I" + j + "=\"X\"," + "J" + j + "-A" + j + ",\"\")",
                      Some(10),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "IF(I" + j + "=\"X\"," + "(J" + j + "-A" + j + ")*12,\"\")",
                      Some(11),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }

              titleRow :: headerRow :: rows.toList
            }
          )
          val sheet2 = Sheet(
            name = "HojaC",
            rows = {
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "INTERVENTORIA ALUMBRADO PUBLICO",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "MUNICIPIO " + municipio.get.muni_descripcion,
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "MANUAL DE PROCEDIMIENTOS",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "INDICE DE DISPONIBILIDAD DE LUMINARIAS DEL SALP",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "ACTA ITAF No.",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "CORRESPONDIENTE AL MES DE ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "En el Municipio SAN JUAN GIRON, se reunieron los Ingenieros GABRIEL EMILIO MARTINEZ MARTINEZ, Director Técnico E Ing Residente ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "de la Concesionaria ISAG S.A. y SALOMON IGLESIAS IGLESIAS  en su calidad Director Técnico de la firma ITAF  S.A.S., Interventora ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "del contrato de Concesión No. 094 con el fin de calcular el Índice de disponibilidad del sistema de alumbrado público del municipio ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  municipio.get.muni_descripcion + ", aplicando la Resolución 123 de la CREG.",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "Índice de disponibilidad: Es el tiempo total sobre un periodo dado, durante el cual un activo del Sistema de Alumbrado ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "Público está disponible para el servicio y se mide a través de las interrupciones por luminarias que no funcionan o funcionan ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "de manera deficiente, reportadas por los usuarios y el interventor al SIAP, se calcula de acuerdo con la siguiente expresión: ",
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Center,
                      font = Font(height = 11.points, fontName = "Arial Narrow")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "ID",
                  Some(0),
                  style = Some(
                    CellStyle(
                      font = Font(bold = true),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Top
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "Índice de disponibilidad de la infraestructura, para el período de remuneración.",
                  Some(1),
                  style = Some(CellStyle(font = Font(bold = false))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "Wi",
                  Some(0),
                  style = Some(
                    CellStyle(
                      font = Font(bold = true),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Top
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "Potencia de la luminaria i en kW , reportada al registro de quejas y reclamos de alumbrado público. ",
                  Some(1),
                  style = Some(CellStyle(font = Font(bold = false))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "HSSi",
                  Some(0),
                  style = Some(
                    CellStyle(
                      font = Font(bold = true),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Top
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "Número total de horas sin servicio de la luminaria i.",
                  Some(1),
                  style = Some(CellStyle(font = Font(bold = false))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "WT",
                  Some(0),
                  style = Some(
                    CellStyle(
                      font = Font(bold = true),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Top
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "Potencia total instalada en kW de las luminarias que componen el  Sistema de Alumbrado Público.",
                  Some(1),
                  style = Some(CellStyle(font = Font(bold = false))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "T",
                  Some(0),
                  style = Some(
                    CellStyle(
                      font = Font(bold = true),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Top
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "Número de horas del  período de facturación de las luminarias.",
                  Some(1),
                  style = Some(CellStyle(font = Font(bold = false))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("PERIODO")
              _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("", "", "Wi", "", "HSSI", "WT", "T", "ID")
              _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("m", "", "W", "KW", "", "", dias.get)
              var j = 27
              var rows = resultSet.map {
                i =>
                  j += 1
                  _listRow += com.norbitltd.spoiwo.model.Row(
                    NumericCell(
                      j - 27,
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      "",
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "+HojaT!$E" + (j - 25),
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "+C" + (j) + "/1000",
                      Some(3),
                      style =
                        Some(CellStyle(dataFormat = CellDataFormat("0.0000"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "+HojaT!$F" + (j - 25),
                      Some(4),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell(
                      wt,
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "$G$27*12",
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    FormulaCell(
                      "(D" + j + "*E" + j + ")/(F" + j + "*G" + j + ")",
                      Some(7),
                      style =
                        Some(CellStyle(dataFormat = CellDataFormat("0.0000%"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }

              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "INDICE DE DISPONIBILIDAD",
                  Some(0),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(1),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(2),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(3),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(4),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(5),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(6),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "1-SUM(H28:H" + j + ")",
                  Some(7),
                  style = Some(CellStyle(dataFormat = CellDataFormat("00.00%"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )

              _listRow.toList
            },
            mergedRegions = {
              _listMerged += CellRange((0, 0), (0, 7))
              _listMerged += CellRange((1, 1), (0, 7))
              _listMerged += CellRange((2, 2), (0, 7))
              _listMerged += CellRange((3, 3), (0, 7))
              _listMerged += CellRange((4, 4), (0, 7))
              _listMerged += CellRange((5, 5), (0, 7))
              _listMerged += CellRange((6, 6), (0, 7))
              _listMerged += CellRange((7, 7), (0, 7))
              _listMerged += CellRange((8, 8), (0, 7))
              _listMerged += CellRange((9, 9), (0, 7))
              _listMerged += CellRange((10, 10), (0, 7))
              _listMerged += CellRange((11, 11), (0, 7))
              _listMerged += CellRange((12, 12), (0, 7))
              _listMerged += CellRange((13, 13), (0, 7))
              _listMerged += CellRange((14, 14), (0, 7))
              _listMerged.toList
            },
            columns = {
              _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 7, width = new Width(40, WidthUnit.Character))
              _listColumn.toList
            }
          )

          println("Escribiendo en el Stream")
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1, sheet2).writeToOutputStream(os)
          println("Stream Listo")
          os.toByteArray
        case None =>
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          os.toByteArray
      }
    }
  }

  /**
    *  imprimir
    * @param fecha_inicial: scala.Long
    * @param fecha_final: scala.Long
    * @param usua_id: scala.Long
    * @param empr_id: scala.Long
    * @param num_id: scala.Int
    * @return OutputStream
    */
  def siap_graficos_reporte(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long,
      num_id: Int
  ): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val municipio = municipioService.buscarPorId(empresa.muni_id).get
    val usuario = usuarioService.buscarPorId(usua_id).get
    db.withConnection { connection =>
      var os = Array[Byte]()
      var compiledFile = ""
      num_id match {
        case 1 =>
          compiledFile = REPORT_DEFINITION_PATH + "siap_oym_recibidas.jasper"
        case 2 =>
          compiledFile = REPORT_DEFINITION_PATH + "siap_oym_uso_recibidas.jasper"
        case 3 =>
          compiledFile = REPORT_DEFINITION_PATH + "siap_oym_atendidos.jasper"
        case 4 =>
          compiledFile = REPORT_DEFINITION_PATH + "siap_oym_recibidas_vs_pendientes.jasper"
        case 5 =>
          compiledFile = REPORT_DEFINITION_PATH + "siap_oym_sector_recibidas.jasper"
      }
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      val reportParams = new HashMap[String, java.lang.Object]()
      reportParams.put(
        "FECHA_INICIAL",
        new java.sql.Timestamp(fi.getTimeInMillis())
      )
      reportParams.put(
        "FECHA_FINAL",
        new java.sql.Timestamp(ff.getTimeInMillis())
      )
      reportParams.put("EMPR_ID", Long.valueOf(empresa.empr_id.get))
      reportParams.put("EMPRESA", empresa.empr_descripcion)
      reportParams.put("MUNICIPIO", municipio.muni_descripcion)
      // reportParams.put("USUARIO", usuario.usua_nombre + " " + usuario.usua_apellido)
      os =
        JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
      os
    }
  }

  /**
    *  imprimir
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_reporte_consolidado_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Future[Iterable[Siap_consolidado]] = Future[Iterable[Siap_consolidado]] {
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      SQL("""select distinct 
                    r.repo_consecutivo,
                    r.repo_fecharecepcion,
                    r.repo_nombre,
                    r.repo_telefono,
                    r.repo_direccion,
                    b.barr_descripcion,
                    case when b.tiba_id = 2 then 'X' else '' end as vereda,
                    o.orig_descripcion,
                    a.acti_descripcion,
                    r.repo_descripcion,
                    r.repo_fechasolucion,
                    r.repo_reportetecnico
                   from siap.reporte r 
                    left join siap.reporte_adicional ra on ra.repo_id = r.repo_id
                    left join siap.barrio b on b.barr_id = r.barr_id
                    left join siap.origen o on o.orig_id = r.orig_id
                    left join siap.actividad a on a.acti_id = ra.acti_id
                    where r.reti_id = 1 and r.repo_fecharecepcion between {fecha_inicial} and {fecha_final} and r.empr_id = {empr_id} and r.rees_id = 3
                    order by r.repo_fecharecepcion, r.repo_consecutivo""")
        .on(
          'fecha_inicial -> fi.getTime(),
          'fecha_final -> ff.getTime(),
          'empr_id -> empr_id
        )
        .as(Siap_consolidado.set *)
    }
  }

  def siap_material_repetido_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      tiel_id: scala.Long,
      empr_id: scala.Long
  ) = Future[Iterable[Siap_material_repetido]] {
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      val repetidas =
        SQL("""select distinct re.even_codigo_instalado, rt.reti_descripcion, r.repo_consecutivo, r.repo_fechasolucion, re.aap_id, a.aap_direccion, b.barr_descripcion, te.tiel_descripcion from siap.reporte r
            inner join siap.reporte_evento re on re.repo_id = r.repo_id
            inner join siap.elemento e on e.elem_id = re.elem_id
            inner join siap.tipoelemento te on te.tiel_id = e.tiel_id
            inner join siap.reporte_tipo rt on rt.reti_id = r.reti_id
			inner join siap.aap a on a.aap_id = re.aap_id
			inner join siap.barrio b on b.barr_id = a.barr_id
            where re.even_codigo_instalado in
            (select o.even_codigo_instalado from
            (select distinct r.repo_fechasolucion, r.repo_consecutivo, re.aap_id, te.tiel_descripcion, re.even_codigo_instalado, re.even_codigo_retirado from siap.reporte r
            inner join siap.reporte_evento re on re.repo_id = r.repo_id
            inner join siap.elemento e on e.elem_id = re.elem_id
            inner join siap.tipoelemento te on te.tiel_id = e.tiel_id
			where r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and te.tiel_id = {tiel_id} and r.empr_id = {empr_id}
            order by re.even_codigo_instalado) o
            group by o.even_codigo_instalado
            having count(o) > 1 and o.even_codigo_instalado > '') and te.tiel_id = {tiel_id}
            order by re.even_codigo_instalado""")
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'tiel_id -> tiel_id,
            'empr_id -> empr_id
          )
          .as(Siap_material_repetido._set *)
      repetidas
    }
  }

  /**
    *  siap_eficiencia_xls
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_eficiencia_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Array[Byte] = {
    import Height._
    db.withConnection { implicit connection =>
      val empresa = empresaService.buscarPorId(empr_id)
      empresa match {
        case Some(empresa) =>
          val municipio = municipioService.buscarPorId(empresa.muni_id)
          val fmt = DateTimeFormat.forPattern("yyyyMMdd")
          val format = new SimpleDateFormat("yyyy-MM-dd");
          val fi = new DateTime(fecha_inicial)
          val ff = new DateTime(fecha_final)
          val dias = ff.dayOfMonth()
          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var _listMerged = new ListBuffer[CellRange]()
          var conteo = SQL(
            """SELECT COUNT(o) FROM (select distinct 
                                    r.repo_direccion,
                                    b.barr_descripcion,
                                    r.repo_consecutivo,
                                    e.aap_id,
                                    r.repo_fecharecepcion,
                                    r.repo_fechasolucion
                                from siap.reporte r 
                                left join siap.barrio b on b.barr_id = r.barr_id
                                inner join siap.reporte_evento e on e.repo_id = r.repo_id
                                where r.reti_id = 1 and r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.reti_id = 1 and r.empr_id = {empr_id} and e.aap_id <> 9999999) o"""
          ).on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'empr_id -> empr_id
            )
            .as(SqlParser.scalar[scala.Long].single)

          val resultSet = SQL(
            """select distinct 
                                    r.repo_direccion,
                                    b.barr_descripcion,
                                    r.repo_consecutivo,
                                    e.aap_id,
                                    r.repo_fecharecepcion::text,
                                    r.repo_fechasolucion::text,
                                    (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion AND r.repo_fechasolucion) AS festivos
                                   from siap.reporte r 
                                   left join siap.barrio b on b.barr_id = r.barr_id
                                   inner join siap.reporte_evento e on e.repo_id = r.repo_id
                                   where r.reti_id = 1 and r.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r.reti_id = 1 and r.empr_id = {empr_id} and e.aap_id <> 9999999
                                   order by r.repo_fecharecepcion::text, r.repo_consecutivo, e.aap_id"""
          ).on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'empr_id -> empr_id
            )
            .as(Siap_eficiencia._set *)

          val sheet1 = Sheet(
            name = "HojaT",
            rows = {
              val titleRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa.empr_descripcion)
              val headerRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  "Consecutivo",
                  "Dirección",
                  "Barrio",
                  "Reporte",
                  "Código Luminaria",
                  "Fecha De Recepción",
                  "Fecha de Solución",
                  "Días Solución",
                  "ei"
                )
              var j = 2
              val conteo2 = conteo + 6
              val rows = {
                resultSet.map {
                  i =>
                    j += 1
                    _listRow += com.norbitltd.spoiwo.model.Row(
                      NumericCell(
                        j - 2,
                        Some(0),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      StringCell(
                        i.repo_direccion match {
                          case Some(d) => d
                          case None    => ""
                        },
                        Some(1),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      StringCell(
                        i.barr_descripcion match {
                          case Some(b) => b
                          case None    => ""
                        },
                        Some(2),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      NumericCell(
                        i.repo_consecutivo.get,
                        Some(3),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      NumericCell(
                        i.aap_id match { case Some(value) => value },
                        Some(4),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      DateCell(
                        i.repo_fecharecepcion match {
                          case Some(value) => format.parse(value)
                        },
                        Some(5),
                        style = Some(
                          CellStyle(dataFormat = CellDataFormat("YYYY-MM-DD"))
                        ),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      DateCell(
                        i.repo_fechasolucion match {
                          case Some(value) => format.parse(value)
                        },
                        Some(6),
                        style = Some(
                          CellStyle(dataFormat = CellDataFormat("YYYY-MM-DD"))
                        ),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      FormulaCell(
                        "G" + j + "-F" + j,
                        Some(7),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      FormulaCell(
                        "IF(H" + j + "<$D$" + conteo2 + ",1,$D" + conteo2 + "/H" + j + ")",
                        Some(8),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("0.00%"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      )
                    )
                }
                _listRow += com.norbitltd.spoiwo.model.Row(
                  StringCell(
                    "",
                    Some(0),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(1),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(2),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(3),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(4),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(5),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(6),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(7),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "SUM(I3:I" + (j) + ")/D" + (conteo2 - 1),
                    Some(8),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("0.00%"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                )
                _listRow += com.norbitltd.spoiwo.model.Row(
                  StringCell(
                    "",
                    Some(0),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(1),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(2),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(3),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(4),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(5),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(6),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(7),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(8),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                )
                _listRow += com.norbitltd.spoiwo.model.Row(
                  StringCell(
                    "Número de Reportes",
                    Some(0),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(1),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(2),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    conteo,
                    Some(3),
                    style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(4),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(5),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(6),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(7),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(8),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                )
                _listRow += com.norbitltd.spoiwo.model.Row(
                  StringCell(
                    "Patrón de Operación (p)",
                    Some(0),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(1),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(2),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    3,
                    Some(3),
                    style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(4),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(5),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(6),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(7),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(8),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                )
              }
              titleRow :: headerRow :: rows.toList
            }
          )

          println("Escribiendo en el Stream")
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1).writeToOutputStream(os)
          println("Stream Listo")
          os.toByteArray
        case None =>
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          os.toByteArray
      }
    }
  }

  /**
    *  siap_eficiencia_xls
    * @param repo_id: scala.Long
    * @return OutputStream
    */
  def siap_calculo_carga_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ): Array[Byte] = {
    import Height._
    db.withConnection { implicit connection =>
      val empresa = empresaService.buscarPorId(empr_id)
      var electrificadora = SQL(
        """SELECT g.gene_valor FROM siap.general g WHERE g.gene_id = {gene_id}"""
      ).on('gene_id -> 1).as(SqlParser.scalar[String].single)
      empresa match {
        case Some(empresa) =>
          val municipio = municipioService.buscarPorId(empresa.muni_id)
          val fi = new DateTime(fecha_inicial)
          val ff = new DateTime(fecha_final)
          val ft = new DateTime(fecha_final)

          val fecha_ant = fi.minusDays(1)
          val zanho_ant = fecha_ant.getYear()
          val zmes_ant = fecha_ant.getMonthOfYear()
          val anho = ff.getYear()
          val periodo = ff.getMonthOfYear()
          val mes = periodo

          println("Fecha Toma:" + ft)
          println("Fecha Inicial:" + fi)
          println("Fecha Final:" + ff)

          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var _listMerged = new ListBuffer[CellRange]()
          val _headerData = com.norbitltd.spoiwo.model
            .Row(
              style = CellStyle(
                font = Font(bold = true, height = 8.points, fontName = "Arial"),
                horizontalAlignment = HA.Center
              )
            )
            .withCellValues(
              "Conexión",
              "Cuenta",
              "Tecnología",
              "Potencia",
              "Retirada",
              "Instalada"
            )

          val _cuentaParser = str("aacu_descripcion") ~ int("aacu_id") map {
            case n ~ p => (n, p)
          }
          val _aapParser = get[Option[String]]("aap_tecnologia") ~ get[Option[
            Int
          ]]("aap_potencia") ~ get[Option[Double]]("aap_potenciareal") ~ get[
            Option[Int]
          ]("cantidad") ~ get[Option[Int]]("retirada") ~ get[Option[Int]](
            "instalada"
          ) map { case t ~ p ~ r ~ c ~ e ~ i => (t, p, r, c, e, i) }
          println("Leyento Cuentas Alumbrado")
          val _rcuentas: List[(String, Int)] = SQL(
            """SELECT aacu_id, aacu_descripcion FROM siap.aap_cuentaap WHERE empr_id = {empr_id} AND aacu_estado = 1 ORDER BY aacu_orden"""
          ).on('empr_id -> empr_id).as(_cuentaParser.*)

          val sqlInsert =
            """INSERT INTO siap.carga (empr_id, zanho, zperiodo, aaco_id, aacu_id, aap_tecnologia, aap_potencia, cantidad, retirada, instalada)
                               VALUES({empr_id}, {zanho}, {zperiodo}, {aaco_id}, {aacu_id}, {aap_tecnologia}, {aap_potencia}, {cantidad}, {retirada}, {instalada})"""

          val sqlUpdate =
            """UPDATE siap.carga SET retirada = retirada + {retirada}, instalada = instalada + {instalada} 
                               WHERE empr_id = {empr_id} and zanho = {zanho} and zperiodo = {zperiodo} and
                                     aaco_id = {aaco_id} and aacu_id = {aacu_id} and aap_tecnologia = {aap_tecnologia} and aap_potencia = {aap_potencia}"""

          /// Carga Inicial Tabla
          val _cargaInicialParser = int("aaco_id") ~ int("aacu_id") ~ str(
            "aap_tecnologia"
          ) ~ int("aap_potencia") ~ int("cantidad") map {
            case a ~ b ~ c ~ d ~ e => (a, b, c, d, e)
          }
          println("Leyendo carga Inicial")
          var r = Random
          val rand = r.nextInt(500)
          val tablename = "tmp_" + rand
          val _parseTemp = get[Int]("aap_id") ~ 
                           get[Int]("esta_id") ~
                           get[Int]("aaco_id") ~ 
                           get[String]("aap_tecnologia") ~ 
                           get[Int]("aap_potencia") map { case a ~ b ~ c ~ d ~ e => (a, b, c, d, e) }
          println("Temp Table Name:" + tablename)
          val _queryTemp = f"""CREATE TEMP TABLE $tablename%s AS (select a.aap_id, a.esta_id, a.aaco_id, a.aacu_id, ad.aap_tecnologia, ad.aap_potencia from siap.aap a
          inner join siap.aap_adicional ad on ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
          where a.aap_fechatoma < {fecha_inicial} and a.aap_id <> 9999999
          order by ad.aap_tecnologia desc, ad.aap_potencia, a.aap_id)"""

          val _querySelTemp = f"SELECT * FROM $tablename%s a ORDER BY a.aap_tecnologia desc, a.aap_potencia, a.aap_id"

          val _queryUpdTemp = f"UPDATE $tablename%s SET aaco_id = {aaco_id}, aap_tecnologia = {aap_tecnologia}, aap_potencia = {aap_potencia}, esta_id = {esta_id} WHERE aap_id = {aap_id}"

          val _querySearchLastReport = """SELECT distinct on (aap_id) dd.aap_id, r.repo_id, r.repo_fechasolucion, d.tire_id, dd.aaco_id_anterior, dd.aaco_id, dd.aap_tecnologia_anterior, dd.aap_tecnologia, dd.aap_potencia_anterior, dd.aap_potencia FROM siap.reporte_direccion_dato dd
                                          INNER JOIN siap.reporte_direccion d on d.aap_id = dd.aap_id and d.repo_id = dd.repo_id and d.even_id = dd.even_id
                                          INNER JOIN siap.reporte r ON r.repo_id = dd.repo_id
                                          WHERE r.repo_fechasolucion < {fecha_corte} and r.reti_id <> 1 and dd.aap_id  = {aap_id}
                                          ORDER BY dd.aap_id, r.repo_fechasolucion desc"""
          
          val _parseSearch = get[Int]("aap_id") ~ get[Int]("repo_id") ~ get[DateTime]("repo_fechasolucion") ~ get[Option[Int]]("tire_id") ~
                             get[Option[Int]]("aaco_id_anterior") ~ get[Option[Int]]("aaco_id") ~ 
                             get[Option[String]]("aap_tecnologia_anterior") ~ get[Option[String]]("aap_tecnologia") ~
                             get[Option[Int]]("aap_potencia_anterior") ~ get[Option[Int]]("aap_potencia") map 
                             { case a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j => (a, b, c, d, e, f, g, h, i, j) }
          // Analizamos la información por cada luminaria seleccionada
          val _rCreateTemp = SQL(_queryTemp)
            .on(
              'fecha_inicial -> fi
            ).execute()
          
          val _rSel = SQL(_querySelTemp).as(_parseTemp.*)
          _rSel.map {
            aaps => 
              val _r = SQL(_querySearchLastReport)
              .on(
                'aap_id -> aaps._1,
                'fecha_corte -> fi
              ).as(_parseSearch.singleOpt)
              _r match {
                case Some(r) => // Actualizar la tabla con los valores existentes
                     var conexion = aaps._3
                     var tecnologia = aaps._4
                     var potencia = aaps._5
                     var estado = aaps._2
                     r._4 match {
                       case Some(3) => estado = 9
                       case Some(1) => estado = 1
                       case Some(2) => estado = 2
                       case None => None 
                     } 
                     r._6 match {
                       case Some(aaco_id) => conexion = aaco_id
                       case None => None
                     }
                     r._8 match {
                       case Some(aap_tecnologia) => tecnologia = aap_tecnologia
                       case None => None
                     }
                     r._10 match {
                       case Some(aap_potencia) => potencia = aap_potencia
                       case None => None 
                     }

                     SQL(_queryUpdTemp).on(
                       'aaco_id -> conexion,
                       'esta_id -> estado,
                       'aap_tecnologia -> tecnologia,
                       'aap_potencia -> potencia,
                       'aap_id -> aaps._1
                     )

                case None => None
              }
          }

          /*
          * Reemplazamos la lectura de la tabla temporal con el calculo de estado
          * de las luminarias, por la lectura de las tabla corte
          */
            /* val _rcargaInicial = SQL(
                f"""SELECT a.aaco_id, a.aacu_id, a.aap_tecnologia, a.aap_potencia, COUNT(a) AS cantidad FROM $tablename a
                INNER JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
                LEFT JOIN siap.aap_potenciareal ap ON ap.aapr_tecnologia = ad.aap_tecnologia AND ap.aapr_potencia = ad.aap_potencia 
                WHERE a.esta_id <> 9 AND a.aaco_id <> 3
                GROUP BY a.aaco_id, a.aacu_id, a.aap_tecnologia, a.aap_potencia 
                ORDER BY a.aaco_id, a.aacu_id, a.aap_tecnologia DESC, a.aap_potencia"""
              ).as(_cargaInicialParser.*)
            */  
          val _rcargaInicial = SQL("""SELECT a.aaco_id, a.aacu_id, ad.aap_tecnologia, ad.aap_potencia, count(a) as cantidad FROM siap.aap_corte_periodo a
          INNER JOIN siap.aap_adicional_corte_periodo ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id and ad.zanho = a.zanho and ad.zmes = a.zmes
          LEFT JOIN siap.aap_potenciareal apr ON apr.aapr_tecnologia = ad.aap_tecnologia AND apr.aapr_potencia = ad.aap_potencia
          WHERE a.esta_id <> 9 AND a.aaco_id <> 4 AND a.zanho = {zanho} AND a.zmes = {zmes} AND a.aap_id <> 9999999
          GROUP BY a.aaco_id, a.aacu_id, ad.aap_tecnologia, ad.aap_potencia
          ORDER BY a.aaco_id, a.aacu_id, ad.aap_tecnologia, ad.aap_potencia""").on(
            'zanho -> zanho_ant,
            'zmes -> zmes_ant
          ).as(_cargaInicialParser.*)
          // Eliminar anterior
          println("Eliminando Carga Anterior mismo Periodo")
          SQL(
            """DELETE FROM siap.carga WHERE empr_id = {empr_id} and zanho = {zanho} and zperiodo = {zperiodo}"""
          ).on(
              'empr_id -> empr_id,
              'zanho -> anho,
              'zperiodo -> periodo
            )
            .execute()

          println("Insertando carga Inicial")
          _rcargaInicial.map(
            r =>
              SQL(
                """INSERT INTO siap.carga (empr_id, zanho, zperiodo, aaco_id, aacu_id, aap_tecnologia, aap_potencia, cantidad, retirada, instalada) 
                        VALUES ({empr_id}, {zanho}, {zperiodo}, {aaco_id}, {aacu_id}, {aap_tecnologia}, {aap_potencia}, {cantidad}, {retirada}, {instalada})"""
              ).on(
                  'empr_id -> empr_id,
                  'zanho -> anho,
                  'zperiodo -> periodo,
                  'aaco_id -> r._1,
                  'aacu_id -> r._2,
                  'aap_tecnologia -> r._3,
                  'aap_potencia -> r._4,
                  'cantidad -> r._5,
                  'retirada -> 0,
                  'instalada -> 0
                )
                .executeUpdate()
          )

          val _dataParser = int("aaco_id") ~ int("aacu_id") ~ str("aacu_descripcion") ~ 
                            str("aap_tecnologia") ~ int("aap_potencia") ~ int("count") map {
            case a ~ b ~ c ~ d ~ e ~ f => (a, b, c, d, e, f)
          }


          /*
          println("Actualizando Cantidades Anteriores")
          var zanho_ant = anho
          var zperiodo_ant = periodo - 1
          if (zperiodo_ant == 0) {
            zperiodo_ant = 12
            zanho_ant = anho - 1
          }
          val _rdataAnterior = SQL(
            """SELECT c.aaco_id, c.aacu_id, c.aap_tecnologia, c.aap_potencia, (cantidad - retirada + instalada) as count
                                        FROM siap.carga c
                                        WHERE c.empr_id = {empr_id} AND c.zanho = {zanho} AND c.zperiodo = {zperiodo}"""
          ).on(
              'empr_id -> empr_id,
              'zanho -> zanho_ant,
              'zperiodo -> zperiodo_ant
            )
            .as(_dataParser.*)

          _rdataAnterior.map { r =>
            SQL(
              """UPDATE siap.carga SET cantidad = {cantidad}
                       WHERE empr_id = {empr_id} and zanho = {zanho} and zperiodo = {zperiodo} and
                             aaco_id = {aaco_id} and aacu_id = {aacu_id} and aap_tecnologia = {aap_tecnologia} and aap_potencia = {aap_potencia}"""
            ).on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._3,
                'aap_potencia -> r._4,
                'cantidad -> r._5,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
          }
          */
          println("Por Expansion")
          var _listExpansionRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          val _qcargaPorExpansion =
            """
            SELECT o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 2 AND ra.repo_tipo_expansion <> 4 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          val _rcargaPorExpansion = SQL(_qcargaPorExpansion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listExpansionRow += _headerData
          _rcargaPorExpansion.map { r =>
            _listExpansionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )

            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
            println("Lineas Expansion Actualizadas: " + updated)
          }

          println("Por Reubicacion")
          var _listReubicacionRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          val _qcargaPorReubicacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 3 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          val _rcargaPorReubicacion = SQL(_qcargaPorReubicacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listReubicacionRow += _headerData
          _rcargaPorReubicacion.map { r =>
            _listReubicacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )

            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
          }

          println("Por Repotenciacion")
          // Instalada
          var _listRepotenciacionRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()          
          var _qcargaPorRepotenciacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 4 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          var _rcargaPorRepotenciacion = SQL(_qcargaPorRepotenciacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listRepotenciacionRow += _headerData
          _rcargaPorRepotenciacion.map { r =>
            _listRepotenciacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )            
            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
          }
          // Retirada
            _qcargaPorRepotenciacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia_anterior as aap_potencia, dd.aap_tecnologia_anterior as aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 4 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia_anterior, dd.aap_potencia_anterior) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          _rcargaPorRepotenciacion = SQL(_qcargaPorRepotenciacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)

          _rcargaPorRepotenciacion.map { r =>
            _listRepotenciacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )              
            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> r._6,
                'instalada -> 0,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()  
          }          

          println("Por Actualizacion")

          // Ingresar
          var _listActualizacionRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()   
          var _qcargaPorActualizacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 5 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          var _rcargaPorActualizacion = SQL(_qcargaPorActualizacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listActualizacionRow += _headerData
          _rcargaPorActualizacion.map { r =>

            _listActualizacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )              


            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
          }
          // Retirar
            _qcargaPorActualizacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia_anterior as aap_potencia, dd.aap_tecnologia_anterior as aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 5 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia_anterior, dd.aap_potencia_anterior) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          _rcargaPorActualizacion = SQL(_qcargaPorActualizacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)

          _rcargaPorActualizacion.map { r =>

            _listActualizacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )              

            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()   
          }      

          println("Por Modernizacion")
          
          // Instalada
          var _listModernizacionRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()   
          var _qcargaPorModernizacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 6 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          var _rcargaPorModernizacion = SQL(_qcargaPorModernizacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listModernizacionRow += _headerData
          _rcargaPorModernizacion.map { r =>

            _listModernizacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )              

            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
            println("Lineas Modernización Actualizadas: " + updated)
          }

          // Retirada
          _qcargaPorModernizacion =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia_anterior as aap_potencia, dd.aap_tecnologia_anterior as aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 6 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia_anterior, dd.aap_potencia_anterior) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          _rcargaPorModernizacion = SQL(_qcargaPorModernizacion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)

          _rcargaPorModernizacion.map { r =>
            _listModernizacionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )            
            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> r._6,
                'instalada -> 0,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
            println("Lineas Modernización Actualizadas: " + updated)
          }     

          // Por Cambio de Medida

          println("Por Cambio de Medida")
          
          // Instalada
          var _listCambioMedidaRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()   
          var _qcargaPorCambioMedida =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, dd.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 9 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          var _rcargaPorCambioMedida = SQL(_qcargaPorCambioMedida)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
            _listCambioMedidaRow += _headerData
            _rcargaPorCambioMedida.map { r =>

              _listCambioMedidaRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )              

            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
          }

          // Retirada
          _qcargaPorCambioMedida =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia_anterior as aap_potencia, dd.aap_tecnologia_anterior as aap_tecnologia, dd.aaco_id_anterior as aaco_id, ac.aacu_id, ac.aacu_descripcion
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 6 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia_anterior, dd.aap_potencia_anterior) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          _rcargaPorCambioMedida = SQL(_qcargaPorCambioMedida)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)

          _rcargaPorCambioMedida.map { r =>
            _listCambioMedidaRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )            
            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> r._6,
                'instalada -> 0,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
          }     

          // Fin Por Cambio de Medida

          println("Por Reposicion")
          var _listReposicionRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          val _qcargaPorReposicion =
            """
            SELECT o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia, dd.aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id
            WHERE r.reti_id = 7 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia, dd.aap_potencia) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          val _rcargaPorReposicion = SQL(_qcargaPorReposicion)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listReposicionRow += _headerData
          _rcargaPorReposicion.map { r =>
            _listReposicionRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )            
            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> 0,
                'instalada -> r._6,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
            println("Lineas Reposición Actualizadas: " + updated)
          }

          println("Por Retiro")
          var _listRetiroRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]() 
          val _qcargarPorRetiro =
            """
            SELECT o.aaco_id, o.aacu_id,  o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia, count(o) from
            (SELECT DISTINCT d.aap_id, dd.aap_potencia_anterior as aap_potencia, dd.aap_tecnologia_anterior as aap_tecnologia, co.aaco_id, ac.aacu_id, ac.aacu_descripcion 
            FROM siap.reporte r
            LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
            LEFT JOIN siap.reporte_direccion d ON d.repo_id = r.repo_id
            LEFT join siap.reporte_direccion_dato dd ON dd.repo_id = d.repo_id AND dd.even_id = d.even_id AND dd.aap_id = d.aap_id
            LEFT JOIN siap.reporte_direccion_dato_adicional dda ON dda.repo_id = d.repo_id AND dda.even_id = d.even_id AND dda.aap_id = d.aap_id
            LEFT JOIN siap.aap a ON a.aap_id = d.aap_id
            LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
            LEFT JOIN siap.aap_conexion co ON co.aaco_id = dd.aaco_id_anterior
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = dda.aacu_id_anterior
            WHERE r.reti_id = 8 AND r.repo_fechasolucion BETWEEN {fecha_inicial} AND {fecha_final} AND r.empr_id = {empr_id} AND a.aap_id <> 9999999 AND d.even_estado <> 9 AND dd.aaco_id_anterior in (1,2) AND a.aap_fechatoma <= {fecha_toma}
            ORDER BY ac.aacu_id, dd.aap_tecnologia_anterior, dd.aap_potencia_anterior) o
            GROUP BY o.aaco_id, o.aacu_id, o.aacu_descripcion, o.aap_tecnologia, o.aap_potencia
            ORDER BY o.aaco_id, o.aacu_id, o.aap_tecnologia, o.aap_potencia"""

          val _rcargarPorRetiro = SQL(_qcargarPorRetiro)
            .on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'fecha_toma -> ft,
              'empr_id -> empr_id
            )
            .as(_dataParser.*)
          _listRetiroRow += _headerData
          _rcargarPorRetiro.map { r =>

            _listRetiroRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                r._1 match { case 1 => "AFORO" case 2 => "MEDIDOR" },
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                r._3,
                Some(1),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet                
              ),
              StringCell(
                r._4,
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._5,
                Some(3),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                r._6,
                Some(4),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                0,
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )            

            val updated = SQL(sqlUpdate)
              .on(
                'aaco_id -> r._1,
                'aacu_id -> r._2,
                'aap_tecnologia -> r._4,
                'aap_potencia -> r._5,
                'retirada -> r._6,
                'instalada -> 0,
                'empr_id -> empr_id,
                'zanho -> anho,
                'zperiodo -> periodo
              )
              .executeUpdate()
            println("Lineas Retiro Actualizadas: " + updated)
          }

          val filasHoja = ListBuffer[com.norbitltd.spoiwo.model.Row]()
          val titleRow1 = com.norbitltd.spoiwo.model
            .Row(
              style = CellStyle(
                font = Font(bold = true),
                horizontalAlignment = HA.Center
              )
            )
            .withCellValues(electrificadora)
          val titleRow2 = com.norbitltd.spoiwo.model
            .Row(
              style = CellStyle(
                font = Font(bold = true),
                horizontalAlignment = HA.Center
              )
            )
            .withCellValues(
              "RESUMEN CARGA INSTALADA " + empresa.empr_descripcion
            )
          val titleRow3 = com.norbitltd.spoiwo.model
            .Row(
              style = CellStyle(
                font = Font(bold = true),
                horizontalAlignment = HA.Center
              )
            )
            .withCellValues(
              "SEGUN INVENTARIO VERIFICADO Y ACEPTADO POR LAS PARTES"
            )
          val headerRow1 = com.norbitltd.spoiwo.model
            .Row(
              style = CellStyle(
                font = Font(bold = true, height = 8.points, fontName = "Arial"),
                horizontalAlignment = HA.Center
              )
            )
            .withCellValues(
              "Tipo",
              "Potencia",
              "Potencia",
              "Datos " + mes + " " + anho,
              "",
              "Novedades",
              "",
              "",
              "Datos Finales Energía Calculada Según RES CREG 123/2011 Art. 12",
              "",
              "",
              "",
              "",
              "",
              "",
              "TARIFA",
              "Valor Energía",
              "DESCUENTO",
              "Valor Energía"
            )
          val headerRow2 = com.norbitltd.spoiwo.model
            .Row(
              style = CellStyle(
                font = Font(bold = true, height = 8.points, fontName = "Arial"),
                horizontalAlignment = HA.Center
              )
            )
            .withCellValues(
              "de",
              "Nominal",
              "Real",
              "Cantidad",
              "Potencia",
              "Retiradas",
              "Instaladas",
              "Balance",
              "Cantidad",
              "Potencia",
              "Qn",
              "Tn",
              "DPFn",
              "CEEn",
              "",
              "$/Kwh",
              "Mes/$",
              "Mes/$",
              "Mes/$"
            )
          val headerRow3 =
            com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font =
                    Font(bold = true, height = 10.points, fontName = "Arial"),
                  horizontalAlignment = HA.Center
                )
              )
              .withCellValues(
                "Luminaria",
                "[w]",
                "(w)",
                "Cantidad",
                "Potencia",
                "Retiradas",
                "Instaladas",
                "Balance",
                "[u]",
                "[w]",
                "[Kw]",
                "",
                "",
                "Kwh",
                "",
                "",
                "BRUTA",
                0.03,
                "NETA"
              )

          _listRow += titleRow1
          _listRow += titleRow2
          _listRow += titleRow3
          _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
          _listRow += headerRow1
          _listRow += headerRow2
          _listRow += headerRow3

          var j = 7
          var fila_suma_inicial = j + 1
          var filas_suma_total = ""
          var rangos = ListBuffer[(Int, Int)]()
          var filas = {
            _rcuentas.map { cuenta =>
              println("cuenta: " + cuenta)
              val _raaps = SQL(
                """SELECT c.aap_tecnologia, c.aap_potencia, ap.aap_potenciareal, c.cantidad, c.retirada, c.instalada FROM siap.carga c
                                    LEFT JOIN siap.aap_potenciareal ap ON ap.aapr_tecnologia = c.aap_tecnologia AND ap.aapr_potencia = c.aap_potencia
                                    WHERE c.aaco_id = {aaco_id} AND c.aacu_id = {aacu_id} AND c.empr_id = {empr_id} AND c.zanho = {zanho} and c.zperiodo = {zperiodo}
                                    ORDER BY c.aap_tecnologia, c.aap_potencia"""
              ).on(
                  'aaco_id -> 1,
                  'aacu_id -> cuenta._2,
                  'empr_id -> empr_id,
                  'zanho -> anho,
                  'zperiodo -> periodo
                )
                .as(_aapParser.*)

              _raaps.map { aap =>
                j += 1
                _listRow += com.norbitltd.spoiwo.model.Row(
                  StringCell(
                    aap._1.get,
                    Some(0),
                    style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    aap._2.get,
                    Some(1),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    aap._3 match {
                      case Some(value) => value
                      case None        => 0
                    },
                    Some(2),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    aap._4.get.doubleValue(),
                    Some(3),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "+D" + j + "*C" + j,
                    Some(4),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    aap._5.get.doubleValue(),
                    Some(5),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    aap._6.get.doubleValue(),
                    Some(6),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "+G" + j + "-F" + j,
                    Some(7),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "+D" + j + "+H" + j,
                    Some(8),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "I" + j + "*C" + j,
                    Some(9),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "J" + j + "/1000",
                    Some(10),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    12,
                    Some(11),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    ff.getDayOfMonth(), // fecha_final.getActualMaximum(Calendar.DATE),
                    Some(12),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  FormulaCell(
                    "K" + j + "*L" + j + "*M" + j,
                    Some(13),
                    style =
                      Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                )
              }

              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  "Cuenta No. " + cuenta,
                  Some(0),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      fillForegroundColor = Color.Yellow,
                      fillPattern = CellFill.Solid
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(1),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(2),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(D" + fila_suma_inicial + ":D" + j + ")",
                  Some(3),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(4),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(5),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(6),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(7),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(I" + fila_suma_inicial + ":I" + j + ")",
                  Some(8),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(J" + fila_suma_inicial + ":J" + j + ")",
                  Some(9),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(K" + fila_suma_inicial + ":K" + j + ")",
                  Some(10),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(11),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(12),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(N" + fila_suma_inicial + ":N" + j + ")",
                  Some(13),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(14),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(15),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  512,
                  Some(16),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "N" + (j + 1) + "*R7",
                  Some(17),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "(N" + (j + 1) + "-R" + (j + 1) + ")*Q" + (j + 1),
                  Some(18),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "S" + (j + 1),
                  Some(19),
                  style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              rangos += { (fila_suma_inicial, j) }
              j += 1
              filas_suma_total += "D" + j + ","
              _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
              j += 1
              fila_suma_inicial = j
            }
            var cadena = ""
            rangos.foreach { r =>
              cadena += "SUM(E" + r._1 + ":E" + r._2 + ")+"
            }
            cadena = cadena.splitAt(cadena.lastIndexOf("+"))._1
            var cadena2 = cadena.replace("E", "F")
            var cadena3 = cadena.replace("E", "G")
            var cadena4 = cadena.replace("E", "H")
            var cadena5 = cadena.replace("E", "I")
            var cadena6 = cadena.replace("E", "J")
            var cadena7 = cadena.replace("E", "N")
            println("cadena suma: " + cadena)
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "TOTALES ",
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    fillForegroundColor = Color.Yellow,
                    fillPattern = CellFill.Solid
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total + ")",
                Some(3),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                cadena,
                Some(4),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                cadena2,
                Some(5),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                cadena3,
                Some(6),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                cadena4,
                Some(7),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total.replace("D", "I") + ")",
                Some(8),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total.replace("D", "J") + ")",
                Some(9),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(10),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(11),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(12),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total.replace("D", "N") + ")",
                Some(13),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(14),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(15),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "TOTAL $",
                Some(16),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total.replace("D", "R") + ")",
                Some(17),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total.replace("D", "S") + ")",
                Some(18),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(" + filas_suma_total.replace("D", "T") + ")",
                Some(19),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          }
          j += 1
          _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("")
          j += 1

          fila_suma_inicial = j
          var medidor = SQL(
            """SELECT c.aap_tecnologia, c.aap_potencia, ap.aap_potenciareal, SUM(c.cantidad) AS cantidad, SUM(c.retirada) AS retirada, SUM(c.instalada) AS instalada FROM siap.carga c
            LEFT JOIN siap.aap_potenciareal ap ON ap.aapr_tecnologia = c.aap_tecnologia AND ap.aapr_potencia = c.aap_potencia
            WHERE c.aaco_id = {aaco_id} AND c.empr_id = {empr_id} and c.zanho = {zanho} and c.zperiodo = {zperiodo}
            GROUP BY c.aap_tecnologia, c.aap_potencia, ap.aap_potenciareal
            ORDER BY c.aap_tecnologia, c.aap_potencia"""
          ).on(
              'empr_id -> empr_id,
              'aaco_id -> 2,
              'zanho -> anho,
              'zperiodo -> periodo
            )
            .as(_aapParser.*)

          medidor.map { aap =>
            j += 1
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                aap._1.get,
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                aap._2.get,
                Some(1),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                aap._3 match {
                  case Some(value) => value
                  case None        => 0
                },
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                aap._4.get.doubleValue(),
                Some(3),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "+D" + j + "*C" + j,
                Some(4),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                aap._5.get.doubleValue(),
                Some(5),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                aap._6.get.doubleValue(),
                Some(6),
                style =
                  Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "+G" + j + "-F" + j,
                Some(7),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "+D" + j + "+H" + j,
                Some(8),
                style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          }

          _listRow += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL BAJO MEDIDA",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  fillForegroundColor = Color.Yellow,
                  fillPattern = CellFill.Solid
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(1),
              style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(2),
              style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(D" + fila_suma_inicial + ":D" + j + ")",
              Some(3),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(E" + fila_suma_inicial + ":E" + j + ")",
              Some(4),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(F" + fila_suma_inicial + ":F" + j + ")",
              Some(5),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(G" + fila_suma_inicial + ":G" + j + ")",
              Some(6),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(H" + fila_suma_inicial + ":H" + j + ")",
              Some(7),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(I" + fila_suma_inicial + ":I" + j + ")",
              Some(8),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(J" + fila_suma_inicial + ":J" + j + ")",
              Some(9),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(K" + fila_suma_inicial + ":K" + j + ")",
              Some(10),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(11),
              style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(12),
              style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(N" + fila_suma_inicial + ":N" + j + ")",
              Some(13),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          j += 1
          _listRow += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "GRAN TOTAL INVENTARIO",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  fillForegroundColor = Color.Yellow,
                  fillPattern = CellFill.Solid
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(1),
              style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(2),
              style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(D" + (fila_suma_inicial - 1) + ",D" + j + ")",
              Some(3),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(E" + (fila_suma_inicial - 1) + ",E" + j + ")",
              Some(4),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(F" + (fila_suma_inicial - 1) + ",F" + j + ")",
              Some(5),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(G" + (fila_suma_inicial - 1) + ",G" + j + ")",
              Some(6),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(H" + (fila_suma_inicial - 1) + ",H" + j + ")",
              Some(7),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(I" + (fila_suma_inicial - 1) + ",I" + j + ")",
              Some(8),
              style = Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )

          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 13))
            _listMerged += CellRange((1, 1), (0, 13))
            _listMerged += CellRange((2, 2), (0, 13))
            _listMerged += CellRange((4, 4), (3, 4))
            _listMerged += CellRange((4, 4), (5, 7))
            _listMerged += CellRange((4, 4), (8, 13))
            _listMerged.toList
          }

          var columnstyle = {
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 0, width = new Width(50, WidthUnit.Character))
            _listColumn.toList
          }
          val sheet1 = Sheet(
            name = "HojaT",
            rows = _listRow.toList,
            mergedRegions = mergedColumns,
            columns = columnstyle
          )
          val sheetExpansion = Sheet(
            name = "Expansión",
            rows = _listExpansionRow.toList,
          )
          val sheetReubicacion = Sheet(
            name = "Reubicación",
            rows = _listReubicacionRow.toList,
          )     
          val sheetRepotenciacion = Sheet(
            name = "Repotenciación",
            rows = _listRepotenciacionRow.toList,
          ) 
          val sheetActualizacion = Sheet(
            name = "Actualización",
            rows = _listActualizacionRow.toList,
          )    
          var sheetModernizacion = Sheet(
            name = "Modernización",
            rows = _listModernizacionRow.toList,
          ) 
          var sheetRetiro = Sheet(
            name = "Retiro",
            rows = _listRetiroRow.toList,
          )   
          var sheetReposicion = Sheet(
            name = "Reposición",
            rows = _listReposicionRow.toList,
          )
          var sheetCambioMedida = Sheet(
            name = "Cambio Medida",
            rows = _listCambioMedidaRow.toList,
          )                                                         

          println("Escribiendo en el Stream")
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook(sheet1, 
                   sheetRetiro,
                   sheetExpansion, 
                   sheetReubicacion, 
                   sheetReposicion,
                   sheetRepotenciacion, 
                   sheetActualizacion,
                   sheetModernizacion,
                   sheetCambioMedida
                   ).writeToOutputStream(os)
          println("Stream Listo")
          os.toByteArray
      }
    }

  }

  def siap_medidor_xls(empr_id: scala.Long): Array[Byte] = {
    import Height._
    db.withConnection { implicit connection =>
      val empresa = empresaService.buscarPorId(empr_id)
      empresa match {
        case Some(empresa) =>
          val format = new SimpleDateFormat("yyyy-MM-dd HH:mm")
          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 3))
            _listMerged += CellRange((1, 1), (0, 3))
            _listMerged += CellRange((2, 2), (0, 3))
            _listMerged.toList
          }
          val _mParser = get[Option[Int]]("medi_id") ~ get[Option[String]]("medi_codigo") ~ get[Option[String]](
            "medi_numero"
          ) ~ get[Option[String]]("medi_direccion") ~ get[Option[String]](
            "aacu_descripcion"
          ) ~ get[Option[Int]]("cantidad") map {
            case a ~ b ~ c ~ d ~ e ~ f => (a, b, c, d, e, f)
          }
          val resultSet = SQL(
            """SELECT m.medi_id, to_char(m.medi_id, '0000') as medi_codigo, m.medi_numero, m.medi_direccion, ac.aacu_descripcion, COUNT(a.*) AS cantidad FROM siap.medidor m
                                 LEFT JOIN siap.aap_medidor am ON am.medi_id = m.medi_id AND am.empr_id = m.empr_id
                                 LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = m.aacu_id
                                 LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id and a.esta_id <> 9 and a.aap_id <> 999999
                                 WHERE m.empr_id = {empr_id}
                                 GROUP BY m.medi_id, medi_codigo, m.medi_numero, m.medi_direccion, ac.aacu_descripcion 
                                 ORDER BY m.medi_id"""
          ).on(
              'empr_id -> empr_id
            )
            .as(_mParser.*)
          val sheet1 = Sheet(
            name = "Medidores",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa.empr_descripcion)
              val title2Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("INFORME DE MEDIDORES")
              val title3Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  "GENERADO EL " + format
                    .format(Calendar.getInstance().getTime())
                )
              val headerRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  "Código",
                  "Número",
                  "Dirección",
                  "Cuenta Alumbrado",
                  "Cantidad"
                )
              var j = 2
              val rows = resultSet.map {
                med =>
                  j += 1
                  val link = new HyperLinkUrl(med._3 match {
                    case Some(m) => m
                    case None    => ""
                  }, "#M" + (med._3 match {
                    case Some(m) => m
                    case None    => ""
                  }))
                  com.norbitltd.spoiwo.model.Row(
                    StringCell(
                      med._2 match {
                        case Some(m) => m
                        case None    => ""
                      },
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),                    
                    HyperLinkUrlCell(
                      link,
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      med._4 match {
                        case Some(m) => m
                        case None    => ""
                      },
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      med._5 match {
                        case Some(m) => m
                        case None    => ""
                      },
                      Some(3),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell(
                      med._6.get,
                      Some(4),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }
              title1Row :: title2Row :: title3Row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var _listSheet = new ListBuffer[Sheet]()
          _listSheet += sheet1
          resultSet.map { m =>
            val fmt = DateTimeFormat.forPattern("yyyyMMdd")
            val sheet = Sheet(
              name = "M" + (m._3 match {
                case Some(m) => m
                case None    => ""
              }),
              rows = {
                val headerRow = com.norbitltd.spoiwo.model
                  .Row()
                  .withCellValues(
                    "Código",
                    "Apoyo",
                    "Estado",
                    "Dirección",
                    "Barrio",
                    "Sector",
                    "Uso",
                    "Modernizada",
                    "Año Modernizada",
                    "Tipo Luminaria",
                    "Medidor",
                    "Tipo Medida",
                    "Marca",
                    "Modelo",
                    "Poste",
                    "Poste Altura",
                    "Poste Propietario",
                    "Brazo",
                    "Collarin",
                    "Potencia",
                    "Tecnologia",
                    "Reporte Técnico",
                    "Bombillo",
                    "Balasto",
                    "Arrancador",
                    "Condensador",
                    "Fotocelda",
                    "Medidor Código",
                    "Medidor Número",
                    "Cuenta Alumbrado",
                    "Transformador Código",
                    "Transformador Número"
                  )
                val aapSet =
                  SQL("""SELECT
                          a.aap_id,
                          a.aap_apoyo,
                          s.esta_descripcion,
                          a.aap_direccion,
                          b.barr_descripcion,
                          tb.tiba_descripcion,
                          us.aaus_descripcion,
                          a.aap_modernizada,
                          d.aap_modernizada_anho,
                          tc.aatc_descripcion,
                          a.aap_medidor,
                          co.aaco_descripcion,
                          ma.aama_descripcion,
                          mo.aamo_descripcion,
                          tp.tipo_descripcion,
                          d.aap_poste_altura,
                          d.aap_brazo,
                          d.aap_collarin,
                          d.aap_potencia,
                          d.aap_tecnologia,
                          d.aap_rte,
                          d.aap_poste_propietario,
                          e.aap_bombillo,
                          e.aap_balasto,
                          e.aap_arrancador,
                          e.aap_condensador,
                          e.aap_fotocelda,
                          to_char(m.medi_id, '0000') as medi_codigo,
                          m.medi_numero,
                          (CASE WHEN a.aaco_id = 1 THEN cu.aacu_descripcion WHEN a.aaco_id = 2 THEN mcu.aacu_descripcion ELSE '' END) AS aacu_descripcion,
                          to_char(t.tran_id, '0000') as tran_codigo,
                          t.tran_numero
                         FROM siap.aap a
                         LEFT JOIN siap.estado s ON s.esta_id = a.esta_id
                         LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id AND d.empr_id = a.empr_id
                         LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id AND e.empr_id = a.empr_id
                         LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                         LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                         LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                         LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                         LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                         LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                         LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                         LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                         LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                         LEFT JOIN siap.aap_medidor am ON am.aap_id = a.aap_id AND am.empr_id = a.empr_id
                         LEFT JOIN siap.medidor m ON m.medi_id = am.medi_id and m.empr_id = am.empr_id
                         LEFT JOIN siap.aap_cuentaap mcu ON mcu.aacu_id = m.aacu_id and mcu.empr_id = m.empr_id
                         LEFT JOIN siap.aap_transformador at ON at.aap_id = a.aap_id and at.empr_id = a.empr_id
                         LEFT JOIN siap.transformador t ON t.tran_id = at.tran_id and t.empr_id = at.empr_id
                         WHERE a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id <> 9999999 and am.medi_id = {medi_id}
                         ORDER BY a.aap_id ASC
                                """)
                    .on(
                      'empr_id -> empr_id,
                      'medi_id -> m._1
                    )
                    .as(Siap_inventario.Siap_inventario_set *)
                val rows = aapSet.map {
                  i =>
                    com.norbitltd.spoiwo.model
                      .Row()
                      .withCellValues(
                        i.aap_id match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_apoyo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.esta_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_direccion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.barr_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tiba_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aaus_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_modernizada match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_modernizada_anho match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aatc_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_medidor match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aaco_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aama_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aamo_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tipo_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_poste_altura match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_poste_propietario match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_brazo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_collarin match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_potencia match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_tecnologia match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_rte match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_bombillo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_balasto match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_arrancador match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_condensador match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_fotocelda match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.medi_codigo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.medi_numero match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aacu_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tran_codigo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tran_numero match {
                          case Some(value) => value
                          case None        => ""
                        }
                      )
                }
                headerRow :: rows.toList
              }
            )
            _listSheet += sheet
          }
          println("Escribiendo en el Stream")
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook().addSheets(_listSheet).writeToOutputStream(os)
          println("Stream Listo")
          os.toByteArray
        case None =>
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          os.toByteArray
      }
    }
  }

  def siap_transformador_xls(empr_id: scala.Long): Array[Byte] = {
    import Height._
    db.withConnection { implicit connection =>
      val empresa = empresaService.buscarPorId(empr_id)
      empresa match {
        case Some(empresa) =>
          val format = new SimpleDateFormat("yyyy-MM-dd HH:mm")
          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 3))
            _listMerged += CellRange((1, 1), (0, 3))
            _listMerged += CellRange((2, 2), (0, 3))
            _listMerged.toList
          }
          val _mParser = int("tran_id") ~ str("tran_numero") ~ str(
            "tran_direccion"
          ) ~ str("barr_descripcion") ~ int("cantidad") map {
            case a ~ b ~ c ~ d ~ e => (a, b, c, d, e)
          }
          val resultSet = SQL(
            """SELECT m.tran_id, m.tran_numero, m.tran_direccion, b.barr_descripcion, COUNT(a.*) AS cantidad FROM siap.transformador m
                                 LEFT JOIN siap.aap_transformador am ON am.tran_id = m.tran_id AND am.empr_id = m.empr_id
                                 LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
                                 LEFT JOIN siap.barrio b ON b.barr_id = m.barr_id
                                 WHERE m.empr_id = {empr_id}
                                 GROUP BY m.tran_id, m.tran_numero, m.tran_direccion, b.barr_descripcion """
          ).on(
              'empr_id -> empr_id
            )
            .as(_mParser.*)
          val sheet1 = Sheet(
            name = "Transformadores",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa.empr_descripcion)
              val title2Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("INFORME DE TRANSFORMADORES")
              val title3Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  "GENERADO EL " + format
                    .format(Calendar.getInstance().getTime())
                )
              val headerRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Número", "Dirección", "Barrio", "Cantidad")
              var j = 2
              val rows = resultSet.map {
                med =>
                  j += 1
                  val link = new HyperLinkUrl(med._2, "#T" + med._2)
                  com.norbitltd.spoiwo.model.Row(
                    HyperLinkUrlCell(
                      link,
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      med._3,
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      med._4,
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell(
                      med._5,
                      Some(3),
                      style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }
              title1Row :: title2Row :: title3Row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var _listSheet = new ListBuffer[Sheet]()
          _listSheet += sheet1
          resultSet.map { m =>
            val fmt = DateTimeFormat.forPattern("yyyyMMdd")
            val sheet = Sheet(
              name = "T" + m._2,
              rows = {
                val headerRow = com.norbitltd.spoiwo.model
                  .Row()
                  .withCellValues(
                    "Código",
                    "Apoyo",
                    "Estado",
                    "Dirección",
                    "Barrio",
                    "Sector",
                    "Uso",
                    "Modernizada",
                    "Año Modernizada",
                    "Tipo Luminaria",
                    "Medidor",
                    "Tipo Medida",
                    "Marca",
                    "Modelo",
                    "Cuenta Alumbrado",
                    "Poste",
                    "Poste Altura",
                    "Poste Propietario",
                    "Brazo",
                    "Collarin",
                    "Potencia",
                    "Tecnologia",
                    "Reporte Técnico",
                    "Bombillo",
                    "Balasto",
                    "Arrancador",
                    "Condensador",
                    "Fotocelda",
                    "Medidor Código",
                    "Medidor Número",
                    "Cuenta Alumbrado",
                    "Transformador Código",
                    "Transformador Número"
                  )
                val aapSet =
                  SQL("""SELECT
                  a.aap_id,
                  a.aap_apoyo,
                  s.esta_descripcion,
                  a.aap_direccion,
                  b.barr_descripcion,
                  tb.tiba_descripcion,
                  us.aaus_descripcion,
                  a.aap_modernizada,
                  d.aap_modernizada_anho,
                  tc.aatc_descripcion,
                  a.aap_medidor,
                  co.aaco_descripcion,
                  ma.aama_descripcion,
                  mo.aamo_descripcion,
                  tp.tipo_descripcion,
                  d.aap_poste_altura,
                  d.aap_brazo,
                  d.aap_collarin,
                  d.aap_potencia,
                  d.aap_tecnologia,
                  d.aap_rte,
                  d.aap_poste_propietario,
                  e.aap_bombillo,
                  e.aap_balasto,
                  e.aap_arrancador,
                  e.aap_condensador,
                  e.aap_fotocelda,
                  to_char(m.medi_id, '0000') as medi_codigo,
                  m.medi_numero,
                  (CASE WHEN a.aaco_id = 1 THEN cu.aacu_descripcion WHEN a.aaco_id = 2 THEN mcu.aacu_descripcion ELSE '' END) AS aacu_descripcion,
                  to_char(t.tran_id, '0000') as tran_codigo,
                  t.tran_numero
                 FROM siap.aap a
                 LEFT JOIN siap.estado s ON s.esta_id = a.esta_id
                 LEFT JOIN siap.aap_adicional d on d.aap_id = a.aap_id AND d.empr_id = a.empr_id
                 LEFT JOIN siap.aap_elemento e on e.aap_id = a.aap_id AND e.empr_id = a.empr_id
                 LEFT JOIN siap.barrio b on b.barr_id = a.barr_id
                 LEFT JOIN siap.tipobarrio tb on tb.tiba_id = b.tiba_id
                 LEFT JOIN siap.aap_uso us on us.aaus_id = a.aaus_id	
                 LEFT JOIN siap.aap_tipo_carcasa tc on tc.aatc_id = a.aatc_id
                 LEFT JOIN siap.aap_conexion co on co.aaco_id = a.aaco_id
                 LEFT JOIN siap.aap_marca ma on ma.aama_id = a.aama_id
                 LEFT JOIN siap.aap_modelo mo on mo.aamo_id = a.aamo_id
                 LEFT JOIN siap.aap_cuentaap cu on cu.aacu_id = a.aacu_id
                 LEFT JOIN siap.tipo_poste tp on tp.tipo_id = d.tipo_id
                 LEFT JOIN siap.aap_medidor am ON am.aap_id = a.aap_id AND am.empr_id = a.empr_id
                 LEFT JOIN siap.medidor m ON m.medi_id = am.medi_id and m.empr_id = am.empr_id
                 LEFT JOIN siap.aap_cuentaap mcu ON mcu.aacu_id = m.aacu_id and mcu.empr_id = m.empr_id
                 LEFT JOIN siap.aap_transformador at ON at.aap_id = a.aap_id and at.empr_id = a.empr_id
                 LEFT JOIN siap.transformador t ON t.tran_id = at.tran_id and t.empr_id = at.empr_id
                 WHERE a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id <> 9999999 and am.tran_id = {tran_id}
                                ORDER BY a.aap_id ASC
                                """)
                    .on(
                      'empr_id -> empr_id,
                      'tran_id -> m._1
                    )
                    .as(Siap_inventario.Siap_inventario_set *)
                val rows = aapSet.map {
                  i =>
                    com.norbitltd.spoiwo.model
                      .Row()
                      .withCellValues(
                        i.aap_id match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_apoyo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.esta_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_direccion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.barr_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tiba_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aaus_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_modernizada match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_modernizada_anho match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aatc_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_medidor match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aaco_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aama_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aamo_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tipo_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_poste_altura match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_poste_propietario match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_brazo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_collarin match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_potencia match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_tecnologia match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_rte match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_bombillo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_balasto match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_arrancador match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_condensador match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aap_fotocelda match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.medi_codigo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.medi_numero match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.aacu_descripcion match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tran_codigo match {
                          case Some(value) => value
                          case None        => ""
                        },
                        i.tran_numero match {
                          case Some(value) => value
                          case None        => ""
                        }
                      )
                }
                headerRow :: rows.toList
              }
            )
            _listSheet += sheet
          }
          println("Escribiendo en el Stream")
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook().addSheets(_listSheet).writeToOutputStream(os)
          println("Stream Listo")
          os.toByteArray
        case None =>
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          os.toByteArray
      }
    }
  }

  def siap_ucap_xls(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long
  ): Array[Byte] = {
    db.withConnection { implicit connection =>
      val empresa = empresaService.buscarPorId(empr_id)
      empresa match {
        case Some(empresa) =>
          val format = new SimpleDateFormat("yyyy-MM-dd HH:mm")
          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
          var _listMerged = new ListBuffer[CellRange]()
          var mergedColumns = {
            _listMerged += CellRange((0, 0), (0, 3))
            _listMerged += CellRange((1, 1), (0, 3))
            _listMerged += CellRange((2, 2), (0, 3))
            _listMerged.toList
          }
          val fi = new DateTime(fecha_inicial)
          val ff = new DateTime(fecha_final)
          val _uParser = int("ucap_id") ~ str("ucap_descripcion") ~ double(
            "cantidad"
          ) map { case a ~ b ~ c => (a, b, c) }
          val resultSet = SQL(
            """SELECT u.ucap_id, u.ucap_descripcion, SUM(re.even_cantidad_instalado) as cantidad FROM siap.ucap u 
            INNER JOIN siap.elemento e ON e.ucap_id = u.ucap_id
            INNER JOIN siap.reporte_evento re ON re.elem_id = e.elem_id
            INNER JOIN siap.reporte r ON r.repo_id = re.repo_id
            WHERE u.ucap_estado <> 9 and r.empr_id = {empr_id} and r.repo_fechasolucion between {fecha_inicial} and {fecha_final}
            GROUP BY u.ucap_id, u.ucap_descripcion"""
          ).on(
              'fecha_inicial -> fi,
              'fecha_final -> ff,
              'empr_id -> empr_id
            )
            .as(_uParser *)

          val sheet1 = Sheet(
            name = "UCAPs",
            rows = {
              val title1Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa.empr_descripcion)
              val title2Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("INFORME DE UNIDADES CONSTRUCTIVAS")
              val title3Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  "GENERADO EL " + format
                    .format(Calendar.getInstance().getTime())
                )
              val headerRow = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Uid", "Unidad Construtiva", "Cantidad")
              var j = 2
              val rows = resultSet.map {
                u =>
                  j += 1
                  val link = new HyperLinkUrl(u._1.toString, "#U" + u._1)
                  com.norbitltd.spoiwo.model.Row(
                    HyperLinkUrlCell(
                      link,
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      u._2,
                      Some(1),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    NumericCell(
                      u._3,
                      Some(3),
                      style =
                        Some(CellStyle(dataFormat = CellDataFormat("#0.00"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )
                  )
              }
              title1Row :: title2Row :: title3Row :: headerRow :: rows.toList
            },
            mergedRegions = mergedColumns
          )
          var _listSheet = new ListBuffer[Sheet]()
          _listSheet += sheet1
          val _ueParser = str("elem_codigo") ~ str("elem_descripcion") ~ double(
            "cantidad"
          ) map { case a ~ b ~ c => (a, b, c) }

          resultSet.map { e =>
            val fmt = DateTimeFormat.forPattern("yyyyMMdd")
            val sheet = Sheet(
              name = "U" + e._1,
              rows = {
                val headerRow = com.norbitltd.spoiwo.model
                  .Row()
                  .withCellValues("Código", "Material", "Cantidad")
                val elemSet =
                  SQL("""SELECT
                            e.elem_codigo,
                            e.elem_descripcion,
                            SUM(re.even_cantidad_instalado) as cantidad
                            FROM siap.elemento e
                            LEFT JOIN siap.reporte_evento re on re.elem_id = e.elem_id
                            LEFT JOIN siap.reporte r ON r.repo_id = re.repo_id
                            WHERE re.empr_id = {empr_id} and r.rees_id <> 9 and e.ucap_id = {ucap_id} and r.repo_fechasolucion between {fecha_inicial} and {fecha_final}
                            GROUP BY e.elem_codigo, e.elem_descripcion
                            ORDER BY e.elem_codigo ASC
                            """)
                    .on(
                      'empr_id -> empr_id,
                      'ucap_id -> e._1,
                      'fecha_inicial -> fi,
                      'fecha_final -> ff
                    )
                    .as(_ueParser *)
                val rows = elemSet.map {
                  i =>
                    com.norbitltd.spoiwo.model.Row(
                      StringCell(
                        i._1,
                        Some(0),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      StringCell(
                        i._2,
                        Some(1),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      NumericCell(
                        i._3,
                        Some(3),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("#0.00"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      )
                    )
                }
                headerRow :: rows.toList
              }
            )
            _listSheet += sheet
          }
          println("Escribiendo en el Stream")
          var os: ByteArrayOutputStream = new ByteArrayOutputStream()
          Workbook().addSheets(_listSheet).writeToOutputStream(os)
          println("Stream Listo")
          os.toByteArray
      }
    }
  }

  def siap_informe_solicitud_xls(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long
  ): Future[Iterable[SolicitudR]] = {
    val result = db.withConnection { implicit connection =>
      var query: String =
        """SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud s
                                            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
                                            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
                                            WHERE s.empr_id = {empr_id} and s.soli_fecha between {fecha_inicial} and {fecha_final}
                                            and s.soli_estado <> 9 ORDER BY s.soli_id, s.soli_fecha ASC """
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      val reps = SQL(query)
        .on(
          'empr_id -> empr_id,
          'fecha_inicial -> fi.getTime(),
          'fecha_final -> ff.getTime()
        )
        .as(Soli._set *)

      var _listBuffer = new ListBuffer[SolicitudR]()
      reps.map { s =>
        val a = new SolicitudAR(
          s.soli_id,
          s.soti_id,
          s.soti_descripcion,
          s.soli_fecha,
          s.soli_nombre,
          s.soli_radicado,
          s.soli_direccion,
          s.barr_id,
          s.barr_descripcion,
          s.soli_telefono,
          s.soli_email,
          s.soli_solicitud,
          s.soli_respuesta,
          s.soli_informe,
          s.soli_consecutivo
        )
        val b = new SolicitudBR(
          s.soli_fecharespuesta,
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
          s.soli_estado_descripcion,
          s.empr_id,
          s.usua_id
        )
        val solicitud = new SolicitudR(a, b)
        _listBuffer += solicitud
      }
      _listBuffer.toList
    }
    Future.successful(result)
  }

  def siap_informe_solicitud_x_vencer_xls(
      empr_id: Long
  ): Future[Iterable[SolicitudR]] = {
    val result = db.withConnection { implicit connection =>
      var query: String =
        """SELECT *, (CASE WHEN s.soli_estado = 1 THEN 'PENDIENTE' WHEN s.soli_estado = 2 THEN 'EN SUPERVISOR' WHEN s.soli_estado = 3 THEN 'EN VISITA' WHEN s.soli_estado = 4 THEN 'EN CRONOGRAMA' WHEN s.soli_estado = 5 THEN 'EN INFORME' WHEN s.soli_estado = 6 THEN 'RESPONDIDA' END) as soli_estado_descripcion FROM siap.solicitud s
                                            LEFT JOIN siap.barrio b on s.barr_id = b.barr_id
                                            LEFT JOIN siap.solicitud_tipo st ON st.soti_id = s.soti_id
                                            WHERE s.empr_id = {empr_id} and ((s.soli_fechalimite - CURRENT_TIMESTAMP)) <= (4 * '1 day'::interval)
                                            and s.soli_estado < 6 ORDER BY s.soli_fecha DESC """
      val reps = SQL(query)
        .on(
          'empr_id -> empr_id
        )
        .as(Soli._set *)

      var _listBuffer = new ListBuffer[SolicitudR]()
      reps.map { s =>
        val a = new SolicitudAR(
          s.soli_id,
          s.soti_id,
          s.soti_descripcion,
          s.soli_fecha,
          s.soli_nombre,
          s.soli_radicado,
          s.soli_direccion,
          s.barr_id,
          s.barr_descripcion,
          s.soli_telefono,
          s.soli_email,
          s.soli_solicitud,
          s.soli_respuesta,
          s.soli_informe,
          s.soli_consecutivo
        )
        val b = new SolicitudBR(
          s.soli_fecharespuesta,
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
          s.soli_estado_descripcion,
          s.empr_id,
          s.usua_id
        )
        val solicitud = new SolicitudR(a, b)
        _listBuffer += solicitud
      }
      _listBuffer.toList
    }
    Future.successful(result)
  }

  /*
   * @desc Informe de Orden de Trabajo Municipio
   * @param fecha_inicial: Long
   * @param fecha_final: Long
   * @param empr_id: Long
   */
  def siap_informe_muot_xls(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long
  ): Array[Byte] = {
    val result = db.withConnection { implicit connection =>
        val empresa = empresaService.buscarPorId(empr_id)
        empresa match {
          case Some(empresa) =>
            val format = new SimpleDateFormat("yyyy-MM-dd HH:mm")
            var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
            var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
            var _listMerged = new ListBuffer[CellRange]()
            var mergedColumns = {
              _listMerged += CellRange((0, 0), (0, 3))
              _listMerged += CellRange((1, 1), (0, 3))
              _listMerged += CellRange((2, 2), (0, 3))
              _listMerged.toList
            }
  
          var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.setTimeInMillis(fecha_inicial)
      ff.setTimeInMillis(fecha_final)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)
      val motParser = int("muot_id") ~ date("fecha") map { case a ~ b => (a, b) }
      val muots = SQL(
        """SELECT o.muot_id, MIN(o.obra_fecharecepcion) AS fecha FROM siap.obra o
            WHERE o.obra_fecharecepcion BETWEEN {fecha_inicial} AND {fecha_final} AND o.empr_id = {empr_id}
            AND o.rees_id = 3
            GROUP BY o.muot_id
            ORDER BY fecha"""
      ).on(
          'fecha_inicial -> fi.getTime(),
          'fecha_final -> ff.getTime(),
          'empr_id -> empr_id
        )
        .as(motParser *)

      // Primera Hoja, relación de OTs
      val sheet1 = Sheet(
        name = "MUOTs",
        rows = {
          val title1Row = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(empresa.empr_descripcion)
          val title2Row = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("INFORME DE ORDEN DE TRABAJO MUNICIPIO")
          val title3Row = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "GENERADO EL " + format.format(Calendar.getInstance().getTime())
            )
          val headerRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("OT NUMERO", "FECHA")
          var j = 2
          val rows = muots.map {
            ot =>
              j += 1
              val link = new HyperLinkUrl(ot._1.toString, "#OT" + ot._1)
              com.norbitltd.spoiwo.model.Row(
                HyperLinkUrlCell(
                  link,
                  Some(0),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                DateCell(
                  ot._2,
                  Some(1),
                  style = Some(CellStyle(dataFormat = CellDataFormat("YYYY-MM-DD"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
          }
          title1Row :: title2Row :: title3Row :: headerRow :: rows.toList
        },
        mergedRegions = mergedColumns
      )
      var _listSheet = new ListBuffer[Sheet]()
      _listSheet += sheet1
      val _rParser = str("tipo") ~ int("consecutivo") ~ date("fecharecepcion") ~ date("fechasolucion") ~ str("descripcion") ~ str("direccion") ~ str("barrio") map { case a ~ b ~ c ~ d ~ e ~ f ~ g => (a, b, c, d , e, f, g) }

      // Recorrer muots y buscar reportes

      muots.map { ot =>
        var j = 3
 
        val fmt = DateTimeFormat.forPattern("yyyy/MM/dd")
        val fmdt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss")
        println("Creando Hoja")
        val sheet = Sheet(
            name = "OT" + ot._1,
            rows = {
                val header0Row = com.norbitltd.spoiwo.model.Row().withCellValues("REPORTES")
                val header1Row = com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Tipo Reporte", "Consecutivo", "Fecha Recepción", "Fecha Solución", "Descripción", "Dirección", "Barrio")
                val reportes = SQL("""SELECT 'EXPANSION' as tipo,
                  r.repo_consecutivo as consecutivo, 
                  r.repo_fecharecepcion as fecharecepcion, 
                  r.repo_fechasolucion as fechasolucion, 
                  concat(r.repo_descripcion) as descripcion,
                  r.repo_direccion as direccion,
                  b.barr_descripcion as barrio
                  FROM siap.reporte r
                  LEFT JOIN siap.reporte_adicional ra ON ra.repo_id = r.repo_id
                  LEFT JOIN siap.barrio b on b.barr_id = r.barr_id
                  WHERE r.empr_id = {empr_id} and ra.muot_id = {muot_id}
                  ORDER BY fecharecepcion""").on(
                    'empr_id -> empr_id,
                    'muot_id -> ot._1
                    ).as(_rParser *)                
                val rows = reportes.map { r =>
                    println("Agregando reporte a la OT")
                    val row = com.norbitltd.spoiwo.model.Row(
                      StringCell(
                        r._1,
                        Some(0),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      NumericCell(
                        r._2,
                        Some(1),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      DateCell(
                        r._3,
                        Some(2),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("YYYY/MM/DD"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      DateCell(
                        r._4,
                        Some(3),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("YYYY/MM/DD"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      StringCell(
                        r._5,
                        Some(4),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      StringCell(
                        r._6,
                        Some(5),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      ),
                      StringCell(
                        r._7,
                        Some(6),
                        style =
                          Some(CellStyle(dataFormat = CellDataFormat("@"))),
                        CellStyleInheritance.CellThenRowThenColumnThenSheet
                      )
                    )
                    row
                }
                header0Row :: header1Row :: rows.toList
            }
        )
        //_listSheet += sheet
      }
      // 
      println("Escribiendo en el Stream")
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      Workbook().addSheets(_listSheet).writeToOutputStream(os)
      println("Stream Listo")
      os.toByteArray
    case None =>
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      os.toByteArray
    }
    }
    result
  }

}
