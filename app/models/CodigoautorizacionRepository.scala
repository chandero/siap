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
// Utility
import utilities.Utility
import org.checkerframework.checker.units.qual.s
import org.apache.poi.ss.usermodel.CellType
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import java.io.ByteArrayOutputStream

class CodigoautorizacionRepository @Inject()(dbapi: DBApi, empresaService: EmpresaRepository)(
    implicit ec: DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    /**
     * Código autorización por tipo
     * @param tipo:Int
     * @param empr_id:Long
     * @param usua_id:Long
     * @return String
     */
    def codigo(tipo: Int, empr_id: scala.Long, usua_id: scala.Long): String = {
        db.withConnection { implicit connection =>
            val codigo = Utility.randomString(8)
            val fecha = Calendar.getInstance()
            SQL("""INSERT INTO siap.codigo_autorizacion (coau_tipo, coau_codigo, usua_id, coau_fechacreacion, coau_estado, empr_id) VALUES (
                {coau_tipo}, {coau_codigo}, {usua_id}, {coau_fechacreacion}, {coau_estado}, {empr_id})""").
                on(
                    'coau_tipo -> tipo,
                    'coau_codigo -> codigo,
                    'usua_id -> usua_id,
                    'coau_fechacreacion -> new DateTime(Calendar.getInstance().getTimeInMillis()),
                    'coau_estado -> 0,
                    'empr_id -> empr_id
                ).executeUpdate()
            codigo
        }
    }

    /**
     * Validar código
     * @param codigo
     * @param tipo
     * @return Boolean
     */
    def validar(tipo: Int, codigo: String, empr_id: scala.Long): Boolean = {
        db.withConnection{ implicit connection => 
          val cantidad = SQL("""SELECT COUNT(*) FROM siap.codigo_autorizacion WHERE coau_tipo = {coau_tipo} and coau_codigo = {coau_codigo} and coau_estado = {coau_estado} and empr_id = {empr_id}""").
          on(
            'coau_tipo -> tipo,
            'coau_codigo -> codigo,
            'coau_estado -> 0,
            'empr_id -> empr_id
          ).as(SqlParser.scalar[Int].singleOpt)
          cantidad match {
              case Some(cantidad) => if (cantidad > 0) { 
                                        true
                                     } else {
                                        false
                                     }
              case None => false
          }
        }
    }

    def informeUsoCodigoAutorizacion(fi: scala.Long, ff: scala.Long, empr_id: scala.Long) = Future {
        val _parser = get[Option[String]]("tireuc_descripcion") ~ 
                      get[Option[String]]("reti_descripcion") ~
                      get[Option[Int]]("repo_consecutivo") ~
                        get[Option[String]]("coau_codigo") ~
                        get[Option[DateTime]]("coau_fechacreacion") ~
                        get[Option[DateTime]]("coau_fechauso") ~
                        get[Option[String]]("generador") ~
                        get[Option[String]]("usuario") ~
                        get[Option[String]]("usuario_reporte") ~
                        get[Option[Int]]("coau_tipo") ~
                        get[Option[Int]]("empr_id")  map {
                        case tireuc_descripcion ~ 
                             reti_descripcion ~
                             repo_consecutivo ~
                             coau_codigo ~
                             coau_fechacreacion ~
                             coau_fechauso ~
                             generador ~
                             usuario ~
                             usuario_reporte ~
                             coau_tipo ~
                             empr_id => 
                             (tireuc_descripcion, 
                              reti_descripcion, 
                              repo_consecutivo,
                              coau_codigo,
                              coau_fechacreacion,
                              coau_fechauso,
                              generador,
                              usuario,
                              usuario_reporte,
                              coau_tipo,
                              empr_id)
                        }
        val _query = """select * from (select 
                        'Luminaria' as tireuc_descripcion, 
                        rt1.reti_descripcion, 
                        r1.repo_consecutivo,
                        rca1.coau_codigo,
                        ca1.coau_fechacreacion,
                        ca1.coau_fechauso,
                        u1.usua_nombre || ' ' || u1.usua_apellido as generador,
                        u2.usua_nombre || ' ' || u2.usua_apellido as usuario,
                        u3.usua_nombre || ' ' || u3.usua_apellido as usuario_reporte,
                        case when ca1.coau_tipo = 1 then 1
                             when ca1.coau_tipo = 2 then 2
                             when ca1.coau_tipo = 3 then 3
                        end as coau_tipo,
                        ca1.empr_id
                        from siap.reporte_codigo_autorizacion rca1
                        inner join siap.reporte r1 on r1.repo_id = rca1.repo_id
                        inner join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
                        inner join siap.codigo_autorizacion ca1 on ca1.coau_codigo = rca1.coau_codigo
                        inner join siap.usuario u1 on u1.usua_id = ca1.usua_id 
                        inner join siap.usuario u2 on u2.usua_id = ca1.coau_usua_id
                        inner join siap.usuario u3 on u3.usua_id = r1.usua_id
                        where ca1.coau_estado = 1
                        union all 
                        select 
                        'Control' as tireuc_descripcion, 
                        rt1.reti_descripcion, 
                        r1.repo_consecutivo,
                        rca1.coau_codigo,
                        ca1.coau_fechacreacion,
                        ca1.coau_fechauso,
                        u1.usua_nombre || ' ' || u1.usua_apellido as generador,
                        u2.usua_nombre || ' ' || u2.usua_apellido as usuario,
                        u3.usua_nombre || ' ' || u3.usua_apellido as usuario_reporte,
                        case when ca1.coau_tipo = 1 then 1
                            when ca1.coau_tipo = 2 then 2
                            when ca1.coau_tipo = 3 then 3
                        end as coau_tipo,
                        ca1.empr_id
                        from siap.control_reporte_codigo_autorizacion rca1
                        inner join siap.control_reporte r1 on r1.repo_id = rca1.repo_id
                        inner join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
                        inner join siap.codigo_autorizacion ca1 on ca1.coau_codigo = rca1.coau_codigo
                        inner join siap.usuario u1 on u1.usua_id = ca1.usua_id 
                        inner join siap.usuario u2 on u2.usua_id = ca1.coau_usua_id
                        inner join siap.usuario u3 on u3.usua_id = r1.usua_id
                        where ca1.coau_estado = 1
                        union all 
                        select 
                        'Transformador' as tireuc_descripcion, 
                        rt1.reti_descripcion, 
                        r1.repo_consecutivo,
                        rca1.coau_codigo,
                        ca1.coau_fechacreacion,
                        ca1.coau_fechauso,
                        u1.usua_nombre || ' ' || u1.usua_apellido as generador,
                        u2.usua_nombre || ' ' || u2.usua_apellido as usuario,
                        u3.usua_nombre || ' ' || u3.usua_apellido as usuario_reporte,
                        case when ca1.coau_tipo = 1 then 1
                             when ca1.coau_tipo = 2 then 2
                             when ca1.coau_tipo = 3 then 3
                        end as coau_tipo,
                        ca1.empr_id
                        from siap.transformador_reporte_codigo_autorizacion rca1
                        inner join siap.transformador_reporte r1 on r1.repo_id = rca1.repo_id
                        inner join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
                        inner join siap.codigo_autorizacion ca1 on ca1.coau_codigo = rca1.coau_codigo
                        inner join siap.usuario u1 on u1.usua_id = ca1.usua_id 
                        inner join siap.usuario u2 on u2.usua_id = ca1.coau_usua_id
                        inner join siap.usuario u3 on u3.usua_id = r1.usua_id
                        where ca1.coau_estado = 1
                        union all 
                        select 
                        'Inventario' as tireuc_descripcion, 
                        'LUMINARIA' as reti_descripcion, 
                        r1.aap_id as repo_consecutivo,
                        rca1.coau_codigo,
                        ca1.coau_fechacreacion,
                        ca1.coau_fechauso,
                        u1.usua_nombre || ' ' || u1.usua_apellido as generador,
                        u2.usua_nombre || ' ' || u2.usua_apellido as usuario,
                        u3.usua_nombre || ' ' || u3.usua_apellido as usuario_reporte,
                        case when ca1.coau_tipo = 1 then 1
                             when ca1.coau_tipo = 2 then 2
                             when ca1.coau_tipo = 3 then 3
                        end as coau_tipo,
                        ca1.empr_id
                        from siap.aap_codigo_autorizacion rca1
                        inner join siap.aap r1 on r1.aap_id = rca1.aap_id
                        inner join siap.codigo_autorizacion ca1 on ca1.coau_codigo = rca1.coau_codigo
                        inner join siap.usuario u1 on u1.usua_id = ca1.usua_id 
                        inner join siap.usuario u2 on u2.usua_id = ca1.coau_usua_id
                        inner join siap.usuario u3 on u3.usua_id = r1.usua_id
                        where ca1.coau_estado = 1) as o
                        where o.coau_fechauso between {fecha_inicial} and {fecha_final} and o.empr_id = {empr_id}
                        order by o.coau_fechauso  desc"""
            db.withConnection { implicit connection =>
                val _resultSet = SQL(_query).
                on(
                    'fecha_inicial -> new DateTime(fi),
                    'fecha_final -> new DateTime(ff),
                    'empr_id -> empr_id
                ).as(_parser *)
                _resultSet.toList
            }
    }

    def informeUsoCodigoAutorizacionXls(fi: scala.Long, ff: scala.Long, empr_id: scala.Long, usua_id: scala.Long): Array[Byte] = {
        val _resultSet = Await.result(informeUsoCodigoAutorizacion(fi,ff,empr_id), Duration.Inf)
        db.withConnection { implicit connection =>
            val empresa = empresaService.buscarPorId(empr_id).get
              var _fecha_inicial = Calendar.getInstance()
              var _fecha_final = Calendar.getInstance()
              _fecha_inicial.setTimeInMillis(fi)
              _fecha_final.setTimeInMillis(ff)
              val fmt = DateTimeFormat.forPattern("yyyyMMdd")
              val sdf = new SimpleDateFormat("yyyy-MM-dd")
              var _listMerged01 = new ListBuffer[CellRange]()
              var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
              var _listMerged02 = new ListBuffer[CellRange]()
              var _listRow02 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
              var _listMerged03 = new ListBuffer[CellRange]()
              var _listRow03 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
              var _listMerged04 = new ListBuffer[CellRange]()
              var _listRow04 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()

              val sheet01 = Sheet(
                name = "Códigos",
                rows = {
                  val titleRow1 = com.norbitltd.spoiwo.model
                    .Row()
                    .withCellValues(empresa.empr_descripcion)
                  _listRow04 += titleRow1
                  val titleRow2 = com.norbitltd.spoiwo.model
                    .Row()
                    .withCellValues("Informe de Códigos de Autorización")
                  _listRow04 += titleRow2
                  val titleRow3 = com.norbitltd.spoiwo.model.Row(
                    StringCell(
                      "Rango Desde:",
                      Some(0),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      _fecha_inicial.getTime(),
                      Some(1),
                      style = Some(
                        CellStyle(dataFormat = CellDataFormat("YYYY/MM/DD"))
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    StringCell(
                      "Hasta:",
                      Some(2),
                      style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    ),
                    DateCell(
                      _fecha_final.getTime(),
                      Some(3),
                      style = Some(
                        CellStyle(dataFormat = CellDataFormat("YYYY/MM/DD"))
                      ),
                      CellStyleInheritance.CellThenRowThenColumnThenSheet
                    )                    
                  )
                  _listRow04 += titleRow3
                  val headerRow = com.norbitltd.spoiwo.model
                    .Row()
                    .withCellValues(
                      "Tipo",
                      "Tipo de Operación",
                      "Número",
                      "Fecha Generación",
                      "Fecha Uso",
                      "Código",
                      "Generador",
                      "Usuario",
                      "Usuario Reporte"
                    ).withStyle(CellStyle(font = Font(bold = true)))
                  _listRow04 += headerRow
                  _resultSet.map { r =>
                    _listRow04 += com.norbitltd.spoiwo.model
                      .Row(
                        StringCell(
                            r._1 match { case Some(value) => value case None => ""},
                            index = Some(0),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),
                        StringCell(
                            r._2 match { case Some(value) => value case None => ""},
                            index = Some(1),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),
                        StringCell(
                            r._3 match { case Some(value) => value.toString case None => ""},
                            index = Some(2),
                            style = Some(CellStyle(dataFormat = CellDataFormat("#0"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),
                        StringCell(
                            r._5 match { case Some(value) => sdf.format(value.toDate) case None => ""},
                            index = Some(3),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),
                        StringCell(
                            r._6 match { case Some(value) => sdf.format(value.toDate) case None => ""},
                            index = Some(4),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),
                        StringCell(
                            r._4 match { case Some(value) => value case None => ""},
                            index = Some(5),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),                        
                        StringCell(
                            r._7 match { case Some(value) => value case None => ""},
                            index = Some(6),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),                        
                        StringCell(
                            r._8 match { case Some(value) => value case None => ""},
                            index = Some(7),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),
                        StringCell(
                            r._9 match { case Some(value) => value case None => ""},
                            index = Some(8),
                            style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                            CellStyleInheritance.CellThenRowThenColumnThenSheet
                        ),                        
                      )
                  }
                  _listRow04.toList
                }
              )
                println("Escribiendo en el Stream")
                var os: ByteArrayOutputStream = new ByteArrayOutputStream()
                Workbook(sheet01).writeToOutputStream(os)
                println("Stream Listo")
                os.toByteArray
            }
    }
}