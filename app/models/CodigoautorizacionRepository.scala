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

    def informeUsoCodigoAutorizacion(fi: scala.Long, ff: scala.Long, empr_id: scala.Long) = Future {
        val _parser = get[Option[String]]("tireuc_descripcion") ~ 
                      get[Option[String]]("reti_descripcion") ~
                      get[Option[Int]]("repo_consecutivo") ~
                        get[Option[String]]("coau_codigo") ~
                        get[Option[DateTime]]("coau_fechacreacion") ~
                        get[Option[DateTime]]("coau_fechauso") ~
                        get[Option[String]]("generador") ~
                        get[Option[String]]("usuario") ~
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

    def informeUsoCodigoAutorizacionXls(fi: scala.Long, ff: scala.Long, empr_id: scala.Long, usua_id: scala.Long) = {

    }
}