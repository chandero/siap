package utilities

import net.liftweb.json.ext.JodaTimeSerializers
import net.liftweb.json.DefaultFormats
import java.text.SimpleDateFormat
import net.liftweb.json.Formats
import net.liftweb.json.CustomSerializer
import net.liftweb.json.MappingException
import org.joda.time.DateTime
import net.liftweb.json.JsonAST.JString
import net.liftweb.json.JsonAST.JNull

trait ImplicitMobileJsonFormats {
//    implicit protected val jsonFormats = DefaultFormats ++ JodaTimeSerializers.all

    private val default: DefaultFormats with Object {def dateFormatter: SimpleDateFormat} = new DefaultFormats {
        override def dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSZ")
    }

    implicit val liftJsonFormats = default ++ JodaTimeSerializers.all
}

object DateMobileParser {
  def parse(s: String, format: Formats) = 
    format.dateFormat.parse(s).map(_.getTime).getOrElse(throw new MappingException("Invalid date format " + s))
}

case object DateTimeMobileSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => {
      // 2022-08-24T08:00:00.059-500
      if (s.length > 0) {
        println("Fecha Recibida: " + s)
        val year = s.substring(0,4)
        val month = s.substring(5,7)
        val day = s.substring(8,10)
        var hour = "08"
        var minute = "00"
        var second = "00"
        if (s.length > 10) {
          hour = s.substring(11,13)
          minute = s.substring(14, 16)
          second = s.substring(17,19)
        }
        println("Year:" + year)
        println("Month:" + month)
        println("Day:" + day)
        println("Hour:" + hour)
        println("Minute:" + minute)
        println("Second:" + second)
        new DateTime(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt)
      } else { null }
    }
      //new DateTime(DateMobileParser.parse(s, format))
    case JNull => null
  },
  {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }
))

/* case object DateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => {
      val parser1 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      val parser2 = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

      val parsers = List[java.text.SimpleDateFormat](parser1, parser2)

      parsers.foreach { parser =>
        try {
          val result = parser.parse(s)
          result
        } catch {
           case _: Throwable => println("date deserialization continue...")
        }
      }

      null
    }
    case JNull => null
  },
  {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }
)) */