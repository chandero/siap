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

case class Elemento(elem_id: Option[Long],
                    elem_descripcion: Option[String],
                    elem_codigo: Option[String],
                    elem_ucap: Option[Boolean],
                    elem_estado: Option[Int],
                    tiel_id: Option[Long],
                    empr_id: Option[Long],
                    usua_id: Option[Long],
                    ucap_id: Option[Long],
                    caracteristicas: List[ElementoCaracteristica])

case class ElementoD(elem_id: Option[Long],
                    elem_descripcion: Option[String],
                    elem_codigo: Option[String],
                    elem_ucap: Option[Boolean])

object Elemento {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val elementoWrites = new Writes[Elemento] {
    def writes(elemento: Elemento) = Json.obj(
      "elem_id" -> elemento.elem_id,
      "elem_descripcion" -> elemento.elem_descripcion,
      "elem_codigo" -> elemento.elem_codigo,
      "elem_ucap" -> elemento.elem_ucap,
      "elem_estado" -> elemento.elem_estado,
      "tiel_id" -> elemento.tiel_id,
      "empr_id" -> elemento.empr_id,
      "usua_id" -> elemento.usua_id,
      "ucap_id" -> elemento.ucap_id,
      "caracteristicas" -> elemento.caracteristicas
    )
  }

  implicit val elementoReads: Reads[Elemento] = (
    (__ \ "elem_id").readNullable[Long] and
      (__ \ "elem_descripcion").readNullable[String] and
      (__ \ "elem_codigo").readNullable[String] and
      (__ \ "elem_ucap").readNullable[Boolean] and
      (__ \ "elem_estado").readNullable[Int] and
      (__ \ "tiel_id").readNullable[Long] and
      (__ \ "empr_id").readNullable[Long] and
      (__ \ "usua_id").readNullable[Long] and
      (__ \ "ucap_id").readNullable[Long] and
      (__ \ "caracteristicas").read[List[ElementoCaracteristica]]
  )(Elemento.apply _)
}

object ElementoD {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val elementoWrites = new Writes[ElementoD] {
    def writes(elemento: ElementoD) = Json.obj(
      "elem_id" -> elemento.elem_id,
      "elem_descripcion" -> elemento.elem_descripcion,
      "elem_codigo" -> elemento.elem_codigo,
      "elem_ucap" -> elemento.elem_ucap
    )
  }

  implicit val elementoReads: Reads[ElementoD] = (
    (__ \ "elem_id").readNullable[Long] and
      (__ \ "elem_descripcion").readNullable[String] and
      (__ \ "elem_codigo").readNullable[String] and
      (__ \ "elem_ucap").readNullable[Boolean]
  )(ElementoD.apply _)

  val _set = {
    get[Option[Long]]("elemento.elem_id") ~
      get[Option[String]]("elemento.elem_descripcion") ~
      get[Option[String]]("elemento.elem_codigo") ~
      get[Option[Boolean]]("elemento.elem_ucap") map {
      case elem_id ~ elem_descripcion ~ elem_codigo ~ elem_ucap =>
        ElementoD(elem_id,
                 elem_descripcion,
                 elem_codigo,
                 elem_ucap)
      }  
    }
}

class ElementoRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Parsear un Elemento desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("elemento.elem_id") ~
      get[Option[String]]("elemento.elem_descripcion") ~
      get[Option[String]]("elemento.elem_codigo") ~
      get[Option[Boolean]]("elemento.elem_ucap") ~
      get[Option[Int]]("elemento.elem_estado") ~
      get[Option[Long]]("elemento.tiel_id") ~
      get[Option[Long]]("elemento.empr_id") ~
      get[Option[Long]]("elemento.usua_id") ~
      get[Option[Long]]("elemento.ucap_id") map {
      case elem_id ~ elem_descripcion ~ elem_codigo ~ elem_ucap ~ elem_estado ~ tiel_id ~ empr_id ~ usua_id ~ ucap_id =>
        Elemento(elem_id,
                 elem_descripcion,
                 elem_codigo,
                 elem_ucap,
                 elem_estado,
                 tiel_id,
                 empr_id,
                 usua_id,
                 ucap_id,
                 null)
    }
  }

  /**
    Parsear un ElementoCaracteristica desde un ResultSet
    */
  private val caracteristicamodel = {
    get[Option[Long]]("elemento_caracteristica.elca_id") ~
      get[String]("elemento_caracteristica.elca_valor") ~
      get[Long]("elemento_caracteristica.cara_id") ~
      get[Long]("elemento_caracteristica.elem_id") ~
      get[Int]("elemento_caracteristica.elca_estado") map {
      case elca_id ~ elca_valor ~ cara_id ~ elem_id ~ elca_estado =>
        ElementoCaracteristica(elca_id, elca_valor, cara_id, elem_id, elca_estado)
    }
  }  

  /**
    Recuperar un Elemento por su elem_id
    */
  def buscarPorId(elem_id: Long): Option[Elemento] = {
    db.withConnection { implicit connection =>
      var elemen:Option[Elemento]= SQL("SELECT * FROM siap.elemento WHERE elem_id = {elem_id} and elem_estado <> 9")
        .on(
          'elem_id -> elem_id
        )
        .as(simple.singleOpt)

      elemen.map { e => 

        val caracts:List[ElementoCaracteristica] = 
        SQL("""
            SELECT * FROM siap.elemento_caracteristica WHERE elem_id = {elem_id} and elca_estado <> 9
        """).on(
          'elem_id -> elem_id
        ).as(caracteristicamodel *)

        val elemento = e.copy(caracteristicas = caracts)

        elemento
      }
    }
  }

  /**
    Recuperar Elemento por su elem_descripcion
    */
  def buscarPorDescripcion(elem_descripcion: String,
                           empr_id: Long): Future[Iterable[ElementoD]] =
    Future[Iterable[ElementoD]] {
      db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[ElementoD]
        val lista:List[ElementoD] = SQL("SELECT * FROM siap.elemento WHERE elem_descripcion LIKE {elem_descripcion} and empr_id = {empr_id} and elem_estado <> 9")
          .on(
            'elem_descripcion -> elem_descripcion.toUpperCase,
            'empr_id -> empr_id
          )
          .as(ElementoD._set *)
        lista  
      }
    }

  /**
    Recuperar un Elemento por su elem_id
    */
    def buscarPorCodigo(elem_codigo: String): Option[ElementoD] = {
      db.withConnection { implicit connection =>
          var elemen:Option[ElementoD]= SQL("SELECT * FROM siap.elemento WHERE elem_codigo::integer = {elem_codigo} and elem_estado <> 9")
          .on(
            'elem_codigo -> elem_codigo.toInt
          )
          .as(ElementoD._set.singleOpt)
          elemen
      }
    }
    
    
  /**
  * Recuperar total de registros
  * @param empr_id: Long
  * @return total
  */
  def cuenta(filter: String, empr_id: Long): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.elemento WHERE empr_id = {empr_id} and (elem_descripcion like {elem_descripcion} OR elem_codigo like {elem_codigo}) and elem_estado <> 9").
      on(
        'empr_id -> empr_id,
        'elem_descripcion -> (filter + "%"),
        'elem_codigo -> (filter + "%")
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }    

  /**
    Recuperar todos los Elemento de una empresa
    @param empr_id: Long
    */
  def todos(filter: String, page_size:Long, current_page:Long, empr_id: Long): Future[Iterable[Elemento]] =
    Future[Iterable[Elemento]] {
      var lista_result = new ListBuffer[Elemento]
      db.withConnection { implicit connection =>
        val lista:List[Elemento] = SQL("SELECT * FROM siap.elemento WHERE empr_id = {empr_id} and (elem_descripcion like {elem_descripcion} OR elem_codigo like {elem_codigo}) and elem_estado <> 9 ORDER BY elem_descripcion ASC LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)")
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'elem_descripcion -> (filter + "%"),
            'elem_codigo -> (filter + "%")
          )
          .as(simple *)
        for ( e <- lista ) {
          val c = SQL("SELECT * FROM siap.elemento_caracteristica WHERE elem_id = {elem_id} and elca_estado <> 9").
          on(
            'elem_id -> e.elem_id
          ).as(caracteristicamodel *)
          val elemento = e.copy(caracteristicas = c)
          lista_result += elemento
        }
        lista_result.toList
      }
    }


  /**
  * Recuperar todos los Unidad activas
  */
  def elementos(empr_id:Long): Future[Iterable[Elemento]] = Future[Iterable[Elemento]] {
      var lista_result = new ListBuffer[Elemento]
      db.withConnection { implicit connection =>
        val lista:List[Elemento] = SQL("SELECT * FROM siap.elemento WHERE empr_id = {empr_id} and elem_estado <> 9 ORDER BY elem_descripcion")
          .on(
            'empr_id -> empr_id,
          )
          .as(simple *)
        for ( e <- lista ) {
          val c = SQL("SELECT * FROM siap.elemento_caracteristica WHERE elem_id = {elem_id} and elca_estado <> 9").
          on(
            'elem_id -> e.elem_id
          ).as(caracteristicamodel *)
          val elemento = e.copy(caracteristicas = c)
          lista_result += elemento
        }
        lista_result.toList
      }
    }    

  /**
        Crear un Elemento
        @param elemento: Elemento
    */
  def crear(elemento: Elemento): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
      val id: Long = SQL(
        "INSERT INTO siap.elemento (elem_descripcion, elem_codigo, elem_ucap, elem_estado, tiel_id, empr_id, usua_id, ucap_id) VALUES ({elem_descripcion}, {elem_codigo}, {elem_ucap}, {elem_estado}, {tiel_id}, {empr_id}, {usua_id}, {ucap_id})")
        .on(
          'elem_descripcion -> elemento.elem_descripcion,
          'elem_codigo -> elemento.elem_codigo,
          'elem_ucap -> elemento.elem_ucap,
          'elem_estado -> elemento.elem_estado,
          'tiel_id -> elemento.tiel_id,
          'empr_id -> elemento.empr_id,
          'usua_id -> elemento.usua_id,
          'ucap_id -> elemento.ucap_id
        )
        .executeInsert()
        .get

      // Insertar elemento caracteristica
        for(ec <- elemento.caracteristicas) {
          SQL(
            """INSERT INTO siap.elemento_caracteristica
               (elca_valor, cara_id, elem_id, elca_estado) VALUES 
               ({elca_valor}, {cara_id}, {elem_id}, {elca_estado})
            """
            ).on(
              'elca_valor -> ec.elca_valor,
              'cara_id -> ec.cara_id,
              'elem_id -> id,
              'elca_estado -> ec.elca_estado
          ).executeInsert()
        }
      //  

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> elemento.usua_id,
          'audi_tabla -> "elemento",
          'audi_uid -> id,
          'audi_campo -> "elem_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> elemento.elem_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    Actualizar Elemento
    @param elemento: Elemento
    */
  def actualizar(elemento: Elemento): Boolean = {
    val elemento_ant: Option[Elemento] = buscarPorId(elemento.elem_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
      val result: Boolean = SQL(
        "UPDATE siap.elemento SET elem_descripcion = {elem_descripcion}, elem_codigo = {elem_codigo}, elem_ucap = {elem_ucap}, elem_estado = {elem_estado}, tiel_id = {tiel_id}, empr_id = {empr_id} , usua_id = {usua_id}, ucap_id = {ucap_id} WHERE elem_id = {elem_id}")
        .on(
          'elem_descripcion -> elemento.elem_descripcion,
          'elem_codigo -> elemento.elem_codigo,
          'elem_ucap -> elemento.elem_ucap,
          'elem_estado -> elemento.elem_estado,
          'tiel_id -> elemento.tiel_id,
          'empr_id -> elemento.empr_id,
          'usua_id -> elemento.usua_id,
          'ucap_id -> elemento.ucap_id,
          'elem_id -> elemento.elem_id
        )
        .executeUpdate() > 0

      if (result) {
        // Insertar elemento caracteristica
        SQL("UPDATE siap.elemento_caracteristica SET elca_estado = 9 WHERE elem_id = {elem_id}").
        on(
          'elem_id -> elemento.elem_id
        ).executeUpdate()

          for(ec <- elemento.caracteristicas) {
                SQL(
                """INSERT INTO siap.elemento_caracteristica
                   (elca_valor, cara_id, elem_id, elca_estado) VALUES 
                   ({elca_valor}, {cara_id}, {elem_id}, {elca_estado})
                """
                ).on(
                  'elca_valor -> ec.elca_valor,
                  'cara_id -> ec.cara_id,
                  'elem_id -> ec.elem_id,
                  'elca_estado -> ec.elca_estado
              ).executeInsert()
          } 
          //
        }

      if (elemento_ant != None) {
        if (elemento_ant.get.elem_descripcion != elemento.elem_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "elem_descripcion",
              'audi_valorantiguo -> elemento_ant.get.elem_descripcion,
              'audi_valornuevo -> elemento.elem_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (elemento_ant.get.elem_codigo != elemento.elem_codigo) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "elem_codigo",
              'audi_valorantiguo -> elemento_ant.get.elem_codigo,
              'audi_valornuevo -> elemento.elem_codigo,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (elemento_ant.get.elem_ucap != elemento.elem_ucap) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "elem_ucap",
              'audi_valorantiguo -> elemento_ant.get.elem_ucap,
              'audi_valornuevo -> elemento.elem_ucap,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (elemento_ant.get.elem_estado != elemento.elem_estado) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "elem_estado",
              'audi_valorantiguo -> elemento_ant.get.elem_estado,
              'audi_valornuevo -> elemento.elem_estado,
              'audi_evento -> "A"
            )
            .executeInsert()
        }  
        if (elemento_ant.get.tiel_id != elemento.tiel_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "tiel_id",
              'audi_valorantiguo -> elemento_ant.get.tiel_id,
              'audi_valornuevo -> elemento.tiel_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (elemento_ant.get.empr_id != elemento.empr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "empr_id",
              'audi_valorantiguo -> elemento_ant.get.empr_id,
              'audi_valornuevo -> elemento.empr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (elemento_ant.get.usua_id != elemento.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.elem_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> elemento_ant.get.usua_id,
              'audi_valornuevo -> elemento.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (elemento_ant.get.ucap_id != elemento.ucap_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> elemento.usua_id,
              'audi_tabla -> "elemento",
              'audi_uid -> elemento.ucap_id,
              'audi_campo -> "ucap_id",
              'audi_valorantiguo -> elemento_ant.get.ucap_id,
              'audi_valornuevo -> elemento.ucap_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }        
      }

      result
    }

  }

  /**
    Eliminar elemento
    @param elemento: Elemento
   */
   def borrar(elem_id: Long, usua_id: Long): Boolean = {
      db.withConnection { implicit connection => 
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        


      val count: Long =
        SQL("UPDATE siap.elemento SET elem_estado = 9 WHERE elem_id = {elem_id}").
        on(
            'elem_id -> elem_id
        ).executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "elemento",
          'audi_uid -> elem_id,
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
