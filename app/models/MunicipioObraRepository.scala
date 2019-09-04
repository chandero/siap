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

case class MunicipioObra( muob_id: Option[scala.Long],
                          empr_id: Option[scala.Long],
                          muob_consecutivo: Option[Int],
                          muob_descripcion: Option[String],
                          muob_reportetecnico: Option[Int],
                          muob_estado: Option[Int],
                          usua_id: Option[scala.Long],
                          muob_fecharecepcion: Option[DateTime],
                          muob_radicado: Option[String],
                          muob_fechaentrega: Option[DateTime],
                          muob_direccion: Option[String],
                          barr_id: Option[scala.Long]
                        )

object MunicipioObra {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val motWrites = new Writes[MunicipioObra] {
        def writes(mot: MunicipioObra) = Json.obj(
          "muob_id" -> mot.muob_id,
          "empr_id" -> mot.empr_id,
          "muob_consecutivo" -> mot.muob_consecutivo,
          "muob_descripcion" -> mot.muob_descripcion,
          "muob_reportetecnico" -> mot.muob_reportetecnico,
          "muob_estado" -> mot.muob_estado,
          "usua_id" -> mot.usua_id,
          "muob_fecharecepcion" -> mot.muob_fecharecepcion,
          "muob_radicado" -> mot.muob_radicado,
          "muob_fechaentrega" -> mot.muob_fechaentrega,
          "muob_direccion" -> mot.muob_direccion,
          "barr_id" -> mot.barr_id
        )
    }

    implicit val motReads: Reads[MunicipioObra] = (
        (__ \ "muob_id").readNullable[scala.Long] and
          (__ \ "empr_id").readNullable[scala.Long] and
          (__ \ "muob_consecutivo").readNullable[Int] and
          (__ \ "muob_descripcion").readNullable[String] and
          (__ \ "muob_reportetecnico").readNullable[Int] and
          (__ \ "muob_estado").readNullable[Int] and
          (__ \ "usua_id").readNullable[scala.Long] and
          (__ \ "muob_fecharecepcion").readNullable[DateTime] and
          (__ \ "muob_radicado").readNullable[String] and
          (__ \ "muob_fechaentrega").readNullable[DateTime] and
          (__ \ "muob_direccion").readNullable[String] and
          (__ \ "barr_id").readNullable[scala.Long]
     )(MunicipioObra.apply _)

     val set = {
       get[Option[scala.Long]]("muob_id") ~
       get[Option[scala.Long]]("empr_id") ~
       get[Option[Int]]("muob_consecutivo") ~
       get[Option[String]]("muob_descripcion") ~
       get[Option[Int]]("muob_reportetecnico") ~
       get[Option[Int]]("muob_estado") ~
       get[Option[scala.Long]]("usua_id") ~
       get[Option[DateTime]]("muob_fecharecepcion") ~
       get[Option[String]]("muob_radicado") ~
       get[Option[DateTime]]("muob_fechaentrega") ~
       get[Option[String]]("muob_direccion") ~
       get[Option[scala.Long]]("barr_id") map {
         case muob_id ~
              empr_id ~
              muob_consecutivo ~
              muob_descripcion ~
              muob_reportetecnico ~
              muob_estado ~
              usua_id ~
              muob_fecharecepcion ~
              muob_radicado ~
              muob_fechaentrega ~ 
              muob_direccion ~
              barr_id => MunicipioObra(
                muob_id,
                empr_id,
                muob_consecutivo,
                muob_descripcion,
                muob_reportetecnico,
                muob_estado,
                usua_id,
                muob_fecharecepcion,
                muob_radicado,
                muob_fechaentrega,
                muob_direccion,
                barr_id
              )
       }
     }
}

class MunicipioObraRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Recuperar un MunicipioObra por su muob_id
    */
  def buscarPorId(muob_id: scala.Long): Option[MunicipioObra] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.muniobra WHERE muob_id = {muob_id} and muob_estado <> 9")
        .on(
          'muob_id -> muob_id
        )
        .as(MunicipioObra.set.singleOpt)
    }
  }

  /**
    Recuperar un MunicipioObra por su muob_id
    */
  def buscarPorConsecutivo(muob_consecutivo: scala.Long, empr_id: scala.Long): Option[MunicipioObra] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.muniobra WHERE muob_consecutivo = {muob_consecutivo} and empr_id = {empr_id} and muob_estado <> 9")
        .on(
          'muob_consecutivo -> muob_consecutivo,
          'empr_id -> empr_id
        )
        .as(MunicipioObra.set.singleOpt)
    }
  }

    /**
  * Recuperar total de registros
  * @param empr_id: scala.Long
  * @return total
  */
  def cuenta(empr_id: scala.Long): scala.Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.muniobra WHERE empr_id = {empr_id} and muob_estado <> 9").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[scala.Long].single)
      result
    }
  }

  /**
    Recuperar todos los MunicipioObra de una empresa paginado
    @param empr_id: scala.Long
    @param page_size: scala.Long
    @param current_page: scala.Long
    @return Future[Iterable[MunicipioObra]]
    */
  def todos(page_size: scala.Long, current_page: scala.Long, empr_id: scala.Long, orderby: String, filter: String): Future[Iterable[MunicipioObra]] =
    Future[Iterable[MunicipioObra]] {
      db.withConnection { implicit connection =>
        var query: String = """SELECT *
                               FROM siap.muniobra m 
                               WHERE m.empr_id = {empr_id}
                                          and m.muob_estado <> 9 """
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    } else {
                        query = query + s" ORDER BY m.muob_estado ASC, m.muob_fecharecepcion ASC"
                    }
                    query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        SQL(query)                        
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(MunicipioObra.set *)
      }
    }

  /**
    Recuperar todos los MunicipioObra de una empresa paginado
    @param empr_id: scala.Long
    @return Future[Iterable[MunicipioObra]]
    */
  def mots(empr_id: scala.Long): Future[Iterable[MunicipioObra]] =
    Future[Iterable[MunicipioObra]] {
      db.withConnection { implicit connection =>
        var query: String = """SELECT *
                               FROM siap.muniobra m 
                               WHERE m.empr_id = {empr_id}
                                          and m.muob_estado <> 9 """
            query = query + s" ORDER BY m.muob_estado ASC, m.muob_fecharecepcion ASC"
        SQL(query)                        
          .on(
            'empr_id -> empr_id
          )
          .as(MunicipioObra.set *)
      }
    }

  /*
    Crear MunicipioOrderTrabajo
    @param mot: MunicipioObra
    @return id: scala.Long
  */
  def crear(mot: MunicipioObra): Future[(scala.Long, scala.Long)] = Future[(scala.Long, scala.Long)] {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            var consec = SQL("SELECT COUNT(*) FROM siap.muniobra WHERE empr_id = {empr_id}").
                         on(
                           'empr_id -> mot.empr_id
                         ).as(SqlParser.scalar[scala.Long].single)
            consec = consec + 1
            val id:scala.Long = SQL("""INSERT INTO siap.muniobra (
                                                  empr_id, 
                                                  muob_consecutivo, 
                                                  muob_descripcion, 
                                                  muob_reportetecnico, 
                                                  muob_estado, 
                                                  usua_id, 
                                                  muob_fecharecepcion, 
                                                  muob_radicado, 
                                                  muob_fechaentrega,
                                                  muob_direccion,
                                                  barr_id
                                                  )
                                       VALUES (
                                                  {empr_id},
                                                  {muob_consecutivo},
                                                  {muob_descripcion},
                                                  {muob_reportetecnico},
                                                  {muob_estado},
                                                  {usua_id},
                                                  {muob_fecharecepcion},
                                                  {muob_radicado},
                                                  {muob_fechaejecucion},
                                                  {muob_direccion},
                                                  {barr_id}
                                       )
            """).on(
              'empr_id -> mot.empr_id,
              'muob_consecutivo -> consec,
              'muob_descripcion -> mot.muob_descripcion,
              'muob_reportetecnico -> mot.muob_reportetecnico,
              'muob_estado -> mot.muob_estado,
              'usua_id -> mot.usua_id,
              'muob_fecharecepcion -> mot.muob_fecharecepcion,
              'muob_radicado -> mot.muob_radicado,
              'muob_fechaentrega -> mot.muob_fechaentrega,
              'muob_direccion -> mot.muob_direccion,
              'barr_id -> mot.barr_id
            ).executeInsert().get

              SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
              on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra", 
                'audi_uid -> id,
                'audi_campo -> "muob_id",
                'audi_valorantiguo -> "",
                'audi_valornuevo -> id,
                'audi_evento -> "I").
                executeInsert()            
            (id, consec)
        }
  }

  /**
  * Actualizar MunicipioObra
  * @param mot: MunicipioObra
  */
  def actualizar(mot: MunicipioObra) : Boolean = {
      val mot_ant: Option[MunicipioObra] = buscarPorId(mot.muob_id.get)
      db.withConnection { implicit connection => 
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())

        val result: Boolean = SQL("""UPDATE siap.muniobra SET 
                                        muob_descripcion = {muob_descripcion},
                                        muob_reportetecnico = {muob_reportetecnico},
                                        muob_estado = {muob_estado},
                                        usua_id = {usua_id},
                                        muob_fecharecepcion = {muob_fecharecepcion},
                                        muob_radicado = {muob_radicado},
                                        muob_fechaentrega = {muob_fechaentrega},
                                        muob_direccion = {muob_direccion},
                                        barr_id = {barr_id}
                                      WHERE muob_id = {muob_id}
                              """).on(
                                      'muob_descripcion -> mot.muob_descripcion,
                                      'muob_reportetecnico -> mot.muob_reportetecnico,
                                      'muob_estado -> mot.muob_estado,
                                      'usua_id -> mot.usua_id,
                                      'muob_fecharecepcion -> mot.muob_fecharecepcion,
                                      'muob_radicado -> mot.muob_radicado,
                                      'muob_fechaentrega -> mot.muob_fechaentrega,
                                      'muob_direccion -> mot.muob_direccion,
                                      'barr_id -> mot.barr_id,
                                      'muob_id -> mot.muob_id
                              ).executeUpdate() > 0
        if (mot_ant != None) {
          if (mot_ant.get.muob_descripcion != mot.muob_descripcion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_descripcion",
                'audi_valorantiguo -> mot_ant.get.muob_descripcion,
                'audi_valornuevo -> mot.muob_descripcion,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muob_reportetecnico != mot.muob_reportetecnico) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_reportetecnico",
                'audi_valorantiguo -> mot_ant.get.muob_reportetecnico,
                'audi_valornuevo -> mot.muob_reportetecnico,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muob_estado != mot.muob_estado) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_estado",
                'audi_valorantiguo -> mot_ant.get.muob_estado,
                'audi_valornuevo -> mot.muob_estado,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muob_fecharecepcion != mot.muob_fecharecepcion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_fecharecepcion",
                'audi_valorantiguo -> mot_ant.get.muob_fecharecepcion,
                'audi_valornuevo -> mot.muob_fecharecepcion,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muob_radicado != mot.muob_radicado) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_radicado",
                'audi_valorantiguo -> mot_ant.get.muob_radicado,
                'audi_valornuevo -> mot.muob_radicado,
                'audi_evento -> "A"
              ).executeInsert()
          }
          if (mot_ant.get.muob_fechaentrega != mot.muob_fechaentrega) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_fechaentrega",
                'audi_valorantiguo -> mot_ant.get.muob_fechaentrega,
                'audi_valornuevo -> mot.muob_fechaentrega,
                'audi_evento -> "A"
              ).executeInsert()
          }                     
          if (mot_ant.get.muob_direccion != mot.muob_direccion) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "muob_direccion",
                'audi_valorantiguo -> mot_ant.get.muob_direccion,
                'audi_valornuevo -> mot.muob_direccion,
                'audi_evento -> "A"
              ).executeInsert()
          }                     
          if (mot_ant.get.barr_id != mot.barr_id) {
            SQL(
              "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
              .on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> mot.usua_id,
                'audi_tabla -> "muniobra",
                'audi_uid -> mot.muob_id,
                'audi_campo -> "barr_id",
                'audi_valorantiguo -> mot_ant.get.barr_id,
                'audi_valornuevo -> mot.barr_id,
                'audi_evento -> "A"
              ).executeInsert()
          }                     

        }
        result
      }
  }

    /**
    * Eliminar MunicipioObra
    */
    def borrar(muob_id: scala.Long, usua_id: scala.Long) : Boolean = {
        db.withConnection { implicit connection => 
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

            val count:scala.Long = SQL("UPDATE siap.muniobra SET muob_estado = 9 WHERE muob_id = {muob_id}").
            on(
                'muob_id -> muob_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "muniobra", 
                    'audi_uid -> muob_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0
        }
    }

}

