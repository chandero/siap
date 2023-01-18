package utilities

import javax.inject.Inject

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, double, date, bool, scalar, flatten}
import anorm.JodaParameterMetaData._

import models.DatabaseExecutionContext

class DataUtil @Inject()(
    dbapi: DBApi
 )(implicit ec: DatabaseExecutionContext) {
    def getConsecutivo(id: Int): Int = {
        val db = dbapi.database("default")
        db.withTransaction { implicit connection =>
            var _numero = SQL("""SELECT gene_numero FROM siap.general WHERE gene_id = {id}""").on("id" -> id).as(scalar[Int].single)
            _numero = _numero + 1            
            SQL("""UPDATE siap.general SET gene_numero = {numero} WHERE gene_id = {id}""").on("numero" -> _numero, "id" -> id).executeUpdate()
            _numero
        }
    }
}