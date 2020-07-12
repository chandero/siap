package utilities

import play.api.mvc._

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import scala.util.Try

import pdi.jwt.JwtSession

import org.joda.time.DateTime

import dto._

class Utility {
    private val meses = List[String]("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    private val dias = List[String]("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    
    def mes(periodo: Int): String = {
      meses(periodo - 1)
    }

    def fechaatexto(fecha: Option[DateTime]): String = {
      var texto = new StringBuffer()
      fecha match {
        case Some(fecha) =>
              val anho = fecha.year().get()
              val mes = fecha.monthOfYear().get() - 1
              val diasemana = fecha.dayOfWeek().get()
              val dia = fecha.dayOfMonth().get
              texto.append(dias(diasemana))
              texto.append(" ")
              texto.append(dia.toString)
              texto.append(" de ")
              texto.append(meses(mes))
              texto.append(" de ")
              texto.append(anho.toString)
        case None => texto.append("")
      }
      texto.toString()
    }

    def validarToken(token: String, secret: String): Boolean = {
      if (token == secret) {
        true
      } else {
        false
      }
    }

    def extraerUsuario(request: Request[AnyContent]): Option[Long] = {
      val session = request.session
      val usuaId = session.get("usua_id")
      usuaId match {
        case None => {
          None
        }
        case Some(usuaId) => {
          Some(usuaId.toLong)
        }
      }
    }

    def extraerEmpresa(request: Request[AnyContent]): Option[Long] = {
      val session = request.session
      val emprId = session.get("empr_id")
      emprId match {
        case None => {
            None
          }
        case Some(emprId) => {
            Some(emprId.toLong)
          }
      }
    }

    def procesarFiltrado(query: QueryDto): String = {
      var resultString = "( "
      var logicalOperator =
      query.logicalOperator match {
        case Some("all") => "and"
        case Some("any") => "or"
        case _ => ""
      }
      query.children match {
        case Some(children) => {
            children.foreach { child =>
              child.qtype match {
                case "query-builder-group" =>  {
                  val query = child.query.as[QueryDto]
                  resultString = resultString + procesarFiltrado(query) + " "
                }
                case "query-builder-rule" => {
                  val rule = child.query.as[RuleDto]
                  var value = Json.prettyPrint(rule.value)
                  println("value: " + value)
                  value = value.replace("\"","")
                  println("value: " + value)                  
                  rule.operator match {
                    case "igual a" => resultString = resultString + rule.rule + " = " + rule.value + " "
                    case "no igual a" => resultString = resultString + rule.rule + " <> " + rule.value + " "                    
                    case "contiene a" => resultString = resultString + rule.rule + " like '%" + value + "%'"
                    case "comienza con" => resultString = resultString + rule.rule + " like '" + value + "%'"
                    case "termina con" => resultString = resultString + rule.rule + " like '%" + value + "'"
                    case _ => resultString = resultString + rule.rule + " " + rule.operator + " " + rule.value + " "
                  }
                }
              }
              resultString = resultString + " " + logicalOperator + " "
            }
          }
          case None =>
      }
      resultString = resultString.trim.stripSuffix(logicalOperator)
      resultString = resultString + ")"
      resultString.trim.replace("\"", "'")
      resultString.trim.replace("\"", "'")
    }

    def randomString(largo: Int): String = {
      val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
      randomStringFromCharList(largo, chars)
    }

    def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
      val sb = new StringBuilder
      for (i <- 1 to length) {
        val randomNum = scala.util.Random.nextInt(chars.length)
        sb.append(chars(randomNum))
      }
      sb.toString
    }    
}

object Utility extends Utility