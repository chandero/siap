package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str, int }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime


case class Redes(aap_id: Option[Long], 
                   empr_id: Option[Long], 
                   usua_id: Option[Long], 
                   aap_direccion: Option[String], 
                   barr_id: Option[Long], 
                   esta_id: Option[Int],
                   aap_fechacreacion: Option[DateTime],
                   historia: Option[List[AapHistoria]])

case class InformeR(aap_id: Option[Int],
                    aap_direccion: Option[String], 
                    barr_descripcion: Option[String], 
                    cantidad: Option[Int])

object Redes {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val RedesWrites = new Writes[Redes] {
        def writes(m: Redes) = Json.obj(
            "aap_id" -> m.aap_id,
            "empr_id" -> m.empr_id,
            "usua_id" -> m.usua_id,            
            "aap_direccion" -> m.aap_direccion,
            "barr_id" -> m.barr_id,
            "esta_id" -> m.esta_id,
            "aap_fechacreacion" -> m.aap_fechacreacion,
            "historia" -> m.historia
        )
    }

    implicit val RedesReads: Reads[Redes] = (
        (__ \ "aap_id").readNullable[Long] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long] and
        (__ \ "aap_direccion").readNullable[String] and
        (__ \ "barr_id").readNullable[Long] and
        (__ \ "esta_id").readNullable[Int] and
        (__ \ "aap_fechacreacion").readNullable[DateTime] and
        (__ \ "historia").readNullable[List[AapHistoria]]        
    )(Redes.apply _)

    val _set = {
      get[Option[Long]]("aap_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[String]]("aap_direccion") ~      
      get[Option[Long]]("barr_id") ~
      get[Option[Int]]("esta_id") ~
      get[Option[DateTime]]("aap_fechacreacion") map {
          case aap_id ~
               empr_id ~
               usua_id ~
               aap_direccion ~
               barr_id ~               
               esta_id ~
               aap_fechacreacion => Redes(aap_id,
               empr_id,
               usua_id,
               aap_direccion,
               barr_id,
               esta_id,
               aap_fechacreacion,
               None)
      }
  }    
}

object InformeR {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val mWrites = new Writes[InformeR] {
        def writes(m: InformeR) = Json.obj(
            "aap_id" -> m.aap_id,
            "aap_direccion" -> m.aap_direccion,
            "barr_descripcion" -> m.barr_descripcion,
            "cantidad" -> m.cantidad
        )
    }

    val _set = {
      get[Option[Int]]("aap_id") ~ 
      get[Option[String]]("aap_direccion") ~
      get[Option[String]]("barr_descripcion") ~
      get[Option[Int]]("cantidad") map {
          case aap_id ~
               aap_direccion ~
               barr_descripcion ~
               cantidad => InformeR(aap_id,
                                   aap_direccion,
                                   barr_descripcion,
                                   cantidad)
      }
  }
}


class RedesRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoRedes desde un ResultSet
    */


    /**
    * Recuperar un Redes dado su aap_id
    * @param aap_id: Long
    */
    def buscarPorId(aap_id: Long, empr_id: Long) : Option[Redes] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.redes WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).
            as(Redes._set.singleOpt)
        }
    }

    def buscarSiguienteACrear(empr_id: scala.Long): Int = {
        db.withConnection { implicit connection =>
            val ultimo = SQL("""SELECT a.aap_id FROM siap.redes a WHERE a.empr_id = {empr_id} and a.aap_id <> 9999999
                                ORDER BY a.aap_id DESC LIMIT 1""").
                        on(
                            'empr_id -> empr_id
                        ).as(SqlParser.scalar[Int].singleOpt)
            ultimo match {
                case Some(u) => u + 1
                case None => 1
            }
        }
    }    

   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(empr_id: Long, filter: String): Long =  {
     db.withConnection{ implicit connection =>

      var query = """SELECT COUNT(*) AS c FROM siap.redes a WHERE a.esta_id <> 9 and a.empr_id = {empr_id}"""
         
      if (!filter.isEmpty){
          println("Filtro: " + filter)
          query = query + " and " + filter
      }        

       val result = SQL(query)
       .on(
         'empr_id -> empr_id
       )
       .as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los Redes activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todos(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[Iterable[Redes]] = Future[Iterable[Redes]] {
        db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[Redes]
        var query = """SELECT * FROM siap.redes a WHERE a.esta_id <> 9 and a.empr_id = {empr_id}"""
        
        if (!filter.isEmpty){
          println("Filtro: " + filter)
          query = query + " and " + filter
        }     

        if (!orderby.isEmpty) {
            query = query + s" ORDER BY $orderby"
        } else {
            query = query + s" ORDER BY a.empr_id, a.aap_id"
        }
        query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""

        var lista = SQL(query).
            on(
              'page_size -> page_size,
              'current_page -> current_page,
              'empr_id -> empr_id
            ).as(Redes._set *)

            for ( e <- lista ) {
                        val h = SQL("SELECT * FROM siap.redes_elemento_historia WHERE aap_id = {aap_id} and empr_id = {empr_id} ORDER BY aael_fecha DESC").
                        on(
                            'aap_id -> e.aap_id,
                            'empr_id -> empr_id
                        ).as(AapHistoria.aapelementohistoriaSet *)
                        val aaph = e.copy(historia = Some(h))
                        lista_result += aaph
            } 
            lista_result.toList            
        }        
    }

    // De Baja
/**
   * Recuperar total de registros
   * @return total
   */
   def cuentaEliminados(empr_id: Long, filter: String): Long =  {
     db.withConnection{ implicit connection =>

      var query = """SELECT COUNT(*) AS c FROM siap.redes a WHERE a.esta_id = 9 and a.empr_id = {empr_id}"""
         
      if (!filter.isEmpty){
          println("Filtro: " + filter)
          query = query + " and " + filter
      }        

       val result = SQL(query)
       .on(
         'empr_id -> empr_id
       )
       .as(SqlParser.scalar[Long].single)
       result
     }
   }
    /**
    * Recuperar todos los Redes activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todosEliminados(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[Iterable[Redes]] = Future[Iterable[Redes]] {
        db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[Redes]
        var query = """SELECT * FROM siap.redes a WHERE a.esta_id = 9 and a.empr_id = {empr_id}"""
        
        if (!filter.isEmpty){
          println("Filtro: " + filter)
          query = query + " and " + filter
        }     

        if (!orderby.isEmpty) {
            query = query + s" ORDER BY $orderby"
        } else {
            query = query + s" ORDER BY a.empr_id, a.aap_id"
        }
        query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""

        var lista = SQL(query).
            on(
              'page_size -> page_size,
              'current_page -> current_page,
              'empr_id -> empr_id
            ).as(Redes._set *)

            for ( e <- lista ) {
                        val h = SQL("SELECT * FROM siap.redes_elemento_historia WHERE aap_id = {aap_id} and empr_id = {empr_id} ORDER BY aael_fecha DESC").
                        on(
                            'aap_id -> e.aap_id,
                            'empr_id -> empr_id
                        ).as(AapHistoria.aapelementohistoriaSet *)
                        val aaph = e.copy(historia = Some(h))
                        lista_result += aaph
            } 
            lista_result.toList            
        }        
    }

    // Fin de Baja

    /**
    * Recuperar todos los TipoRedes activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def redes(empr_id: Long): Future[Iterable[Redes]] = Future[Iterable[Redes]] {
        db.withConnection { implicit connection =>
            var lista_result = new ListBuffer[Redes]
            var lista = SQL("SELECT * FROM siap.redes WHERE esta_id <> 9 and empr_id = {empr_id} ORDER BY aap_id").
            on(
              'empr_id -> empr_id
            ).
            as(Redes._set *)
            for ( e <- lista ) {

                        val h = SQL("SELECT * FROM siap.redes_elemento_historia WHERE aap_id = {aap_id} and empr_id = {empr_id} ORDER BY aael_fecha DESC").
                        on(
                            'aap_id -> e.aap_id,
                            'empr_id -> empr_id
                        ).as(AapHistoria.aapelementohistoriaSet *)
                        val aaph = e.copy(historia = Some(h))
                        lista_result += aaph
            } 
            lista_result.toList
        }        
    }    

    /**
    * Crear TipoRedes
    * @param Redes: Redes
    */
    def crear(redes: Redes) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("""INSERT INTO siap.redes (
            aap_id,
            empr_id,
            usua_id,
            aap_direccion,
            barr_id,
            esta_id,
            aap_fechacreacion) VALUES (
            {aap_id},
            {empr_id},
            {usua_id},
            {aap_direccion},
            {barr_id},
            {esta_id},
            {aap_fechacreacion})""").
            on(
              'aap_id -> redes.aap_id,
              'empr_id -> redes.empr_id,
              'usua_id -> redes.usua_id,
              'aap_direccion -> redes.aap_direccion,
              'barr_id -> redes.barr_id,
              'esta_id -> 1,
              'aap_fechacreacion -> redes.aap_fechacreacion
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> redes.usua_id,
                'audi_tabla -> "redes", 
                'audi_uid -> id,
                'audi_campo -> "aap_id", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> redes.aap_id,
                'audi_evento -> "I").
                executeInsert()

            id
        }
    }

    /**
    * Actualizar Redes
    * @param Redes: Redes
    */
    def actualizar(redes: Redes) : Boolean = {
        val redes_ant: Option[Redes] = buscarPorId(redes.aap_id.get, redes.empr_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.redes SET usua_id = {usua_id}, aap_direccion = {aap_direccion}, barr_id = {barr_id}, esta_id = {esta_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
              'aap_id -> redes.aap_id,
              'empr_id -> redes.empr_id,
              'usua_id -> redes.usua_id,
              'aap_direccion -> redes.aap_direccion,
              'barr_id -> redes.barr_id,
              'esta_id -> redes.esta_id
            ).executeUpdate() > 0

            if (redes_ant != None){
                if (redes_ant.get.aap_id != redes.aap_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> redes.usua_id,
                    'audi_tabla -> "redes", 
                    'audi_uid -> redes.aap_id,
                    'audi_campo -> "aap_id", 
                    'audi_valorantiguo -> redes_ant.get.aap_id,
                    'audi_valornuevo -> redes.aap_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (redes_ant.get.usua_id != redes.usua_id){
                    SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                            'audi_fecha -> fecha,
                            'audi_hora -> hora,
                            'usua_id -> redes.usua_id,
                            'audi_tabla -> "redes", 
                            'audi_uid -> redes.usua_id,
                            'audi_campo -> "usua_id", 
                            'audi_valorantiguo -> redes_ant.get.usua_id,
                            'audi_valornuevo -> redes.usua_id,
                            'audi_evento -> "A").
                            executeInsert()                    
                        }
                        if (redes_ant.get.aap_direccion != redes.aap_direccion){
                          SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                          on(
                              'audi_fecha -> fecha,
                              'audi_hora -> hora,
                              'usua_id -> redes.usua_id,
                              'audi_tabla -> "redes", 
                              'audi_uid -> redes.aap_direccion,
                              'audi_campo -> "aap_direccion", 
                              'audi_valorantiguo -> redes_ant.get.aap_direccion,
                              'audi_valornuevo -> redes.aap_direccion,
                              'audi_evento -> "A").
                              executeInsert()                    
                          }
                          if (redes_ant.get.barr_id != redes.barr_id){
                            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                            on(
                                'audi_fecha -> fecha,
                                'audi_hora -> hora,
                                'usua_id -> redes.usua_id,
                                'audi_tabla -> "redes", 
                                'audi_uid -> redes.barr_id,
                                'audi_campo -> "barr_id", 
                                'audi_valorantiguo -> redes_ant.get.barr_id,
                                'audi_valornuevo -> redes.barr_id,
                                'audi_evento -> "A").
                                executeInsert()                    
                            }        
            }

            result
        }
    }

    /**
    * Elimnar TipoRedes
    * @param Redes: TipoRedes
    */
    def borrar(aap_id: Long, empr_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.redes SET esta_id = 9 WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "redes", 
                    'audi_uid -> aap_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0            
        }
    }

    def buscarParaVerificar(aap_id: Long, empr_id: Long): Int = {
      db.withConnection { implicit connection =>
        var result = 0
        val _aapParser = int("aap_id") ~ int("esta_id") map { case a ~ e => (a,e)}
        val a = SQL("""SELECT a.aap_id, a.esta_id FROM siap.redes a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE a.aap_id = {aap_id} and a.empr_id = {empr_id}""").
        on(
            'aap_id -> aap_id,
            'empr_id -> empr_id
        ).as(_aapParser.singleOpt)
        a match {
            case None => result = 404
            case Some(a) => 
                if (a._2 == 9) {
                  result = 401
                } else {
                  result = 200
                }
        }
        result
      }
    }   
    
    /**
    * Recuperar un aap por su aap_id
    */
    def buscarParaEditar(aap_id: Long, empr_id: Long): Option[Redes] = {
      db.withConnection { implicit connection => 
        SQL("""SELECT a.*, b.*, t.* FROM siap.redes a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE aap_id = {aap_id} and empr_id = {empr_id}""").
        on('aap_id -> aap_id, 'empr_id-> empr_id).as(Redes._set.singleOpt)
      }
    }     

    def informe_siap_redes(empr_id: scala.Long): Future[Iterable[InformeR]] = Future[Iterable[InformeR]] {
        db.withConnection { implicit connection => 
            SQL("""SELECT m.aap_id, m.aap_direccion, b.barr_descripcion, COUNT(a.*) AS cantidad FROM siap.redes m
            LEFT JOIN siap.aap_redes am ON am.aap_id = m.aap_id AND am.empr_id = m.empr_id
            LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
            LEFT JOIN siap.barrio b ON b.barr_id = m.barr_id
            WHERE m.empr_id = {empr_id}
            GROUP BY m.aap_id, m.aap_direccion, b.barr_descripcion
            ORDER BY m.aap_id""").
            on(
                'empr_id -> empr_id
            ).
            as(InformeR._set *)
        }
    }
}