package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

case class ManoObra(maob_id: Option[Long],
                    maob_tipo: Option[Int],
                    maob_descripcion: Option[String],
                    maob_estado: Option[Int],
                    empr_id: Option[Long],
                    usua_id: Option[Long]
                )

case class ManoObra_Precio(maob_id: Option[Long],
                           mopr_anho: Option[Int],
                           mopr_precio: Option[BigDecimal],
                           mopr_unidad: Option[String])

object ManoObra {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val wWrites = new Writes[ManoObra] {
    def writes(e: ManoObra) = Json.obj(
      "maob_id" -> e.maob_id,
      "maob_tipo" -> e.maob_tipo,
      "maob_descripcion" -> e.maob_descripcion,
      "maob_estado" -> e.maob_estado,
      "empr_id" -> e.empr_id,
      "usua_id" -> e.usua_id
    )
  }

  implicit val rReads: Reads[ManoObra] = (
      (__ \ "maob_id").readNullable[Long] and
      (__ \ "maob_tipo").readNullable[Int] and
      (__ \ "maob_descripcion").readNullable[String] and
      (__ \ "maob_estado").readNullable[Int] and
      (__ \ "empr_id").readNullable[Long] and
      (__ \ "usua_id").readNullable[Long]
  )(ManoObra.apply _)

  val _set = {
      get[Option[Long]]("maob_id") ~
      get[Option[Int]]("maob_tipo") ~
      get[Option[String]]("maob_descripcion") ~
      get[Option[Int]]("maob_estado") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") map {
      case maob_id ~ maob_tipo ~ maob_descripcion ~ maob_estado ~ empr_id ~ usua_id =>
        ManoObra(maob_id, maob_tipo, maob_descripcion, maob_estado, empr_id, usua_id)
      }       
  }
}

object ManoObra_Precio {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val wWrites = new Writes[ManoObra_Precio] {
    def writes(e: ManoObra_Precio) = Json.obj(
      "maob_id" -> e.maob_id,
      "mopr_anho" -> e.mopr_anho,
      "mopr_precio" -> e.mopr_precio,
      "mopr_unidad" -> e.mopr_unidad
    )
  }

  implicit val rReads: Reads[ManoObra_Precio] = (
      (__ \ "maob_id").readNullable[Long] and
      (__ \ "mopr_anho").readNullable[Int] and
      (__ \ "mopr_precio").readNullable[BigDecimal] and
      (__ \ "mopr_unidad").readNullable[String]
  )(ManoObra_Precio.apply _)

  val _set = {
      get[Option[Long]]("maob_id") ~
      get[Option[Int]]("mopr_anho") ~
      get[Option[BigDecimal]]("mopr_precio") ~
      get[Option[String]]("mopr_unidad") map {
      case maob_id ~ mopr_anho ~ mopr_precio ~ mopr_unidad =>
        ManoObra_Precio(maob_id,
                 mopr_anho,
                 mopr_precio,
                 mopr_unidad)
      }  
    }

}

class ManoObraRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def buscarPorId(id: Long): Option[ManoObra] = {
    var e:Option[ManoObra] = db.withConnection { implicit connection =>
       SQL("SELECT * FROM siap.manoobra WHERE maob_id = {maob_id} and maob_estado <> 9")
        .on(
          'maob_id -> id
        )
        .as(ManoObra._set.singleOpt)
    }
    e
  }

  def buscarPorDescripcion(maob_descripcion: String,
                           empr_id: Long): Future[Iterable[ManoObra]] =
    Future[Iterable[ManoObra]] {
      db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[ManoObra]
        val lista:List[ManoObra] = SQL("SELECT * FROM siap.manoobra WHERE maob_descripcion LIKE {maob_descripcion} and empr_id = {empr_id} and maob_estado <> 9")
          .on(
            'maob_descripcion -> maob_descripcion.toUpperCase,
            'empr_id -> empr_id
          )
          .as(ManoObra._set *)
        lista  
      }
    }

    
  def cuenta(empr_id: Long, filter: String): Long =  {
    db.withConnection{ implicit connection =>
      var query: String = "SELECT COUNT(*) AS c FROM siap.manoobra m WHERE m.empr_id = {empr_id} and m.maob_estado <> 9"      
      if (!filter.isEmpty){
          println("Filtro: " + filter)
          query = query + " and " + filter
      }      
      val result = SQL(query).
      on(
        'empr_id -> empr_id,
        'maob_descripcion -> (filter + "%")
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }    

  def todos(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[Iterable[ManoObra]] =
    Future[Iterable[ManoObra]] {
      db.withConnection { implicit connection =>
        var query: String = "SELECT * FROM siap.manoobra m WHERE m.empr_id = {empr_id} and m.maob_estado <> 9"
        if (!filter.isEmpty) {
          query = query + " and " + filter
        }
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY m.maob_tipo, m.maob_descripcion"
        }
        query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""  
        println("query: " + query)         
        SQL(query)
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'maob_descripcion -> (filter + "%")
          )
          .as(ManoObra._set *)
      }
    }


  /**
  * Recuperar todos los Unidad activas
  */
  def manoobras(empr_id:Long): Future[Iterable[ManoObra]] = Future[Iterable[ManoObra]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.manoobra WHERE empr_id = {empr_id} and maob_estado <> 9 ORDER BY maob_descripcion")
          .on(
            'empr_id -> empr_id,
          )
          .as(ManoObra._set *)
      }
    }    

  def crear(mo: ManoObra): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
      val id: Long = SQL(
        "INSERT INTO siap.manoobra (maob_tipo, maob_descripcion, maob_estado, empr_id, usua_id) VALUES ({maob_tipo}, {maob_descripcion}, {maob_estado}, {empr_id}, {usua_id})")
        .on(
          'maob_tipo -> mo.maob_tipo,
          'maob_descripcion -> mo.maob_descripcion,
          'maob_estado -> mo.maob_estado,
          'empr_id -> mo.empr_id,
          'usua_id -> mo.usua_id
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> mo.usua_id,
          'audi_tabla -> "manoobra",
          'audi_uid -> id,
          'audi_campo -> "maob_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> mo.maob_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  def actualizar(mo: ManoObra): Boolean = {
    val mo_ant: Option[ManoObra] = buscarPorId(mo.maob_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
      val result: Boolean = SQL(
        "UPDATE siap.manoobra SET maob_tipo = {maob_tipo}, maob_descripcion = {maob_descripcion}, maob_estado = {maob_estado}, empr_id = {empr_id} , usua_id = {usua_id} WHERE maob_id = {maob_id}")
        .on(
          'maob_tipo -> mo.maob_tipo,
          'maob_descripcion -> mo.maob_descripcion,
          'maob_estado -> mo.maob_estado,
          'empr_id -> mo.empr_id,
          'usua_id -> mo.usua_id,
          'maob_id -> mo.maob_id
        )
        .executeUpdate() > 0

      if (mo_ant != None) {
        if (mo_ant.get.maob_descripcion != mo.maob_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> mo.usua_id,
              'audi_tabla -> "manoobra",
              'audi_uid -> mo.maob_id,
              'audi_campo -> "maob_descripcion",
              'audi_valorantiguo -> mo_ant.get.maob_descripcion,
              'audi_valornuevo -> mo.maob_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (mo_ant.get.maob_tipo != mo.maob_tipo) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> mo.usua_id,
              'audi_tabla -> "manoobra",
              'audi_uid -> mo.maob_id,
              'audi_campo -> "maob_tipo",
              'audi_valorantiguo -> mo_ant.get.maob_tipo,
              'audi_valornuevo -> mo.maob_tipo,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (mo_ant.get.maob_estado != mo.maob_estado) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> mo.usua_id,
              'audi_tabla -> "manoobra",
              'audi_uid -> mo.maob_id,
              'audi_campo -> "maob_estado",
              'audi_valorantiguo -> mo_ant.get.maob_estado,
              'audi_valornuevo -> mo.maob_estado,
              'audi_evento -> "A"
            )
            .executeInsert()
        }  

        if (mo_ant.get.empr_id != mo.empr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> mo.usua_id,
              'audi_tabla -> "manoobra",
              'audi_uid -> mo.maob_id,
              'audi_campo -> "empr_id",
              'audi_valorantiguo -> mo_ant.get.empr_id,
              'audi_valornuevo -> mo.empr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (mo_ant.get.usua_id != mo.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> mo.usua_id,
              'audi_tabla -> "manoobra",
              'audi_uid -> mo.maob_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> mo_ant.get.usua_id,
              'audi_valornuevo -> mo.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
     
      }

      result
    }

  }

   def borrar(maob_id: Long, usua_id: Long): Boolean = {
      db.withConnection { implicit connection => 
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        


      val count: Long =
        SQL("UPDATE siap.manoobra SET maob_estado = 9 WHERE maob_id = {maob_id}").
        on(
            'maob_id -> maob_id
        ).executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "manoobra",
          'audi_uid -> maob_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> "",
          'audi_evento -> "E"
        )
        .executeInsert()

      count > 0        
     }  
   }
}
