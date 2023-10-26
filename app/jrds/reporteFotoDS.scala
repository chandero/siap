package jrds

import scala.collection.mutable.ListBuffer
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRField

import org.joda.time.DateTime 

import java.nio.file.{Paths, Files}

import java.text.SimpleDateFormat
import java.util.{ HashMap, Map }
import java.util.Date
import com.typesafe.config.ConfigFactory
import play.api.Configuration


class ReporteFotoDS(listData: List[(Int, String, Int, Int, Int, String, Int, String, Date, String)]) extends JRDataSource {
    
    val conf = Configuration(ConfigFactory.load("application.conf"))

    var i = -1
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

    override def next(): Boolean = {
        i += 1
        i < listData.length
      }

    override def getFieldValue(jrField: JRField): AnyRef = {
        print("Procesando campo: " + jrField.getName + ", ")
        val emptyImg = System.getProperty("user.dir") + "/public/img/empty.jpg"
        val retorno = jrField.getName match {
          case "tireuc_id" => new java.lang.Integer(listData(i)._1)
          case "reti_descripcion" => new java.lang.String(listData(i)._2)
          case "repo_consecutivo" => new java.lang.Integer(listData(i)._3)
          case "repo_tipo_expansion" => listData(i)._4 match {
            case 1 => new java.lang.String("I")
            case 2 => new java.lang.String("II")
            case 3 => new java.lang.String("III")
            case 4 => new java.lang.String("IV")
            case 5 => new java.lang.String("V")
            case _ => new java.lang.String("0")
          }
          case "aap_id" => new java.lang.Integer(listData(i)._5)
          case "barr_descripcion" => new java.lang.String(listData(i)._6)
          case "refo_id" => new java.lang.Integer(listData(i)._7)
          case "refo_data" => listData(i)._8.trim() match {
            case v if (v.isBlank || v.isEmpty()) => new java.lang.String(emptyImg)
            case v => println("validando v: " + v)
                      if (v.isBlank() || v.isEmpty()) {
                        new java.lang.String(emptyImg)
                      } else {
                        val ruta1 = conf.get[String]("fotos.ubicacion") + v
                        val ruta2 = ruta1.replace("aap_","")
                        if (Files.exists(Paths.get(ruta1))) {
                          new java.lang.String(ruta1)
                        } else if (Files.exists(Paths.get(ruta2))){
                          new java.lang.String(ruta2)
                        } else {
                          new java.lang.String(emptyImg)
                        }
                      }
            
          }
          case "repo_fechasolucion" => dateFormat.format(listData(i)._9)
          case "tiop_descripcion" => listData(i)._10          
          case _ => None
        }
        println("Retornando: " + retorno)
        retorno
      }

}