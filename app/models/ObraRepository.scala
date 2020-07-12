package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{ OutputStream, ByteArrayOutputStream }
import java.util.{ Map, HashMap, Date }
import java.lang.Long
import java.sql.Date

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
import anorm.SqlParser.{ get, str, scalar }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.DateTimeZone


// ya tiene los 22 elementos
case class Obra(obra_id: Option[scala.Long],
                   obra_consecutivo: Option[scala.Long],
                   obra_nombre: Option[String],                   
                   obra_fecharecepcion: Option[DateTime],
                   obra_direccion: Option[String], 
                   obra_solicitante: Option[String],
                   obra_telefono: Option[String],
                   obra_email: Option[String],
                   obra_fechasolucion: Option[DateTime], 
                   obra_reportetecnico: Option[String],                    
                   obra_descripcion: Option[String],
                   obra_horainicio: Option[String],
                   obra_horafin: Option[String],
                   obra_modificado: Option[DateTime],
                   muot_id: Option[scala.Long],                   
                   orig_id: Option[scala.Long],
                   rees_id: Option[scala.Long], 
                   barr_id: Option[scala.Long],
                   empr_id: Option[scala.Long],
                   usua_id: Option[scala.Long],
                   meams: Option[List[scala.Long]],
                   eventos: Option[List[ObraEvento]])

case class ObraConsulta(obra_id: Option[scala.Long],
                   obra_consecutivo: Option[scala.Long],
                   obra_nombre: Option[String],                   
                   obra_fecharecepcion: Option[DateTime], 
                   obra_direccion: Option[String], 
                   obra_solicitante: Option[String],
                   obra_telefono: Option[String], 
                   obra_email: Option[String],                  
                   obra_fechasolucion: Option[DateTime], 
                   obra_reportetecnico: Option[String],                   
                   obra_descripcion: Option[String],
                   obra_horainicio: Option[String],
                   obra_horafin: Option[String],
                   obra_modificado: Option[DateTime],
                   muot_id: Option[scala.Long],
                   orig_id: Option[scala.Long],
                   rees_id: Option[scala.Long], 
                   barr_id: Option[scala.Long],
                   empr_id: Option[scala.Long],
                   usua_id: Option[scala.Long])


case class ObraEstado(rees_id: Option[scala.Long],
                         rees_descripcion: String,
                         rees_estado: Int,
                         usua_id: scala.Long)

case class ObraEvento(
                  even_fecha: Option[DateTime],
                  even_codigo_instalado: Option[String],
                  even_cantidad_instalado: Option[Double],
                  even_codigo_retirado: Option[String],
                  even_cantidad_retirado: Option[Double],
                  even_estado: Option[Int],
                  aap_id: Option[scala.Long],
                  obra_id: Option[scala.Long],
                  elem_id: Option[scala.Long],
                  usua_id: Option[scala.Long],
                  empr_id: Option[scala.Long],
                  even_id: Option[scala.Long])

object ObraEvento {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val obraeventoWrites = new Writes[ObraEvento] {
    def writes(evento: ObraEvento) = Json.obj(
      "even_fecha" -> evento.even_fecha,
      "even_codigo_instalado" -> evento.even_codigo_instalado,
      "even_cantidad_instalado" -> evento.even_cantidad_instalado,
      "even_codigo_retirado" -> evento.even_codigo_retirado,
      "even_cantidad_retirado" -> evento.even_cantidad_retirado,      
      "even_estado" -> evento.even_estado,
      "aap_id" -> evento.aap_id,
      "obra_id" -> evento.obra_id,
      "elem_id" -> evento.elem_id,
      "usua_id" -> evento.usua_id,
      "empr_id" -> evento.empr_id,
      "even_id" -> evento.even_id
    )
  }

  implicit val obraeventoReads: Reads[ObraEvento] = (
      (__ \ "even_fecha").readNullable[DateTime] and
      (__ \ "even_codigo_instalado").readNullable[String] and
      (__ \ "even_cantidad_instalado").readNullable[Double] and
      (__ \ "even_codigo_retirado").readNullable[String] and
      (__ \ "even_cantidad_retirado").readNullable[Double] and
      (__ \ "even_estado").readNullable[Int] and
      (__ \ "aap_id").readNullable[scala.Long] and
      (__ \ "obra_id").readNullable[scala.Long] and
      (__ \ "elem_id").readNullable[scala.Long] and
      (__ \ "usua_id").readNullable[scala.Long] and
      (__ \ "empr_id").readNullable[scala.Long] and
      (__ \ "even_id").readNullable[scala.Long]
  )(ObraEvento.apply _)

  val set = {
      get[Option[DateTime]]("even_fecha") ~
      get[Option[String]]("even_codigo_instalado") ~
      get[Option[Double]]("even_cantidad_instalado") ~
      get[Option[String]]("even_codigo_retirado") ~
      get[Option[Double]]("even_cantidad_retirado") ~
      get[Option[Int]]("even_estado") ~
      get[Option[scala.Long]]("aap_id") ~
      get[Option[scala.Long]]("obra_id") ~
      get[Option[scala.Long]]("elem_id") ~
      get[Option[scala.Long]]("usua_id") ~
      get[Option[scala.Long]]("empr_id") ~
      get[Option[scala.Long]]("even_id") map {
      case even_fecha ~ even_codigo_instalado ~ even_cantidad_instalado ~ even_codigo_retirado ~ even_cantidad_retirado ~ even_estado ~ aap_id ~ obra_id ~ elem_id ~ usua_id ~ empr_id ~ even_id =>
        ObraEvento(
               even_fecha,
               even_codigo_instalado,
               even_cantidad_instalado,
               even_codigo_retirado,
               even_cantidad_retirado,
               even_estado,
               aap_id,
               obra_id,
               elem_id,
               usua_id,
               empr_id,
               even_id)
    }
  }
}                  

object Obra {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val obraWrites = new Writes[Obra] {
        def writes(obra: Obra) = Json.obj(
            "obra_id" -> obra.obra_id,
            "obra_consecutivo" -> obra.obra_consecutivo,
            "obra_nombre" -> obra.obra_nombre,            
            "obra_fecharecepcion" -> obra.obra_fecharecepcion,
            "obra_direccion" -> obra.obra_direccion,
            "obra_solicitante" -> obra.obra_solicitante,
            "obra_telefono" -> obra.obra_telefono,
            "obra_email" -> obra.obra_email,
            "obra_reportetecnico" -> obra.obra_reportetecnico,
            "obra_fechasolucion" -> obra.obra_fechasolucion,
            "obra_descripcion" -> obra.obra_descripcion,
            "obra_horainicio" -> obra.obra_horainicio,
            "obra_horafin" -> obra.obra_horafin,
            "obra_modificado" -> obra.obra_modificado,
            "muot_id" -> obra.muot_id,
            "rees_id" -> obra.rees_id,
            "orig_id" -> obra.orig_id,
            "barr_id" -> obra.barr_id,
            "empr_id" -> obra.empr_id,
            "usua_id" -> obra.usua_id,
            "meams" -> obra.meams,
            "eventos" -> obra.eventos
        )
    }

    implicit val obraReads: Reads[Obra] = (
        (__ \ "obra_id").readNullable[scala.Long] and
        (__ \ "obra_consecutivo").readNullable[scala.Long] and
        (__ \ "obra_nombre").readNullable[String] and        
        (__ \ "obra_fecharecepcion").readNullable[DateTime] and
        (__ \ "obra_direccion").readNullable[String] and
        (__ \ "obra_solicitante").readNullable[String] and
        (__ \ "obra_telefono").readNullable[String] and
        (__ \ "obra_email").readNullable[String] and
        (__ \ "obra_fechasolucion").readNullable[DateTime] and
        (__ \ "obra_reportetecnico").readNullable[String] and       
        (__ \ "obra_descripcion").readNullable[String] and
        (__ \ "obra_horainicio").readNullable[String] and       
        (__ \ "obra_horafin").readNullable[String] and
        (__ \ "obra_modificado").readNullable[DateTime] and
        (__ \ "muot_id").readNullable[scala.Long] and
        (__ \ "rees_id").readNullable[scala.Long] and
        (__ \ "orig_id").readNullable[scala.Long] and
        (__ \ "barr_id").readNullable[scala.Long] and
        (__ \ "empr_id").readNullable[scala.Long] and
        (__ \ "usua_id").readNullable[scala.Long] and
        (__ \ "meams").readNullable[List[scala.Long]] and
        (__ \ "eventos").readNullable[List[ObraEvento]]
    )(Obra.apply _)
}

object ObraConsulta {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val rcWrites = new Writes[ObraConsulta] {
        def writes(a: ObraConsulta) = Json.obj(
            "obra_id" -> a.obra_id,
            "obra_consecutivo" -> a.obra_consecutivo,
            "obra_nombre" -> a.obra_nombre,            
            "obra_fecharecepcion" -> a.obra_fecharecepcion,
            "obra_direccion" -> a.obra_direccion,
            "obra_solicitante" -> a.obra_solicitante,
            "obra_telefono" -> a.obra_telefono,
            "obra_email" -> a.obra_email,
            "obra_reportetecnico" -> a.obra_reportetecnico,
            "obra_fechasolucion" -> a.obra_fechasolucion,
            "obra_descripcion" -> a.obra_descripcion,
            "obra_horainicio" -> a.obra_horainicio,
            "obra_horafin" -> a.obra_horafin,
            "obra_modificado" -> a.obra_modificado,
            "muot_id" -> a.muot_id,
            "rees_id" -> a.rees_id,
            "orig_id" -> a.orig_id,
            "barr_id" -> a.barr_id,
            "empr_id" -> a.empr_id,
            "usua_id" -> a.usua_id
        )
    }

    implicit val rcReads: Reads[ObraConsulta] = (
        (__ \ "obra_id").readNullable[scala.Long] and
        (__ \ "obra_consecutivo").readNullable[scala.Long] and
        (__ \ "obra_nombre").readNullable[String] and        
        (__ \ "obra_fecharecepcion").readNullable[DateTime] and
        (__ \ "obra_direccion").readNullable[String] and
        (__ \ "obra_solicitante").readNullable[String] and
        (__ \ "obra_telefono").readNullable[String] and
        (__ \ "obra_email").readNullable[String] and
        (__ \ "obra_fechasolucion").readNullable[DateTime] and
        (__ \ "obra_reportetecnico").readNullable[String] and
        (__ \ "obra_descripcion").readNullable[String] and
        (__ \ "obra_horainicio").readNullable[String] and
        (__ \ "obra_horafin").readNullable[String] and
        (__ \ "obra_modificado").readNullable[DateTime] and
        (__ \ "muot_id").readNullable[scala.Long] and
        (__ \ "rees_id").readNullable[scala.Long] and
        (__ \ "orig_id").readNullable[scala.Long] and
        (__ \ "barr_id").readNullable[scala.Long] and
        (__ \ "empr_id").readNullable[scala.Long] and
        (__ \ "usua_id").readNullable[scala.Long]
    )(ObraConsulta.apply _)
}

class ObraRepository @Inject()(dbapi: DBApi, eventoService: EventoRepository, elementoService: ElementoRepository, empresaService: EmpresaRepository, usuarioService: UsuarioRepository, aapService: AapRepository)(implicit ec:DatabaseExecutionContext){
    private val db = dbapi.database("default")
    private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"
    /**
    * Parsear un Obra desde un ResultSet
    */
    private val simple = {
        get[Option[scala.Long]]("obra_id") ~
        get[Option[scala.Long]]("obra_consecutivo") ~
        get[Option[String]]("obra_nombre") ~        
        get[Option[DateTime]]("obra_fecharecepcion") ~
        get[Option[String]]("obra_direccion") ~
        get[Option[String]]("obra_solicitante") ~
        get[Option[String]]("obra_telefono") ~
        get[Option[String]]("obra_email") ~
        get[Option[DateTime]]("obra_fechasolucion") ~
        get[Option[String]]("obra_reportetecnico") ~
        get[Option[String]]("obra_descripcion") ~
        get[Option[String]]("obra_horainicio") ~
        get[Option[String]]("obra_horafin") ~     
        get[Option[DateTime]]("obra_modificado") ~
        get[Option[scala.Long]]("muot_id") ~
        get[Option[scala.Long]]("rees_id") ~
        get[Option[scala.Long]]("orig_id") ~
        get[Option[scala.Long]]("barr_id") ~
        get[Option[scala.Long]]("empr_id") ~
        get[Option[scala.Long]]("usua_id") map {
            case obra_id ~
                 obra_consecutivo ~
                 obra_nombre ~                 
                 obra_fecharecepcion ~
                 obra_direccion ~
                 obra_solicitante ~
                 obra_telefono ~
                 obra_email ~
                 obra_fechasolucion ~
                 obra_reportetecnico ~
                 obra_descripcion ~
                 obra_horainicio ~
                 obra_horafin ~
                 obra_modificado ~
                 muot_id ~
                 rees_id ~
                 orig_id ~
                 barr_id ~
                 empr_id ~
                 usua_id => Obra(
                 obra_id,
                 obra_consecutivo,
                 obra_nombre,
                 obra_fecharecepcion,
                 obra_direccion,
                 obra_solicitante,
                 obra_telefono,
                 obra_email,
                 obra_fechasolucion,
                 obra_reportetecnico,
                 obra_descripcion,
                 obra_horainicio,
                 obra_horafin,
                 obra_modificado,
                 muot_id,
                 rees_id,
                 orig_id,
                 barr_id,
                 empr_id,
                 usua_id,
                 null,
                 null
                 )
        }
    }

  /*
  *
  */
  def validarCodigo(elem_id: scala.Long, codigo: String, empr_id: scala.Long): String = {
      db.withConnection { implicit connection =>
            var resultado: String = ""
            val tiel_id = SQL("""SELECT tiel_id FROM siap.elemento WHERE elem_id = {elem_id}""").
                          on(
                              'elem_id -> elem_id
                          ).as(SqlParser.scalar[scala.Long].single)
            val conteo1: scala.Long = SQL("""SELECT COUNT(*) AS conteo FROM siap.reporte_evento r
                           INNER JOIN siap.reporte s ON s.repo_id = r.repo_id
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_retirado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}""").
                           on(
                               'tiel_id -> tiel_id,
                               'codigo -> codigo,
                               'empr_id -> empr_id
                           ).as(SqlParser.scalar[scala.Long].single)
            val conteo2: scala.Long = SQL("""SELECT COUNT(*) AS conteo FROM siap.reporte_evento r
                           INNER JOIN siap.reporte s ON s.repo_id = r.repo_id
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_instalado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}""").
                           on(
                               'tiel_id -> tiel_id,
                               'codigo -> codigo,
                               'empr_id -> empr_id
                           ).as(SqlParser.scalar[scala.Long].single)
            val conteo3: scala.Long = SQL("""SELECT COUNT(*) AS conteo FROM siap.obra_evento r
                           INNER JOIN siap.obra s ON s.obra_id = r.obra_id            
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_retirado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}""").
                           on(
                               'tiel_id -> tiel_id,
                               'codigo -> codigo,
                               'empr_id -> empr_id
                           ).as(SqlParser.scalar[scala.Long].single)
            val conteo4: scala.Long = SQL("""SELECT COUNT(*) AS conteo FROM siap.obra_evento r
                           INNER JOIN siap.obra s ON s.obra_id = r.obra_id            
                           INNER JOIN siap.elemento e ON e.elem_id = r.elem_id
                           INNER JOIN siap.tipoelemento t ON t.tiel_id = e.tiel_id
                           WHERE r.even_codigo_instalado = {codigo} and s.empr_id = {empr_id} and t.tiel_id = {tiel_id}""").
                           on(
                               'tiel_id -> tiel_id,
                               'codigo -> codigo,
                               'empr_id -> empr_id
                           ).as(SqlParser.scalar[scala.Long].single)

            if (conteo1 > 0 || conteo3 > 0) {
                resultado = resultado + "1"
            } else {
                resultado = resultado + "0"
            }
            if (conteo2 > 0 || conteo4 > 0) {
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
  def cuenta(empr_id: scala.Long): scala.Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.obra WHERE empr_id = {empr_id} and rees_id <> 9").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[scala.Long].single)
      result
    }
  }

  /**
    Recuperar todos los Obra de una empresa
    @param empr_id: scala.Long
    @param page_size: scala.Long
    @param current_page: scala.Long
    @return Future[Iterable[Obra]]
    */
  def todos(page_size: scala.Long, current_page: scala.Long, empr_id: scala.Long, orderby: String, filter: String): Future[Iterable[Obra]] =
    Future[Iterable[Obra]] {
      db.withConnection { implicit connection =>
        var _list:ListBuffer[Obra] = new ListBuffer[Obra]()
        var query: String = """SELECT *
                                          FROM siap.obra r 
                                          LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                                          WHERE r.empr_id = {empr_id}
                                          and r.rees_id <> 9 """
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY r.rees_id ASC, r.obra_id DESC"
                    }
                    query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
          val reps = SQL(query)                        
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(simple *)
          reps.map { r =>
            val eventos = SQL("""SELECT * FROM siap.obra_evento WHERE obra_id = {obra_id}""").
                              on(
                                 'obra_id -> r.obra_id
                                ).as(ObraEvento.set *)
            val meams = SQL("""SELECT m.meam_id FROM siap.obra_medioambiente m WHERE m.obra_id = {obra_id}""").
                              on(
                                'obra_id -> r.obra_id
                                ).as(scalar[scala.Long].*)
            val obra = new Obra(
                r.obra_id,
                r.obra_consecutivo,
                r.obra_nombre,
                r.obra_fecharecepcion,
                r.obra_direccion,
                r.obra_solicitante,
                r.obra_telefono,
                r.obra_email,
                r.obra_fechasolucion,
                r.obra_reportetecnico,
                r.obra_descripcion,
                r.obra_horainicio,
                r.obra_horafin,
                r.obra_modificado,
                r.muot_id,
                r.rees_id,
                r.orig_id,
                r.barr_id,
                r.empr_id,
                r.usua_id,
                Some(meams),
                Some(eventos)
             )
             _list += obra
          }
          _list
       }
    }


  /**
  * Recuperar todos los Unidad activas
  */
  def obras(empr_id:scala.Long): Future[Iterable[Obra]] = Future[Iterable[Obra]] {
      db.withConnection { implicit connection =>
        var _list:ListBuffer[Obra] = new ListBuffer[Obra]()
        val reps = SQL("""SELECT * FROM siap.obra r 
                                          WHERE r.empr_id = {empr_id} 
                                          and r.rees_id <> 9""")
                    .on(
                      'empr_id -> empr_id,
                       )
                    .as(simple *)
          reps.map { r =>
            val eventos = SQL("""SELECT * FROM siap.obra_evento WHERE obra_id = {obra_id}""").
                              on(
                                 'obra_id -> r.obra_id
                                ).as(ObraEvento.set *)
            val meams = SQL("""SELECT m.meam_id FROM siap.obra_medioambiente m WHERE m.obra_id = {obra_id}""").
                              on(
                                'obra_id -> r.obra_id
                                ).as(scalar[scala.Long].*)  
            val obra = new Obra(
                r.obra_id,
                r.obra_consecutivo,
                r.obra_nombre,
                r.obra_fecharecepcion,
                r.obra_direccion,
                r.obra_solicitante,
                r.obra_telefono,
                r.obra_email,
                r.obra_fechasolucion,
                r.obra_reportetecnico,
                r.obra_descripcion,
                r.obra_horainicio,
                r.obra_horafin,
                r.obra_modificado,
                r.muot_id,
                r.rees_id,
                r.orig_id,
                r.barr_id,
                r.empr_id,
                r.usua_id,
                Some(meams),
                Some(eventos)
             )
             _list += obra
          }
        _list        
      }
    }     

    /**
    * Recuperar un Obra dado su obra_id
    * @param obra_id: scala.Long
    */
    def buscarPorId(obra_id: scala.Long) : Option[Obra] = {
        db.withConnection { implicit connection => 
            val r = SQL("""SELECT * FROM siap.obra r
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.obra_id = {obra_id}""").
            on(
                'obra_id -> obra_id
            ).as(simple.singleOpt)

            val eventos = SQL("""SELECT * FROM siap.obra_evento WHERE obra_id = {obra_id} ORDER BY even_id ASC""").
            on(
                'obra_id -> obra_id
            ).as(ObraEvento.set *)
            val meams = SQL("""SELECT m.meam_id FROM siap.obra_medioambiente m WHERE m.obra_id = {obra_id}""").
                          on(
                            'obra_id -> obra_id
                            ).as(scalar[scala.Long].*)    
            r.map { r =>
             val obra = new Obra(
                r.obra_id,
                r.obra_consecutivo,
                r.obra_nombre,
                r.obra_fecharecepcion,
                r.obra_direccion,
                r.obra_solicitante,
                r.obra_telefono,
                r.obra_email,
                r.obra_fechasolucion,
                r.obra_reportetecnico,
                r.obra_descripcion,
                r.obra_horainicio,
                r.obra_horafin,
                r.obra_modificado,
                r.muot_id,
                r.rees_id,
                r.orig_id,
                r.barr_id,
                r.empr_id,
                r.usua_id,
                Some(meams),
                Some(eventos)
             )
             obra
            }
        }
    }

    /**
    * Recuperar un Obra dado su obra_consecutivo
    * @param reti_id: scala.Long
    * @param obra_consecutivo: scala.Long
    * @param empr_id: scala.Long
    */
    def buscarPorConsecutivo(obra_consecutivo: scala.Long, empr_id: scala.Long) : Option[Obra] = {
        db.withConnection { implicit connection => 
            val r = SQL("""SELECT * FROM siap.obra r
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.obra_consecutivo = {obra_consecutivo} and r.empr_id = {empr_id} and r.rees_id <> 9""").
            on(
                'obra_consecutivo -> obra_consecutivo,
                'empr_id -> empr_id
            ).as(simple.singleOpt)
            
            r match {
              case Some(r) =>  val eventos = SQL("""SELECT * FROM siap.obra_evento WHERE obra_id = {obra_id} ORDER BY even_id ASC""").
                                            on(
                                                'obra_id -> r.obra_id
                                              ).as(ObraEvento.set *)
                               val meams = SQL("""SELECT m.meam_id FROM siap.obra_medioambiente m WHERE m.obra_id = {obra_id}""").
                                            on(
                                                'obra_id -> r.obra_id
                                              ).as(scalar[scala.Long].*)   
                               val obra = new Obra(
                                                                  r.obra_id,
                                                                  r.obra_consecutivo,
                                                                  r.obra_nombre,
                                                                  r.obra_fecharecepcion,
                                                                  r.obra_direccion,
                                                                  r.obra_solicitante,
                                                                  r.obra_telefono,
                                                                  r.obra_email,
                                                                  r.obra_fechasolucion,
                                                                  r.obra_reportetecnico,
                                                                  r.obra_descripcion,
                                                                  r.obra_horainicio,
                                                                  r.obra_horafin,
                                                                  r.obra_modificado,
                                                                  r.muot_id,
                                                                  r.rees_id,
                                                                  r.orig_id,
                                                                  r.barr_id,
                                                                  r.empr_id,
                                                                  r.usua_id,
                                                                  Some(meams),
                                                                  Some(eventos)
                                                                 )
                               Some(obra)

              case None => None
            }
        }
    } 


    /**
    * Recuperar un Obra dado su obra_consecutivo
    * @param reti_id: scala.Long
    * @param obra_consecutivo: scala.Long
    * @param empr_id: scala.Long
    */
    def buscarPorConsecutivoConsulta(obra_consecutivo: scala.Long, empr_id: scala.Long) : Option[ObraConsulta] = {
        db.withConnection { implicit connection => 
            val r = SQL("""SELECT * FROM siap.obra r
                            LEFT JOIN siap.origen o on r.orig_id = o.orig_id
                            LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                            INNER JOIN siap.reporte_estado e on r.rees_id = e.rees_id
                    WHERE r.obra_consecutivo = {obra_consecutivo} and r.empr_id = {empr_id} and r.rees_id <>9""").
            on(
                'obra_consecutivo -> obra_consecutivo,
                'empr_id -> empr_id
            ).as(simple.singleOpt)
            
            r match {
              case Some(r) =>  val Obra = new ObraConsulta(
                                                                  r.obra_id,
                                                                  r.obra_consecutivo,
                                                                  r.obra_nombre,
                                                                  r.obra_fecharecepcion,
                                                                  r.obra_direccion,
                                                                  r.obra_solicitante,
                                                                  r.obra_telefono,
                                                                  r.obra_email,
                                                                  r.obra_fechasolucion,
                                                                  r.obra_reportetecnico,
                                                                  r.obra_descripcion,
                                                                  r.obra_horainicio,
                                                                  r.obra_horafin,
                                                                  r.obra_modificado,
                                                                  r.muot_id,
                                                                  r.rees_id,
                                                                  r.orig_id,
                                                                  r.barr_id,
                                                                  r.empr_id,
                                                                  r.usua_id
                                                                 )
                               Some(Obra)
              case None => None
            }
        }
    }

    /**
    * Recuperar todos los Obra dado su rango de fecha de recepcion
    * @param anho: Int
    * @param mes: Int
    * @param empr_id: scala.Long
    */
    def buscarPorRango(anho: Int, mes: Int, empr_id: scala.Long) : Future[Iterable[Obra]] = Future[Iterable[Obra]] {
        db.withConnection { implicit connection => 
        var _list:ListBuffer[Obra] = new ListBuffer[Obra]()
        var query: String = """SELECT *
                                          FROM siap.obra r 
                                          LEFT JOIN siap.barrio b on r.barr_id = b.barr_id
                                          WHERE r.empr_id = {empr_id} and r.obra_fecharecepcion between {fecha_inicial} and {fecha_final}
                                          and r.rees_id <> 9 ORDER BY r.rees_id, r.obra_fecharecepcion DESC """
                    /*
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY r.rees_id ASC, r.obra_id DESC"
                    }
                    */
          val fechaini = new DateTime(anho,mes,1,0,0,0,0)
          val lastDay = fechaini.dayOfMonth().getMaximumValue()
          val fechafin = new DateTime(anho,mes,lastDay,23,59,59,999)
          val reps:List[Obra] = SQL(query)
          .on(
            'empr_id -> empr_id,
            'fecha_inicial -> fechaini,
            'fecha_final -> fechafin
          )
          .as(simple *)
          reps.map { r =>
            val eventos = SQL("""SELECT * FROM siap.obra_evento WHERE obra_id = {obra_id} """).
                              on(
                                 'obra_id -> r.obra_id
                                ).as(ObraEvento.set *)
            val meams = SQL("""SELECT m.meam_id FROM siap.obra_medioambiente m WHERE m.obra_id = {obra_id}""").
                              on(
                                'obra_id -> r.obra_id
                                ).as(scalar[scala.Long].*)
                             
            val obra = new Obra(
                r.obra_id,
                r.obra_consecutivo,
                r.obra_nombre,
                r.obra_fecharecepcion,
                r.obra_direccion,
                r.obra_solicitante,
                r.obra_telefono,
                r.obra_email,
                r.obra_fechasolucion,
                r.obra_reportetecnico,
                r.obra_descripcion,
                r.obra_horainicio,
                r.obra_horafin,
                r.obra_modificado,
                r.muot_id,
                r.rees_id,
                r.orig_id,
                r.barr_id,
                r.empr_id,
                r.usua_id,
                Some(meams),
                Some(eventos)
             )
             _list += obra
          }
          _list
       }
    }

    /**
    * Recuperar todos los Obra dado su rango de fecha de solucion
    * @param fecha_inicial: DateTime
    * @param fecha_final: DateTime
    * @param empr_id: scala.Long
    */
    def buscarPorRangoFechaSolucion(fecha_inicial: DateTime, fecha_final: DateTime, empr_id: scala.Long) : Future[Iterable[Obra]] = Future[Iterable[Obra]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.obra WHERE obra_fechasolucion BETWEEN {fecha_inicial} and {fecha_final} and empr_id = {empr_id}").
            on(
                'fecha_inicial -> fecha_inicial,
                'fecha_final -> fecha_final,
                'empr_id -> empr_id
            ).as(simple *)
        }
    }

    /**
    * Recuperar un Obra dado su rees_id
    * @param rees_id: Int
    * @param empr_id: scala.Long
    */
    def buscarPorEstado(rees_id: scala.Long, empr_id: scala.Long) : Future[Iterable[Obra]] = Future[Iterable[Obra]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.obra WHERE rees_id = {rees_id} and empr_id = {empr_id}").
            on(
                'rees_id -> rees_id,
                'empr_id -> empr_id
            ).as(simple *)
        }
    } 

    /**
    * Recuperar un Obra dado su orig_id
    * @param orig_id: scala.Long
    * @param empr_id: scala.Long
    */
    def buscarPorOrigen(orig_id: scala.Long, empr_id: scala.Long) : Future[Iterable[Obra]] = Future[Iterable[Obra]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.obra WHERE orig_id = {orig_id} and empr_id = {empr_id}").
            on(
                'orig_id -> orig_id,
                'empr_id -> empr_id
            ).as(simple *)
        }
    }

    /**
    * Recuperar un Obra dada una busqueda multiple
    * @param criteria: Map[String, Object]
    */
    /**
    * TODO: manejar el filtro avanzado
    */
    def buscarAvanzado(filtro: scala.collection.mutable.Map[String, Object]) : Future[Iterable[Obra]] = Future[Iterable[Obra]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.obra")
            .as(simple *)
        }        
    }

    /**
    * Crear Obra
    */
    def crear(obra: Obra): Future[(scala.Long, scala.Long)] = Future[(scala.Long, scala.Long)] {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val consec = consecutivo(obra.empr_id.get)
            if (consec > 0) {
              val id: scala.Long = SQL("INSERT INTO siap.obra (obra_fecharecepcion, obra_direccion, obra_solicitante, obra_telefono, obra_email, obra_fechasolucion, obra_reportetecnico, obra_descripcion, obra_horainicio, obra_horafin, obra_modificado, muot_id, rees_id, orig_id, barr_id, empr_id, usua_id, obra_consecutivo, obra_nombre) VALUES ({obra_fecharecepcion}, {obra_direccion}, {obra_solicitante}, {obra_telefono}, {obra_email}, {obra_fechasolucion}, {obra_reportetecnico}, {obra_descripcion}, {obra_horainicio}, {obra_horafin}, {obra_modificado}, {muot_id}, {rees_id}, {orig_id}, {barr_id}, {empr_id}, {usua_id}, {obra_consecutivo}, {obra_nombre})").
              on(
                'obra_fecharecepcion -> obra.obra_fecharecepcion,
                'obra_direccion -> obra.obra_direccion, 
                'obra_solicitante -> obra.obra_solicitante,
                'obra_telefono -> obra.obra_telefono,
                'obra_email -> obra.obra_email,
                'obra_fechasolucion -> obra.obra_fechasolucion,
                'obra_reportetecnico -> obra.obra_reportetecnico,
                'obra_horainicio -> obra.obra_horainicio,
                'obra_horafin -> obra.obra_horafin,
                'obra_modificado -> obra.obra_modificado,
                'muot_id -> obra.muot_id,
                'orig_id -> obra.orig_id, 
                'barr_id -> obra.barr_id,
                'usua_id -> obra.usua_id,
                'empr_id -> obra.empr_id,                
                'rees_id -> obra.rees_id,                
                'obra_descripcion -> obra.obra_descripcion,
                'obra_consecutivo -> consec,
                'obra_nombre -> obra.obra_nombre
              ).executeInsert().get

              SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
              on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> obra.usua_id,
                'audi_tabla -> "obra", 
                'audi_uid -> id,
                'audi_campo -> "obra_id", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> id,
                'audi_evento -> "I").
                executeInsert()
            
              (id,consec)
            } else {
              (0,0)  
            }           
        }
    }

    /**
    * Actualizar Obra
    * @param obra: Obra
    */
    def actualizar(obra: Obra) : Boolean = {
        val obra_ant: Option[Obra] = buscarPorId(obra.obra_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            var historia = scala.collection.mutable.Map[scala.Long, ElementoHistoria]()
            val result: Boolean = SQL("UPDATE siap.obra SET obra_nombre = {obra_nombre}, obra_direccion = {obra_direccion}, obra_solicitante = {obra_solicitante}, obra_telefono = {obra_telefono}, obra_email = {obra_email}, obra_fechasolucion = {obra_fechasolucion}, obra_reportetecnico = {obra_reportetecnico}, obra_descripcion = {obra_descripcion}, obra_horainicio = {obra_horainicio}, obra_horafin = {obra_horafin}, obra_modificado = {obra_modificado} ,muot_id = {muot_id}, rees_id = {rees_id}, orig_id = {orig_id}, barr_id = {barr_id}, empr_id = {empr_id}, usua_id = {usua_id} WHERE obra_id = {obra_id}").
            on(
                'obra_id -> obra.obra_id,
                'obra_nombre -> obra.obra_nombre,
                'obra_direccion -> obra.obra_direccion, 
                'obra_solicitante -> obra.obra_solicitante,
                'obra_telefono -> obra.obra_telefono,
                'obra_email -> obra.obra_email,
                'obra_fechasolucion -> obra.obra_fechasolucion, 
                'obra_reportetecnico -> obra.obra_reportetecnico,
                'obra_descripcion -> obra.obra_descripcion,
                'obra_horainicio -> obra.obra_horainicio,
                'obra_horafin -> obra.obra_horafin,  
                'obra_modificado -> hora,
                'muot_id -> obra.muot_id,
                'rees_id -> obra.rees_id, 
                'orig_id -> obra.orig_id, 
                'barr_id -> obra.barr_id,
                'empr_id -> obra.empr_id,
                'usua_id -> obra.usua_id
            ).executeUpdate() > 0

            obra.eventos.map { eventos =>
                for(e <- eventos) {
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
                    var eventoActualizado = false
                    var eventoInsertado = false
                    var estado = 0
                    e.elem_id match {
                        case None => None
                        case Some(elem_id) => elemento = elementoService.buscarPorId(elem_id).get
                    // Actualizar Evento si ya Existe
                                              var estado = 0
                                              e.even_estado match {
                                               case Some(1) => estado = 2
                                               case Some(2) => estado = 2
                                               case Some(8) => estado = 9
                                               case Some(9) => estado = 9
                                               case _ => estado = 2
                                              }
                                              var eventoActualizado: Boolean = false
                                              var eventoInsertado: Boolean = false

                                              eventoActualizado = SQL("""UPDATE siap.obra_evento SET 
                                                                even_fecha = {even_fecha}, 
                                                                elem_id = {elem_id},
                                                                even_estado = {even_estado},
                                                                even_codigo_retirado = {even_codigo_retirado},                                                                 
                                                                even_cantidad_retirado = {even_cantidad_retirado}, 
                                                                even_codigo_instalado = {even_codigo_instalado}, 
                                                                even_cantidad_instalado = {even_cantidad_instalado},
                                                                usua_id = {usua_id}
                                                            WHERE empr_id = {empr_id} and obra_id = {obra_id} and even_id = {even_id}
                                                        """).on(
                                                            'even_fecha -> hora,
                                                            'elem_id -> e.elem_id,
                                                            'even_codigo_retirado -> e.even_codigo_retirado,
                                                            'even_cantidad_retirado -> e.even_cantidad_retirado,
                                                            'even_codigo_instalado -> e.even_codigo_instalado,
                                                            'even_cantidad_instalado -> e.even_cantidad_instalado,
                                                            'usua_id -> obra.usua_id,
                                                            'even_estado -> estado,
                                                            'empr_id -> obra.empr_id,
                                                            'obra_id -> obra.obra_id,
                                                            'aap_id -> e.aap_id,
                                                            'even_id -> e.even_id
                                                        ).executeUpdate() > 0

                                              if (!eventoActualizado) {
                                              eventoInsertado = SQL(
                                                    """INSERT INTO siap.obra_evento (even_fecha, 
                                                        even_codigo_instalado,
                                                        even_cantidad_instalado,
                                                        even_codigo_retirado,
                                                        even_cantidad_retirado, 
                                                        even_estado, 
                                                        aap_id, 
                                                        obra_id, 
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
                                                        {obra_id}, 
                                                        {elem_id}, 
                                                        {usua_id}, 
                                                        {empr_id},
                                                        {even_id})""")
                                                    .on(
                                                        "even_fecha" -> hora,
                                                        "even_codigo_instalado" -> e.even_codigo_instalado,
                                                        "even_cantidad_instalado" -> e.even_cantidad_instalado,
                                                        "even_codigo_retirado" -> e.even_codigo_retirado,
                                                        "even_cantidad_retirado" -> e.even_cantidad_retirado,
                                                        "even_estado" -> estado,
                                                        "aap_id" -> e.aap_id,
                                                        "obra_id" -> obra.obra_id,
                                                        "elem_id" -> e.elem_id,
                                                        "usua_id" -> e.usua_id,
                                                        "empr_id" -> obra.empr_id,
                                                        "even_id" -> e.even_id
                                                        ).executeUpdate() > 0
                                                }
                }
                    if (e.aap_id != None) {
                      if ( (eventoActualizado || eventoInsertado) && (estado != 9) ) {
                            // validar elemento y actualizar aap_elemento
                            elemento.tiel_id match {
                                case Some(1) => SQL("""UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo} where aap_id = {aap_id}""").
                                            on(
                                              'aap_bombillo -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_bombillo_retirado -> e.even_codigo_retirado,
                                              'aap_bombillo_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeUpdate()
                                          }
                                case Some(2) => SQL("""UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto} where aap_id = {aap_id}""").
                                            on(
                                              'aap_balasto -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_balasto_retirado -> e.even_codigo_retirado,
                                              'aap_balasto_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeUpdate()
                                          }
                                case Some(3) => SQL("""UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador} where aap_id = {aap_id}""").
                                            on(
                                              'aap_arrancador -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_arrancador_retirado -> e.even_codigo_retirado,
                                              'aap_arrancador_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeUpdate()
                                          }
                                case Some(4) => SQL("""UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador} where aap_id = {aap_id}""").
                                            on(
                                              'aap_condensador -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_condensador_retirado -> e.even_codigo_retirado,
                                              'aap_condensador_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeUpdate()
                                          }
                                case Some(5) => SQL("""UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda} where aap_id = {aap_id}""").
                                            on(
                                              'aap_fotocelda -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_fotocelda_retirado -> e.even_codigo_retirado,
                                              'aap_fotocelda_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeUpdate()
                                          }
                                case _ => None
                            }
                        }
                    } 
                }
            }

            //
            // guardar medio ambiente
            SQL("""DELETE FROM siap.obra_medioambiente WHERE obra_id = {obra_id}""").
            on(
                'obra_id -> obra.obra_id
            ).execute()

            obra.meams.map { meams =>
                for (m <- meams) {
                    SQL("""INSERT INTO siap.obra_medioambiente (obra_id, meam_id) VALUES ({obra_id}, {meam_id})""").
                    on(
                        'obra_id -> obra.obra_id.get,
                        'meam_id -> m
                    ).executeInsert()
                }
            }
            //


            if (obra_ant != None){
                if (obra_ant.get.obra_fecharecepcion != obra.obra_fecharecepcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "obra_fecharecepcion", 
                    'audi_valorantiguo -> obra_ant.get.obra_fecharecepcion,
                    'audi_valornuevo -> obra.obra_fecharecepcion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }    

                if (obra_ant.get.obra_direccion != obra.obra_direccion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "obra_direccion", 
                    'audi_valorantiguo -> obra_ant.get.obra_direccion,
                    'audi_valornuevo -> obra.obra_direccion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }    

                if (obra_ant.get.obra_fechasolucion != obra.obra_fechasolucion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "obra_fechasolucion", 
                    'audi_valorantiguo -> obra_ant.get.obra_fechasolucion,
                    'audi_valornuevo -> obra.obra_fechasolucion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }   

                if (obra_ant.get.obra_reportetecnico != obra.obra_reportetecnico){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "obra_reportetecnico", 
                    'audi_valorantiguo -> obra_ant.get.obra_reportetecnico,
                    'audi_valornuevo -> obra.obra_reportetecnico,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (obra_ant.get.rees_id != obra.rees_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.rees_id,
                    'audi_campo -> "rees_id", 
                    'audi_valorantiguo -> obra_ant.get.rees_id,
                    'audi_valornuevo -> obra.rees_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                

                if (obra_ant.get.orig_id != obra.orig_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "orig_id", 
                    'audi_valorantiguo -> obra_ant.get.orig_id,
                    'audi_valornuevo -> obra.orig_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 

                if (obra_ant.get.barr_id != obra.barr_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "barr_id", 
                    'audi_valorantiguo -> obra_ant.get.barr_id,
                    'audi_valornuevo -> obra.barr_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }   

                if (obra_ant.get.empr_id != obra.empr_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "empr_id", 
                    'audi_valorantiguo -> obra_ant.get.empr_id,
                    'audi_valornuevo -> obra.empr_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }

                if (obra_ant.get.usua_id != obra.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> obra.usua_id,
                    'audi_tabla -> "obra", 
                    'audi_uid -> obra.obra_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> obra_ant.get.usua_id,
                    'audi_valornuevo -> obra.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                                                           
            }

            result
        }
    }

    /**
    * Eliminar Obra
    */
    def borrar(obra_id: scala.Long, usua_id: scala.Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:scala.Long = SQL("UPDATE siap.obra SET rees_id = 9 WHERE obra_id = {obra_id}").
            on(
                'obra_id -> obra_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "Obra", 
                    'audi_uid -> obra_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

    def actualizarHistoria(empr_id: scala.Long):Boolean = {
        db.withConnection { implicit connection =>
          obras(empr_id).map { obras =>
           for (obra <- obras) {
            obra.eventos.map { eventos =>
                for(e <- eventos) {
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
                        case Some(elem_id) => elemento = elementoService.buscarPorId(elem_id).get
                    } 
                    // Actualizar Evento si ya Existe
                    var estado = 0
                    e.even_estado match {
                        case Some(1) => estado = 2
                        case Some(2) => estado = 2
                        case Some(8) => estado = 9
                        case Some(9) => estado = 9
                        case _ => estado = 2
                    }
                    if ( estado != 9 ) {
                            // validar elemento y actualizar aap_elemento
                            elemento.tiel_id match {
                                case Some(1) => SQL("""UPDATE siap.aap_elemento SET aap_bombillo = {aap_bombillo} where aap_id = {aap_id}""").
                                            on(
                                              'aap_bombillo -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_bombillo_retirado = {aap_bombillo_retirado}, aap_bombillo_instalado = {aap_bombillo_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_bombillo_retirado -> e.even_codigo_retirado,
                                              'aap_bombillo_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeInsert()
                                          }
                                case Some(2) => SQL("""UPDATE siap.aap_elemento SET aap_balasto = {aap_balasto} where aap_id = {aap_id}""").
                                            on(
                                              'aap_balasto -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_balasto_retirado = {aap_balasto_retirado}, aap_balasto_instalado = {aap_balasto_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_balasto_retirado -> e.even_codigo_retirado,
                                              'aap_balasto_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeInsert()
                                          }
                                case Some(3) => SQL("""UPDATE siap.aap_elemento SET aap_arrancador = {aap_arrancador} where aap_id = {aap_id}""").
                                            on(
                                              'aap_arrancador -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_arrancador_retirado = {aap_arrancador_retirado}, aap_arrancador_instalado = {aap_arrancador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_arrancador_retirado -> e.even_codigo_retirado,
                                              'aap_arrancador_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeInsert()
                                          }
                                case Some(4) => SQL("""UPDATE siap.aap_elemento SET aap_condensador = {aap_condensador} where aap_id = {aap_id}""").
                                            on(
                                              'aap_condensador -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_condensador_retirado = {aap_condensador_retirado}, aap_condensador_instalado = {aap_condensador_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_condensador_retirado -> e.even_codigo_retirado,
                                              'aap_condensador_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeInsert()
                                          }
                                case Some(5) => SQL("""UPDATE siap.aap_elemento SET aap_fotocelda = {aap_fotocelda} where aap_id = {aap_id}""").
                                            on(
                                              'aap_fotocelda -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id
                                              ).executeUpdate()
                                          val updated: Boolean = SQL("""UPDATE siap.aap_elemento_historia SET aap_fotocelda_retirado = {aap_fotocelda_retirado}, aap_fotocelda_instalado = {aap_fotocelda_instalado}
                                                 WHERE aap_id = {aap_id} and aael_fecha = {aael_fecha} and reti_id = {reti_id} and obra_consecutivo = {obra_consecutivo}"""
                                          ).on(
                                              'aap_fotocelda_retirado -> e.even_codigo_retirado,
                                              'aap_fotocelda_instalado -> e.even_codigo_instalado,
                                              'aap_id -> e.aap_id,
                                              'aael_fecha -> obra.obra_fechasolucion,
                                              'reti_id -> 0,
                                              'obra_consecutivo -> obra.obra_consecutivo
                                          ).executeUpdate() > 0
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
                                                    obra_consecutivo
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
                                                    {obra_consecutivo}
                                                    )                                                    
                                                """).on(
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
                                                        'aael_fecha -> obra.obra_fechasolucion,
                                                        'reti_id -> 0,
                                                        'obra_consecutivo -> obra.obra_consecutivo                                                
                                                  ).executeInsert()
                                          }
                                case _ => None
                            }
                        } 
                  }
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
    def estados(): Future[Iterable[ReporteEstado]] = Future[Iterable[ReporteEstado]] {
      db.withConnection { implicit connection =>
        SQL("""SELECT * FROM siap.reporte_estado WHERE rees_estado <> 9""").as(ReporteEstado.oEstado *)
      }
    }

    /**
    *  imprimir
    * @param obra_id: scala.Long
    * @return OutputStream
    */
    def imprimir(obra_id: scala.Long, empr_id: scala.Long): Array[Byte] = {
      var os = Array[Byte]()
      val Obra:Option[Obra] = buscarPorId(obra_id)
      db.withConnection { implicit connection =>
        empresaService.buscarPorId(empr_id).map { empresa =>
          try {
            var compiledFile = REPORT_DEFINITION_PATH + "siap_obra.jasper";
            var reportParams = new HashMap[String, java.lang.Object]()
            reportParams.put("OBRA_ID", new java.lang.Long(obra_id.longValue()))
            reportParams.put("EMPRESA", empresa.empr_descripcion)
            reportParams.put("DIRECCION", empresa.empr_direccion)
            reportParams.put("CONCESION", empresa.empr_concesion.get)
            os = JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
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
        empresaService.buscarPorId(empr_id).map { empresa =>
          try {
            var compiledFile = REPORT_DEFINITION_PATH + "siap_obra_blanco.jasper";
            var reportParams = new HashMap[String, java.lang.Object]()
            reportParams.put("EMPRESA", empresa.empr_descripcion)
            reportParams.put("DIRECCION", empresa.empr_direccion)
            reportParams.put("CONCESION", empresa.empr_concesion.get)
            reportParams.put("reti_descripcion", "OBRA")
            os = JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
          } catch {
            case e: Exception => e.printStackTrace();
          }
        }
      }
      os
    }


    /**
    *  imprimir
    * @param obra_id: scala.Long
    * @return OutputStream
    */
    def imprimirRelacion(fecha_inicial: Long, fecha_final: Long, empr_id: scala.Long, usua_id: scala.Long): Array[Byte] = {
            val empresa = empresaService.buscarPorId(empr_id).get
            val usuario = usuarioService.buscarPorId(usua_id).get
            db.withConnection { connection =>
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
            var os = Array[Byte]()
            val compiledFile = REPORT_DEFINITION_PATH + "siap_obra_relacion.jasper";
            val reportParams = new HashMap[String, java.lang.Object]()
                reportParams.put("FECHA_INICIAL", new java.sql.Timestamp(fi.getTimeInMillis()))
                reportParams.put("FECHA_FINAL", new java.sql.Timestamp(ff.getTimeInMillis()))
                reportParams.put("EMPRESA", empresa.empr_descripcion)
                reportParams.put("USUARIO", usuario.usua_nombre + " " + usuario.usua_apellido)
                reportParams.put("EMPR_ID", new java.lang.Long(empr_id.longValue()))
 
               os = JasperRunManager.runReportToPdf(compiledFile, reportParams, connection)
            os
            }
    }

    def consecutivo(empr_id: scala.Long): scala.Long = {
        db.withConnection { implicit connection => 
            var consec = SQL("SELECT COUNT(*) FROM siap.obra WHERE empr_id = {empr_id}").
                                on(
                                    'empr_id -> empr_id
                                ).as(SqlParser.scalar[scala.Long].single)
            consec + 1
        }
    }


}