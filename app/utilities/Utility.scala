package utilities

import play.api.mvc._

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._


import pdi.jwt.JwtSession

import dto._

class Utility {
    private val meses = List[String]("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    
    def mes(periodo: Int): String = {
      meses(periodo - 1)
    }

    def validarToken(token: String, secret: String): Boolean = {
      if (token == secret) {
        true
      } else {
        false
      }
    }

    def extraerUsuario(request: Request[AnyContent]): Option[Long] = {
      val token = request.headers.get("Authorization")
      token match {
        case None => {
         return None
        }
        case Some(token) => {
          val session = JwtSession.deserialize(token)
          val usuaId = session.get("usua_id")
          usuaId match {
            case None => {
              None
            }
            case Some(usuaId) => {
              Option(usuaId.as[Long])
            }
          }
        }
      }
    }

    def extraerEmpresa(request: Request[AnyContent]): Option[Long] = {
      val token = request.headers.get("Authorization")
      token match {
        case None => {
         return None
        }
        case Some(token) => {
          val session = JwtSession.deserialize(token)
          val emprId = session.get("empr_id")
          emprId match {
            case None => {
              None
            }
            case Some(emprId) => {
              Option(emprId.as[Long])
            }
          }
        }
      }
    }

    def procesarFiltrado(query: QueryDto): String = {
      var resultString = "( "
      var logicalOperator =
      query.logicalOperator match {
        case Some(logicalOperator) => logicalOperator
        case None => ""
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
                  resultString = resultString + rule.rule + " " + rule.selectedOperator + " " + rule.value + " "
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