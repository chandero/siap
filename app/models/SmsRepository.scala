package models

import play.api.db.DBApi

import play.api.Configuration

import com.typesafe.config.ConfigFactory

import javax.inject.Inject

import scala.util.matching.Regex

import org.joda.time.DateTime

import http.HttpClient

import anorm._
import anorm.SqlParser.{get, str, int, date, double, scalar, flatten}
import anorm.JodaParameterMetaData._

class SmsRepository @Inject()(
    http: HttpClient,
    dbapi: DBApi
)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")
  
    def SendSmsTest():Unit = {
        println("Enviando SMS Test")
        http.sendSMS("3142425575", "Test Ciclo")
    }

    def SendSms()  {
        val conf = Configuration(ConfigFactory.load("application.conf"))
        // 
        println("Enviando SMS")
        val _queryGuardar = """INSERT INTO siap.sms_control
            (tireuc_id, reti_id, repo_id, smsc_message, smsc_fecha, smsc_destino, smsc_referencia, smsc_confirmado)
            VALUES({tireuc_id}, {reti_id}, {repo_id}, {smsc_message}, {smsc_fecha}, {smsc_destino}, {smsc_referencia}, {smsc_confirmado})"""
        // query para buscar reportes a trabajar
        val _parser = get[Option[Int]]("tireuc_id") ~ get[Option[Int]]("reti_id") ~ get[Option[Int]]("repo_id") ~ 
                      get[Option[Int]]("repo_consecutivo") ~ get[Option[String]]("acti_descripcion") ~ get[Option[String]]("repo_telefono") map {
                        case a1 ~ a2 ~ a3 ~ a4 ~ a5 ~ a6 => (a1,a2,a3,a4,a5,a6) 
                      }
        val _query = """select * from (select r1.tireuc_id, r1.reti_id, r1.repo_id, r1.repo_consecutivo, a1.acti_descripcion, r1.repo_telefono from siap.reporte r1
inner join siap.reporte_adicional ra1 on ra1.repo_id = r1.repo_id
inner join siap.actividad a1 on a1.acti_id = ra1.acti_id 
where r1.repo_fechasolucion >= ( current_date - 3 ) and r1.reti_id = 1 and r1.rees_id = 3
union all 
select r1.tireuc_id, r1.reti_id, r1.repo_id, r1.repo_consecutivo, a1.acti_descripcion, r1.repo_telefono from siap.control_reporte r1
inner join siap.control_reporte_adicional ra1 on ra1.repo_id = r1.repo_id
inner join siap.actividad a1 on a1.acti_id = ra1.acti_id 
where r1.repo_fechasolucion >= ( current_date - 3 ) and r1.reti_id = 1 and r1.rees_id = 3
union all 
select r1.tireuc_id, r1.reti_id, r1.repo_id, r1.repo_consecutivo, a1.acti_descripcion, r1.repo_telefono from siap.transformador_reporte r1
inner join siap.transformador_reporte_adicional ra1 on ra1.repo_id = r1.repo_id
inner join siap.actividad a1 on a1.acti_id = ra1.acti_id 
where r1.repo_fechasolucion >= ( current_date - 3 ) and r1.reti_id = 1 and r1.rees_id = 3
union all 
select r1.tireuc_id, r1.reti_id, r1.repo_id, r1.repo_consecutivo, a1.acti_descripcion, r1.repo_telefono from siap.medidor_reporte r1
inner join siap.medidor_reporte_adicional ra1 on ra1.repo_id = r1.repo_id
inner join siap.actividad a1 on a1.acti_id = ra1.acti_id 
where r1.repo_fechasolucion >= ( current_date - 3 ) and r1.reti_id = 1 and r1.rees_id = 3
) o
where (select count(*) from siap.sms_control sc1 where sc1.tireuc_id =o.tireuc_id and sc1.reti_id = o.reti_id and sc1.repo_id = o.repo_id) = 0"""

        db.withConnection { implicit connection => 
            val _resultSet = SQL(_query).as(_parser.*).toList
            val message = conf.get[String]("sms.message")
            val _regexCellPhonePattern = "^[\\+]?[(]?3[0-9]{2}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$"
            println("Enviando: ResultSet:" + _resultSet)
            _resultSet.map { reporte =>
                // validar número de teléfono
                reporte._6 match {
                case Some(numero) => numero.matches(_regexCellPhonePattern) match {
                    case true => {
                        println("Enviando Mensaje de Reporte Cerrado")
                        var _texto = message.format(reporte._4.get, reporte._5.get)
                        println("Enviando Mensaje: " + _texto)
                        SQL(_queryGuardar).on(
                            'tireuc_id -> reporte._1,
                            'reti_id -> reporte._2,
                            'repo_id -> reporte._3,
                            'smsc_message -> _texto,
                            'smsc_fecha -> new DateTime(),
                            'smsc_destino -> numero,
                            'smsc_referencia -> numero,
                            'smsc_confirmado -> new DateTime()
                        ).executeUpdate()
                         http.sendSMS(numero, _texto)
                    }
                    case _ => None
                }
                 case _ => None
                }
            }
        }
    }
}