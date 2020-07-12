package jrds

import scala.collection.mutable.ListBuffer
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JRField

import java.util.{ HashMap, Map }


class ReporteDS(listData: ListBuffer[ReporteDS]) extends JRDataSource {

    var i = -1

    override def next(): Boolean = {
        i += 1
        i < listData.length
      }

    override def getFieldValue(jrField: JRField): AnyRef = {
      /*
        jrField.getName match {
          case "codigo" ⇒ listData(i).codigo.get.substring(0,4)
          case "nombre" ⇒ listData(i).nombre.get
          case "anterior" ⇒ listData(i).saldo_inicial.get.bigDecimal
          case "debito" ⇒ listData(i).debito.get.bigDecimal
          case "credito" ⇒ listData(i).credito.get.bigDecimal
          case "actual" ⇒ listData(i).saldo_final.get.bigDecimal
          case _ ⇒ None
        }
      */
        None
      }
}