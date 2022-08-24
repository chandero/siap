package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, long, double}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

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
import Height._
import org.apache.poi.common.usermodel.HyperlinkType

import java.io.ByteArrayOutputStream

case class Elemento_Precio(
    elem_id: Option[Long],
    elpr_anho_anterior: Option[Int],
    elpr_precio_anterior: Option[BigDecimal],
    elpr_incremento: Option[Double],
    elpr_anho: Option[Int],
    elpr_precio: Option[BigDecimal],
    elpr_unidad: Option[String]
)

case class Elemento(
    elem_id: Option[Long],
    elem_descripcion: Option[String],
    elem_codigo: Option[String],
    elem_ucap: Option[Boolean],
    elem_estado: Option[Int],
    tiel_id: Option[Long],
    empr_id: Option[Long],
    usua_id: Option[Long],
    ucap_id: Option[Long],
    caracteristicas: List[ElementoCaracteristica],
    precio: Option[Elemento_Precio],
    unitarios: List[Unitario]
)

case class ElementoD(
    elem_id: Option[Long],
    elem_descripcion: Option[String],
    elem_codigo: Option[String],
    elem_ucap: Option[Boolean],
    unitarios: List[Unitario]
)

object Elemento_Precio {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val wWrites = new Writes[Elemento_Precio] {
    def writes(e: Elemento_Precio) = Json.obj(
      "elem_id" -> e.elem_id,
      "elpr_anho_anterior" -> e.elpr_anho_anterior,
      "elpr_precio_anterior" -> e.elpr_precio_anterior,
      "elpr_incremento" -> e.elpr_incremento,
      "elpr_anho" -> e.elpr_anho,
      "elpr_precio" -> e.elpr_precio,
      "elpr_unidad" -> e.elpr_unidad
    )
  }

  implicit val rReads: Reads[Elemento_Precio] = (
    (__ \ "elem_id").readNullable[Long] and
      (__ \ "elpr_anho_anterior").readNullable[Int] and
      (__ \ "elpr_precio_anterior").readNullable[BigDecimal] and
      (__ \ "elpr_incremento").readNullable[Double] and
      (__ \ "elpr_anho").readNullable[Int] and
      (__ \ "elpr_precio").readNullable[BigDecimal] and
      (__ \ "elpr_unidad").readNullable[String]
  )(Elemento_Precio.apply _)

  val _set = {
    get[Option[Long]]("elem_id") ~
      get[Option[Int]]("elpr_anho_anterior") ~
      get[Option[BigDecimal]]("elpr_precio_anterior") ~
      get[Option[Double]]("elpr_incremento") ~
      get[Option[Int]]("elpr_anho") ~
      get[Option[BigDecimal]]("elpr_precio") ~
      get[Option[String]]("elpr_unidad") map {
      case elem_id ~ elpr_anho_anterior ~ elpr_precio_anterior ~ elpr_incremento ~ elpr_anho ~ elpr_precio ~ elpr_unidad =>
        Elemento_Precio(
          elem_id,
          elpr_anho_anterior,
          elpr_precio_anterior,
          elpr_incremento,
          elpr_anho,
          elpr_precio,
          elpr_unidad
        )
    }
  }
}

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
      "caracteristicas" -> elemento.caracteristicas,
      "precio" -> elemento.precio,
      "unitarios" -> elemento.unitarios
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
      (__ \ "caracteristicas").read[List[ElementoCaracteristica]] and
      (__ \ "precio").readNullable[Elemento_Precio] and
      (__ \ "unitarios").read[List[Unitario]]
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
      "elem_ucap" -> elemento.elem_ucap,
      "unitarios" -> elemento.unitarios
    )
  }

  implicit val elementoReads: Reads[ElementoD] = (
    (__ \ "elem_id").readNullable[Long] and
      (__ \ "elem_descripcion").readNullable[String] and
      (__ \ "elem_codigo").readNullable[String] and
      (__ \ "elem_ucap").readNullable[Boolean] and
      (__ \ "unitarios").read[List[Unitario]]
  )(ElementoD.apply _)

  val _set = {
    get[Option[Long]]("elemento.elem_id") ~
      get[Option[String]]("elemento.elem_descripcion") ~
      get[Option[String]]("elemento.elem_codigo") ~
      get[Option[Boolean]]("elemento.elem_ucap") map {
      case elem_id ~ elem_descripcion ~ elem_codigo ~ elem_ucap =>
        ElementoD(elem_id, elem_descripcion, elem_codigo, elem_ucap, null)
    }
  }
}

class ElementoRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext
) {
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
        Elemento(
          elem_id,
          elem_descripcion,
          elem_codigo,
          elem_ucap,
          elem_estado,
          tiel_id,
          empr_id,
          usua_id,
          ucap_id,
          null,
          None,
          null
        )
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
        ElementoCaracteristica(
          elca_id,
          elca_valor,
          cara_id,
          elem_id,
          elca_estado
        )
    }
  }

  /**
    Recuperar un Elemento por su elem_id
    */
  def buscarPorId(elem_id: Long): Option[Elemento] = {
    db.withConnection { implicit connection =>
      var elemen: Option[Elemento] = SQL(
        "SELECT * FROM siap.elemento e1 WHERE e1.elem_id = {elem_id}"
      ).on(
          'elem_id -> elem_id
        )
        .as(simple.singleOpt)

      val elem = elemen match {
        case Some(e) =>
          val caracts: List[ElementoCaracteristica] =
            SQL("""
                  SELECT * FROM siap.elemento_caracteristica WHERE elem_id = {elem_id} and elca_estado <> 9
                """)
              .on(
                'elem_id -> elem_id
              )
              .as(caracteristicamodel *)

          val anho = new DateTime().getYear()

          var ep = db.withConnection { implicit connection =>
            SQL(
              "SELECT * FROM siap.elemento_precio ep WHERE ep.elpr_anho = {anho} and ep.elem_id = {elem_id}"
            ).on(
                'anho -> anho,
                'elem_id -> elem_id
              )
              .as(Elemento_Precio._set.singleOpt)
          }

          var eu = db.withConnection { implicit connection =>
            SQL(
              """SELECT u1.unit_id, u1.unit_codigo, u1.unit_descripcion, u1.empr_id FROM siap.elemento_unitario eu1
                       INNER JOIN siap.unitario u1 ON u1.unit_id = eu1.unit_id
                       WHERE elem_id = {elem_id}"""
            ).on('elem_id -> elem_id).as(Unitario._set *)
          }

          var nep = ep match {
            case None =>
              new Elemento_Precio(
                None,
                None,
                None,
                None,
                Some(anho),
                None,
                None
              )
            case Some(ep) => ep
          }
          val elemento = e.copy(
            caracteristicas = caracts,
            precio = Some(nep),
            unitarios = eu
          )

          Some(elemento)
        case None => None
      }
      elem
    }
  }

  /**
    Recuperar Elemento por su elem_descripcion
    */
  def buscarPorDescripcion(
      elem_descripcion: String,
      empr_id: Long
  ): Future[Iterable[ElementoD]] =
    Future[Iterable[ElementoD]] {
      db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[ElementoD]
        val _descripcion = s"%${elem_descripcion}"
        println("Descrip: " + _descripcion)
        val lista: List[ElementoD] = SQL(
          "SELECT * FROM siap.elemento WHERE elem_descripcion LIKE {elem_descripcion} and empr_id = {empr_id} and elem_estado <> 9"
        ).on(
            'elem_descripcion -> _descripcion.toUpperCase,
            'empr_id -> empr_id
          )
          .as(ElementoD._set *)
        lista.map { e =>
          var eu = db.withConnection { implicit connection =>
            SQL(
              """SELECT u1.unit_id, u1.unit_codigo, u1.unit_descripcion, u1.empr_id FROM siap.elemento_unitario eu1
                  INNER JOIN siap.unitario u1 ON u1.unit_id = eu1.unit_id
                  WHERE elem_id = {elem_id}"""
            ).on('elem_id -> e.elem_id.get).as(Unitario._set *)
          }
          lista_result += e.copy(unitarios = eu)
        }

        lista_result.toList
      }
    }

  /**
    Recuperar un Elemento por su elem_id
    */
  def buscarPorCodigo(elem_codigo: String): Option[ElementoD] = {
    db.withConnection { implicit connection =>
      var elemen: Option[ElementoD] = SQL(
        "SELECT * FROM siap.elemento WHERE elem_codigo::integer = {elem_codigo} and elem_estado <> 9"
      ).on(
          'elem_codigo -> elem_codigo.toInt
        )
        .as(ElementoD._set.singleOpt)
      elemen match {
        case Some(e) =>
          var eu = db.withConnection { implicit connection =>
            SQL(
              """SELECT u1.unit_id, u1.unit_codigo, u1.unit_descripcion, u1.empr_id FROM siap.elemento_unitario eu1
                  INNER JOIN siap.unitario u1 ON u1.unit_id = eu1.unit_id
                  WHERE elem_id = {elem_id}"""
            ).on('elem_id -> e.elem_id.get).as(Unitario._set *)
          }
          Some(e.copy(unitarios = eu))
        case None => None
      }
    }
  }

  /**
    * Recuperar total de registros
    * @param empr_id: Long
    * @return total
    */
  def cuenta(empr_id: Long, filter: String): Long = {
    db.withConnection { implicit connection =>
      var query =
        "SELECT COUNT(*) AS c FROM siap.elemento e WHERE e.empr_id = {empr_id} and e.elem_estado <> 9 "
      if (!filter.isEmpty) {
        query = query + " and " + filter
      }
      val result = SQL(query)
        .on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    Recuperar todos los Elemento de una empresa
    @param empr_id: Long
    */
  def todos(
      empr_id: Long,
      page_size: Long,
      current_page: Long,
      orderby: String,
      filter: String
  ): Future[Iterable[Elemento]] =
    Future[Iterable[Elemento]] {
      var lista_result = new ListBuffer[Elemento]
      db.withConnection { implicit connection =>
        var query =
          "SELECT * FROM siap.elemento e WHERE empr_id = {empr_id} and elem_estado <> 9"
        if (!filter.isEmpty) {
          query = query + " and " + filter
        }
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY e.elem_descripcion"
        }
        query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        val lista: List[Elemento] = SQL(
          query
        ).on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'elem_descripcion -> (filter + "%"),
            'elem_codigo -> (filter + "%")
          )
          .as(simple *)
        for (e <- lista) {
          val caracts: List[ElementoCaracteristica] =
            SQL("""
                  SELECT * FROM siap.elemento_caracteristica WHERE elem_id = {elem_id} and elca_estado <> 9
                """)
              .on(
                'elem_id -> e.elem_id
              )
              .as(caracteristicamodel *)

          val anho = new DateTime().getYear()

          var ep = db.withConnection { implicit connection =>
            SQL(
              "SELECT * FROM siap.elemento_precio ep WHERE ep.elpr_anho = {anho} and ep.elem_id = {elem_id}"
            ).on(
                'anho -> anho,
                'elem_id -> e.elem_id
              )
              .as(Elemento_Precio._set.singleOpt)
          }
          var nep = ep match {
            case None =>
              new Elemento_Precio(
                None,
                None,
                None,
                None,
                Some(anho),
                None,
                None
              )
            case Some(ep) => ep
          }

          var eu = db.withConnection { implicit connection =>
            SQL(
              """SELECT u1.unit_id, u1.unit_codigo, u1.unit_descripcion, u1.empr_id FROM siap.elemento_unitario eu1
                       INNER JOIN siap.unitario u1 ON u1.unit_id = eu1.unit_id
                       WHERE elem_id = {elem_id}"""
            ).on('elem_id -> e.elem_id).as(Unitario._set *)
          }

          val elemento = e.copy(
            caracteristicas = caracts,
            precio = Some(nep),
            unitarios = eu
          )
          lista_result += elemento
        }
        lista_result.toList
      }
    }

  /**
    * Recuperar todos los Unidad activas
    */
  def elementos(empr_id: Long): Future[Iterable[Elemento]] =
    Future[Iterable[Elemento]] {
      var lista_result = new ListBuffer[Elemento]
      db.withConnection { implicit connection =>
        val lista: List[Elemento] = SQL(
          "SELECT * FROM siap.elemento WHERE empr_id = {empr_id} and elem_estado <> 9 ORDER BY elem_descripcion"
        ).on(
            'empr_id -> empr_id
          )
          .as(simple *)
        for (e <- lista) {
          val caracts: List[ElementoCaracteristica] =
            SQL("""
                  SELECT * FROM siap.elemento_caracteristica WHERE elem_id = {elem_id} and elca_estado <> 9
                """)
              .on(
                'elem_id -> e.elem_id
              )
              .as(caracteristicamodel *)

          val anho = new DateTime().getYear()

          var ep = db.withConnection { implicit connection =>
            SQL(
              """SELECT * FROM siap.elemento_precio ep WHERE ep.elpr_anho = {anho} and ep.elem_id = {elem_id}
                    ORDER BY elpr_fecha DESC LIMIT 1"""
            ).on(
                'anho -> anho,
                'elem_id -> e.elem_id
              )
              .as(Elemento_Precio._set.singleOpt)
          }
          var nep = ep match {
            case None =>
              new Elemento_Precio(
                None,
                None,
                None,
                None,
                Some(anho),
                None,
                None
              )
            case Some(ep) => ep
          }
          var eu = db.withConnection { implicit connection =>
            SQL(
              """SELECT u1.unit_id, u1.unit_codigo, u1.unit_descripcion, u1.empr_id FROM siap.elemento_unitario eu1
                       INNER JOIN siap.unitario u1 ON u1.unit_id = eu1.unit_id
                       WHERE elem_id = {elem_id}"""
            ).on('elem_id -> e.elem_id).as(Unitario._set *)
          }
          val elemento = e.copy(
            caracteristicas = caracts,
            precio = Some(nep),
            unitarios = eu
          )
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
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      val id: Long = SQL(
        "INSERT INTO siap.elemento (elem_descripcion, elem_codigo, elem_ucap, elem_estado, tiel_id, empr_id, usua_id, ucap_id) VALUES ({elem_descripcion}, {elem_codigo}, {elem_ucap}, {elem_estado}, {tiel_id}, {empr_id}, {usua_id}, {ucap_id})"
      ).on(
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
      for (ec <- elemento.caracteristicas) {
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
          )
          .executeInsert()
      }
      //

      for (u <- elemento.unitarios) {
        SQL(
          """INSERT INTO siap.elemento_unitario
               (unit_id, elem_id) VALUES 
               ({unit_id}, {elem_id})
            """
        ).on(
            'unit_id -> u.unit_id,
            'elem_id -> id
          )
          .executeInsert()
      }

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
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
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      val result: Boolean = SQL(
        "UPDATE siap.elemento SET elem_descripcion = {elem_descripcion}, elem_codigo = {elem_codigo}, elem_ucap = {elem_ucap}, elem_estado = {elem_estado}, tiel_id = {tiel_id}, empr_id = {empr_id} , usua_id = {usua_id}, ucap_id = {ucap_id} WHERE elem_id = {elem_id}"
      ).on(
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
        SQL(
          "UPDATE siap.elemento_caracteristica SET elca_estado = 9 WHERE elem_id = {elem_id}"
        ).on(
            'elem_id -> elemento.elem_id
          )
          .executeUpdate()

        for (ec <- elemento.caracteristicas) {
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
            )
            .executeInsert()
        }

        SQL(
          "DELETE FROM siap.elemento_unitario WHERE elem_id = {elem_id}"
        ).on(
            'elem_id -> elemento.elem_id
          )
          .executeUpdate()

        for (u <- elemento.unitarios) {
          SQL(
            """INSERT INTO siap.elemento_unitario
                   (unit_id, elem_id) VALUES 
                   ({unit_id}, {elem_id})
                """
          ).on(
              'unit_id -> u.unit_id,
              'elem_id -> elemento.elem_id
            )
            .executeInsert()
        }
        //
      }

      if (elemento_ant != None) {
        if (elemento_ant.get.elem_descripcion != elemento.elem_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
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
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())

      val count: Long =
        SQL(
          "UPDATE siap.elemento SET elem_estado = (8 + elem_estado) WHERE elem_id = {elem_id}"
        ).on(
            'elem_id -> elem_id
          )
          .executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
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

  def actualizarPrecio(
      elem_id: Long,
      elpr_anho: Int,
      elpr_precio: BigDecimal,
      usua_id: Long
  ): Boolean = {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      var seActualizo: Boolean = false
      var seInserto: Boolean = false

      seActualizo =
        SQL(
          "UPDATE siap.elemento_precio SET elpr_precio = {elpr_precio} WHERE elem_id = {elem_id} and elpr_anho = {elpr_anho}"
        ).on(
            'elem_id -> elem_id,
            'elpr_anho -> elpr_anho,
            'elpr_precio -> elpr_precio
          )
          .executeUpdate() > 0

      if (!seActualizo) {
        seInserto = SQL(
          """INSERT INTO siap.elemento_precio (
                          elem_id,
                          elpr_anho,
                          elpr_precio,
                          elpr_unidad,
                          elpr_fecha,
                          elpr_precio_nuevo
                          ) VALUES ({elem_id}, {elpr_anho}, {elpr_precio}, {elpr_unidad}, {elpr_fecha}, {elpr_precio_nuevo})"""
        ).on(
            'elem_id -> elem_id,
            'elpr_anho -> elpr_anho,
            'elpr_precio -> elpr_precio,
            'elpr_unidad -> "UND",
            'elpr_fecha -> fecha,
            'elpr_precio_nuevo -> elpr_precio            
          )
          .executeUpdate() > 0
      }

      if (seActualizo || seInserto) {
        SQL(
          "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
        ).on(
            'audi_fecha -> fecha,
            'audi_hora -> hora,
            'usua_id -> usua_id,
            'audi_tabla -> "elemento_precio",
            'audi_uid -> elem_id,
            'audi_campo -> "",
            'audi_valorantiguo -> "",
            'audi_valornuevo -> elpr_precio.toString(),
            'audi_evento -> "A"
          )
          .executeInsert()
      }
      seActualizo || seInserto
    }
  }

  def nuevoPrecioAnho(anho: Int, tasa: Double, empr_id: Long) = {
    db.withConnection { implicit connection =>
      val _query =
        """INSERT INTO siap.elemento_precio (elem_id, elpr_anho_anterior, elpr_anho, elpr_precio_anterior, elpr_incremento, elpr_precio, elpr_unidad, elpr_fecha) select ep1.elem_id, ep1.elpr_anho as elpr_anho_anterior, {anho} as elpr_anho, ep1.elpr_precio as elpr_precio_anterior, {tasa} as elpr_incremento, ROUND(ep1.elpr_precio + ep1.elpr_precio * ({tasa}/100)) as elpr_precio, ep1.elpr_unidad, {fecha} as elpr_fecha from siap.elemento_precio ep1
                      WHERE ep1.elpr_anho = {anho_anterior}
                      ORDER BY ep1.elem_id asc"""
      var _fecha = Calendar.getInstance().getTime()
      var _anho_anterior = anho - 1
      val _result = SQL(_query)
        .on(
          'anho -> anho,
          'anho_anterior -> _anho_anterior,
          'tasa -> tasa,
          'fecha -> _fecha
        )
        .executeUpdate > 0
      _result
    }
  }

  def buscarElementoSinPrecio(
      empr_id: Long
  ): Future[List[(Long, String, String, Double)]] =
    Future[List[(Long, String, String, Double)]] {
      db.withConnection { implicit connection =>
        val _parser = (long("elem_id") ~ str("elem_codigo") ~ str(
          "elem_descripcion"
        ) ~ double("elpr_precio")).map {
          case elem_id ~ elem_codigo ~ elem_descripcion ~ elem_precio =>
            (elem_id, elem_codigo, elem_descripcion, elem_precio)
        }
        val _anho = Calendar.getInstance().get(Calendar.YEAR)
        val result = SQL(
          """SELECT distinct e1.elem_id, e1.elem_codigo, e1.elem_descripcion, elpr1.elpr_precio 
           FROM siap.reporte r1
           INNER JOIN siap.reporte_evento re1 ON re1.repo_id = r1.repo_id
           INNER JOIN siap.elemento e1 ON e1.elem_id = re1.elem_id
           LEFT JOIN siap.elemento_precio elpr1 ON elpr1.elem_id = e1.elem_id and elpr1.elpr_anho = {anho}
           WHERE r1.reti_id IN (2,6) AND r1.empr_id = {empr_id} AND 
           		 EXTRACT(YEAR FROM r1.repo_fechasolucion) = {anho} AND
           		 e1.elem_estado = 1 AND
           		 elpr1.elpr_precio IS NULL"""
        ).on(
            'empr_id -> empr_id,
            'anho -> _anho
          )
          .as(_parser *)
        result
      }
    }

  def agregarPrecio(
      elem_id: Long,
      elpr_precio: BigDecimal,
      elpr_unidad: String,
      usua_id: Long
  ): Boolean = {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      val _anho = Calendar.getInstance().get(Calendar.YEAR)

      SQL(
        """INSERT INTO siap.elemento_precio VALUES ({elem_id}, {elpr_anho}, {elpr_precio}, {elpr_unidad}, {elpr_fecha})"""
      ).on(
          'elem_id -> elem_id,
          'elpr_anho -> _anho,
          'elpr_precio -> elpr_precio,
          'elpr_unidad -> elpr_unidad,
          'elpr_fecha -> fecha
        )
        .executeUpdate() > 0

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "elemento_precio",
          'audi_uid -> elem_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> elpr_precio.toString(),
          'audi_evento -> "A"
        )
        .executeInsert()
    }
    true
  }

  /**
    * Recuperar total de registros
    * @param empr_id: Long
    * @return total
    */
  def cuentaPrecio(empr_id: Long, filter: String, anho: Int): Long = {
    db.withConnection { implicit connection =>
      var query =
        """select COUNT(*) AS c from (SELECT DISTINCT e1.elem_id, e1.elem_codigo, e1.elem_descripcion
           FROM siap.reporte r1
           INNER JOIN siap.reporte_evento re1 ON re1.repo_id = r1.repo_id
           INNER JOIN siap.elemento e1 ON e1.elem_id = re1.elem_id
           left join siap.elemento_precio elpr1 on elpr1.elem_id = e1.elem_id
           WHERE r1.reti_id IN (2,6) AND
           		 r1.empr_id = {empr_id} AND
               elpr1.elpr_anho = {anho} AND
           		 e1.elem_estado = 1"""
      query = query + ") as o "
      if (!filter.isEmpty) {
        query = query + " WHERE " + filter
      }      
      val result = SQL(query)
        .on(
          'empr_id -> empr_id,
          'anho -> anho
        )
        .as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    Recuperar todos los Elemento de una empresa
    @param empr_id: Long
    */
  def todosPrecio(
      empr_id: Long,
      page_size: Long,
      current_page: Long,
      orderby: String,
      filter: String,
      anho: Int
  ): Future[List[
    (
        Long,
        String,
        String,
        Option[Int],
        Option[Double],
        Option[Double],
        Option[Double],
        Option[Double],
        Option[Double],
        Option[Double],
        Option[Int],
        Option[DateTime],
        Option[String]
    )
  ]] =
    Future[List[
      (
          Long,
          String,
          String,
          Option[Int],
          Option[Double],
          Option[Double],
          Option[Double],
          Option[Double],
          Option[Double],
          Option[Double],
          Option[Int],
          Option[DateTime],
          Option[String]
      )
    ]] {
      val _parser =
        long("elem_id") ~
          str("elem_codigo") ~
          str("elem_descripcion") ~
          get[Option[Int]]("elpr_anho_anterior") ~
          get[Option[Double]]("elpr_precio_anterior") ~
          get[Option[Double]]("elpr_incremento") ~
          get[Option[Double]]("elpr_diff") ~
          get[Option[Double]]("elpr_precio_nuevo") ~
          get[Option[Double]]("elpr_precio_cotizado") ~
          get[Option[Double]]("elpr_precio") ~
          get[Option[Int]]("elpr_anho") ~
          get[Option[DateTime]]("elpr_fecha") ~
          get[Option[String]]("unit_codigo") map {
          case elem_id ~ elem_codigo ~ elem_descripcion ~ elpr_anho_anterior ~ elpr_precio_anterior ~ elpr_incremento ~ elpr_diff ~ elpr_precio_nuevo ~ elpr_precio_cotizado ~ elpr_precio ~ elpr_anho ~ elpr_fecha ~ unit_codigo =>
            (
              elem_id,
              elem_codigo,
              elem_descripcion,
              elpr_anho_anterior,
              elpr_precio_anterior,
              elpr_incremento,
              elpr_diff,
              elpr_precio_nuevo,
              elpr_precio_cotizado,
              elpr_precio,
              elpr_anho,
              elpr_fecha,
              unit_codigo
            )
        }

      db.withConnection { implicit connection =>
        var query =
          """select distinct 
	o.elem_id, 
	o.elem_codigo, 
	o.elem_descripcion, 
	o.elpr_anho_anterior, 
	o.elpr_precio_anterior, 
	o.elpr_incremento,
	o.elpr_diff,
	o.elpr_anho,
	o.elpr_fecha,
	o.elpr_precio_nuevo,
	o.elpr_precio_cotizado,
	o.elpr_precio,
	string_agg(o.unit_codigo, ',') as unit_codigo
from 
(SELECT  DISTINCT
        e1.elem_id, 
		e1.elem_codigo, 
		e1.elem_descripcion, 
        (select ep1.elpr_anho_anterior from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select ep1.elpr_precio_anterior from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),        
        (select ep1.elpr_incremento from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select (ep1.elpr_precio_nuevo - ep1.elpr_precio_anterior) from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0) as elpr_diff,
        (select ep1.elpr_anho from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
		(select ep1.elpr_fecha from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
		(select ep1.elpr_precio_nuevo from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select ep1.elpr_precio_cotizado from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select ep1.elpr_precio from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        u1.unit_codigo AS unit_codigo
           FROM siap.reporte r1
           INNER JOIN siap.reporte_evento re1 ON re1.repo_id = r1.repo_id
           INNER JOIN siap.elemento e1 ON e1.elem_id = re1.elem_id
           LEFT join siap.elemento_precio elpr1 on elpr1.elem_id = e1.elem_id
           LEFT join siap.elemento_unitario eu1 ON eu1.elem_id = e1.elem_id
           LEFT join siap.unitario u1 ON u1.unit_id = eu1.unit_id
           WHERE r1.reti_id IN (2,6) AND
           		 r1.empr_id = 1 AND
           		 e1.elem_estado = 1
      ) as o
      """
        if (!filter.isEmpty) {
          query = query + " where " + filter
        }
        query = query + s"group by 1,2,3,4,5,6,7,8,9,10,11,12"
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY o.elem_descripcion"
        }
        query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        val lista = SQL(
          query
        ).on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'elem_descripcion -> (filter + "%"),
            'elem_codigo -> (filter + "%"),
            'anho -> anho
          )
          .as(_parser *)
        lista.toList
      }
    }

  def todosXls(empr_id: Long): Array[Byte] = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[CellRange]()
    val _fuenteNegrita = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = false,
      strikeout = false
    )
    val sheet1 = Sheet(
      name = "Elemento",
      rows = {
        _listRow01 += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "Material",
            "Codigo",
            "Descripcion",
            "Fecha",
            "Precio"
          )
        val _lista = db.withConnection {
          implicit connection =>
            val _parser =
              long("elem_id") ~
                str("elem_codigo") ~
                str("elem_descripcion") ~
                get[Option[DateTime]]("elpr_fecha") ~
                get[Option[Double]]("elpr_precio") map {
                case elem_id ~ elem_codigo ~ elem_descripcion ~ elpr_fecha ~ elpr_precio =>
                  (
                    elem_id,
                    elem_codigo,
                    elem_descripcion,
                    elpr_fecha,
                    elpr_precio
                  )
              }
            var query =
              """SELECT 
                            e1.elem_id, 
                            e1.elem_codigo, 
                            e1.elem_descripcion,
                            (select ep1.elpr_fecha from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
                            (select ep1.elpr_precio from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0)
                         FROM siap.elemento e1 WHERE e1.empr_id = {empr_id} and
                       		 e1.elem_estado < 9
                         ORDER BY e1.elem_descripcion"""
            SQL(
              query
            ).on(
                'empr_id -> empr_id
              )
              .as(_parser *)
        }
        _lista.map {
          _m =>
            _listRow01 += com.norbitltd.spoiwo.model.Row(
              NumericCell(
                _m._1,
                Some(0),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._2,
                Some(1),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._3,
                Some(2),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._4 match {
                  case Some(v) => v.toString("yyyy/MM/dd")
                  case None    => ""
                },
                Some(3),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._5 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(4),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
        }
        _listRow01.toList
      }
    )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(sheet1)
      .writeToOutputStream(os)
    println("Stream Listo")
    os.toByteArray
  }

  def todosPrecioXls(empr_id: Long): Array[Byte] = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[CellRange]()
    val _fuenteNegrita = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = false,
      strikeout = false
    )
    val sheet1 = Sheet(
      name = "ElementoPrecio",
      rows = {
        _listRow01 += com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "Material",
            "Codigo",
            "Descripcion",
            "Año Anterior",
            "Precio Anterior",
            "IPC Año",
            "Incremento",
            "Precio Actual",
            "Precio Cotizado",
            "Precio Más Favorable",
            "Año Actual",
            "Fecha Aplicación",
            "Unitario"
          )
        val _lista = db.withConnection {
          implicit connection =>
            val _parser =
              long("elem_id") ~
                str("elem_codigo") ~
                str("elem_descripcion") ~
                get[Option[Int]]("elpr_anho_anterior") ~
                get[Option[Double]]("elpr_precio_anterior") ~
                get[Option[Double]]("elpr_incremento") ~
                get[Option[Double]]("elpr_diff") ~
                get[Option[Double]]("elpr_precio_nuevo") ~
                get[Option[Double]]("elpr_precio_cotizado") ~
                get[Option[Double]]("elpr_precio") ~
                get[Option[Int]]("elpr_anho") ~
                get[Option[DateTime]]("elpr_fecha") ~
                get[Option[String]]("unit_codigo") map {
                case elem_id ~ elem_codigo ~ elem_descripcion ~ elpr_anho_anterior ~ elpr_precio_anterior ~ elpr_incremento ~ elpr_diff ~ elpr_precio_nuevo ~ elpr_precio_cotizado ~ elpr_precio ~ elpr_anho ~ elpr_fecha ~ unit_codigo =>
                  (
                    elem_id,
                    elem_codigo,
                    elem_descripcion,
                    elpr_anho_anterior,
                    elpr_precio_anterior,
                    elpr_incremento,
                    elpr_diff,
                    elpr_precio_nuevo,
                    elpr_precio_cotizado,
                    elpr_precio,
                    elpr_anho,
                    elpr_fecha,
                    unit_codigo
                  )
              }
            var query =
              """select distinct 
	o.elem_id, 
	o.elem_codigo, 
	o.elem_descripcion, 
	o.elpr_anho_anterior, 
	o.elpr_precio_anterior, 
	o.elpr_incremento,
	o.elpr_diff,
	o.elpr_anho,
	o.elpr_fecha,
	o.elpr_precio_nuevo,
	o.elpr_precio_cotizado,
	o.elpr_precio,
	string_agg(o.unit_codigo, ',') as unit_codigo
from 
(SELECT  DISTINCT
        e1.elem_id, 
		e1.elem_codigo, 
		e1.elem_descripcion, 
        (select ep1.elpr_anho_anterior from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select ep1.elpr_precio_anterior from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),        
        (select ep1.elpr_incremento from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select (ep1.elpr_precio_nuevo - ep1.elpr_precio_anterior) from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0) as elpr_diff,
        (select ep1.elpr_anho from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
		(select ep1.elpr_fecha from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
		(select ep1.elpr_precio_nuevo from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select ep1.elpr_precio_cotizado from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        (select ep1.elpr_precio from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id order by ep1.elpr_fecha desc limit 1 offset 0),
        u1.unit_codigo AS unit_codigo
           FROM siap.reporte r1
           INNER JOIN siap.reporte_evento re1 ON re1.repo_id = r1.repo_id
           INNER JOIN siap.elemento e1 ON e1.elem_id = re1.elem_id
           LEFT join siap.elemento_precio elpr1 on elpr1.elem_id = e1.elem_id
           LEFT join siap.elemento_unitario eu1 ON eu1.elem_id = e1.elem_id
           LEFT join siap.unitario u1 ON u1.unit_id = eu1.unit_id
           WHERE r1.reti_id IN (2,6) AND
           		 r1.empr_id = {empr_id} AND
           		 e1.elem_estado = 1
        ) as o
        group by 1,2,3,4,5,6,7,8,9,10,11,12"""
            SQL(
              query
            ).on(
                'empr_id -> empr_id
              )
              .as(_parser *)
        }
        _lista.map {
          _m =>
            _listRow01 += com.norbitltd.spoiwo.model.Row(
              NumericCell(
                _m._1,
                Some(0),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._2,
                Some(1),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._3,
                Some(2),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(_m._4 match {
                case Some(v) => v
                case None    => 0
              }, Some(3), Some(CellStyle(dataFormat = CellDataFormat("###0"))), CellStyleInheritance.CellThenRowThenColumnThenSheet),
              NumericCell(
                _m._5 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(4),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._6 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(5),
                Some(CellStyle(dataFormat = CellDataFormat("#0.00"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._7 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(6),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(_m._8 match {
                case Some(v) => v
                case None    => 0
              }, Some(7), Some(CellStyle(dataFormat = CellDataFormat("#,##0"))), CellStyleInheritance.CellThenRowThenColumnThenSheet),
              NumericCell(
                _m._9 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(8),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._10 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(9),
                Some(CellStyle(dataFormat = CellDataFormat("#,##0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._11 match {
                  case Some(v) => v
                  case None    => 0d
                },
                Some(10),
                Some(CellStyle(dataFormat = CellDataFormat("###0"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._12 match {
                  case Some(v) => v.toString("yyyy/MM/dd")
                  case None    => ""
                },
                Some(11),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _m._13 match {
                  case Some(v) => v
                  case None    => ""
                },
                Some(12),
                Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
        }
        _listRow01.toList
      }
    )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(sheet1)
      .writeToOutputStream(os)
    println("Stream Listo")
    os.toByteArray
  }
}
