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

trait ImplicitJsonFormats {
//    implicit protected val jsonFormats = DefaultFormats ++ JodaTimeSerializers.all

    private val default: DefaultFormats with Object {def dateFormatter: SimpleDateFormat} = new DefaultFormats {
        override def dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }
    implicit val liftJsonFormats = default ++ JodaTimeSerializers.all
}

object DateParser {
  def parse(s: String, format: Formats) = 
    format.dateFormat.parse(s).map(_.getTime).getOrElse(throw new MappingException("Invalid date format " + s))
}

case object DateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => new DateTime(DateParser.parse(s, format))
    case JNull => null
  },
  {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }
))