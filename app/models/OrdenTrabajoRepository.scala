package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{ OutputStream, ByteArrayOutputStream }
import java.util.{ Map, HashMap }
import java.lang.Long

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

case class OrdenEvento(even_id: Option[Int], even_estado: Option[Int], repo_id: Option[scala.Long], reti_id: Option[scala.Long], repo_consecutivo: Option[Int], repo_descripcion: Option[String])
case class OrdenObra(even_id: Option[Int], even_estado: Option[Int], obra_id: Option[scala.Long], obra_consecutivo: Option[Int], obra_nombre: Option[String])

case class OrdenTrabajo(ortr_id: Option[scala.Long], 
                       ortr_fecha: DateTime, 
                       ortr_observacion: String, 
                       ortr_consecutivo: Option[Int], 
                       otes_id: scala.Long, 
                       cuad_id: scala.Long, 
                       tiba_id: Option[scala.Long],
                       empr_id: scala.Long, 
                       usua_id: scala.Long, 
                       reportes: Option[List[OrdenEvento]],
                       obras: Option[List[OrdenObra]])

object OrdenEvento {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val ordeneventoWrites = new Writes[OrdenEvento] {
        def writes(ortr:OrdenEvento) = Json.obj(
            "even_id" -> ortr.even_id,
            "even_estado" -> ortr.even_estado,
            "repo_id" -> ortr.repo_id,
            "reti_id" -> ortr.reti_id,
            "repo_consecutivo" -> ortr.repo_consecutivo,
            "repo_descripcion" -> ortr.repo_descripcion,
        )
    }

    implicit val ordeneventoReads: Reads[OrdenEvento] = (
       (__ \ "even_id").readNullable[Int] and
         (__ \ "even_estado").readNullable[Int] and
         (__ \ "repo_id").readNullable[scala.Long] and
         (__ \ "reti_id").readNullable[scala.Long] and
         (__ \ "repo_consecutivo").readNullable[Int] and
         (__ \ "repo_descripcion").readNullable[String]
    )(OrdenEvento.apply _)

    val set = {
        get[Option[Int]]("even_id") ~
        get[Option[Int]]("even_estado") ~
        get[Option[scala.Long]]("repo_id") ~
        get[Option[scala.Long]]("reti_id") ~
        get[Option[Int]]("repo_consecutivo") ~
        get[Option[String]]("repo_descripcion") map {
            case even_id ~ even_estado ~ repo_id ~ reti_id ~ repo_consecutivo ~ repo_descripcion => OrdenEvento(even_id,even_estado,repo_id,reti_id,repo_consecutivo,repo_descripcion)
        }
    }      
}

object OrdenObra {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val ordenobraWrites = new Writes[OrdenObra] {
        def writes(ortr:OrdenObra) = Json.obj(
            "even_id" -> ortr.even_id,
            "even_estado" -> ortr.even_estado,
            "obra_id" -> ortr.obra_id,
            "obra_consecutivo" -> ortr.obra_consecutivo,
            "obra_nombre" -> ortr.obra_nombre,
        )
    }

    implicit val ordenobraReads: Reads[OrdenObra] = (
       (__ \ "even_id").readNullable[Int] and
         (__ \ "even_estado").readNullable[Int] and
         (__ \ "obra_id").readNullable[scala.Long] and
         (__ \ "obra_consecutivo").readNullable[Int] and
         (__ \ "obra_nombre").readNullable[String]
    )(OrdenObra.apply _)

    val set = {
        get[Option[Int]]("even_id") ~
        get[Option[Int]]("even_estado") ~
        get[Option[scala.Long]]("obra_id") ~
        get[Option[Int]]("obra_consecutivo") ~
        get[Option[String]]("obra_nombre") map {
            case even_id ~ even_estado ~ obra_id ~ obra_consecutivo ~ obra_nombre => OrdenObra(even_id,even_estado,obra_id,obra_consecutivo,obra_nombre)
        }
    }      
}


object OrdenTrabajo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val ordentrabajoWrites = new Writes[OrdenTrabajo] {
        def writes(ortr:OrdenTrabajo) = Json.obj(
            "ortr_id" -> ortr.ortr_id,
            "ortr_fecha" -> ortr.ortr_fecha,
            "ortr_observacion" -> ortr.ortr_observacion,
            "ortr_consecutivo" -> ortr.ortr_consecutivo,
            "otes_id" -> ortr.otes_id,
            "cuad_id" -> ortr.cuad_id,
            "tiba_id" -> ortr.tiba_id,
            "empr_id" -> ortr.empr_id,
            "usua_id" -> ortr.usua_id,
            "reportes" -> ortr.reportes,
            "obras" -> ortr.obras
        )
    }

    implicit val ordentrabajoReads: Reads[OrdenTrabajo] = (
       (__ \ "ortr_id").readNullable[scala.Long] and
         (__ \ "ortr_fecha").read[DateTime] and
         (__ \ "ortr_observacion").read[String] and
         (__ \ "ortr_consecutivo").readNullable[Int] and
         (__ \ "otes_id").read[scala.Long] and
         (__ \ "cuad_id").read[scala.Long] and
         (__ \ "tiba_id").readNullable[scala.Long] and
         (__ \ "empr_id").read[scala.Long] and
         (__ \ "usua_id").read[scala.Long] and
         (__ \ "reportes").readNullable[List[OrdenEvento]] and
         (__ \ "obras").readNullable[List[OrdenObra]]
    )(OrdenTrabajo.apply _)

    /**
    * Parsear un OrdenTrabajo desde un ResultSet
    */
    val simple = {
        get[Option[scala.Long]]("ordentrabajo.ortr_id") ~
        get[DateTime]("ordentrabajo.ortr_fecha") ~
        get[String]("ordentrabajo.ortr_observacion") ~
        get[Option[Int]]("ordentrabajo.ortr_consecutivo") ~
        get[scala.Long]("ordentrabajo.otes_id") ~
        get[scala.Long]("ordentrabajo.cuad_id") ~
        get[Option[scala.Long]]("ordentrabajo.tiba_id") ~
        get[scala.Long]("ordentrabajo.empr_id") ~
        get[scala.Long]("ordentrabajo.usua_id") map {
            case ortr_id ~ ortr_fecha ~ ortr_observacion ~ ortr_consecutivo ~ otes_id ~ cuad_id ~ tiba_id ~ empr_id ~ usua_id => OrdenTrabajo(ortr_id, ortr_fecha, ortr_observacion, ortr_consecutivo, otes_id, cuad_id, tiba_id, empr_id, usua_id, None, None)
        }
    }    
}

class OrdenTrabajoRepository @Inject()(dbapi: DBApi, empresaService:EmpresaRepository)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")
    private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

    /**
    * Recuperar un OrdenTrabajo por su ortr_id
    * @param ortr_id: scala.Long
    */
    def buscarPorId(ortr_id: scala.Long) : Option[OrdenTrabajo] = {
        db.withConnection { implicit connection => 
            val o = SQL("SELECT * FROM siap.ordentrabajo WHERE ortr_id = {ortr_id}").
            on(
                'ortr_id -> ortr_id
            ).as(OrdenTrabajo.simple.single)
            val e = SQL("""SELECT e.even_id, e.even_estado, e.repo_id, r.reti_id, r.repo_consecutivo, r.repo_descripcion FROM siap.ordentrabajo_reporte e 
                           LEFT JOIN siap.reporte r ON r.repo_id = e.repo_id
                           WHERE e.ortr_id = {ortr_id}""").
            on(
                'ortr_id -> ortr_id
            ).as(OrdenEvento.set *)
            val b = SQL("""SELECT e.even_id, e.even_estado, e.obra_id, r.obra_consecutivo, r.obra_nombre FROM siap.ordentrabajo_obra e 
                           LEFT JOIN siap.obra r ON r.obra_id = e.obra_id
                           WHERE e.ortr_id = {ortr_id}""").
            on(
                'ortr_id -> ortr_id
            ).as(OrdenObra.set *)            
            Some(o.copy(reportes = Some(e), obras = Some(b)))
        }
    }

    /**
    * Recuperar un OrdenTrabajo por su ortr_consecutivo
    * @param ortr_consecutivo: Int
    * @param empr_id: scala.Long
    */
    def buscarPorConsecutivo(ortr_consecutivo: Int, empr_id: scala.Long) : Option[OrdenTrabajo] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.ordentrabajo WHERE ortr_consecutivo = {ortr_consecutivo} and empr_id = {empr_id}").
            on(
                'ortr_consecutivo -> ortr_consecutivo,
                'empr_id -> empr_id
            ).as(OrdenTrabajo.simple.singleOpt)
        }
    }

    /**
    * Recuperar el último OrdenTrabajo por su empr_id
    * @param empr_id: scala.Long
    */
    def buscarUltimoPorEmpresa(empr_id: scala.Long) : Option[OrdenTrabajo] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.ordentrabajo WHERE empr_id = {empr_id} ORDER BY ortr_id DESC LIMIT 1 OFFSET 0").
            on(
                'empr_id -> empr_id
            ).as(OrdenTrabajo.simple.singleOpt)
        }
    }

  /**
  * Recuperar total de registros
  * @param empr_id: scala.Long
  * @return total
  */
  def cuenta(empr_id: scala.Long): scala.Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.ordentrabajo WHERE empr_id = {empr_id} and otes_id <> 99").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[scala.Long].single)
      result
    }
  }        

  /**
    Recuperar todos los Elemento de una empresa
    @param empr_id: scala.Long
    @param page_size: scala.Long
    @param current_page: scala.Long
    @return Future[Iterable[Elemento]]
    */
  def todos(page_size: scala.Long, current_page: scala.Long, empr_id: scala.Long, orderby: String, filter: String): Future[Iterable[OrdenTrabajo]] =
    Future[Iterable[OrdenTrabajo]] {
      db.withConnection { implicit connection =>
        var _list:ListBuffer[OrdenTrabajo] = new ListBuffer[OrdenTrabajo]()  
            SQL("SELECT * FROM siap.ordentrabajo WHERE empr_id = {empr_id} AND otes_id <> 99 ORDER BY ortr_id DESC LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) ").
            on(
                'empr_id -> empr_id,
                'page_size -> page_size,
                'current_page -> current_page
            ).as(OrdenTrabajo.simple *)        
      }
    }


    /**
    * Recuperar todos los OrdenTrabajo de una Empresa usando el empr_id
    * @param empr_id: scala.Long
    */
    def ordenes(empr_id: scala.Long): Future[Iterable[OrdenTrabajo]] = Future[Iterable[OrdenTrabajo]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.ordentrabajo WHERE empr_id = {empr_id} AND otes_id <> 99 ORDER BY ortr_id DESC").
            on(
                'empr_id -> empr_id
            ).as(OrdenTrabajo.simple *)
        }        
    }

    /**
    * Crear OrdenTrabajo
    * @param ortr: OrdenTrabajo
    */
    def crear(ortr: OrdenTrabajo, empr_id: scala.Long, usua_id: scala.Long) : Future[scala.Long] = Future {
        db.withConnection{ implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            var consecutivo = SQL("""SELECT COUNT(*) FROM siap.ordentrabajo WHERE empr_id = {empr_id}""").
            on(
                'empr_id -> empr_id
            ).as(SqlParser.scalar[Int].single)
            consecutivo = consecutivo + 1
            val id: scala.Long = SQL("""INSERT INTO siap.ordentrabajo (ortr_fecha, ortr_observacion, ortr_consecutivo, otes_id, cuad_id, empr_id, usua_id, tiba_id) VALUES ({ortr_fecha}, {ortr_observacion}, {ortr_consecutivo}, {otes_id}, {cuad_id}, {empr_id}, {usua_id}, {tiba_id})""").
            on(
                'ortr_fecha -> ortr.ortr_fecha,
                'ortr_observacion -> ortr.ortr_observacion, 
                'ortr_consecutivo -> consecutivo, 
                'otes_id -> ortr.otes_id, 
                'cuad_id -> ortr.cuad_id, 
                'empr_id -> empr_id, 
                'usua_id -> usua_id,
                'tiba_id -> ortr.tiba_id
            ).executeInsert().get
            // Guardar relacion de reportes
            ortr.reportes.map { reportes => 
                for(r <- reportes) {
                    r.repo_consecutivo match {
                       case Some(consec) =>
                         SQL("""INSERT INTO siap.ordentrabajo_reporte (ortr_id, repo_id, even_id, even_estado) VALUES ({ortr_id}, {repo_id}, {even_id}, {even_estado})""").
                         on(
                             'ortr_id -> id,
                             'repo_id -> r.repo_id,
                             'even_id -> r.even_id,
                             'even_estado -> r.even_estado
                         ).executeInsert()
                       case None => None
                    }
                }
            }

            // Guardar relacion de obras
            ortr.obras.map { reportes => 
                for(r <- reportes) {
                    r.obra_consecutivo match {
                       case Some(consec) =>
                         SQL("""INSERT INTO siap.ordentrabajo_obra (ortr_id, obra_id, even_id, even_estado) VALUES ({ortr_id}, {obra_id}, {even_id}, {even_estado})""").
                         on(
                             'ortr_id -> id,
                             'obra_id -> r.obra_id,
                             'even_id -> r.even_id,
                             'even_estado -> r.even_estado
                         ).executeInsert()
                       case None => None
                    }
                }
            }

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> ortr.usua_id,
                'audi_tabla -> "ordentrabajo", 
                'audi_uid -> id,
                'audi_campo -> "ortr_consecutivo", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> ortr.ortr_consecutivo,
                'audi_evento -> "I").
                executeInsert()
            
            id 
        }             
    }

    /**
    *  Actualizar OrdenTrabajo
    *  @param ortr: OrdenTrabajo
    */
    def actualizar(ortr: OrdenTrabajo) : Boolean = {
        val ortr_ant : Option[OrdenTrabajo] = buscarPorId(ortr.ortr_id.get)

        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha
            val count: scala.Long = SQL("UPDATE siap.ordentrabajo SET ortr_fecha = {ortr_fecha}, ortr_observacion = {ortr_observacion}, ortr_consecutivo = {ortr_consecutivo}, otes_id = {otes_id}, cuad_id = {cuad_id}, empr_id = {empr_id}, usua_id = {usua_id} WHERE ortr_id = {ortr_id}").
            on(
                "ortr_id" -> ortr.ortr_id,
                "ortr_fecha" -> ortr.ortr_fecha,
                "ortr_observacion" -> ortr.ortr_observacion, 
                "ortr_consecutivo" -> ortr.ortr_consecutivo, 
                "otes_id" -> ortr.otes_id, 
                "cuad_id" -> ortr.cuad_id, 
                "empr_id" -> ortr.empr_id, 
                "usua_id" -> ortr.usua_id
            ).executeUpdate()

            // Guardar relacion de reportes
            ortr.reportes.map { reportes => 
                for(r <- reportes) {
                    r.repo_consecutivo match {
                       case Some(consec) =>
                         val esactualizado:Boolean = SQL("""UPDATE siap.ordentrabajo_reporte SET repo_id = {repo_id}, even_estado = {even_estado} where ortr_id = {ortr_id} and even_id = {even_id}""").
                         on(
                             'ortr_id -> ortr.ortr_id,
                             'even_id -> r.even_id,
                             'repo_id -> r.repo_id,
                             'even_estado -> r.even_estado
                         ).executeUpdate() > 0
                         if (!esactualizado) {
                           SQL("""INSERT INTO siap.ordentrabajo_reporte (ortr_id, repo_id, even_id, even_estado) VALUES ({ortr_id}, {repo_id}, {even_id}, {even_estado})""").
                           on(
                             'ortr_id -> ortr.ortr_id,
                             'repo_id -> r.repo_id,
                             'even_id -> r.even_id,
                             'even_estado -> r.even_estado
                           ).executeInsert()
                         }
                       case None => None
                    }
                }
            }            

            // Guardar relacion de reportes
            ortr.obras.map { reportes => 
                for(r <- reportes) {
                    r.obra_consecutivo match {
                       case Some(consec) =>
                         val esactualizado:Boolean = SQL("""UPDATE siap.ordentrabajo_obra SET obra_id = {obra_id}, even_estado = {even_estado} where ortr_id = {ortr_id} and even_id = {even_id}""").
                         on(
                             'ortr_id -> ortr.ortr_id,
                             'even_id -> r.even_id,
                             'obra_id -> r.obra_id,
                             'even_estado -> r.even_estado
                         ).executeUpdate() > 0
                         if (!esactualizado) {
                           SQL("""INSERT INTO siap.ordentrabajo_obra (ortr_id, obra_id, even_id, even_estado) VALUES ({ortr_id}, {obra_id}, {even_id}, {even_estado})""").
                           on(
                             'ortr_id -> ortr.ortr_id,
                             'obra_id -> r.obra_id,
                             'even_id -> r.even_id,
                             'even_estado -> r.even_estado
                           ).executeInsert()
                         }
                       case None => None
                    }
                }
            }

            if (ortr_ant != None){
                if (ortr_ant.get.ortr_fecha != ortr.ortr_fecha){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "ortr_fecha", 
                    'audi_valorantiguo -> ortr_ant.get.ortr_fecha,
                    'audi_valornuevo -> ortr.ortr_fecha,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (ortr_ant.get.ortr_observacion != ortr.ortr_observacion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "ortr_observacion", 
                    'audi_valorantiguo -> ortr_ant.get.ortr_observacion,
                    'audi_valornuevo -> ortr.ortr_observacion,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (ortr_ant.get.ortr_consecutivo != ortr.ortr_consecutivo){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "ortr_consecutivo", 
                    'audi_valorantiguo -> ortr_ant.get.ortr_consecutivo,
                    'audi_valornuevo -> ortr.ortr_consecutivo,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (ortr_ant.get.otes_id != ortr.otes_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "otes_id", 
                    'audi_valorantiguo -> ortr_ant.get.otes_id,
                    'audi_valornuevo -> ortr.otes_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 
                if (ortr_ant.get.cuad_id != ortr.cuad_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "cuad_id", 
                    'audi_valorantiguo -> ortr_ant.get.cuad_id,
                    'audi_valornuevo -> ortr.cuad_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }   
                if (ortr_ant.get.empr_id != ortr.empr_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "empr_id", 
                    'audi_valorantiguo -> ortr_ant.get.empr_id,
                    'audi_valornuevo -> ortr.empr_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                } 
                if (ortr_ant.get.usua_id != ortr.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> ortr.usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr.ortr_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> ortr_ant.get.usua_id,
                    'audi_valornuevo -> ortr.usua_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }                                                               
            }

            count > 0
        }
    }

    /**
    * Eliminar Orden Trabajo
    */
    def borrar(ortr_id: scala.Long, usua_id: scala.Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())

            val count:scala.Long = SQL("UPDATE siap.ordentrabajo SET otes_id = 99 WHERE ortr_id = {ortr_id}").
            on(
                'ortr_id -> ortr_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "ordentrabajo", 
                    'audi_uid -> ortr_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }    

    /**
    * estado
    */
    def estados(): Future[Iterable[ReporteEstado]] = Future[Iterable[ReporteEstado]] {
      db.withConnection { implicit connection =>
        SQL("""SELECT * FROM siap.reporte_estado""").as(ReporteEstado.oEstado *)
      }
    }

    /**
    *  imprimir
    * @param ortr_id: scala.Long
    * @return Array[Byte]
    */
    def imprimir(ortr_id: scala.Long, empr_id: scala.Long): Array[Byte] = {
      var os = Array[Byte]()
      val orden = buscarPorId(ortr_id)
      db.withConnection { implicit connection => 
        empresaService.buscarPorId(empr_id).map { empresa =>
          try {
            var compiledFile = REPORT_DEFINITION_PATH + "siap_orden_trabajo.jasper";
            var reportParams = new HashMap[String, java.lang.Object]()
            reportParams.put("ORTR_ID", new java.lang.Long(ortr_id.longValue()))
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

}