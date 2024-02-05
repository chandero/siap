package models

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, scalar}
import anorm.JodaParameterMetaData._

import javax.inject.Inject

import scala.concurrent.{Await, Future}

case class GeoreferenciaLuminaria(
    aap_id: Option[String],
    aap_direccion: Option[String],
    barr_descripcion: Option[String],
    aap_tecnologia: Option[String],
    aap_potencia: Option[Int],
    aap_lat: Option[String],
    aap_lng: Option[String]
)

object GeoreferenciaLuminaria {
    val _set = {
        get[Option[String]]("aap_id") ~
        get[Option[String]]("aap_direccion") ~
        get[Option[String]]("barr_descripcion") ~
        get[Option[String]]("aap_tecnologia") ~
        get[Option[Int]]("aap_potencia") ~
        get[Option[String]]("aap_lat") ~
        get[Option[String]]("aap_lng") map {
            case aap_id ~ aap_direccion ~ barr_descripcion ~ aap_tecnologia ~ aap_potencia ~ aap_lat ~ aap_lng => GeoreferenciaLuminaria(
                aap_id,
                aap_direccion,
                barr_descripcion,
                aap_tecnologia,
                aap_potencia,
                aap_lat,
                aap_lng
            )
        }
    }
}

class GeoreferenciacionRepository @Inject()(
    dbapi: DBApi,
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def getGeoreferencia(barr_id: Int, aap_tecnologia: String, aap_potencia: Int, aap_id: Int) = Future {
    db.withConnection{ implicit connection =>
        val _consultaInicial = """SELECT a1.aap_id::varchar, a1.aap_direccion, b1.barr_descripcion, ad1.aap_tecnologia, ad1.aap_potencia, a1.aap_lat, a1.aap_lng
                        FROM siap.aap a1
                        LEFT JOIN siap.aap_adicional ad1 ON ad1.aap_id = a1.aap_id
                        LEFT JOIN siap.barrio b1 ON b1.barr_id = a1.barr_id
                        WHERE a1.aap_lat IS NOT NULL AND a1.aap_lat <> '' AND a1.aap_lng IS NOT NULL AND a1.aap_lng <> '' AND a1.esta_id = 1"""
        val _whereBarrio = """ AND b1.barr_id = {barr_id} """
        val _WhereTecnologia = """ AND ad1.aap_tecnologia = {aap_tecnologia}"""
        val _wherePotencia = """ AND ad1.aap_potencia = {aap_potencia}"""
        val _whereLuminaria = """ AND a1.aap_id = {aap_id}"""
        var _queryFinal = barr_id match {
            case -1 => _consultaInicial 
            case _ => _consultaInicial + _whereBarrio
        }
         _queryFinal = aap_tecnologia match {
            case "-1" => _queryFinal
            case _ => _queryFinal + _WhereTecnologia
        }
          _queryFinal = aap_potencia match {
            case -1 => _queryFinal
            case _ => _queryFinal + _wherePotencia
        }
        _queryFinal = aap_id match {
            case -1 => _queryFinal
            case _ => _queryFinal + _whereLuminaria
        }
        var _query = SQL(_queryFinal).on()
        barr_id match {
            case -1 =>None
            case _ => _query=_query.on("barr_id" -> barr_id)
        }
        aap_tecnologia match {
            case "-1" =>None
            case _ => _query=_query.on("aap_tecnologia" -> aap_tecnologia)
        }
        aap_potencia match {
            case -1 =>None
            case _ => _query=_query.on("aap_potencia" -> aap_potencia)
        }
        aap_id match {
            case -1 =>None
            case _ => _query=_query.on("aap_id" -> aap_id)
        }
        val _resultSet = _query.as(GeoreferenciaLuminaria._set *)
        _resultSet
    }
  }
}