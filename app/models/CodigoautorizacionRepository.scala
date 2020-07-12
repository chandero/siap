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

import utilities._

class CodigoautorizacionRepository @Inject()(dbapi: DBApi)(
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
}