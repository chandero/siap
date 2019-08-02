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
import org.joda.time.LocalDateTime

case class MunicipioOrdenTrabajo(muot_id: Option[scala.Long],
                                 empr_id: Option[scala.Long],
                                 muot_consecutivo: Option[Int],
                                 muot_descripcion: Option[String],
                                 muot_asunto: Option[String],
                                 muot_remitente: Option[String],
                                 muot_recibe: Option[String],
                                 muot_reportetecnico: Option[String],
                                 muot_estado: Option[Int],
                                 usua_id: Option[scala.Long],
                                 muot_fecharecepcion: Option[DateTime],
                                 muot_radicado: Option[String],
                                 muot_fechaejecucion: Option[DateTime] 
                                )

object MunicipioOrdenTrabajo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val motWrites = new Writes[MunicipioOrdenTrabajo] {
        def writes(mot: MunicipioOrdenTrabajo) = Json.obj(
          "muot_id" -> mot.muot_id,
          "empr_id" -> mot.empr_id,
          "muot_consecutivo" -> mot.muot_consecutivo,
          "muot_descripcion" -> mot.muot_descripcion,
          "muot_asunto" -> mot.muot_asunto,
          "muot_remitente" -> mot.muot_remitente,
          "muot_recibe" -> mot.muot_recibe,
          "muot_reportetecnico" -> mot.muot_reportetecnico,
          "muot_estado" -> mot.muot_estado,
          "usua_id" -> mot.usua_id,
          "muot_fecharecepcion" -> mot.muot_fecharecepcion,
          "muot_radicado" -> mot.muot_radicado,
          "muot_fechaejecucion" -> mot.muot_fechaejecucion
        )
    }

    implicit val motReads: Reads[MunicipioOrdenTrabajo] = (
        (__ \ "muot_id").readNullable[scala.Long] and
          (__ \ "empr_id").readNullable[scala.Long] and
          (__ \ "muot_consecutivo").readNullable[Int] and
          (__ \ "muot_descripcion").readNullable[String] and
          (__ \ "muot_asunto").readNullable[String] and
          (__ \ "muot_remitente").readNullable[String] and
          (__ \ "muot_recibe").readNullable[String] and
          (__ \ "muot_reportetecnico").readNullable[String] and
          (__ \ "muot_estado").readNullable[Int] and
          (__ \ "usua_id").readNullable[scala.Long] and
          (__ \ "muot_fecharecepcion").readNullable[DateTime] and
          (__ \ "muot_radicado").readNullable[String] and
          (__ \ "muot_fechaejecucion").readNullable[DateTime]
     )(MunicipioOrdenTrabajo.apply _)

     val set = {
       get[Option[scala.Long]]("muot_id") ~
       get[Option[scala.Long]]("empr_id") ~
       get[Option[Int]]("muot_consecutivo") ~
       get[Option[String]]("muot_descripcion") ~
       get[Option[String]]("muot_asunto") ~
       get[Option[String]]("muot_remitente") ~
       get[Option[String]]("muot_recibe") ~
       get[Option[String]]("muot_reportetecnico") ~
       get[Option[Int]]("muot_estado") ~
       get[Option[scala.Long]]("usua_id") ~
       get[Option[DateTime]]("muot_fecharecepcion") ~
       get[Option[String]]("muot_radicado") ~
       get[Option[DateTime]]("muot_fechaejecucion") map {
         case muot_id ~
              empr_id ~
              muot_consecutivo ~
              muot_descripcion ~
              muot_asunto ~
              muot_remitente ~
              muot_recibe ~
              muot_reportetecnico ~
              muot_estado ~
              usua_id ~
              muot_fecharecepcion ~
              muot_radicado ~
              muot_fechaejecucion => MunicipioOrdenTrabajo(
                muot_id,
                empr_id,
                muot_consecutivo,
                muot_descripcion,
                muot_asunto,
                muot_remitente,
                muot_recibe,
                muot_reportetecnico,
                muot_estado,
                usua_id,
                muot_fecharecepcion,
                muot_radicado,
                muot_fechaejecucion
              )
       }
     }
}

class MunicipioOrdenTrabajoRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Recuperar un MunicipioOrdenTrabajo por su muot_id
    */
  def buscarPorId(muot_id: scala.Long): Option[MunicipioOrdenTrabajo] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.municipio_ordentrabajo WHERE muot_id = {muot_id} and muot_estado <> 9")
        .on(
          'muot_id -> muot_id
        )
        .as(MunicipioOrdenTrabajo.set.singleOpt)
    }
  }

  /**
    Recuperar un MunicipioOrdenTrabajo por su muot_id
    */
  def buscarPorConsecutivo(muot_consecutivo: scala.Long, empr_id: scala.Long): Option[MunicipioOrdenTrabajo] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.municipio_ordentrabajo WHERE muot_consecutivo = {muot_consecutivo} and empr_id = {empr_id} and muot_estado <> 9")
        .on(
          'muot_consecutivo -> muot_consecutivo,
          'empr_id -> empr_id
        )
        .as(MunicipioOrdenTrabajo.set.singleOpt)
    }
  }

    /**
  * Recuperar total de registros
  * @param empr_id: scala.Long
  * @return total
  */
  def cuenta(empr_id: scala.Long): scala.Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.municipio_ordentrabajo WHERE empr_id = {empr_id} and muot_estado <> 9").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[scala.Long].single)
      result
    }
  }

  /**
    Recuperar todos los MunicipioOrdenTrabajo de una empresa paginado
    @param empr_id: scala.Long
    @param page_size: scala.Long
    @param current_page: scala.Long
    @return Future[Iterable[MunicipioOrdenTrabajo]]
    */
  def todos(page_size: scala.Long, current_page: scala.Long, empr_id: scala.Long, orderby: String, filter: String): Future[Iterable[MunicipioOrdenTrabajo]] =
    Future[Iterable[MunicipioOrdenTrabajo]] {
      db.withConnection { implicit connection =>
        var query: String = """SELECT *
                               FROM siap.municipio_ordentrabajo m 
                               WHERE m.empr_id = {empr_id}
                                          and m.muot_estado <> 9 """
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY m.muot_estado ASC, m.muot_fecharecepcion ASC"
                    }
                    query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        SQL(query)                        
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(MunicipioOrdenTrabajo.set *)
      }
    }

  /**
    Recuperar todos los MunicipioOrdenTrabajo de una empresa paginado
    @param empr_id: scala.Long
    @return Future[Iterable[MunicipioOrdenTrabajo]]
    */
  def mots(empr_id: scala.Long): Future[Iterable[MunicipioOrdenTrabajo]] =
    Future[Iterable[MunicipioOrdenTrabajo]] {
      db.withConnection { implicit connection =>
        var query: String = """SELECT *
                               FROM siap.municipio_ordentrabajo m 
                               WHERE m.empr_id = {empr_id}
                                          and m.muot_estado <> 9 """
            query = query + s" ORDER BY m.muot_estado ASC, m.muot_fecharecepcion ASC"
        SQL(query)                        
          .on(
            'empr_id -> empr_id
          )
          .as(MunicipioOrdenTrabajo.set *)
      }
    }

  /*
    Crear MunicipioOrderTrabajo
    @param mot: MunicipioOrdenTrabajo
    @return id: scala.Long
  */
  def crear(mot: MunicipioOrdenTrabajo): Future[(scala.Long, scala.Long)] = Future[(scala.Long, scala.Long)] {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            var consec = SQL("SELECT COUNT(*) FROM siap.municipio_ordentrabajo WHERE empr_id = {empr_id}").
                         on(
                           'empr_id -> mot.empr_id
                         ).as(SqlParser.scalar[scala.Long].single)
            consec = consec + 1
            val id:scala.Long = SQL("""INSERT INTO siap.municipio_ordentrabajo (
                                                  empr_id, 
                                                  muot_consecutivo, 
                                                  muot_descripcion, 
                                                  muot_asunto, 
                                                  muot_remitente, 
                                                  muot_recibe, 
                                                  muot_reportetecnico, 
                                                  muot_estado, 
                                                  usua_id, 
                                                  muot_fecharecepcion, 
                                                  muot_radicado, 
                                                  muot_fechaejecucion
                                                  )
                                       VALUES (
                                                  {empr_id},
                                                  {muot_consecutivo},
                                                  {muot_descripcion},
                                                  {muot_asunto},
                                                  {muot_remitente},
                                                  {muot_recibe},
                                                  {muot_reportetecnico},
                                                  {muot_estado},
                                                  {usua_id},
                                                  {muot_fecharecepcion},
                                                  {muot_radicado},
                                                  {muot_fechaejecucion}
                                       )
            """).on(
              'empr_id -> mot.empr_id,
              'muot_consecutivo -> consec,
              'muot_descripcion -> mot.muot_descripcion,
              'muot_asunto -> mot.muot_asunto,
              'muot_remitente -> mot.muot_remitente,
              'muot_recibe -> mot.muot_recibe,
              'muot_reportetecnico -> mot.muot_reportetecnico,
              'muot_estado -> mot.muot_estado,
              'usua_id -> mot.usua_id,
              'muot_fecharecepcion -> mot.muot_fecharecepcion,
              'muot_radicado -> mot.muot_radicado,
              'muot_fechaejecucion -> mot.muot_fechaejecucion
            ).executeInsert().get

              SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
              on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo", 
                'audi_uid -> id,
                'audi_campo -> "muot_id",
                'audi_valorantiguo -> "",
                'audi_valornuevo -> id,
                'audi_evento -> "I").
                executeInsert()            
            (id, consec)
        }
  }

  /**
  * Actualizar MunicipioOrdenTrabajo
  * @param mot: MunicipioOrdenTrabajo
  */
  def actualizar(mot: MunicipioOrdenTrabajo) : Boolean = {
      val mot_ant: Option[MunicipioOrdenTrabajo] = buscarPorId(mot.muot_id.get)
      db.withConnection { implicit connection => 
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())

        val result: Boolean = SQL("""UPDATE siap.municipio_ordentrabajo SET 
                                        muot_descripcion = {muot_descripcion},
                                        muot_asunto = {muot_asunto},
                                        muot_remitente = {muot_remitente},
                                        muot_recibe = {muot_recibe},
                                        muot_reportetecnico = {muot_reportetecnico},
                                        muot_estado = {muot_estado},
                                        usua_id = {usua_id},
                                        muot_fecharecepcion = {muot_fecharecepcion},
                                        muot_radicado = {muot_radicado},
                                        muot_fechaejecucion = {muot_fechaejecucion}
                                      WHERE muot_id = {muot_id}
                              """).on(
                                      'muot_descripcion -> mot.muot_descripcion,
                                      'muot_asunto -> mot.muot_asunto,
                                      'muot_remitente -> mot.muot_remitente,
                                      'muot_recibe -> mot.muot_recibe,
                                      'muot_reportetecnico -> mot.muot_reportetecnico,
                                      'muot_estado -> mot.muot_estado,
                                      'usua_id -> mot.usua_id,
                                      'muot_fecharecepcion -> mot.muot_fecharecepcion,
                                      'muot_radicado -> mot.muot_radicado,
                                      'muot_fechaejecucion -> mot.muot_fechaejecucion,
                                      'muot_id -> mot.muot_id
                              ).executeUpdate() > 0
        if (mot_ant != None) {
          if (mot_ant.get.muot_descripcion != mot.muot_descripcion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_descripcion",
                'audi_valorantiguo -> mot_ant.get.muot_descripcion,
                'audi_valornuevo -> mot.muot_descripcion,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_asunto != mot.muot_asunto) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_asunto",
                'audi_valorantiguo -> mot_ant.get.muot_asunto,
                'audi_valornuevo -> mot.muot_asunto,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_remitente != mot.muot_remitente) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_remitente",
                'audi_valorantiguo -> mot_ant.get.muot_remitente,
                'audi_valornuevo -> mot.muot_remitente,
                'audi_evento -> "A"
              ).executeInsert()
          } 
          if (mot_ant.get.muot_recibe != mot.muot_recibe) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_recibe",
                'audi_valorantiguo -> mot_ant.get.muot_recibe,
                'audi_valornuevo -> mot.muot_recibe,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_reportetecnico != mot.muot_reportetecnico) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_reportetecnico",
                'audi_valorantiguo -> mot_ant.get.muot_reportetecnico,
                'audi_valornuevo -> mot.muot_reportetecnico,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_estado != mot.muot_estado) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_estado",
                'audi_valorantiguo -> mot_ant.get.muot_estado,
                'audi_valornuevo -> mot.muot_estado,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_fecharecepcion != mot.muot_fecharecepcion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_fecharecepcion",
                'audi_valorantiguo -> mot_ant.get.muot_fecharecepcion,
                'audi_valornuevo -> mot.muot_fecharecepcion,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_radicado != mot.muot_radicado) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_radicado",
                'audi_valorantiguo -> mot_ant.get.muot_radicado,
                'audi_valornuevo -> mot.muot_radicado,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muot_fechaejecucion != mot.muot_fechaejecucion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "municipio_ordentrabajo",
                'audi_uid -> mot.muot_id,
                'audi_campo -> "muot_fechaejecucion",
                'audi_valorantiguo -> mot_ant.get.muot_fechaejecucion,
                'audi_valornuevo -> mot.muot_fechaejecucion,
                'audi_evento -> "A"
              ).executeInsert()
          }                     
        }
        result
      }
  }

    /**
    * Eliminar MunicipioOrdenTrabajo
    */
    def borrar(muot_id: scala.Long, usua_id: scala.Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:scala.Long = SQL("UPDATE siap.municipio_ordentrabajo SET muot_estado = 9 WHERE muot_id = {muot_id}").
            on(
                'muot_id -> muot_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "municipio_ordentrabajo", 
                    'audi_uid -> muot_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0
        }
    }

}

