package models

import javax.inject.Inject
import java.sql.Connection

import anorm._
import anorm.SqlParser.{get, str, scalar}
import anorm.JodaParameterMetaData._

import play.api.db.DBApi

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.ListBuffer

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
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import java.io.ByteArrayOutputStream

case class ManoObraOrden (
  maob_id: Option[Long],
  maob_descripcion: Option[String],
  empr_id : Option[Long],
  maob_codigo: Option[Int]
)

case class ManoObraOrdenPrecio (
  maob_id: Option[Long],
  maobpr_anho: Option[Int],
  maobpr_fecha: Option[Long],
  maobpr_precio: Option[Double],
  maobpr_rendimiento: Option[Double],
  maob_codigo: Option[Int],
  empr_id: Option[Long],
  cotr_tipo_obra: Option[Int],
  cotr_tipo_obra_tipo: Option[String]
)

case class ManoHerramientaOrden (
  math_id: Option[Long],
  math_descripcion: Option[String],
  math_codigo: Option[Int],
  empr_id : Option[Long]
)

case class ManoHerramientaOrdenPrecio (
  math_id: Option[Long],
  mathpr_anho: Option[Int],
  mathpr_fecha: Option[Long],
  mathpr_precio: Option[Double],
  mathpr_es_porcentaje: Option[Boolean],
  mathpr_rendimiento: Option[Double],
  mathpr_cantidad: Option[Int],
  math_codigo: Option[Int],
  empr_id: Option[Long],
  cotr_tipo_obra: Option[Int],
  cotr_tipo_obra_tipo: Option[String]
)

object ManoObraOrden {
    val _set = {
        get[Option[Long]]("maob_id") ~
        get[Option[String]]("maob_descripcion") ~
        get[Option[Long]]("empr_id") ~
        get[Option[Int]]("maob_codigo") map {
            case maob_id ~ maob_descripcion ~ empr_id ~ maob_codigo => ManoObraOrden(maob_id, maob_descripcion, empr_id, maob_codigo)
        }
    }
}

object ManoObraOrdenPrecio {
    val _set = {
        get[Option[Long]]("maob_id") ~
        get[Option[Int]]("maobpr_anho") ~
        get[Option[Long]]("maobpr_fecha") ~
        get[Option[Double]]("maobpr_precio") ~
        get[Option[Double]]("maobpr_rendimiento") ~
        get[Option[Int]]("maob_codigo") ~
        get[Option[Long]]("empr_id") ~
        get[Option[Int]]("cotr_tipo_obra") ~
        get[Option[String]]("cotr_tipo_obra_tipo") map {
            case maob_id ~ maobpr_anho ~ maobpr_fecha ~ maobpr_precio ~ maobpr_rendimiento ~ maob_codigo ~ empr_id ~ cotr_tipo_obra ~ cotr_tipo_obra_tipo => ManoObraOrdenPrecio(maob_id, maobpr_anho, maobpr_fecha, maobpr_precio, maobpr_rendimiento, maob_codigo, empr_id, cotr_tipo_obra, cotr_tipo_obra_tipo)
        }
    }
}

object ManoHerramientaOrden {
  val _set = {
    get[Option[Long]]("math_id") ~
    get[Option[String]]("math_descripcion") ~
    get[Option[Int]]("math_codigo") ~
    get[Option[Long]]("empr_id") map {
      case math_id ~ math_descripcion ~ math_codigo ~ empr_id => ManoHerramientaOrden(math_id, math_descripcion, math_codigo, empr_id)
    }
  }
}

object ManoHerramientaOrdenPrecio {
  val _set = {
    get[Option[Long]]("math_id") ~
    get[Option[Int]]("mathpr_anho") ~
    get[Option[Long]]("mathpr_fecha") ~
    get[Option[Double]]("mathpr_precio") ~
    get[Option[Boolean]]("mathpr_es_porcentaje") ~
    get[Option[Double]]("mathpr_rendimiento") ~
    get[Option[Int]]("mathpr_cantidad") ~
    get[Option[Int]]("math_codigo") ~
    get[Option[Long]]("empr_id") ~
    get[Option[Int]]("cotr_tipo_obra") ~
    get[Option[String]]("cotr_tipo_obra_tipo") map {
      case math_id ~ mathpr_anho ~ mathpr_fecha ~ mathpr_precio ~ mathpr_es_porcentaje ~ mathpr_rendimiento ~ mathpr_cantidad ~ math_codigo ~ empr_id ~ cotr_tipo_obra ~ cotr_tipo_obra_tipo => ManoHerramientaOrdenPrecio(math_id, mathpr_anho, mathpr_fecha, mathpr_precio, mathpr_es_porcentaje, mathpr_rendimiento, mathpr_cantidad, math_codigo, empr_id, cotr_tipo_obra, cotr_tipo_obra_tipo)
    }
  }
}

class ManoHerramientaRepository @Inject()(
    dbapi: DBApi,
    empresaService: EmpresaRepository,
    usuarioService: UsuarioRepository
)(
    implicit ec: DatabaseExecutionContext
){
  private val db = dbapi.database("default")

  // Mano Obra

  def getManoObraOrden(empr_id: Long) = {
    db.withConnection { implicit connection =>
      val _query = """SELECT * FROM siap.mano_obra mo WHERE mo.empr_id = {empr_id} order by mo.maob_id"""
      val _result = SQL(_query).on('empr_id -> empr_id).as(ManoObraOrden._set *)
      _result.toList
    }
  }

  def guardarManoObraOrden(mano_obra: ManoObraOrden): Future[Boolean] = Future {
    val result = db.withConnection { implicit connection =>
      val _queryActualizar = """UPDATE siap.mano_obra SET maob_descripcion = {maob_descripcion}, maob_codigo = {maob_codigo} WHERE maob_id = {maob_id}"""
      val _queryInsertar = """INSERT INTO siap.mano_obra (maob_descripcion, empr_id, maob_codigo) VALUES ({maob_descripcion}, {empr_id}, {maob_codigo})"""
      val _esActualizado = SQL(_queryActualizar).on(
        'maob_descripcion -> mano_obra.maob_descripcion,
        'maob_codigo -> mano_obra.maob_codigo,
        'maob_id -> mano_obra.maob_id
      ).executeUpdate() > 0
      val _esInsertado = if (!_esActualizado) {
        SQL(_queryInsertar).on(
          'maob_descripcion -> mano_obra.maob_descripcion,
          'empr_id -> mano_obra.empr_id,
          'maob_codigo -> mano_obra.maob_codigo
        ).executeUpdate() > 0
      } else { false }
      _esInsertado || _esActualizado
    }
    result
  }

  def guardarManoObraOrdenPrecio(mano_obra_precio: ManoObraOrdenPrecio): Future[Boolean] = Future {
    val result = db.withConnection { implicit connection =>
      val _queryActualizar = """UPDATE siap.mano_obra_precio SET maobpr_precio = {maobpr_precio}, maobpr_rendimiento = {maobpr_rendimiento} WHERE maob_id = {maob_id} AND maobpr_anho = {maobpr_anho} and empr_id = {empr_id} and cotr_tipo_obra = {cotr_tipo_obra} AND cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}"""
      val _queryInsertar = """INSERT INTO siap.mano_obra_precio (maob_id, maobpr_anho, maobpr_fecha, maob_codigo, maobpr_precio, maobpr_rendimiento, cotr_tipo_obra, cotr_tipo_obra_tipo, empr_id) VALUES ({maob_id}, {maobpr_anho}, {maobpr_fecha}, {maob_codigo}, {maobpr_precio}, {maobpr_rendimiento}, {cotr_tipo_obra}, {cotr_tipo_obra_tipo}, {empr_id})"""
      val _fecha = mano_obra_precio.maobpr_fecha match { case Some(fecha) => new DateTime(fecha); case None => new DateTime()}
      val _esActualizado = SQL(_queryActualizar).on(
        'maob_id -> mano_obra_precio.maob_id,
        'maobpr_anho -> mano_obra_precio.maobpr_anho,
        'maobpr_fecha -> _fecha,
        'maob_codigo -> mano_obra_precio.maob_codigo,
        'maobpr_precio -> mano_obra_precio.maobpr_precio,
        'maobpr_rendimiento -> mano_obra_precio.maobpr_rendimiento,
        'cotr_tipo_obra -> mano_obra_precio.cotr_tipo_obra,
        'cotr_tipo_obra_tipo -> mano_obra_precio.cotr_tipo_obra_tipo,
        'empr_id -> mano_obra_precio.empr_id
      ).executeUpdate() > 0
      val _esInsertado = if (!_esActualizado) {
        SQL(_queryInsertar).on(
          'maob_id -> mano_obra_precio.maob_id,
          'maobpr_anho -> mano_obra_precio.maobpr_anho,
          'maobpr_fecha -> _fecha,
          'maob_codigo -> mano_obra_precio.maob_codigo,
          'maobpr_precio -> mano_obra_precio.maobpr_precio,
          'maobpr_rendimiento -> mano_obra_precio.maobpr_rendimiento,
          'cotr_tipo_obra -> mano_obra_precio.cotr_tipo_obra,
          'cotr_tipo_obra_tipo -> mano_obra_precio.cotr_tipo_obra_tipo,
          'empr_id -> mano_obra_precio.empr_id
        ).executeUpdate() > 0
      } else { false }
      _esInsertado || _esActualizado
    }
    result
  }

  def cuentaManoObraOrdenPrecio(empr_id: Long, filter: String, anho: Int): Long = {
    db.withConnection { implicit connection =>
      var query =
        """select * from (select COUNT(*) AS cuenta from siap.mano_obra mo
        left join siap.mano_obra_precio mop on mop.maob_id = mo.maob_id and mop.maobpr_anho = {anho}
        where mo.empr_id = {empr_id}) as o"""
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

  def todosManoObraOrdenPrecio(
      empr_id: Long,
      page_size: Long,
      current_page: Long,
      orderby: String,
      filter: String,
      anho: Int): Future[List[
    (
        Option[Long],
        Option[String],
        Option[Int],
        Option[Int],
        Option[Long],
        Option[Double],
        Option[Double],
        Option[Int],
        Option[String],
        Option[Int],
        Option[Double]
    )
  ]] = Future {
    val _parser = get[Option[Long]]("maob_id") ~
                  get[Option[String]]("maob_descripcion") ~
                  get[Option[Int]]("maob_codigo") ~
                  get[Option[Int]]("maobpr_anho") ~
                  get[Option[Long]]("maobpr_fecha") ~
                  get[Option[Double]]("maobpr_precio") ~
                  get[Option[Double]]("maobpr_rendimiento") ~
                  get[Option[Int]]("cotr_tipo_obra") ~
                  get[Option[String]]("cotr_tipo_obra_tipo") ~ 
                  get[Option[Int]]("maobpr_anho_anterior") ~
                  get[Option[Double]]("maobpr_precio_anterior") map {
                  case 
                    maob_id ~ 
                    maob_descripcion ~ 
                    maob_codigo ~ 
                    maobpr_anho ~ 
                    maobpr_fecha ~ 
                    maobpr_precio ~ 
                    maobpr_rendimiento ~ 
                    cotr_tipo_obra ~ 
                    cotr_tipo_obra_tipo ~ 
                    maobpr_anho_anterior ~ 
                    maobpr_precio_anterior => (
                        maob_id,
                        maob_descripcion,
                        maob_codigo,
                        maobpr_anho,
                        maobpr_fecha,
                        maobpr_precio,
                        maobpr_rendimiento,
                        cotr_tipo_obra,
                        cotr_tipo_obra_tipo,
                        maobpr_anho_anterior,
                        maobpr_precio_anterior
                    )
                  }

      var query = """select o.* from (
select mo.maob_id, mo.maob_descripcion, mo.maob_codigo,
mop.maobpr_anho,
mop.maobpr_fecha,
mop.maobpr_rendimiento,
mop.cotr_tipo_obra,
mop.cotr_tipo_obra_tipo,
mop.maobpr_precio,
{anho_anterior} as maobpr_anho_anterior,
(select mop2.maobpr_precio from siap.mano_obra_precio mop2 where mop2.maob_id = mo.maob_id and mop2.cotr_tipo_obra = mop.cotr_tipo_obra and mop2.cotr_tipo_obra_tipo = mop.cotr_tipo_obra_tipo and mop2.maobpr_anho = {anho_anterior}) as maobpr_precio_anterior
from siap.mano_obra mo
        left join siap.mano_obra_precio mop on mop.maob_id = mo.maob_id and mop.maobpr_anho = {anho}
        where mo.empr_id = {empr_id}) AS o """
      if (!filter.isEmpty) {
          query = query + " where " + filter
        }
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY o.maob_id"
        }
        query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        val lista = db.withConnection { implicit connection => 
          SQL(query).on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'elem_descripcion -> (filter + "%"),
            'elem_codigo -> (filter + "%"),
            'anho -> anho,
            'anho_anterior -> (anho-1)
          )
          .as(_parser *)
        }
        lista.toList
  }

  def todosManoObraOrdenPrecioXlsx(empr_id: Long, usua_id: Long, anho: Int): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id)
    val usuario = usuarioService.buscarPorId(usua_id)
    val _parser = get[Option[Long]]("maob_id") ~
                  get[Option[String]]("maob_descripcion") ~
                  get[Option[Int]]("maob_codigo") ~
                  get[Option[Int]]("maobpr_anho") ~
                  get[Option[Long]]("maobpr_fecha") ~
                  get[Option[Double]]("maobpr_precio") ~
                  get[Option[Double]]("maobpr_rendimiento") ~
                  get[Option[Int]]("cotr_tipo_obra") ~
                  get[Option[String]]("cotr_tipo_obra_tipo") map {
                  case 
                    maob_id ~ 
                    maob_descripcion ~ 
                    maob_codigo ~ 
                    maobpr_anho ~ 
                    maobpr_fecha ~ 
                    maobpr_precio ~ 
                    maobpr_rendimiento ~ 
                    cotr_tipo_obra ~ 
                    cotr_tipo_obra_tipo => (
                        maob_id,
                        maob_descripcion,
                        maob_codigo,
                        maobpr_anho,
                        maobpr_fecha,
                        maobpr_precio,
                        maobpr_rendimiento,
                        cotr_tipo_obra,
                        cotr_tipo_obra_tipo
                    )
                  }

      var query = """select * from (select * from siap.mano_obra mo
        left join siap.mano_obra_precio mop on mop.maob_id = mo.maob_id and mop.maobpr_anho = {anho}
        where mo.empr_id = {empr_id} ) as o 
        order by o.maob_descripcion"""

      val _resultSet = db.withConnection { implicit connection =>
        SQL(query).on(
          'empr_id -> empr_id,
          'anho -> anho
        ).as(_parser *)
      }
      var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listMerged = new ListBuffer[CellRange]()        
      val df = DateTimeFormat.forPattern("yyyy-MM-dd")
      val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
      val sheet = Sheet(
          name = "Mano Obra",
          rows = {
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa match { case Some(e) => e.empr_descripcion case None => ""})
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Relación de Precios Mano de Obra Año " + anho.toString)
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Fecha del Informe:", dtf.print(DateTime.now()))
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Usuario:", usuario match { case Some(u) => u.usua_nombre +" " + u.usua_apellido case None => ""})            
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Id","Descripcion","Código","Año","Fecha","Precio","Rendimiento", "Tipo Obra", "Tipo Obra Tipo")
            _resultSet.map {
            i =>
              _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  i._1 match { case Some(v) => v.toString case None => "" },
                  i._2 match { case Some(v) => v case None => "" },
                  i._3 match { case Some(v) => v.toString case None => "" },
                  i._4 match { case Some(v) => v.toString case None => "" },
                  i._5 match { case Some(v) => df.print(v) case None => "" },
                  i._6 match { case Some(v) => v case None => 0 },
                  i._7 match { case Some(v) => v case None => 0 },
                  i._8 match { case Some(v) => v.toString case None => "" },
                  i._9 match { case Some(v) => v.toString case None => "" }
                )
          }
          _listRow.toList
        },
        mergedRegions = {
              _listMerged += CellRange((0, 0), (0, 6))
              _listMerged += CellRange((1, 1), (0, 5))
              _listMerged.toList
            },
        columns = {
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 0, width = new Width(30, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 1, width = new Width(30, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 2, width = new Width(40, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 3, width = new Width(15, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 4, width = new Width(40, WidthUnit.Character))                
              _listColumn.toList
            }        
      )
      println("Escribiendo en el Stream")
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      Workbook(sheet).writeToOutputStream(os)
      println("Stream Listo")
      os.toByteArray
  }

  def borrarManoObraOrdenPrecio(mano_obra_precio: ManoObraOrdenPrecio): Future[Boolean] = Future {
    val result = db.withConnection { implicit connection =>
      val _query = """DELETE FROM siap.mano_obra_precio WHERE maob_id = {maob_id} AND maobpr_anho = {maobpr_anho} and empr_id = {empr_id} AND cotr_tipo_obra = {cotr_tipo_obra} and cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}"""
      val _result = SQL(_query).on(
        'maob_id -> mano_obra_precio.maob_id,
        'maobpr_anho -> mano_obra_precio.maobpr_anho,
        'empr_id -> mano_obra_precio.empr_id,
        'cotr_tipo_obra -> mano_obra_precio.cotr_tipo_obra,
        'cotr_tipo_obra_tipo -> mano_obra_precio.cotr_tipo_obra_tipo
      ).executeUpdate()
      if (_result > 0) true else false
    }
    result
  }

  // Mano Herramienta

  def getManoHerramientaOrden(empr_id: Long) = {
    db.withConnection { implicit connection =>
      val _query = """SELECT * FROM siap.mano_transporte_herramienta mth WHERE mth.empr_id = {empr_id} order by mth.math_id"""
      val _result = SQL(_query).on('empr_id -> empr_id).as(ManoHerramientaOrden._set *)
      _result.toList
    }
  }

  def guardarManoHerramientaOrden(mano_herramienta: ManoHerramientaOrden): Future[Boolean] = Future {
    val result = db.withConnection { implicit connection =>
      val _queryActualizar = """UPDATE siap.mano_transporte_herramienta SET math_descripcion = {math_descripcion}, math_codigo = {math_codigo} WHERE math_id = {math_id} AND empr_id = {empr_id}"""
      val _queryInsertar = """INSERT INTO siap.mano_transporte_herramienta (math_descripcion, empr_id) VALUES ({math_descripcion}, {empr_id})"""
      val _esActualizado = SQL(_queryActualizar).on(
        'math_descripcion -> mano_herramienta.math_descripcion,
        'math_codigo -> mano_herramienta.math_id,
        'math_id -> mano_herramienta.math_id,
        'empr_id -> mano_herramienta.empr_id
      ).executeUpdate() > 0
      val _esInsertado = if (!_esActualizado) {
        val _id = SQL(_queryInsertar).on(
        'math_descripcion -> mano_herramienta.math_descripcion,
        'math_id -> mano_herramienta.math_id,
        'empr_id -> mano_herramienta.empr_id
        ).executeInsert()
        _id match {
          case Some(_id) => SQL("""UPDATE siap.mano_transporte_herramienta SET math_codigo = {id} where math_id = {id}""") 
          .on('id -> _id).executeUpdate() > 0
          case None => false
        }
      } else { false }
      _esInsertado || _esActualizado
    }
    result
  }

  def guardarManoHerramientaOrdenPrecio(mano_herramienta_precio: ManoHerramientaOrdenPrecio): Future[Boolean] = Future {
    val result = db.withConnection { implicit connection =>
      val _queryActualizar = """UPDATE siap.mano_transporte_herramienta_precio SET mathpr_cantidad = {mathpr_cantidad}, mathpr_es_porcentaje = {mathpr_es_porcentaje}, mathpr_precio = {mathpr_precio}, mathpr_rendimiento = {mathpr_rendimiento} WHERE math_id = {math_id} AND mathpr_anho = {mathpr_anho} and cotr_tipo_obra = {cotr_tipo_obra} AND cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}"""
      val _queryInsertar = """INSERT INTO siap.mano_transporte_herramienta_precio (math_id, mathpr_anho, mathpr_fecha, math_codigo, mathpr_precio, mathpr_es_porcentaje, mathpr_rendimiento, mathpr_cantidad, empr_id, cotr_tipo_obra, cotr_tipo_obra_tipo) VALUES ({math_id}, {mathpr_anho}, {mathpr_fecha}, {math_codigo}, {mathpr_precio}, {mathpr_es_porcentaje}, {mathpr_rendimiento}, {mathpr_cantidad}, {empr_id}, {cotr_tipo_obra}, {cotr_tipo_obra_tipo})"""
      val _fecha = mano_herramienta_precio.mathpr_fecha match { case Some(fecha) => new DateTime(fecha); case None => new DateTime()}
      val _esActualizado = SQL(_queryActualizar).on(
        'math_id -> mano_herramienta_precio.math_id,
        'mathpr_anho -> mano_herramienta_precio.mathpr_anho,
        'mathpr_fecha -> _fecha,
        'math_codigo -> mano_herramienta_precio.math_codigo,
        'mathpr_precio -> mano_herramienta_precio.mathpr_precio,
        'mathpr_es_porcentaje -> mano_herramienta_precio.mathpr_es_porcentaje,
        'mathpr_cantidad -> mano_herramienta_precio.mathpr_cantidad,
        'mathpr_rendimiento -> mano_herramienta_precio.mathpr_rendimiento,
        'cotr_tipo_obra -> mano_herramienta_precio.cotr_tipo_obra,
        'cotr_tipo_obra_tipo -> mano_herramienta_precio.cotr_tipo_obra_tipo
      ).executeUpdate() > 0
      val _esInsertado = if (!_esActualizado) {
        SQL(_queryInsertar).on(
        'math_id -> mano_herramienta_precio.math_id,
        'mathpr_anho -> mano_herramienta_precio.mathpr_anho,
        'mathpr_fecha -> _fecha,
        'math_codigo -> mano_herramienta_precio.math_codigo,
        'mathpr_precio -> mano_herramienta_precio.mathpr_precio,
        'mathpr_es_porcentaje -> mano_herramienta_precio.mathpr_es_porcentaje,
        'mathpr_cantidad -> mano_herramienta_precio.mathpr_cantidad,
        'mathpr_rendimiento -> mano_herramienta_precio.mathpr_rendimiento,
        'cotr_tipo_obra -> mano_herramienta_precio.cotr_tipo_obra,
        'cotr_tipo_obra_tipo -> mano_herramienta_precio.cotr_tipo_obra_tipo,
        'empr_id -> mano_herramienta_precio.empr_id
        ).executeUpdate() > 0
      } else { false }
      _esInsertado || _esActualizado
    }
    result
  }

  def borrarManoHerramientaOrdenPrecio(mano_herramienta_precio: ManoHerramientaOrdenPrecio): Future[Boolean] = Future {
    val result = db.withConnection { implicit connection =>
      val _query = """DELETE FROM siap.mano_transporte_herramienta_precio WHERE math_id = {math_id} AND mathpr_anho = {mathpr_anho} and empr_id = {empr_id} AND cotr_tipo_obra = {cotr_tipo_obra} AND cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}"""
      val _fecha = mano_herramienta_precio.mathpr_fecha match { case Some(fecha) => new DateTime(fecha); case None => new DateTime()}
      val _esBorrado = SQL(_query).on(
        'math_id -> mano_herramienta_precio.math_id,
        'mathpr_anho -> mano_herramienta_precio.mathpr_anho,
        'empr_id -> mano_herramienta_precio.empr_id,
        'cotr_tipo_obra -> mano_herramienta_precio.cotr_tipo_obra,
        'cotr_tipo_obra_tipo -> mano_herramienta_precio.cotr_tipo_obra_tipo
      ).executeUpdate() > 0
      _esBorrado
    }
    result
  }

  def cuentaManoHerramientaOrdenPrecio(empr_id: Long, filter: String, anho: Int): Long = {
    db.withConnection { implicit connection =>
      var query =
        """select * from (select COUNT(*) AS cuenta from siap.mano_transporte_herramienta mth
        left join siap.mano_transporte_herramienta_precio mthp on mthp.math_id = mth.math_id and mthp.mathpr_anho = {anho}
        where mth.empr_id = {empr_id}) as o"""
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

  def todosManoHerramientaOrdenPrecio(
      empr_id: Long,
      page_size: Long,
      current_page: Long,
      orderby: String,
      filter: String,
      anho: Int): Future[List[
    (
        Option[Long],
        Option[String],
        Option[Int],
        Option[Int],
        Option[Long],
        Option[Double],
        Option[Boolean],
        Option[Double],
        Option[Int],
        Option[Int],
        Option[String],
        Option[Int],
        Option[Double]
    )
  ]] = Future {
    val _parser = get[Option[Long]]("math_id") ~
                  get[Option[String]]("math_descripcion") ~
                  get[Option[Int]]("math_codigo") ~
                  get[Option[Int]]("mathpr_anho") ~
                  get[Option[Long]]("mathpr_fecha") ~
                  get[Option[Double]]("mathpr_precio") ~
                  get[Option[Boolean]]("mathpr_es_porcentaje") ~
                  get[Option[Double]]("mathpr_rendimiento") ~
                  get[Option[Int]]("mathpr_cantidad") ~
                  get[Option[Int]]("cotr_tipo_obra") ~
                  get[Option[String]]("cotr_tipo_obra_tipo") ~ 
                  get[Option[Int]]("mathpr_anho_anterior") ~
                  get[Option[Double]]("mathpr_precio_anterior") map {
                  case 
                    math_id ~ 
                    math_descripcion ~ 
                    math_codigo ~ 
                    mathpr_anho ~ 
                    mathpr_fecha ~ 
                    mathpr_precio ~ 
                    mathpr_es_porcentaje ~
                    mathpr_rendimiento ~ 
                    mathpr_cantidad ~
                    cotr_tipo_obra ~ 
                    cotr_tipo_obra_tipo ~ 
                    mathpr_anho_anterior ~ 
                    mathpr_precio_anterior => (
                        math_id,
                        math_descripcion,
                        math_codigo,
                        mathpr_anho,
                        mathpr_fecha,
                        mathpr_precio,
                        mathpr_es_porcentaje,
                        mathpr_rendimiento,
                        mathpr_cantidad,
                        cotr_tipo_obra,
                        cotr_tipo_obra_tipo,
                        mathpr_anho_anterior,
                        mathpr_precio_anterior
                    )
                  }

      var query = """select o.* from (
select mth.math_id, mth.math_descripcion, mth.math_codigo,
mthp.mathpr_anho,
mthp.mathpr_fecha,
mthp.mathpr_rendimiento,
mthp.mathpr_cantidad,
mthp.cotr_tipo_obra,
mthp.cotr_tipo_obra_tipo,
mthp.mathpr_precio,
mthp.mathpr_es_porcentaje,
{anho_anterior} as mathpr_anho_anterior,
(select mthp2.mathpr_precio from siap.mano_transporte_herramienta_precio mthp2 where mthp2.math_id = mth.math_id and mthp2.cotr_tipo_obra = mthp.cotr_tipo_obra and mthp2.cotr_tipo_obra_tipo = mthp.cotr_tipo_obra_tipo and mthp2.mathpr_anho = {anho_anterior}) as mathpr_precio_anterior
from siap.mano_transporte_herramienta mth
        left join siap.mano_transporte_herramienta_precio mthp on mthp.math_id = mth.math_id and mthp.mathpr_anho = {anho}
        where mth.empr_id = {empr_id}) AS o """
      if (!filter.isEmpty) {
          query = query + " where " + filter
        }
        if (!orderby.isEmpty) {
          query = query + s" ORDER BY $orderby"
        } else {
          query = query + s" ORDER BY o.math_id, o.cotr_tipo_obra, o.cotr_tipo_obra_tipo"
        }
        query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
        val lista = db.withConnection { implicit connection => 
          SQL(query).on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
            'elem_descripcion -> (filter + "%"),
            'elem_codigo -> (filter + "%"),
            'anho -> anho,
            'anho_anterior -> (anho-1)
          )
          .as(_parser *)
        }
        lista.toList
  }

  def todosManoHerramientaOrdenPrecioXlsx(empr_id: Long, usua_id: Long, anho: Int): Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id)
    val usuario = usuarioService.buscarPorId(usua_id)
    val _parser = get[Option[Long]]("math_id") ~
                  get[Option[String]]("math_descripcion") ~
                  get[Option[Int]]("math_codigo") ~
                  get[Option[Int]]("mathpr_anho") ~
                  get[Option[Long]]("mathpr_fecha") ~
                  get[Option[Double]]("mathpr_precio") ~
                  get[Option[Boolean]]("mathpr_es_porcentaje") ~
                  get[Option[Double]]("mathpr_rendimiento") ~
                  get[Option[Int]]("mathpr_cantidad") ~
                  get[Option[Int]]("cotr_tipo_obra") ~
                  get[Option[String]]("cotr_tipo_obra_tipo") map {
                  case 
                    math_id ~ 
                    math_descripcion ~ 
                    math_codigo ~ 
                    mathpr_anho ~ 
                    mathpr_fecha ~ 
                    mathpr_precio ~ 
                    mathpr_es_porcentaje ~
                    mathpr_rendimiento ~ 
                    mathpr_cantidad ~
                    cotr_tipo_obra ~ 
                    cotr_tipo_obra_tipo => (
                        math_id,
                        math_descripcion,
                        math_codigo,
                        mathpr_anho,
                        mathpr_fecha,
                        mathpr_precio,
                        mathpr_es_porcentaje,
                        mathpr_rendimiento,
                        mathpr_cantidad,
                        cotr_tipo_obra,
                        cotr_tipo_obra_tipo
                    )
                  }

      var query = """select * from (select * from siap.mano_transporte_herramienta mth
        left join siap.mano_transporte_herramienta_precio mthp on mthp.math_id = mth.math_id and mthp.mathpr_anho = {anho}
        where mth.empr_id = {empr_id} ) as o 
        order by o.math_descripcion, o.cotr_tipo_obra, o.cotr_tipo_obra_tipo"""

      val _resultSet = db.withConnection { implicit connection =>
        SQL(query).on(
          'empr_id -> empr_id,
          'anho -> anho
        ).as(_parser *)
      }
      var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listMerged = new ListBuffer[CellRange]()        
      val df = DateTimeFormat.forPattern("yyyy-MM-dd")
      val dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
      val sheet = Sheet(
          name = "Mano Transporte Herramienta",
          rows = {
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(empresa match { case Some(e) => e.empr_descripcion case None => ""})
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Relación de Precios Transporte y Herramienta Año " + anho.toString)
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Fecha del Informe:", dtf.print(DateTime.now()))
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Usuario:", usuario match { case Some(u) => u.usua_nombre +" " + u.usua_apellido case None => ""})            
            _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues("Id","Descripcion","Código","Año","Fecha","Precio","Es Porcentaje","Rendimiento", "Cantidad", "Tipo Obra", "Tipo Obra Tipo")
            _resultSet.map {
            i =>
              _listRow += com.norbitltd.spoiwo.model
                .Row()
                .withCellValues(
                  i._1 match { case Some(v) => v.toString case None => "" },
                  i._2 match { case Some(v) => v case None => "" },
                  i._3 match { case Some(v) => v.toString case None => "" },
                  i._4 match { case Some(v) => v.toString case None => "" },
                  i._5 match { case Some(v) => df.print(v) case None => "" },
                  i._6 match { case Some(v) => v case None => 0 },
                  i._7 match { case Some(v) => v case None => false },
                  i._8 match { case Some(v) => v.toString case None => "" },
                  i._9 match { case Some(v) => v.toString case None => "" },
                  i._10 match { case Some(v) => v.toString case None => "" },
                  i._11 match { case Some(v) => v.toString case None => "" }
                )
          }
          _listRow.toList
        },
        mergedRegions = {
              _listMerged += CellRange((0, 0), (0, 6))
              _listMerged += CellRange((1, 1), (0, 5))
              _listMerged.toList
            },
        columns = {
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 0, width = new Width(30, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 1, width = new Width(30, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 2, width = new Width(40, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 3, width = new Width(15, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 4, width = new Width(40, WidthUnit.Character))                
              _listColumn.toList
            }        
      )
      println("Escribiendo en el Stream")
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      Workbook(sheet).writeToOutputStream(os)
      println("Stream Listo")
      os.toByteArray
  }    
}