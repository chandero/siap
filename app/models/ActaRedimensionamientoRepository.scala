package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream, FileInputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.util.UUID.randomUUID

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, double, date, bool, scalar, flatten}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.immutable.List
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

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

import utilities.DataUtil
import utilities.Utility
import utilities.N2T

case class ActaRedimensionamiento(
    acre_id: Option[Long],
    acre_numero: Option[Int],
    acre_anho: Option[Int],
    acre_periodo: Option[Int],
    acre_fecha: Option[LocalDate],
    acre_valor_acumulado: Option[Double],
    empr_id: Option[Long],
    usua_id: Option[Long],
    acre_estado: Option[Int]
)

object ActaRedimensionamiento {
    val _set = {
        get[Option[Long]]("acre_id") ~
        get[Option[Int]]("acre_numero") ~
        get[Option[Int]]("acre_anho") ~
        get[Option[Int]]("acre_periodo") ~
        get[Option[LocalDate]]("acre_fecha") ~
        get[Option[Double]]("acre_valor_acumulado") ~
        get[Option[Long]]("empr_id") ~
        get[Option[Long]]("usua_id") ~ 
        get[Option[Int]]("acre_estado")  map {
            case acre_id ~ acre_numero ~ acre_anho ~ acre_periodo ~ acre_fecha ~ acre_valor_acumulado ~ empr_id ~ usua_id ~ acre_estado =>
            ActaRedimensionamiento(acre_id, acre_numero, acre_anho, acre_periodo, acre_fecha, acre_valor_acumulado, empr_id, usua_id, acre_estado)
        }
    }
}

class ActaRedimensionamientoRepository @Inject()(
    dbapi: DBApi,
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository,
    dataUtil: DataUtil,
    cobroService: Cobro6Repository
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * Recuperar total de registros
    * @return total
    */
  def cuenta(empr_id: Long, filter: String): Long = {
    db.withConnection { implicit connection =>
      var query: String =
        """
        SELECT COUNT(*) AS c FROM siap.acta_redimensionamiento ar1
        WHERE ar1.empr_id = {empr_id}
      """
      if (!filter.isEmpty) {
        println("Filtro: " + filter)
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

  def getActasRedimensionamiento(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[List[ActaRedimensionamiento]] = Future[List[ActaRedimensionamiento]] {
    db.withConnection { implicit connection =>
      var query: String = "SELECT * FROM siap.acta_redimensionamiento WHERE empr_id = {empr_id}"
      var curr_page: Long = current_page
      if (!filter.isEmpty) {
        query = query + " and " + filter
        curr_page = 1
      }
      if (!orderby.isEmpty) {
        query = query + s" ORDER BY $orderby"
      } else {
        query = query + s" ORDER BY acre_id asc"
      }
      query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
      val _lista = SQL(query)
        .on(
          'empr_id -> empr_id,
          'page_size -> page_size,
          'current_page -> curr_page
        )
        .as(ActaRedimensionamiento._set *)
        _lista.toList
    }
  }

  def buscarPorEmpresaFacturasAnhoPeriodo(empr_id: Long, anho: Int, periodo: Int) = {
    db.withConnection { implicit connection =>

      var query = """select cotr1.* from siap.cobro_factura cf 
                      inner join siap.cobro_factura_orden_trabajo cfot  on cfot.cofa_id = cf.cofa_id
                      inner join siap.cobro_orden_trabajo cotr1 on cotr1.cotr_id = cfot.cotr_id 
                      where cf.empr_id = {empr_id} and cf.cofa_anho = {cofa_anho} and cf.cofa_periodo = {cofa_periodo} and cofa_estado = 1 
                      order by cotr1.cotr_consecutivo"""
      SQL(query)
        .on(
          'empr_id -> empr_id,
          'cofa_anho -> anho,
          'cofa_periodo -> periodo
        )
        .as(orden_trabajo_cobro._set *)
    }
  }  

  def getActaRedimensionamiento(empr_id: Long, anho: Int, periodo: Int, usua_id: Long): (Int, Double, Double, Double, Double, Double /*, Iterable[(String, String, String, String, String)]*/) = {
    //var _listData = new ListBuffer[(String, String, String, String, String)]
    val empresa = empresaService.buscarPorId(empr_id) match {
      case Some(e) => e
      case None => Empresa(None, "", "", "", "", "", None, 0, 0, 0, 0, None, None)
    }
    var _subtotal_expansion = 0.0
    var _subtotal_modernizacion = 0.0
    var _subtotal_desmonte = 0.0
    var _subtotal_total = 0.0
    // validar si existe el acta
    var _existe_acta = false
    var _numero_acta = 0
    val _periodo = Calendar.getInstance()
    _periodo.set(Calendar.YEAR, anho)
    _periodo.set(Calendar.MONTH, (periodo-1))
    _periodo.set(Calendar.DAY_OF_MONTH, 1)
    _periodo.set(Calendar.DATE, _periodo.getActualMaximum(Calendar.DATE))

    var _fecha_corte = _periodo.clone().asInstanceOf[Calendar]
    println("Acta Red Fecha Corte:" + _fecha_corte.getTime())
    var _fecha_corte_anterior = _fecha_corte.clone().asInstanceOf[Calendar]
    _fecha_corte_anterior.add(Calendar.MONTH, -1)    

    val ordenes =  buscarPorEmpresaFacturasAnhoPeriodo(empresa.empr_id.get, _fecha_corte_anterior.get(Calendar.YEAR), _fecha_corte_anterior.get(Calendar.MONTH) + 1)
    val _valor_acumulado_anterior = db.withTransaction { implicit connection =>
      val _fecha_previo = _fecha_corte_anterior.clone().asInstanceOf[Calendar]
      _fecha_previo.add(Calendar.MONTH, -1)
      val _periodo_previo = _fecha_previo.get(Calendar.MONTH) + 1
      val _anho_previo = _fecha_previo.get(Calendar.YEAR)
      println("anho previo: " + _anho_previo)
      println("periodo previo: " + _periodo_previo)
      val _valorOpt = SQL("""SELECT reco_valor FROM siap.redimensionamiento_control rc1 WHERE rc1.reco_anho = {anho} AND rc1.reco_periodo = {periodo} AND rc1.empr_id = {empr_id}""").
      on(
        'anho -> _anho_previo,
        'periodo -> _periodo_previo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt)
      _valorOpt match {
        case Some(v) => v
        case None => 0.0
      }
    }    
    val _acre = db.withTransaction { implicit connection =>
        val _parseActa = int("acre_id") ~ int("acre_numero") map {
          case acre_id ~ acre_numero => (acre_id, acre_numero)
        }
        SQL("""SELECT ar.acre_id, ar.acre_numero FROM siap.acta_redimensionamiento ar 
                            WHERE ar.acre_anho = {anho} AND ar.acre_periodo = {periodo} AND ar.empr_id = {empr_id}""").
      on(
        'anho -> anho,
        'periodo -> periodo,
        'empr_id -> empr_id
      ).as(_parseActa.singleOpt)
    }
    val acre_id = _acre match {
      case Some(v) =>  _existe_acta = true 
                        _numero_acta = v._2
                        v._1
      case None =>  _existe_acta = false
                    val _numero = dataUtil.getConsecutivo(9)
                    _numero_acta = _numero
                    db.withTransaction { implicit connection => 
                      SQL("""INSERT INTO siap.acta_redimensionamiento(acre_numero, acre_anho, acre_periodo, acre_fecha, empr_id, usua_id, acre_estado)
                            VALUES({numero}, {anho}, {periodo}, {fecha}, {empr_id}, {usua_id}, {acre_estado})""").
                      on(
                        'numero -> _numero,
                        'anho -> anho,
                        'periodo -> periodo,
                        'fecha -> Calendar.getInstance().getTime(),
                        'empr_id -> empr_id,
                        'usua_id -> usua_id,
                        'acre_estado -> 1
                      ).executeInsert().get
                    }

    }
    ordenes.map { orden =>
      var _expansion = orden.cotr_tipo_obra match {
                      case Some(2) => cobroService.siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
      var _modernizacion = orden.cotr_tipo_obra match {
                      case Some(6) => cobroService.siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
      var _desmonteRet = orden.cotr_tipo_obra match {
                      case Some(6) => cobroService.siap_orden_trabajo_cobro_calculo_desmonte(empresa, orden, acre_id, _existe_acta)
                      case _ => (0.0, (new ListBuffer[(String, Double, Double)]).toList)
                    }                                       

      val _desmonte = _desmonteRet._1
      val _total = _expansion + _modernizacion - _desmonte
      _subtotal_expansion += _expansion
      _subtotal_modernizacion += _modernizacion
      _subtotal_desmonte += _desmonte
      _subtotal_total += _total
/*       _listData += 
        (
                   ( 
                    "Orden de Trabajo ITAF-" + orden.cotr_consecutivo.get,
                    if (_expansion > 0.0) { "$" + formatter.format(_expansion) } else { "" },
                    if (_modernizacion > 0.0) { "$" + formatter.format(_modernizacion) } else { "" },
                    if (_desmonte > 0.0) { "-$" + formatter.format(_desmonte) } else { "" },
                    "$" + formatter.format(_total)
                  )
        ) */
      }
      // Actualizar redimensionamiento control
      db.withTransaction { implicit connection =>
        var _fecha_valor = Calendar.getInstance()
        _fecha_valor.add(Calendar.MONTH, -1)
        val _actualizado = SQL("""UPDATE siap.redimensionamiento_control SET reco_valor = {valor} WHERE reco_anho = {anho} AND reco_periodo = {periodo} AND empr_id = {empr_id}""").
        on(
          'valor -> (_valor_acumulado_anterior + _subtotal_total),
          'anho -> _fecha_valor.get(Calendar.YEAR),
          'periodo -> (_fecha_valor.get(Calendar.MONTH)),
          'empr_id -> empr_id
        ).executeUpdate() > 0

        if (!_actualizado) {
          SQL("""INSERT INTO siap.redimensionamiento_control(reco_valor, reco_anho, reco_periodo, empr_id) VALUES({valor}, {anho}, {periodo}, {empr_id})""").
          on(
            'valor -> (_valor_acumulado_anterior + _subtotal_total),
            'anho -> _fecha_valor.get(Calendar.YEAR),
            'periodo -> (_fecha_valor.get(Calendar.MONTH)),
            'empr_id -> empr_id
          ).executeInsert()
        }
      }
      (
        _numero_acta,
        _valor_acumulado_anterior,
        _subtotal_expansion,
        _subtotal_modernizacion,
        _subtotal_desmonte,
        _subtotal_total
        // , _listData.toList
      )
  }

  def getAnexoRedimensionamiento(empr_id: Long, anho: Int, periodo: Int, usua_id: Long): (Int, Array[Byte]) = {
    var _listData = new ListBuffer[(String, Double, Double, Double, Double)]
    val empresa = empresaService.buscarPorId(empr_id) match {
      case Some(e) => e
      case None => Empresa(None, "", "", "", "", "", None, 0, 0, 0, 0, None, None)
    }
    val formatter = java.text.NumberFormat.getIntegerInstance
    var _subtotal_expansion = 0.0
    var _subtotal_modernizacion = 0.0
    var _subtotal_desmonte = 0.0
    var _subtotal_total = 0.0
    val _periodo = Calendar.getInstance()
    _periodo.set(Calendar.YEAR, anho)
    _periodo.set(Calendar.MONTH, (periodo-1))
    _periodo.set(Calendar.DAY_OF_MONTH, 1)
    _periodo.set(Calendar.DATE, _periodo.getActualMaximum(Calendar.DATE))

    var _fecha_para_textos = _periodo.clone().asInstanceOf[Calendar]
    var _fecha_para_texto_2_meses_atras = _periodo.clone().asInstanceOf[Calendar]
    _fecha_para_texto_2_meses_atras.add(Calendar.MONTH, -2)
    var _fecha_para_texto_1_mes_atras = _periodo.clone().asInstanceOf[Calendar]
    _fecha_para_texto_1_mes_atras.add(Calendar.MONTH, -1)
    var _fecha_corte = _periodo.clone().asInstanceOf[Calendar]
    _fecha_corte.add(Calendar.MONTH, -1)
    var _fecha_corte_anterior = _fecha_corte.clone().asInstanceOf[Calendar]
    _fecha_corte_anterior.add(Calendar.MONTH, -1)    

    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listRow02 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[CellRange]()
    var _listMerged02 = new ListBuffer[CellRange]()
    val ordenes =  buscarPorEmpresaFacturasAnhoPeriodo(empresa.empr_id.get, _fecha_corte.get(Calendar.YEAR), _fecha_corte.get(Calendar.MONTH) + 1)

    val _valor_acumulado_anterior = db.withTransaction { implicit connection =>
      val _periodo_previo = _fecha_corte.get(Calendar.MONTH) + 1
      val _anho_previo = _fecha_corte.get(Calendar.YEAR)
      println("anho previo: " + _anho_previo)
      println("periodo previo: " + _periodo_previo)
      val _valorOpt = SQL("""SELECT reco_valor FROM siap.redimensionamiento_control rc1 WHERE rc1.reco_anho = {anho} AND rc1.reco_periodo = {periodo} AND rc1.empr_id = {empr_id}""").
      on(
        'anho -> _anho_previo,
        'periodo -> _periodo_previo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt)
      _valorOpt match {
        case Some(v) => v
        case None => 0.0
      }
    }

    val _valor_ipp_base = db.withTransaction { implicit connection =>
      SQL("""SELECT ucap_ipp_valor FROM siap.ucap_ipp ui1 WHERE ui1.ucap_ipp_anho = {anho} AND ui1.empr_id = {empr_id}""").
      on(
        'anho -> 2014,
        'periodo -> periodo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt) match {
        case Some(v) => v
        case None => 0.0
      }
    }

    val _valor_ipp_anho_anterior = db.withTransaction { implicit connection =>
      val _valorOpt = SQL("""SELECT ucap_ipp_valor FROM siap.ucap_ipp ui1 WHERE ui1.ucap_ipp_anho = {anho} AND ui1.empr_id = {empr_id}""").
      on(
        'anho -> (anho - 1),
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt)
      _valorOpt match {
        case Some(v) => v
        case None => 0.0
      }
    }
    val _numero_acta = db.withTransaction { implicit connection => 
      SQL("""SELECT ar.acre_numero FROM siap.acta_redimensionamiento ar 
                            WHERE ar.acre_anho = {anho} AND ar.acre_periodo = {periodo} AND ar.empr_id = {empr_id}""").
      on(
        'anho -> anho,
        'periodo -> periodo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Int].single)
    }
    val _sheet02 = Sheet(
      name = "Anexo 02",
      rows = {
        var _idx0 = 0
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "ANEXO 02 - ACTA DE REDIMENSIONAMIENTO No." + "%02d-%03d".format(anho - 2000, _numero_acta),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 10.points)
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx0 += 1
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            Utility.fechaamesanho(Some(new DateTime(_fecha_para_textos.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 10.points)
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )        
        _idx0 += 1
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "",
            Some(0),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx0 += 1
        ordenes.map { orden =>
          if (orden.cotr_tipo_obra.get == 6) {
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "DESMONTE DE UCAPS MODERNIZADAS ODT ITAF-" + orden.cotr_consecutivo.get,
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points)
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
            )
            _idx0 += 1
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "UCAPS",
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "UND",
              Some(1),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR UCAP DICIEMBRE 2014",
              Some(2),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR IPP DICIEMBRE " + (_fecha_para_textos.get(Calendar.YEAR) - 1),
              Some(3),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR IPP DICIEMBRE 2014",
              Some(4),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR UNITARIO UCAP PARA DESMONTAR",
              Some(5),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TOTAL",
              Some(6),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            ).withHeight(55.points)
            _idx0 += 1
          }
          var _expansion = orden.cotr_tipo_obra match {
                      case Some(2) => cobroService.siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
          var _modernizacion = orden.cotr_tipo_obra match {
                      case Some(6) => cobroService.siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
          var _desmonteRet = orden.cotr_tipo_obra match {
                      case Some(6) => cobroService.siap_orden_trabajo_cobro_calculo_desmonte(empresa, orden, 0, true)
                      case _ => (0.0, (new ListBuffer[(String, Double, Double)]()).toList)
                    }
          if (orden.cotr_tipo_obra.get == 6) {                    
            var _idx01 = _idx0 + 1
            var _hay = false
            _desmonteRet._2.map { _m =>
            _hay = true
            _listRow02 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                _m._1,
                Some(0),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("@"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )                            
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._2,
                Some(1),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )                            
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._3 * _valor_ipp_base / _valor_ipp_anho_anterior,
                Some(2),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _valor_ipp_anho_anterior,
                Some(3),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0.00"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _valor_ipp_base,
                Some(4),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0.00"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "C" + (_idx0 + 1) + "*D" +(_idx0 + 1) + "/E" + (_idx0 + 1),
                Some(5),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0.00"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "B" + (_idx0 + 1) + "*F" +(_idx0 + 1),
                Some(6),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0.00"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )                            
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
            )
            _idx0 += 1
            }
            if (_hay) {
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL",
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(1),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(2),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(3),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(4),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(5),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(G"+ (_idx01) +":G" + (_idx0) + ")",
              Some(6),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("#,##0.00"),
                          font = Font(bold = true, height = 10.points),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
            )
            _idx0 += 1
            }
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                        CellStyle(dataFormat = CellDataFormat("@"))
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
              )
            _idx0 += 1
          }    
          _expansion = _expansion
          _modernizacion = _modernizacion
          val _desmonte = _desmonteRet._1
          val _total = _expansion + _modernizacion - _desmonte
          _subtotal_expansion += _expansion
          _subtotal_modernizacion += _modernizacion
          _subtotal_desmonte += _desmonte
          _subtotal_total += _total
          _listData += 
            (
                   ( 
                    "Orden de Trabajo ITAF-" + orden.cotr_consecutivo.get,
                    _expansion,
                    _modernizacion,
                    _desmonte,
                    _total
                  )
            )
        }
        _listRow02.toList
      },
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(60, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(8, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 5, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 6, width = new Width(19, WidthUnit.Character))
        _listColumn.toList        

      }
    )
    val _sheet01 = Sheet(
      name = "Anexo 01",
      rows = {
        var _idx = -1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "ANEXO 01 - ACTA DE REDIMENSIONAMIENTO No." + "%02d-%03d".format(anho - 2000, _numero_acta),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true)
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx += 1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "REDIMENSIONAMIENTO DE LA INFRAESTRUCTURA DEL SISTEMA DE ALUMBRADO PUBLICO DE " + empresa.muni_descripcion.get,
            Some(0),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"),
                      wrapText = true,
                      verticalAlignment = VA.Center,
                      horizontalAlignment = HA.Center)
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "Expansión",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "Obras complementarias de expansión",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(3),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "TOTAL REDIMENSIONAMIENTO DEL SISTEMA",
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center,                        
                        wrapText = true,
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
        )
        _idx += 1
        _listMerged01 += CellRange((_idx, _idx + 1), (0, 0))
        _listMerged01 += CellRange((_idx, _idx + 1), (1, 1))
        _listMerged01 += CellRange((_idx, _idx), (2, 3))
        _listMerged01 += CellRange((_idx, _idx + 1), (4, 4))
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "",
            Some(0),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(1),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "VR UCAPS Instaladas",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = true,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "VR UCAPS Desmontadas",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = true,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        ).withHeight(40.points)
        _idx += 1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Redimensionamiento de la Infraestructura de Alumbrado Público Desde el 1 de Enero de 2015 hasta el " + Utility.fechaatextosindia(Some(new DateTime(_fecha_para_texto_2_meses_atras.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid,
                        wrapText = true
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),            
          StringCell(
            "",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          NumericCell(
            _valor_acumulado_anterior,
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid,
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        ).withHeight(30.points)
        _idx += 1
        _listData.map { _data =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              _data._1,
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          wrapText = true,
                          horizontalAlignment = HA.Left,
                          verticalAlignment = VA.Top
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _data._2,
              Some(1),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _data._3,
              Some(2),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _data._4,
              Some(3),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(B" + (_idx + 2) + "+C" + (_idx + 2) + "-D" + (_idx + 2) + ")",
              Some(4),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _idx += 1
        }
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Subtotal Redimensionamiento del Mes de " + Utility.fechaamesanho(Some(new DateTime(_fecha_para_texto_1_mes_atras.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        wrapText = true,
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(B5"+ ":B" + (_idx + 1) + ")",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(C5"+ ":C" + (_idx + 1) + ")",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(D5"+ ":D" + (_idx + 1) + ")",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(E5"+ ":E" + (_idx + 1) + ")",
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx += 1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Total redimensionamiento de la Infraestructura del sistema de Alumbrado Público desde el 1 de Enero de 2015 Hasta el " + Utility.fechaatextosindia(Some(new DateTime(_fecha_para_texto_1_mes_atras.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        wrapText = true,
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "E4+"+ "E" + (_idx + 1),
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 11.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        ).withHeight(35.points)
        _listRow01.toList
      },
      mergedRegions = _listMerged01.toList,
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(60, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(19, WidthUnit.Character))
        _listColumn.toList
      }
    )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(_sheet01, _sheet02)
      .writeToOutputStream(os)
    println("Stream Listo")
    (_numero_acta, os.toByteArray())
  }

}