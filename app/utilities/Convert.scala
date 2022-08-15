package utilities

class Convert {
    def toDouble(s: Option[String]): Option[Double] = {
        s match {
         case Some(s) => { 
             try {
                Some((s.trim).toDouble)
             } catch {
                case e: Exception => None
             }
         }
         case None => {
             None
          }
        }
    }
}

object Convert extends Convert