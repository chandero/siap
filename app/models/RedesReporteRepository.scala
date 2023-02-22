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
import anorm.SqlParser.{get, str, scalar}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.DateTimeZone

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

class RedesReporteRepository @Inject()(
    dbapi: DBApi,
    eventoService: EventoRepository,
    elementoService: ElementoRepository,
    empresaService: EmpresaRepository,
    usuarioService: UsuarioRepository,
    aapService: RedesRepository
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
          """SELECT r.repo_consecutivo FROM siap.redes_reporte r 
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
        """SELECT COUNT(*) AS conteo FROM siap.redes_reporte_evento r
                           INNER JOIN siap.redes_reporte s ON s.repo_id = r.repo_id
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
        """SELECT COUNT(*) AS conteo FROM siap.redes_reporte_evento r
                           INNER JOIN siap.redes_reporte s ON s.repo_id = r.repo_id
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
        """SELECT COUNT(*) AS conteo FROM siap.redes_reporte_evento r
                           INNER JOIN siap.redes_reporte s ON s.repo_id = r.repo_id            
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
        """SELECT COUNT(*) AS conteo FROM siap.redes_reporte_evento r
                           INNER JOIN siap.redes_reporte s ON s.repo_id = r.repo_id            
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
        "SELECT COUNT(*) AS c FROM siap.redes_reporte WHERE empr_id = {empr_id} and rees_id < 9"
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
                                          FROM siap.redes_reporte r 
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
        reps.map { r =>
          val adicional = SQL(
            """SELECT * FROM siap.redes_reporte_adicional WHERE repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val eventos = SQL(
            """SELECT * FROM siap.redes_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.redes_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val novedades = SQL("""SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id}""").
          on(
            'repo_id -> r.repo_id
          ).as(ReporteNovedad._set *)
          val direcciones = SQL(
            """SELECT * FROM siap.redes_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.redes_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
              """SELECT * FROM siap.redes_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
            val direccion = d.copy(dato = dat, dato_adicional = adi)
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
  def reportes(empr_id: scala.Long): Future[Iterable[Reporte]] =
    Future[Iterable[Reporte]] {
      db.withConnection { implicit connection =>
        var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
        val reps = SQL("""SELECT * FROM siap.redes_reporte r 
                                          WHERE r.empr_id = {empr_id} 
                                          and r.rees_id < 9 ORDER BY r.repo_id""")
          .on(
            'empr_id -> empr_id
          )
          .as(Reporte._set *)
        reps.map { r =>
          val eventos = SQL(
            """SELECT * FROM siap.redes_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.redes_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val adicional = SQL(
            """SELECT * FROM siap.redes_reporte_adicional WHERE repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val novedades = SQL("""SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id}""").
          on(
            'repo_id -> r.repo_id
          ).as(ReporteNovedad._set *)            
          val direcciones = SQL(
            """SELECT * FROM siap.redes_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.redes_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
            val direccion = d.copy(dato = dat)
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
         o.orig_descripcion, r.repo_nombre, r.repo_telefono, r.repo_direccion, b.barr_descripcion, r.repo_descripcion, r.rees_id from siap.redes_reporte r
        inner join siap.redes_reporte_tipo rt on rt.reti_id = r.reti_id
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
        """SELECT * FROM siap.redes_reporte r
                            INNER JOIN siap.redes_reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.redes_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.redes_reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id}"""
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
      val r = SQL("""SELECT * FROM siap.redes_reporte r
                            INNER JOIN siap.reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.redes_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on a.acti_id = ra.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.repo_id = {repo_id}""")
        .on(
          'repo_id -> repo_id
        )
        .as(Reporte._set.singleOpt)

      val eventos = SQL(
        """SELECT * FROM siap.redes_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
      ).on(
          'repo_id -> repo_id
        )
        .as(Evento.eventoSet *)
      val meams = SQL(
        """SELECT m.meam_id FROM siap.redes_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
      ).on(
          'repo_id -> repo_id
        )
        .as(scalar[scala.Long].*)
      val adicional = SQL(
        """SELECT * FROM siap.redes_reporte_adicional WHERE repo_id = {repo_id}"""
      ).on(
          'repo_id -> repo_id
        )
        .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
      val novedades = SQL("""SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id}""").
          on(
            'repo_id -> repo_id
          ).as(ReporteNovedad._set *)        
      val direcciones = SQL(
        """SELECT * FROM siap.redes_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
      ).on(
          'repo_id -> repo_id
        )
        .as(ReporteDireccion.reporteDireccionSet *)
      var _listDireccion = new ListBuffer[ReporteDireccion]()
      direcciones.map { d =>
        var dat = SQL(
          """SELECT * FROM siap.redes_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
          """SELECT * FROM siap.redes_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
        val direccion = d.copy(dato = dat, dato_adicional = adi)
        _listDireccion += direccion
      }
      println("R: "+ r)
      r.map { r =>
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
        reporte
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
        """SELECT * FROM siap.redes_reporte r
                            INNER JOIN siap.redes_reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.redes_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.redes_reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.reti_id = {reti_id} and r.repo_consecutivo = {repo_consecutivo} and r.empr_id = {empr_id} and r.rees_id < 9"""
      ).on(
          'reti_id -> reti_id,
          'repo_consecutivo -> repo_consecutivo,
          'empr_id -> empr_id
        )
        .as(Reporte._set.singleOpt)

      r match {
        case Some(r) =>
          val eventos = SQL(
            """SELECT * FROM siap.redes_reporte_evento WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(Evento.eventoSet *)
          val meams = SQL(
            """SELECT m.meam_id FROM siap.redes_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(scalar[scala.Long].*)
          val adicional = SQL(
            """SELECT * FROM siap.redes_reporte_adicional WHERE repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
          val novedades = SQL("""SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id}""").
          on(
            'repo_id -> r.repo_id
          ).as(ReporteNovedad._set *)             
          val direcciones = SQL(
            """SELECT * FROM siap.redes_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8 ORDER BY even_id ASC"""
          ).on(
              'repo_id -> r.repo_id
            )
            .as(ReporteDireccion.reporteDireccionSet *)
          var _listDireccion = new ListBuffer[ReporteDireccion]()
          direcciones.map { d =>
            var dat = SQL(
              """SELECT * FROM siap.redes_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
              """SELECT * FROM siap.redes_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
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
            val direccion = d.copy(dato = dat, dato_adicional = adi)
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
  def buscarPorConsecutivoConsulta(
      reti_id: scala.Long,
      repo_consecutivo: scala.Long,
      empr_id: scala.Long
  ): Option[ReporteConsulta] = {
    db.withConnection { implicit connection =>
      val r = SQL(
        """SELECT * FROM siap.redes_reporte r
                            INNER JOIN siap.redes_reporte_tipo t on r.reti_id = t.reti_id
                            LEFT JOIN siap.redes_reporte_adicional ra on r.repo_id = ra.repo_id
                            LEFT JOIN siap.actividad a on ra.acti_id = a.acti_id
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.redes_reporte_estado e on r.rees_id = e.rees_id
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
            """SELECT * FROM siap.redes_reporte_adicional WHERE repo_id = {repo_id}"""
          ).on(
              'repo_id -> r.repo_id
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
      empr_id: scala.Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    db.withConnection { implicit connection =>
      var _list: ListBuffer[Reporte] = new ListBuffer[Reporte]()
      var query: String =
        """SELECT *
                                          FROM siap.redes_reporte r 
                                          LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                                          WHERE r.empr_id = {empr_id} and r.repo_fecharecepcion between {fecha_inicial} and {fecha_final}
                                          and r.rees_id < 9 ORDER BY r.rees_id, r.repo_fecharecepcion DESC """
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
          'fecha_final -> fechafin
        )
        .as(Reporte._set *)
      reps.map { r =>
        val adicional = SQL(
          """SELECT * FROM siap.redes_reporte_adicional WHERE repo_id = {repo_id}"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(ReporteAdicional.reporteAdicionalSet.singleOpt)
        val eventos = SQL(
          """SELECT * FROM siap.redes_reporte_evento WHERE repo_id = {repo_id} """
        ).on(
            'repo_id -> r.repo_id
          )
          .as(Evento.eventoSet *)
        val meams = SQL(
          """SELECT m.meam_id FROM siap.redes_reporte_medioambiente m WHERE m.repo_id = {repo_id}"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(scalar[scala.Long].*)
        val novedades = SQL("""SELECT * FROM siap.reporte_novedad rn WHERE rn.repo_id = {repo_id}""").
          on(
            'repo_id -> r.repo_id
          ).as(ReporteNovedad._set *)           
        val direcciones = SQL(
          """SELECT * FROM siap.redes_reporte_direccion WHERE repo_id = {repo_id} and even_estado < 8"""
        ).on(
            'repo_id -> r.repo_id
          )
          .as(ReporteDireccion.reporteDireccionSet *)
        var _listDireccion = new ListBuffer[ReporteDireccion]()
        direcciones.map { d =>
          val dat = SQL(
            """SELECT * FROM siap.redes_reporte_direccion_dato WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDato.reporteDireccionDatoSet.singleOpt)
          val adi = SQL(
            """SELECT * FROM siap.redes_reporte_direccion_dato_adicional WHERE repo_id = {repo_id} and aap_id = {aap_id} and even_id = {even_id}"""
          ).on(
              'repo_id -> d.repo_id,
              'aap_id -> d.aap_id,
              'even_id -> d.even_id
            )
            .as(ReporteDireccionDatoAdicional._set.singleOpt)
          val direccion = d.copy(dato = dat, dato_adicional = adi)
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
          None,
          None //Some(_listDireccion.toList)
        )
        _list += reporte
      }
      _list
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
        "SELECT * FROM siap.redes_reporte WHERE repo_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and empr_id = {empr_id}"
      ).on(
          'fecha_inicial -> fecha_inicial,
          'fecha_final -> fecha_final,
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
        "SELECT * FROM siap.redes_reporte WHERE rees_id = {rees_id} and empr_id = {empr_id}"
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
        "SELECT * FROM siap.redes_reporte WHERE acti_id = {acti_id} and empr_id = {empr_id}"
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
        "SELECT * FROM siap.redes_reporte WHERE orig_id = {orig_id} and empr_id = {empr_id}"
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
      SQL("SELECT * FROM siap.redes_reporte")
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
            "INSERT INTO siap.redes_reporte (repo_fecharecepcion, repo_direccion, repo_nombre, repo_telefono, repo_fechasolucion, repo_horainicio, repo_horafin, repo_reportetecnico, repo_descripcion, rees_id, orig_id, barr_id, empr_id, tiba_id, usua_id, reti_id, repo_consecutivo) VALUES ({repo_fecharecepcion}, {repo_direccion}, {repo_nombre}, {repo_telefono}, {repo_fechasolucion}, {repo_horainicio}, {repo_horafin}, {repo_reportetecnico}, {repo_descripcion}, {rees_id}, {orig_id}, {barr_id}, {empr_id}, {tiba_id}, {usua_id}, {reti_id}, {repo_consecutivo})"
          ).on(
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
            SQL("""INSERT INTO siap.redes_reporte_adicional (repo_id, 
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
    * convertir
    * 
    * @param reporte
    * @return Boolean
    */
  def convertir(id: scala.Long): scala.Long = {
    db.withConnection { implicit connection =>
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      var _id : scala.Long = 0
      val redes = SQL("SELECT * FROM siap.redes_reporte WHERE repo_id = {repo_id}").on('repo_id -> id).as(Reporte._set.single)
      // Validar si previamente fue convertido
      val reporte: Option[Reporte] = SQL("SELECT * FROM siap.reporte WHERE repo_consecutivo = {repo_consecutivo} and reti_id = {reti_id}")
                    .on(
                      'repo_consecutivo -> redes.repo_consecutivo,
                      'reti_id -> redes.reti_id
                    ).as(Reporte._set.singleOpt)
      reporte match {
        case None =>
                val queryReporte = """INSERT INTO siap.reporte (
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
                      barr_id_anterior) 
                      SELECT 
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
                      barr_id_anterior
                      FROM siap.redes_reporte r 
                      WHERE r.repo_consecutivo = {repo_consecutivo} and r.reti_id = {reti_id}"""
                val queryAdicional = """SELECT
                               repo_id,
                               repo_fechadigitacion,
                               repo_tipo_expansion,
                               repo_luminaria,
                               repo_redes,
                               repo_redes,
                               repo_modificado,
                               repo_subreporte,
                               acti_id,
                               repo_subid,
                               repo_email,
                               repo_codigo,
                               repo_apoyo,
                               urba_id,
                               muot_id,
                               aaco_id_anterior,
                               aaco_id_nuevo,
                               medi_id,
                               tran_id,
                               medi_acta
                              FROM siap.redes_reporte_adicional a WHERE a.repo_id = {repo_id}
                              """

            _id = SQL(queryReporte)
            .on(
                'repo_consecutivo -> redes.repo_consecutivo,
                'reti_id -> redes.reti_id
            )
            .executeInsert().get

            val resultSet = SQL(queryAdicional).on('repo_id -> redes.repo_id).as(ReporteAdicional.reporteAdicionalSet *)
            resultSet.map { r =>
                SQL("""INSERT INTO siap.reporte_adicional VALUES(
                               {repo_id},
                               {repo_fechadigitacion},
                               {repo_tipo_expansion},
                               {repo_luminaria},
                               {repo_redes},
                               {repo_redes},
                               {repo_modificado},
                               {repo_subreporte},
                               {acti_id},
                               {repo_subid},
                               {repo_email},
                               {repo_codigo},
                               {repo_apoyo},
                               {urba_id},
                               {muot_id},
                               {aaco_id_anterior},
                               {aaco_id_nuevo},
                               {medi_id},
                               {tran_id},
                               {medi_acta}
                )""").on(
                      'repo_id -> _id,
                      'repo_fechadigitacion -> r.repo_fechadigitacion,
                      'repo_tipo_expansion -> r.repo_tipo_expansion,
                      'repo_luminaria -> r.repo_luminaria,
                      'repo_redes -> r.repo_redes,
                      'repo_redes -> r.repo_redes,
                      'repo_modificado -> hora,
                      'repo_subreporte -> r.repo_subreporte,
                      'acti_id -> r.acti_id,
                      'repo_subid -> r.repo_subid,
                      'repo_email -> r.repo_email,
                      'repo_codigo -> r.repo_codigo,
                      'repo_apoyo -> r.repo_apoyo,
                      'urba_id -> r.urba_id,
                      'muot_id -> r.muot_id,
                      'aaco_id_anterior -> r.aaco_id_anterior,
                      'aaco_id_nuevo -> r.aaco_id_nuevo,
                      'medi_id -> r.medi_id,
                      'tran_id -> r.tran_id,
                      'medi_acta -> r.medi_acta
                ).executeInsert()
              }


      case Some(r) => 
              _id = r.repo_id.get
              SQL("""UPDATE siap.reporte SET rees_id = 1 WHERE repo_id = {repo_id}""")
              .on(
                'repo_id -> r.repo_id
              ).executeUpdate()
      }
                // Eliminar de Reporte y Reporte Adicional
      SQL("""UPDATE siap.redes_reporte SET rees_id = 10 WHERE repo_id = {repo_id}""")
        .on(
         'repo_id -> redes.repo_id
      ).executeUpdate()
      _id
    }
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
        "UPDATE siap.redes_reporte SET repo_direccion = {repo_direccion}, repo_nombre = {repo_nombre}, repo_telefono = {repo_telefono}, repo_fechasolucion = {repo_fechasolucion}, repo_horainicio = {repo_horainicio}, repo_horafin = {repo_horafin}, repo_reportetecnico = {repo_reportetecnico}, repo_descripcion = {repo_descripcion}, rees_id = {rees_id}, orig_id = {orig_id}, barr_id = {barr_id}, empr_id = {empr_id}, tiba_id = {tiba_id}, usua_id = {usua_id} WHERE repo_id = {repo_id}"
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
          """UPDATE siap.redes_reporte_adicional SET repo_fechadigitacion = {repo_fechadigitacion}, 
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
          SQL("""INSERT INTO siap.redes_reporte_adicional (repo_id, 
                                                               repo_fechadigitacion, 
                                                               repo_tipo_expansion, 
                                                               repo_luminaria, 
                                                               repo_redes, 
                                                               repo_redes, 
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
                                                                {repo_redes}, 
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
              'repo_redes -> adicional.repo_redes,
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

      // Proceso de Creación de Luminarias Nuevas por Expansión Tipo III
      reporte.direcciones.map { direcciones =>
        for (d <- direcciones) {
          if (d.aap_id != None) {
            // var aap_elemento: AapElemento = new AapElemento(d.aap_id, None, None, None, None, None, None, reporte.reti_id, reporte.repo_consecutivo.map(_.toInt))
            var aap: Redes = new Redes(d.aap_id, reporte.empr_id, reporte.usua_id, None, None, Some(1), reporte.repo_fechasolucion, None)
            // var aap_adicional: AapAdicional = new AapAdicional(d.aap_id, None, None, None, None, None, None, None, None, None)
            val aapOption =
              aapService.buscarPorId(d.aap_id.get, reporte.empr_id.get)
            aapOption match {
              case None => aapService.crear(aap)
              case (a) => aap = a.get
            }

      // Fin Proceso de Creación de Luminarias Nuevas por Expansión Tipo I,II,III,V
          }
        }
      }

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
              """UPDATE siap.redes_reporte_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                elem_id = {elem_id},
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id}
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
                'even_id -> e.even_id
              )
              .executeUpdate() > 0
            if (!eventoActualizado) {
              eventoInsertado = SQL(
                """INSERT INTO siap.redes_reporte_evento (even_fecha, 
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
                                    {even_id})"""
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
                  "even_id" -> e.even_id
                )
                .executeUpdate() > 0
            }
            if ((eventoActualizado || eventoInsertado) && (estado != 9)) {
              // validar elemento y actualizar aap_elemento
              elemento.tiel_id match {
                case Some(1) =>
                  SQL(
                    """UPDATE siap.redes_elemento SET aap_bombillo = {aap_bombillo}, reti_id = {reti_id} , repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_bombillo -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.redes_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
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
                    SQL("""INSERT INTO siap.redes_elemento_historia (
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
                    """UPDATE siap.redes_elemento SET aap_balasto = {aap_balasto}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_balasto -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.redes_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
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
                    SQL("""INSERT INTO siap.redes_elemento_historia (
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
                    """UPDATE siap.redes_elemento SET aap_arrancador = {aap_arrancador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_arrancador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.redes_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
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
                    SQL("""INSERT INTO siap.redes_elemento_historia (
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
                    """UPDATE siap.redes_elemento SET aap_condensador = {aap_condensador}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_condensador -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.redes_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
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
                    SQL("""INSERT INTO siap.redes_elemento_historia (
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
                    """UPDATE siap.redes_elemento SET aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                  ).on(
                      'aap_fotocelda -> e.even_codigo_instalado,
                      'aap_id -> e.aap_id,
                      'empr_id -> reporte.empr_id,
                      'reti_id -> reporte.reti_id,
                      'repo_consecutivo -> reporte.repo_consecutivo
                    )
                    .executeUpdate()
                  val updated: Boolean = SQL(
                    """UPDATE siap.redes_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
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
                    SQL("""INSERT INTO siap.redes_elemento_historia (
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
            val aap = aapService.buscarPorId(d.aap_id.get, reporte.empr_id.get).get

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
              """UPDATE siap.redes_reporte_direccion SET
                                                even_direccion = {even_direccion},
                                                barr_id = {barr_id},
                                                even_direccion_anterior = {even_direccion_anterior},
                                                barr_id_anterior = {barr_id_anterior},
                                                even_estado = {even_estado},
                                                tire_id = {tire_id}
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
                'even_id -> d.even_id
              )
              .executeUpdate() > 0

            if (!dirActualizado) {
              dirInsertado = SQL(
                """INSERT INTO siap.redes_reporte_direccion (repo_id, aap_id, even_direccion, barr_id, even_id, even_direccion_anterior, barr_id_anterior, even_estado, tire_id) VALUES ({repo_id}, {aap_id}, {even_direccion}, {barr_id}, {even_id}, {even_direccion_anterior}, {barr_id_anterior}, {even_estado}, {tire_id})"""
              ).on(
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_direccion -> d.even_direccion,
                  'barr_id -> d.barr_id,
                  'even_id -> d.even_id,
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
              """UPDATE siap.redes_reporte_direccion_dato SET
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
              datoInsertado = SQL("""INSERT INTO siap.redes_reporte_direccion_dato (
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
              """UPDATE siap.redes_reporte_direccion_dato_adicional SET 
                                aacu_id_anterior = {aacu_id_anterior},
                                aacu_id = {aacu_id},
                                aaus_id_anterior = {aaus_id_anterior},
                                aaus_id = {aaus_id},
                                medi_id_anterior = {medi_id_anterior},
                                medi_id = {medi_id},
                                tran_id_anterior = {tran_id_anterior},
                                tran_id = {tran_id}
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
                'repo_id -> reporte.repo_id,
                'aap_id -> d.aap_id,
                'even_id -> d.even_id
              )
              .executeUpdate() > 0
            if (!datoadicionalActualizado) {
              datoadicionalInsertado = SQL(
                """INSERT INTO siap.redes_reporte_direccion_dato_adicional (
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
                                    tran_id) 
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
                                    {tran_id}
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
                  'repo_id -> reporte.repo_id,
                  'aap_id -> d.aap_id,
                  'even_id -> d.even_id
                )
                .executeUpdate() > 0
            }

            // Fin Direccion Dato Adicional
            // Actualizar Información de Luminaria
            // No Aplica a Redes
            /*
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
                    println("Actualizando Tipo Redes")
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
                    println("Actualizando Redes Altura")
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
                    println("Actualizando Redes Propietario")
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
                      SQL("DELETE FROM siap.aap_medidor WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                      on(
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      ).executeUpdate()
                    }
                    if (aaco_id == 3) {
                      println("Cambiando estado a RETIRADA")
                      SQL("UPDATE siap.aap SET esta_id = {esta_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                      on(
                        'esta_id -> 2,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id                      
                      ).executeUpdate()
                    }
                    if (aaco_id == 1 || aaco_id == 2) {
                      println("Cambiando estado a ACTIVA")
                      SQL("UPDATE siap.aap SET esta_id = {esta_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                      on(
                        'esta_id -> 1,
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id                      
                      ).executeUpdate()
                    }
                  case None => false
                }
              case None => false
            }
            */
            // Actualización Dato Adicional a Luminaria
            // No Aplica en Redes
            /*
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
            */
            // actualizar direccion de la luminaria y datos adicionales
            if (reporte.reti_id.get == 1 || reporte.reti_id.get == 2 || reporte.reti_id.get == 3) {
              val res = SQL(
                """UPDATE siap.redes SET aap_direccion = {aap_direccion}, barr_id = {barr_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
              ).on(
                  'aap_direccion -> d.even_direccion,
                  'barr_id -> d.barr_id,
                  'aap_id -> d.aap_id,
                  'empr_id -> reporte.empr_id
                )
                .executeUpdate() > 0
            }
            reporte.reti_id match {
              case Some(8) =>
                println("tipo retiro: " + d.tire_id)
                d.tire_id match {
                  case Some(3) =>
                    SQL(
                      """UPDATE siap.redes SET esta_id = 9 WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
                    ).on(
                        'aap_id -> d.aap_id,
                        'empr_id -> reporte.empr_id
                      )
                      .executeUpdate()
                  case _ =>
                    SQL(
                      """UPDATE siap.redes SET esta_id = 2 WHERE aap_id = {aap_id} and empr_id = {empr_id}"""
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
        """DELETE FROM siap.redes_reporte_medioambiente WHERE repo_id = {repo_id}"""
      ).on(
          'repo_id -> reporte.repo_id
        )
        .execute()

      reporte.meams.map { meams =>
        for (m <- meams) {
          SQL(
            """INSERT INTO siap.redes_reporte_medioambiente (repo_id, meam_id) VALUES ({repo_id}, {meam_id})"""
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
          """INSERT INTO siap.redes_reporte_codigo_autorizacion (repo_id, coau_codigo, empr_id) VALUES ({repo_id}, {coau_codigo}, {empr_id})"""
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
              'audi_tabla -> "redes_reporte",
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
    * Eliminar Reporte
    */
  def borrar(repo_id: scala.Long, usua_id: scala.Long): Boolean = {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: scala.Long =
        SQL("UPDATE siap.redes_reporte SET rees_id = 9 WHERE repo_id = {repo_id}")
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
          'audi_tabla -> "redes_reporte",
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

  def actualizarHistoria(empr_id: scala.Long): Boolean = {
    db.withConnection { implicit connection =>
      reportes(empr_id).map { reportes =>
        reportes.foreach { reporte =>
          println("procesando reporte: " + reporte.repo_consecutivo)
          reporte.eventos.foreach { eventos =>
            eventos.foreach { e =>
              println("procesando eventos")
              if (e.aap_id != None) {
                println("procesando aap: " + e.aap_id)
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
                if (estado != 9) {
                  // validar elemento y actualizar aap_elemento
                  println("elemento tipo:" + elemento.tiel_id)
                  elemento.tiel_id match {
                    case Some(1) =>
                      SQL(
                        """UPDATE siap.redes_elemento SET aap_bombillo = {aap_bombillo} where aap_id = {aap_id} and empr_id = {empr_id}"""
                      ).on(
                          'aap_bombillo -> e.even_codigo_instalado,
                          'aap_id -> e.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                      val updated: Boolean = SQL(
                        """UPDATE siap.redes_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
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
                        SQL("""INSERT INTO siap.redes_elemento_historia (
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
                            'aap_balasto_instalado -> Option.empty[String],
                            'aap_arrancador_retirado -> Option.empty[String],
                            'aap_arrancador_instalado -> Option.empty[String],
                            'aap_condensador_retirado -> Option.empty[String],
                            'aap_condensador_instalado -> Option.empty[String],
                            'aap_fotocelda_retirado -> Option.empty[String],
                            'aap_fotocelda_instalado -> Option.empty[String],
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
                        """UPDATE siap.redes_elemento SET aap_balasto = {aap_balasto} where aap_id = {aap_id} and empr_id = {empr_id}"""
                      ).on(
                          'aap_balasto -> e.even_codigo_instalado,
                          'aap_id -> e.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                      val updated: Boolean = SQL(
                        """UPDATE siap.redes_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
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
                          """INSERT INTO siap.redes_elemento_historia (
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
                            'aap_bombillo_retirado -> Option.empty[String],
                            'aap_bombillo_instalado -> Option.empty[String],
                            'aap_balasto_retirado -> e.even_codigo_retirado,
                            'aap_balasto_instalado -> e.even_codigo_instalado,
                            'aap_arrancador_retirado -> Option.empty[String],
                            'aap_arrancador_instalado -> Option.empty[String],
                            'aap_condensador_retirado -> Option.empty[String],
                            'aap_condensador_instalado -> Option.empty[String],
                            'aap_fotocelda_retirado -> Option.empty[String],
                            'aap_fotocelda_instalado -> Option.empty[String],
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
                        """UPDATE siap.redes_elemento SET aap_arrancador = {aap_arrancador} where aap_id = {aap_id} and empr_id = {empr_id}"""
                      ).on(
                          'aap_arrancador -> e.even_codigo_instalado,
                          'aap_id -> e.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                      val updated: Boolean = SQL(
                        """UPDATE siap.redes_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
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
                          """INSERT INTO siap.redes_elemento_historia (
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
                            'aap_bombillo_retirado -> Option.empty[String],
                            'aap_bombillo_instalado -> Option.empty[String],
                            'aap_balasto_retirado -> Option.empty[String],
                            'aap_balasto_instalado -> Option.empty[String],
                            'aap_arrancador_retirado -> e.even_codigo_retirado,
                            'aap_arrancador_instalado -> e.even_codigo_instalado,
                            'aap_condensador_retirado -> Option.empty[String],
                            'aap_condensador_instalado -> Option.empty[String],
                            'aap_fotocelda_retirado -> Option.empty[String],
                            'aap_fotocelda_instalado -> Option.empty[String],
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
                        """UPDATE siap.redes_elemento SET aap_condensador = {aap_condensador} where aap_id = {aap_id} and empr_id = {empr_id}"""
                      ).on(
                          'aap_condensador -> e.even_codigo_instalado,
                          'aap_id -> e.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                      val updated: Boolean = SQL(
                        """UPDATE siap.redes_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
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
                          """INSERT INTO siap.redes_elemento_historia (
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
                            'aap_bombillo_retirado -> Option.empty[String],
                            'aap_bombillo_instalado -> Option.empty[String],
                            'aap_balasto_retirado -> Option.empty[String],
                            'aap_balasto_instalado -> Option.empty[String],
                            'aap_arrancador_retirado -> Option.empty[String],
                            'aap_arrancador_instalado -> Option.empty[String],
                            'aap_condensador_retirado -> e.even_codigo_retirado,
                            'aap_condensador_instalado -> e.even_codigo_instalado,
                            'aap_fotocelda_retirado -> Option.empty[String],
                            'aap_fotocelda_instalado -> Option.empty[String],
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
                        """UPDATE siap.redes_elemento SET aap_fotocelda = {aap_fotocelda} where aap_id = {aap_id} and empr_id = {empr_id}"""
                      ).on(
                          'aap_fotocelda -> e.even_codigo_instalado,
                          'aap_id -> e.aap_id,
                          'empr_id -> reporte.empr_id
                        )
                        .executeUpdate()
                      val updated: Boolean = SQL(
                        """UPDATE siap.redes_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
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
                          """INSERT INTO siap.redes_elemento_historia (
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
                            'aap_bombillo_retirado -> Option.empty[String],
                            'aap_bombillo_instalado -> Option.empty[String],
                            'aap_balasto_retirado -> Option.empty[String],
                            'aap_balasto_instalado -> Option.empty[String],
                            'aap_arrancador_retirado -> Option.empty[String],
                            'aap_arrancador_instalado -> Option.empty[String],
                            'aap_condensador_retirado -> Option.empty[String],
                            'aap_condensador_instalado -> Option.empty[String],
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
                    case _ => println("no existe el elemento")
                  }
                }
              }
              println("eventos procesados")
            }
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
        SQL("""SELECT * FROM siap.redes_reporte_estado WHERE rees_estado < 9""").as(
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
        """SELECT * FROM siap.redes_reporte_tipo WHERE reti_estado < 9 ORDER BY reti_id"""
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
          var compiledFile = REPORT_DEFINITION_PATH + "siap_redes_reporte.jasper";
          reporte match {
            case Some(r) =>
              if (reporte.get.reti_id.get == 2 || reporte.get.reti_id.get == 6) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_redes_reporte_expansion.jasper"
              } else if (reporte.get.reti_id.get == 8) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_redes_reporte_retiro.jasper"
              } else if (reporte.get.reti_id.get == 3) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_redes_reporte_reubicacion.jasper"
              } else if (reporte.get.reti_id.get == 9) {
                compiledFile = REPORT_DEFINITION_PATH + "siap_redes_reporte_cambio_medida.jasper"
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
        """SELECT * FROM siap.redes_reporte_tipo WHERE reti_id = {reti_id}"""
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
      tipo: String
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

      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      val format = new SimpleDateFormat("yyyy-MM-dd")
      var os = Array[Byte]()
      val compiledFile = REPORT_DEFINITION_PATH + "siap_reporte_relacion.jasper";
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
      tipo match {
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
                .withCellValues("POSTE ATENCIÓN A USUARIOS")
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
                  "Fecha y Hora",
                  "Fecha Límite",
                  "Persona/Entidad Usuario",
                  "Teléfono",
                  "Dirección",
                  "Barrio",
                  "Medio",
                  "Daño Reportado",
                  "Cuadrilla"
                )
              var j = 2
              val resultSet =
                SQL("""select r.*, a.*, o.*, rt.*, t.*, b.*, ((r.repo_fecharecepcion + interval '48h')::timestamp + (SELECT COUNT(*) FROM siap.festivo WHERE fest_dia BETWEEN r.repo_fecharecepcion and (r.repo_fecharecepcion + interval '48h')) * '1 day'::interval ) as fecha_limite,  c.cuad_descripcion from siap.redes_reporte r 
                        left join siap.redes_reporte_adicional a on r.repo_id = a.repo_id
                        left join siap.redes_reporte_tipo rt on r.reti_id = rt.reti_id
                        left join siap.actividad t on a.acti_id = t.acti_id
                        left join siap.barrio b on r.barr_id = b.barr_id
                        left join siap.origen o on r.orig_id = o.orig_id
                        left join siap.ordentrabajo_reporte otr on otr.repo_id = r.repo_id
                        left join siap.ordentrabajo ot on ot.ortr_id = otr.ortr_id
                        left join siap.cuadrilla c on c.cuad_id = ot.cuad_id
                        where r.repo_fecharecepcion between {fecha_inicial} and {fecha_final} and r.rees_id = 1 and r.empr_id = {empr_id}
                        order by r.reti_id, r.repo_id
                    """)
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
                    DateCell(
                      i.repo_fecharecepcion match {
                        case Some(v) => new java.util.Date(v.getMillis)
                        case None    => new java.util.Date(0)
                      },
                      Some(1),
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
                      Some(2),
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
                      Some(3),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_telefono match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(4),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.repo_direccion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(5),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.barr_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(6),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.orig_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(7),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.acti_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(8),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      i.cuad_descripcion match {
                        case Some(value) => value
                        case None        => ""
                      },
                      Some(9),
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

}