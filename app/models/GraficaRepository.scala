package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{ OutputStream, ByteArrayOutputStream, FileInputStream }
import java.util.{ Date }
import java.lang.Long
import java.sql.Date
import java.text.SimpleDateFormat

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
//

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str, int, scalar }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }
import scala.collection.mutable.{ Map, HashMap }
import scala.collection.mutable.{ ListBuffer }

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

// Excel Export
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellStyleInheritance
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.norbitltd.spoiwo.model.enums.{CellBorderStyle, CellFill, Pane, CellHorizontalAlignment => HA, CellVerticalAlignment => VA}

case class Siap_grafica_reporte_pendiente(reti_id: Option[scala.Long], reti_descripcion: Option[String], pendiente: Option[Int])
case class Siap_grafica_reporte(label: Option[String], value: Option[scala.Long])
case class Siap_grafica_reporte_3(label1: Option[String], label2: Option[String], value: Option[scala.Long])
case class Siap_grafica_reporte_map(label1: Option[String], values: Option[Map[String, List[Int]]])
case class Siap_lista(aap_id: Option[scala.Long], aap_apoyo: Option[String], aap_direccion: Option[String], barr_descripcion: Option[String], vereda: Option[String], tiba_descripcion: Option[String], aaco_descripcion: Option[String], aap_tecnologia: Option[String], aap_potencia: Option[Int], aaus_descripcion: Option[String])

object Siap_grafica_reporte_pendiente {
  implicit val sdWrites = new Writes[Siap_grafica_reporte_pendiente] {
      def writes(sd: Siap_grafica_reporte_pendiente) = Json.obj(
          "reti_id" -> sd.reti_id,
          "reti_descripcion" -> sd.reti_descripcion,
          "pendiente" -> sd.pendiente
      )
  }

  val _set = {
      get[Option[scala.Long]]("reti_id") ~
      get[Option[String]]("reti_descripcion") ~
      get[Option[Int]]("pendiente") map {
        case
           reti_id ~
           reti_descripcion ~
           pendiente => new Siap_grafica_reporte_pendiente(reti_id, reti_descripcion, pendiente)
      }
  }
}

object Siap_grafica_reporte {
  implicit val sdWrites = new Writes[Siap_grafica_reporte] {
      def writes(sd: Siap_grafica_reporte) = Json.obj(
          "label" -> sd.label,
          "value" -> sd.value
      )
  }

  val _set = {
      get[Option[String]]("label") ~
      get[Option[scala.Long]]("value") map {
        case
           label ~
           value => new Siap_grafica_reporte(label, value)
      }
  }
}

object Siap_grafica_reporte_3 {
  implicit val sdWrites = new Writes[Siap_grafica_reporte_3] {
      def writes(sd: Siap_grafica_reporte_3) = Json.obj(
          "label1" -> sd.label1,
          "label2" -> sd.label2,
          "value" -> sd.value
      )
  }

  val _set = {
      get[Option[String]]("label1") ~
      get[Option[String]]("label2") ~
      get[Option[scala.Long]]("value") map {
        case
           label1 ~
           label2 ~
           value => new Siap_grafica_reporte_3(label1, label2, value)
      }
  }
}

object Siap_lista {
  implicit val sdWrites = new Writes[Siap_lista] {
      def writes(sd: Siap_lista) = Json.obj(
        "aap_id" -> sd.aap_id, 
        "aap_apoyo" -> sd.aap_apoyo, 
        "aap_direccion" -> sd.aap_direccion, 
        "barr_descripcion" -> sd.barr_descripcion, 
        "vereda" -> sd.vereda, 
        "tiba_descripcion" -> sd.tiba_descripcion, 
        "aaco_descripcion" -> sd.aaco_descripcion, 
        "aap_tecnologia" -> sd.aap_tecnologia, 
        "aap_potencia" -> sd.aap_potencia, 
        "aaus_descripcion" -> sd.aaus_descripcion
      )
  }

  val _set = {
      get[Option[scala.Long]]("aap_id") ~
      get[Option[String]]("aap_apoyo") ~
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[String]]("vereda") ~
      get[Option[String]]("tiba_descripcion") ~
      get[Option[String]]("aaco_descripcion") ~
      get[Option[String]]("aap_tecnologia") ~
      get[Option[Int]]("aap_potencia") ~
      get[Option[String]]("aaus_descripcion") map {
        case
         aap_id ~ 
         aap_apoyo ~ 
         aap_direccion ~ 
         barr_descripcion ~ 
         vereda ~ 
         tiba_descripcion ~ 
         aaco_descripcion ~ 
         aap_tecnologia ~ 
         aap_potencia ~ 
         aaus_descripcion => new Siap_lista(         
           aap_id,
           aap_apoyo,
           aap_direccion,
           barr_descripcion,
           vereda,
           tiba_descripcion,
           aaco_descripcion,
           aap_tecnologia,
           aap_potencia,
           aaus_descripcion)
      }
  }
}

class GraficaRepository @Inject()(dbapi: DBApi, usuarioService: UsuarioRepository, empresaService: EmpresaRepository, municipioService: MunicipioRepository)(implicit ec:DatabaseExecutionContext) {
  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

  def siap_lista_potencias(empr_id:scala.Long) : Future[Iterable[scala.Long]] = Future[Iterable[scala.Long]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT aap_potencia FROM siap.aap_adicional ad
      INNER JOIN siap.aap a ON a.aap_id = ad.aap_id 
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id}
      ORDER BY aap_potencia ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[scala.Long] *)
    }
  }

  def siap_lista_tecnologias(empr_id:scala.Long) : Future[Iterable[String]] = Future[Iterable[String]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT aap_tecnologia FROM siap.aap_adicional ad
      INNER JOIN siap.aap a ON a.aap_id = ad.aap_id 
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id}
      ORDER BY aap_tecnologia ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[String] *)
    }
  }

  def siap_lista_medidas(empr_id:scala.Long) : Future[Iterable[String]] = Future[Iterable[String]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT c.aaco_descripcion FROM siap.aap a
      INNER JOIN siap.aap_conexion c ON c.aaco_id = a.aaco_id 
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id}
      ORDER BY c.aaco_descripcion ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[String] *)
    }
  }  

  def siap_lista_sectores(empr_id:scala.Long) : Future[Iterable[String]] = Future[Iterable[String]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT tb.tiba_descripcion FROM siap.aap a
      INNER JOIN siap.barrio b ON b.barr_id = a.barr_id
      INNER JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id 
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id}
      ORDER BY tb.tiba_descripcion ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[String] *)
    }
  }

  def siap_lista_usos(empr_id:scala.Long) : Future[Iterable[String]] = Future[Iterable[String]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT u.aaus_descripcion FROM siap.aap a
      INNER JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id}
      ORDER BY u.aaus_descripcion ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[String] *)
    }
  }

  def siap_lista_barrios(empr_id:scala.Long) : Future[Iterable[String]] = Future[Iterable[String]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT b.barr_descripcion FROM siap.aap a
      INNER JOIN siap.barrio b ON b.barr_id = a.barr_id
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id} and b.tiba_id = 1
      ORDER BY b.barr_descripcion ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[String] *)
    }
  }

  def siap_lista_veredas(empr_id:scala.Long) : Future[Iterable[String]] = Future[Iterable[String]] {
    db.withConnection { implicit connection =>
      SQL("""SELECT DISTINCT b.barr_descripcion FROM siap.aap a
      INNER JOIN siap.barrio b ON b.barr_id = a.barr_id
      WHERE a.aap_id <> 9999999 and a.esta_id <> 9 and a.empr_id = {empr_id} and b.tiba_id = 2
      ORDER BY b.barr_descripcion ASC""").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[String] *)
    }
  }
  
  /**
   * siap_grafica_reporte_pendiente
   * @return List[Siap_grafica_reporte_pendiente]
  */
  def siap_grafica_reporte_pendiente(empr_id: scala.Long): Future[Iterable[Siap_grafica_reporte_pendiente]] = Future[Iterable[Siap_grafica_reporte_pendiente]]  {
    db.withConnection { implicit connection =>

      SQL("""select rt.reti_id, rt.reti_descripcion, count(*) as pendiente from siap.reporte r
      inner join siap.reporte_tipo rt on rt.reti_id = r.reti_id
      where r.rees_id = 1 and r.empr_id = {empr_id}
      group by rt.reti_id, rt.reti_descripcion
      order by rt.reti_id""").on('empr_id -> empr_id).as(Siap_grafica_reporte_pendiente._set *)
    }
  }

  def siap_grafica_reporte_vencido(empr_id: scala.Long): Future[Iterable[Siap_grafica_reporte_pendiente]] = Future[Iterable[Siap_grafica_reporte_pendiente]]  {
    db.withConnection { implicit connection =>

      SQL("""select o.reti_id, o.reti_descripcion, count(o.repo_consecutivo) as pendiente from (
        select rt.reti_id, rt.reti_descripcion, r.repo_consecutivo,  ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval) as fecha_limite, (current_timestamp - ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval)) as horas from siap.reporte r
        inner join siap.reporte_tipo rt on rt.reti_id = r.reti_id
        where r.rees_id = 1 and r.empr_id = {empr_id}
        ) o
        where o.horas > 1 * '1s' :: interval
        group by o.reti_id, o.reti_descripcion
        order by o.reti_id""").on('empr_id -> empr_id).as(Siap_grafica_reporte_pendiente._set *)
    }
  }  

  def siap_grafica_reporte_potencia(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT ad.aap_potencia::text as label, COUNT(*) as value FROM siap.aap a
      INNER JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id}
      GROUP BY ad.aap_potencia
      ORDER BY ad.aap_potencia""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }

  def siap_lista_reporte_potencia(aap_potencia: scala.Long, empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and ad.aap_potencia = {aap_potencia}
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'aap_potencia -> aap_potencia
      ).as(Siap_lista._set *)
      result
    }
  }

  def siap_grafica_reporte_potencia_tecnologia(empr_id:scala.Long):  Future[HashMap[String, ListBuffer[scala.Long]]] = Future[HashMap[String, ListBuffer[scala.Long]]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""select aap_potencia as label1, aap_tecnologia as label2, cantidad as value from siap.fn_potencia_tecnologia({empr_id}::integer)
      order by aap_tecnologia, aap_potencia::integer asc""").on('empr_id -> empr_id).as(Siap_grafica_reporte_3._set *)
      var mymap = new HashMap[String, HashMap[String, ListBuffer[scala.Long]]]()
      var m = new HashMap[String, ListBuffer[scala.Long]]()
      var labelant = ""
      result.map { r => 
        m.get(r.label2.get) match {
          case Some(xs:ListBuffer[scala.Long]) => m.update(r.label2.get, xs :+ r.value.get)
          case None => m += (r.label2.get -> new ListBuffer[scala.Long]())
                       m.get(r.label2.get) match {
                        case Some(xs:ListBuffer[scala.Long]) => m.update(r.label2.get, xs :+ r.value.get)
                        case None => m 
                       }  
        }
      }
      m
    }
  }

  def siap_lista_reporte_potencia_tecnologia(aap_tecnologia: String, aap_potencia: scala.Long,  empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and ad.aap_potencia = {aap_potencia} and ad.aap_tecnologia = {aap_tecnologia}
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'aap_potencia -> aap_potencia,
        'aap_tecnologia -> aap_tecnologia
      ).as(Siap_lista._set *)
      result
    }
  }

  def siap_grafica_reporte_potencia_medida(empr_id:scala.Long):  Future[HashMap[String, ListBuffer[scala.Long]]] = Future[HashMap[String, ListBuffer[scala.Long]]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""select aap_potencia as label1, aap_medida as label2, cantidad as value from siap.fn_potencia_medida({empr_id}::integer)
      order by aap_medida, aap_potencia::integer asc""").on('empr_id -> empr_id).as(Siap_grafica_reporte_3._set *)
      var mymap = new HashMap[String, HashMap[String, ListBuffer[scala.Long]]]()
      var m = new HashMap[String, ListBuffer[scala.Long]]()
      var labelant = ""
      result.map { r => 
        m.get(r.label2.get) match {
          case Some(xs:ListBuffer[scala.Long]) => m.update(r.label2.get, xs :+ r.value.get)
          case None => m += (r.label2.get -> new ListBuffer[scala.Long]())
                       m.get(r.label2.get) match {
                        case Some(xs:ListBuffer[scala.Long]) => m.update(r.label2.get, xs :+ r.value.get)
                        case None => m 

                       }  
        }
      }
      m
    }
  }

  def siap_lista_reporte_potencia_medida(aaco_descripcion: String, aap_potencia: scala.Long,  empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and ad.aap_potencia = {aap_potencia} and m.aaco_descripcion = {aaco_descripcion}
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'aap_potencia -> aap_potencia,
        'aaco_descripcion -> aaco_descripcion
      ).as(Siap_lista._set *)
      result
    }
  }  

  def siap_grafica_reporte_sector(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT tb.tiba_descripcion::text as label, COUNT(*) as value FROM siap.aap a
      LEFT JOIN siap.barrio b ON b.barr_id = a.barr_id
	  LEFT JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id}
      GROUP BY tb.tiba_descripcion
      ORDER BY tb.tiba_descripcion""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }

  def siap_lista_reporte_sector(tiba_descripcion: String, empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and t.tiba_descripcion = {tiba_descripcion}
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'tiba_descripcion -> tiba_descripcion
      ).as(Siap_lista._set *)
      result
    }
  }  

  def siap_grafica_reporte_uso(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT us.aaus_descripcion::text as label, COUNT(*) as value FROM siap.aap a
      LEFT JOIN siap.aap_uso us ON us.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id}
      GROUP BY us.aaus_descripcion::text
      ORDER BY us.aaus_descripcion::text""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }  

  def siap_lista_reporte_uso(aaus_descripcion: String, empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and u.aaus_descripcion = {aaus_descripcion}
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'aaus_descripcion -> aaus_descripcion
      ).as(Siap_lista._set *)
      result
    }
  }

  def siap_grafica_reporte_tecnologia(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT ad.aap_tecnologia::text as label, COUNT(*) as value FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id}
      GROUP BY ad.aap_tecnologia::text
      ORDER BY ad.aap_tecnologia::text""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }
  
  def siap_lista_reporte_tecnologia(aap_tecnologia: String, empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and ad.aap_tecnologia = {aap_tecnologia}
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'aap_tecnologia -> aap_tecnologia
      ).as(Siap_lista._set *)
      result
    }
  }

  def siap_grafica_reporte_medida(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT co.aaco_descripcion::text as label, COUNT(*) as value FROM siap.aap a
      LEFT JOIN siap.aap_conexion co ON co.aaco_id = a.aaco_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id}
      GROUP BY co.aaco_descripcion::text
      ORDER BY co.aaco_descripcion::text""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }  

  def siap_lista_reporte_medida(aaco_descripcion: String, empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and TRIM(BOTH FROM m.aaco_descripcion) = TRIM(BOTH FROM {aaco_descripcion})
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'aaco_descripcion -> aaco_descripcion
      ).as(Siap_lista._set *)
      result
    }
  }

  def siap_grafica_reporte_barrio(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT b.barr_descripcion::text as label, COUNT(*) as value FROM siap.aap a
      LEFT JOIN siap.barrio b ON b.barr_id = a.barr_id
	    LEFT JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and tb.tiba_id = 1
      GROUP BY b.barr_descripcion::text
      ORDER BY b.barr_descripcion::text""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }

  def siap_lista_reporte_barrio(barr_descripcion: String, empr_id:scala.Long):  Future[Iterable[Siap_lista]] = Future[Iterable[Siap_lista]]  {
    db.withConnection { implicit connection =>
      val result = SQL("""SELECT a.aap_id, a.aap_apoyo, a.aap_direccion, b.barr_descripcion, case when b.tiba_id = 2 then 'X' else '' end as vereda ,t.tiba_descripcion, m.aaco_descripcion, ad.aap_tecnologia, ad.aap_potencia, u.aaus_descripcion FROM siap.aap a
      LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
      LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
      LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
      LEFT JOIN siap.aap_conexion m ON m.aaco_id = a.aaco_id
      LEFT JOIN siap.aap_uso u ON u.aaus_id = a.aaus_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and TRIM(BOTH FROM b.barr_descripcion) = TRIM(BOTH FROM {barr_descripcion})
      ORDER BY a.aap_id""").
      on(
        'empr_id -> empr_id, 
        'barr_descripcion -> barr_descripcion
      ).as(Siap_lista._set *)
      result
    }
  }

  def siap_grafica_reporte_vereda(empr_id:scala.Long):  Future[Iterable[Siap_grafica_reporte]] = Future[Iterable[Siap_grafica_reporte]]  {
    db.withConnection { implicit connection =>
      SQL("""SELECT b.barr_descripcion::text as label, COUNT(*) as value FROM siap.aap a
      LEFT JOIN siap.barrio b ON b.barr_id = a.barr_id
	    LEFT JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id
      WHERE a.esta_id <> 9 and a.aap_id <> 9999999 and a.empr_id = {empr_id} and tb.tiba_id = 2
      GROUP BY b.barr_descripcion::text
      ORDER BY b.barr_descripcion::text""").on('empr_id -> empr_id).as(Siap_grafica_reporte._set *)
    }
  }    
}