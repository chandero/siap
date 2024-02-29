package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.sql.Connection
import org.joda.time.DateTime


import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, scalar, int, double, date}
import anorm.JodaParameterMetaData._


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


case class Acta91Expansion(
  aap_fechatoma: DateTime
  aap_id: Int,
  aap_direccion: String,
  barr_descripcion: String,
  aap_tecnologia: String,
  aap_potencia: Int
)

case class Acta91Modernizacion(
  aap_fechatoma: DateTime
  aap_id: Int,
  aap_direccion: String,
  barr_descripcion: String,
  aap_tecnologia: String,
  aap_potencia: Int,
  aap_tecnologia_anterior: String,
  aap_potencia_anterior: String
)

case class Acta91Reubicacion(
  repo_fechasolucion: DateTime,
  aap_id: Int,
  even_direccion_anterior: String,
  barr_descripcion: String,
  even_direccion: String,
  barr_descripcion: String,
  aap_tecnologia: String,
  aap_potencia: Int

)

case class Acta91Retirado (
  repo_fechasolucion: DateTime
  aap_id: Int,
  even_direccion: String,
  barr_descripcion: String,
  aap_tecnologia: String,
  aap_potencia: Int
)

case class Acta91Repotenciacion (
  aap_fechatoma: DateTime
  aap_id: Int,
  aap_direccion: String,
  barr_descripcion: String,
  aap_tecnologia: String,
  aap_potencia: Int,
  aap_tecnologia_anterior: String,
  aap_potencia_anterior: String
)



object Acta91Expansion {
  val _set = {
    get[Option[DateTime]]("aap_fechatoma")~
    get[Option[Int]]("app_id") ~
    get[Option[String]]("aap_direccion") ~
    get[Option[String]]("barr_descripcion") ~
    get[Option[String]]("aap_tecnologia") ~
    get[Option[String]]("app_potencia") ~  {
      case  aap_fechatoma ~ app_id ~ aap_direccion ~ barr_descripcion ~ aap_tecnologia ~ aap_potencia =>
      Acta91Expansion(
        aap_fechatoma,
        aap_id,
        aap_direccion,
        barr_descripcion,
        aap_tecnologia,
        aap_potencia
      )
    }
  }
}

object Acta91Modernizacion {
  val _set = {
    get[Option[DateTime]]("aap_fechatoma")~
    get[Option[Int]]("app_id") ~
    get[Option[String]]("aap_direccion") ~
    get[Option[String]]("barr_descripcion") ~
    get[Option[String]]("aap_tecnologia") ~
    get[Option[String]]("app_potencia") ~ 
    get[Option[String]]("aap_tecnologia_anterior")~ 
    get[Option[String]]("app_potencia_anterior") {
      case  aap_fechatoma ~ app_id ~ aap_direccion ~ barr_descripcion ~ aap_tecnologia ~ aap_potencia ~ aap_tecnologia_anterior ~ aap_potencia_anterior =>
      Acta91Modernizacion(
        aap_fechatoma,
        aap_id,
        aap_direccion,
        barr_descripcion,
        aap_tecnologia,
        aap_potencia,
        rdd.aap_tecnologia_anterior,
        rrd.aap_potencia_anterior
      )
    }
  }
}

object Acta91Reubicacion {
  val _set = {
    get[Option[DateTime]]("repo_fechasolucion") ~
    get[Option[Int]]("aap_id") ~
    get[Option[String]]("even_direccion_anterior") ~
    get[Option[String]]("barr_descripcion") ~
    get[Option[String]]("even_direccion")
    get[Option[String]]("barr_descripcion") ~
    get[Option[String]]("app_tecnologia") ~
    get[Option[Int]]("aap_potencia") {
      case repo_fechasolucion, aap_id, even_direccion_anterior, barr_descripcion, even_direccion, barr_descripcion, app_tecnologia, app_potencia =>
      Acta91Reubicacion (
        repo_fechasolucion,
        app_id,
        even_direccion_anterior,
        barr_descripcion,
        even_direccion,
        barr_descripcion,
        app_tecnologia,
        aap_potencia
      )
    }
  }
}

object Acta91Retirado {
  val _set = {
    get[Option[DateTime]]("repo_fechasolucion") ~
    get[Option[Int]]("aap_id") ~
    get[Option[String]]("even_direccion") ~
    get[Option[String]]("barr_descripcion") ~
    get[Option[String]]("aap_tecnologia") ~
    get[Option[String]]("aap_potencia") {
      case repo_fechasolucion ~ aap_id ~ even_direccion ~ barr_descripcion ~ aap_tecnologia ~ aap_potencia =>
      Acta91Retirado (
        repo_fechasolucion,
        aap_id,
        even_direccion,
        barr_descripcion,
        aap_tecnologia,
        aap_potencia
      )
    }
  }
}

object Acta91Repotenciacion {
  val _set = {
    val _set = 
    get[Option[DateTime]]("aap_fechatoma")~
    get[Option[Int]]("app_id") ~
    get[Option[String]]("aap_direccion") ~
    get[Option[String]]("barr_descripcion") ~
    get[Option[String]]("aap_tecnologia") ~
    get[Option[String]]("app_potencia") ~ 
    get[Option[String]]("aap_tecnologia_anterior")~ 
    get[Option[String]]("app_potencia_anterior") {
      case  aap_fechatoma ~ app_id ~ aap_direccion ~ barr_descripcion ~ aap_tecnologia ~ aap_potencia ~ aap_tecnologia_anterior ~ aap_potencia_anterior =>
      Acta91Repotenciacion (
        aap_fechatoma,
        aap_id,
        aap_direccion,
        barr_descripcion,
        aap_tecnologia,
        aap_potencia,
        rdd.aap_tecnologia_anterior,
        rrd.aap_potencia_anterior
      )
    }
  }
}



class ActaRepository @Inject()(
    dbapi: DBApi
)(implicit ec: DatabaseExecutionContext) { 

  private val db = dbapi.database("default") 

  def getActa91 (fecha_inicial: Long, fecha_final: Long): Array[Byte] =  {
    db.withConnection{ implicit connection => 
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
      val  _fecha_inicial = new DateTime(fi.getTime())
      val _fecha_final = new DateTime(fi.getTime())
      val _resultadoexpansion = getActa91Expansion(_fecha_inicial,_fecha_final)
      val _resultadomodernizacion = getActa91Modernizacion(_fecha_inicial,_fecha_final)
      val _resultadoreubicacion = getActa91Reubicacion(_fecha_inicial,_fecha_final)
      val _resultadoretirado = getActa91Retirado(_fecha_inicial,_fecha_final)
      val _resultadorepotenciacion = getacta91Repotenciacion(_fecha_inicial,_fecha_final)
      var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listMerged = new ListBuffer[CellRange]()
      val sheet1 = Sheet(
        name = "9.1",
        rows = {
           val titleRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(empresa.empr_descripcion)
        }

      )
    }
  }


  def getActa91Expansion (fecha_inicial: DateTime, fecha_final: DateTime) (implicit connection: Connection ) =  {

      val _consultaExpansion = """select r.repo_fechasolucion, rd.aap_id, rd.even_direccion, b.barr_descripcion, rdd.aap_tecnologia, rdd.aap_potencia  from siap.reporte r 
      inner join siap.reporte_direccion rd on rd.repo_id = r.repo_id and rd.even_estado  <  8
      inner join siap.reporte_direccion_dato rdd on rdd.repo_id = rd.repo_id and rdd.aap_id  = rd.aap_id 
      inner join siap.barrio b on b.barr_id = rd.barr_id 
      where r.repo_fechasolucion  between {fecha_inicial} and {fecha_final} and r.rees_id  = 3 and r.reti_id = 2
      order by r.repo_fechasolucion asc, rd.aap_id asc"""
      
      val _resultadoExpansion = SQL(_consultaExpansion).on(
      'fecha_inicial -> fi.getTime()
      'fecha_final -> fi.getTime
      )
      .as(Acta91Expansion._set *)
      _resultadoExpansion 
  }

  def getActa91Modernizacion (fecha_inicial: DateTime, fecha_final: DateTime) (implicit connection: Connection) = {
      val consultaModernizacion = """select r.repo_fechasolucion, rd.aap_id, rd.even_direccion, b.barr_descripcion, rdd.aap_tecnologia, rdd.aap_potencia, rdd.aap_tecnologia_anterior, rdd.aap_potencia_anterior 
      from siap.reporte r 
      inner join siap.reporte_direccion rd on rd.repo_id = r.repo_id and rd.even_estado  <  8
      inner join siap.reporte_direccion_dato rdd on rdd.repo_id = rd.repo_id and rdd.aap_id  = rd.aap_id 
      inner join siap.barrio b on b.barr_id = rd.barr_id 
      where r.repo_fechasolucion  between {fecha_inicial} and {fecha_final} and r.rees_id  = 3 and r.reti_id = 6
      order by r.repo_fechasolucion asc, rd.aap_id asc"""

      val _resultadoModernizacion = SQL(_consultaMornizacion).on(
      'fecha_inicial -> fi.getTime()
      'fecha_final -> fi.getTime
      ).as(Acta91Modernizacion._set *)
      _resultadoModernizacion
  }

  def getActa91Reubicacion (fecha_inicial: DateTime, fecha_final: DateTime) (implicit connection: Connection) = {
      val consultaReubicacion = """ select r.repo_fechasolucion, rd.aap_id, rd2.even_direccion_anterior, b2.barr_descripcion, rd2.even_direccion, b.barr_descripcion, rdd.aap_tecnologia, rdd.aap_potencia
      from siap.reporte r 
      inner join siap.reporte_direccion rd on rd.repo_id = r.repo_id and rd.even_estado  <  8
      inner join siap.reporte_direccion_dato rdd on rdd.repo_id = rd.repo_id and rdd.aap_id  = rd.aap_id 
      inner join siap.barrio b on b.barr_id = rd.barr_id 
      inner join siap.barrio b2 on b2.barr_id = rd.barr_id_anterior 
      inner join siap.reporte_direccion rd2 on rd2.repo_id = r.repo_id 
      where r.repo_fechasolucion  between '2023-03-01' and '2023-03-30' and r.rees_id  = 3 and r.reti_id = 3
      order by r.repo_fechasolucion asc, rd.aap_id asc  """
      val _resultadoReubicacion = SQL (_consultaReubicacion).on(
      'fecha_inicial -> fi.getTime()
      'fecha_final -> fi.getTime
      ).as(ActaReubicacion)
      _resultadoreubicacion
    
  }

  def getActa91Retirado (fecha_inicial: DateTime, fecha_final: DateTime) (implicit connection: Connection) = {
      val consultaRetirado """ select r.repo_fechasolucion, rd.aap_id, rd2.even_direccion, b.barr_descripcion, rdd.aap_tecnologia, rdd.aap_potencia
      from siap.reporte r 
      inner join siap.reporte_direccion rd on rd.repo_id = r.repo_id and rd.even_estado  <  8
      inner join siap.reporte_direccion_dato rdd on rdd.repo_id = rd.repo_id and rdd.aap_id  = rd.aap_id 
      inner join siap.barrio b on b.barr_id = rd.barr_id 
      inner join siap.barrio b2 on b2.barr_id = rd.barr_id_anterior 
      inner join siap.reporte_direccion rd2 on rd2.repo_id = r.repo_id 
      where r.repo_fechasolucion  between '2023-03-01' and '2023-03-30' and r.rees_id  = 3 and r.reti_id = 8
      order by r.repo_fechasolucion asc, rd.aap_id asc  """
      val resultadoRetirado = SQL (_consultaRetirado).on(
      'fecha_inicial -> fi.getTime()
      'fecha_final -> fi.getTime
      ). as(Acta91Reubicacion)
      _resultadoretirado
  }

  def getacta91Repotenciacion  (fecha_inicial: DateTime, fecha_final:DateTime) (implicit connection: Connection) = {
 
      val consultarRepotenciacion """select r.repo_fechasolucion, rd.aap_id, rd.even_direccion, b.barr_descripcion, rdd.aap_tecnologia, rdd.aap_potencia, rdd.aap_tecnologia_anterior, rdd.aap_potencia_anterior 
      from siap.reporte r 
      inner join siap.reporte_direccion rd on rd.repo_id = r.repo_id and rd.even_estado  <  8
      inner join siap.reporte_direccion_dato rdd on rdd.repo_id = rd.repo_id and rdd.aap_id  = rd.aap_id 
      inner join siap.barrio b on b.barr_id = rd.barr_id 
      where r.repo_fechasolucion  between {fecha_inicial} and {fecha_final} and r.rees_id  = 3 and r.reti_id = 4
      order by r.repo_fechasolucion asc, rd.aap_id asc """
      val resultadoRepotenciacion = SQL (_consultaRepotenciacion).on(
      'fecha_inicial -> fi.getTime()
      'fecha_final -> fi.getTime
      ). as(Acta91Modernizacion)
      _resultadoretirado
    
  }
}

