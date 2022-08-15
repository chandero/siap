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


case class Poste(aap_id: Option[Long], 
                   empr_id: Option[Long], 
                   usua_id: Option[Long], 
                   aap_direccion: Option[String], 
                   barr_id: Option[Long], 
                   esta_id: Option[Int],
                   tipo_id: Option[Long],
                   aap_poste_altura: Option[Double],
                   aap_poste_propietario: Option[String],
                   aap_fechacreacion: Option[DateTime],
                   historia: Option[List[AapHistoria]])

case class InformeP(aap_id: Option[Int],
                    aap_direccion: Option[String], 
                    barr_descripcion: Option[String], 
                    cantidad: Option[Int])

object Poste {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val PosteWrites = new Writes[Poste] {
        def writes(m: Poste) = Json.obj(
            "aap_id" -> m.aap_id,
            "empr_id" -> m.empr_id,
            "usua_id" -> m.usua_id,            
            "aap_direccion" -> m.aap_direccion,
            "barr_id" -> m.barr_id,
            "esta_id" -> m.esta_id,
            "tipo_id" -> m.tipo_id,
            "aap_poste_altura" -> m.aap_poste_altura,
            "aap_poste_propietario" -> m.aap_poste_propietario,
            "aap_fechacreacion" -> m.aap_fechacreacion,
            "historia" -> m.historia
        )
    }

    implicit val PosteReads: Reads[Poste] = (
        (__ \ "aap_id").readNullable[Long] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "usua_id").readNullable[Long] and
        (__ \ "aap_direccion").readNullable[String] and
        (__ \ "barr_id").readNullable[Long] and
        (__ \ "esta_id").readNullable[Int] and
        (__ \ "tipo_id").readNullable[Long] and
        (__ \ "aap_poste_altura").readNullable[Double] and
        (__ \ "aap_poste_propietario").readNullable[String] and
        (__ \ "aap_fechacreacion").readNullable[DateTime] and
        (__ \ "historia").readNullable[List[AapHistoria]]        
    )(Poste.apply _)

    val _set = {
      get[Option[Long]]("aap_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[String]]("aap_direccion") ~      
      get[Option[Long]]("barr_id") ~
      get[Option[Int]]("esta_id") ~
      get[Option[Long]]("tipo_id") ~
      get[Option[Double]]("aap_poste_altura") ~
      get[Option[String]]("aap_poste_propietario") ~
      get[Option[DateTime]]("aap_fechacreacion") map {
          case aap_id ~
               empr_id ~
               usua_id ~
               aap_direccion ~
               barr_id ~               
               esta_id ~
               tipo_id ~
               aap_poste_altura ~
               aap_poste_propietario ~
               aap_fechacreacion => Poste(aap_id,
               empr_id,
               usua_id,
               aap_direccion,
               barr_id,
               esta_id,
               tipo_id,
               aap_poste_altura,
               aap_poste_propietario,
               aap_fechacreacion,
               None)
      }
  }    
}

object InformeP {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val mWrites = new Writes[InformeP] {
        def writes(m: InformeP) = Json.obj(
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
               cantidad => InformeP(aap_id,
                                   aap_direccion,
                                   barr_descripcion,
                                   cantidad)
      }
  }
}


class PosteRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
    * Parsear un TipoPoste desde un ResultSet
    */


    /**
    * Recuperar un Poste dado su aap_id
    * @param aap_id: Long
    */
    def buscarPorId(aap_id: Long, empr_id: Long) : Option[Poste] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.poste WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).
            as(Poste._set.singleOpt)
        }
    }

    def buscarSiguienteACrear(empr_id: scala.Long): Int = {
        db.withConnection { implicit connection =>
            val ultimo = SQL("""SELECT a.aap_id FROM siap.poste a WHERE a.empr_id = {empr_id} and a.aap_id <> 9999999
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

      var query = """SELECT COUNT(*) AS c FROM siap.poste a WHERE a.esta_id <> 9 and a.empr_id = {empr_id}"""
         
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
    * Recuperar todos los Poste activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todos(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[Iterable[Poste]] = Future[Iterable[Poste]] {
        db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[Poste]
        var query = """SELECT * FROM siap.poste a WHERE a.esta_id <> 9 and a.empr_id = {empr_id}"""
        
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
            ).as(Poste._set *)

            for ( e <- lista ) {
                        val h = SQL("SELECT * FROM siap.poste_elemento_historia WHERE aap_id = {aap_id} and empr_id = {empr_id} ORDER BY aael_fecha DESC").
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

      var query = """SELECT COUNT(*) AS c FROM siap.poste a WHERE a.esta_id = 9 and a.empr_id = {empr_id}"""
         
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
    * Recuperar todos los Poste activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
    def todosEliminados(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[Iterable[Poste]] = Future[Iterable[Poste]] {
        db.withConnection { implicit connection =>
        var lista_result = new ListBuffer[Poste]
        var query = """SELECT * FROM siap.poste a WHERE a.esta_id = 9 and a.empr_id = {empr_id}"""
        
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
            ).as(Poste._set *)

            for ( e <- lista ) {
                        val h = SQL("SELECT * FROM siap.poste_elemento_historia WHERE aap_id = {aap_id} and empr_id = {empr_id} ORDER BY aael_fecha DESC").
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
    * Recuperar todos los TipoPoste activos
    * @param page_size: Long
    * @param current_page: Long
    */
    def postes(empr_id: Long): Future[Iterable[Poste]] = Future[Iterable[Poste]] {
        db.withConnection { implicit connection =>
            var lista_result = new ListBuffer[Poste]
            var lista = SQL("SELECT * FROM siap.poste WHERE esta_id <> 9 and empr_id = {empr_id} ORDER BY aap_id").
            on(
              'empr_id -> empr_id
            ).
            as(Poste._set *)
            for ( e <- lista ) {

                        val h = SQL("SELECT * FROM siap.poste_elemento_historia WHERE aap_id = {aap_id} and empr_id = {empr_id} ORDER BY aael_fecha DESC").
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
    * Crear TipoPoste
    * @param Poste: Poste
    */
    def crear(poste: Poste) : Future[Long] = Future {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
        db.withConnection { implicit connection =>
            val id: Long = SQL("""INSERT INTO siap.poste (
            aap_id,
            empr_id,
            usua_id,
            aap_direccion,
            barr_id,
            esta_id,
            tipo_id,
            aap_poste_altura,
            aap_poste_propietario,
            aap_fechacreacion) VALUES (
            {aap_id},
            {empr_id},
            {usua_id},
            {aap_direccion},
            {barr_id},
            {esta_id},
            {tipo_id},
            {aap_poste_altura},
            {aap_poste_propietario},            
            {aap_fechacreacion})""").
            on(
              'aap_id -> poste.aap_id,
              'empr_id -> poste.empr_id,
              'usua_id -> poste.usua_id,
              'aap_direccion -> poste.aap_direccion,
              'barr_id -> poste.barr_id,
              'esta_id -> 1,
              'tipo_id -> poste.tipo_id,
              'aap_poste_altura -> poste.aap_poste_altura,
              'aap_poste_propietario -> poste.aap_poste_propietario,
              'aap_fechacreacion -> poste.aap_fechacreacion
            ).executeInsert().get

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> poste.usua_id,
                'audi_tabla -> "poste", 
                'audi_uid -> id,
                'audi_campo -> "aap_id", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> poste.aap_id,
                'audi_evento -> "I").
                executeInsert()

            id
        }
    }

    /**
    * Actualizar Poste
    * @param Poste: Poste
    */
    def actualizar(poste: Poste) : Boolean = {
        val poste_ant: Option[Poste] = buscarPorId(poste.aap_id.get, poste.empr_id.get)
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val result: Boolean = SQL("UPDATE siap.poste SET usua_id = {usua_id}, aap_direccion = {aap_direccion}, barr_id = {barr_id}, esta_id = {esta_id}, tipo_id = {tipo_id}, aap_poste_altura = {aap_poste_altura}, aap_poste_propietario = {aap_poste_propietario} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
              'aap_id -> poste.aap_id,
              'empr_id -> poste.empr_id,
              'usua_id -> poste.usua_id,
              'aap_direccion -> poste.aap_direccion,
              'barr_id -> poste.barr_id,
              'esta_id -> poste.esta_id,
              'tipo_id -> poste.tipo_id,
              'aap_poste_altura -> poste.aap_poste_altura,
              'aap_poste_propietario -> poste.aap_poste_propietario              
            ).executeUpdate() > 0

            if (poste_ant != None){
                if (poste_ant.get.aap_id != poste.aap_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> poste.usua_id,
                    'audi_tabla -> "poste", 
                    'audi_uid -> poste.aap_id,
                    'audi_campo -> "aap_id", 
                    'audi_valorantiguo -> poste_ant.get.aap_id,
                    'audi_valornuevo -> poste.aap_id,
                    'audi_evento -> "A").
                    executeInsert()                    
                }
                if (poste_ant.get.usua_id != poste.usua_id){
                    SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                    on(
                            'audi_fecha -> fecha,
                            'audi_hora -> hora,
                            'usua_id -> poste.usua_id,
                            'audi_tabla -> "poste", 
                            'audi_uid -> poste.usua_id,
                            'audi_campo -> "usua_id", 
                            'audi_valorantiguo -> poste_ant.get.usua_id,
                            'audi_valornuevo -> poste.usua_id,
                            'audi_evento -> "A").
                            executeInsert()                    
                        }
                        if (poste_ant.get.aap_direccion != poste.aap_direccion){
                          SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                          on(
                              'audi_fecha -> fecha,
                              'audi_hora -> hora,
                              'usua_id -> poste.usua_id,
                              'audi_tabla -> "poste", 
                              'audi_uid -> poste.aap_direccion,
                              'audi_campo -> "aap_direccion", 
                              'audi_valorantiguo -> poste_ant.get.aap_direccion,
                              'audi_valornuevo -> poste.aap_direccion,
                              'audi_evento -> "A").
                              executeInsert()                    
                          }
                          if (poste_ant.get.barr_id != poste.barr_id){
                            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                            on(
                                'audi_fecha -> fecha,
                                'audi_hora -> hora,
                                'usua_id -> poste.usua_id,
                                'audi_tabla -> "poste", 
                                'audi_uid -> poste.barr_id,
                                'audi_campo -> "barr_id", 
                                'audi_valorantiguo -> poste_ant.get.barr_id,
                                'audi_valornuevo -> poste.barr_id,
                                'audi_evento -> "A").
                                executeInsert()                    
                            }        
            }

            result
        }
    }

    /**
    * Elimnar TipoPoste
    * @param Poste: TipoPoste
    */
    def borrar(aap_id: Long, empr_id: Long, usua_id: Long): Boolean = {
        val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        

        db.withConnection { implicit connection => 
            val count: Long = SQL("UPDATE siap.poste SET esta_id = 9 WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).executeUpdate()

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "poste", 
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
        val a = SQL("""SELECT a.aap_id, a.esta_id FROM siap.poste a
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
    def buscarParaEditar(aap_id: Long, empr_id: Long): Option[Poste] = {
      db.withConnection { implicit connection => 
        SQL("""SELECT a.*, b.*, t.* FROM siap.poste a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE aap_id = {aap_id} and empr_id = {empr_id}""").
        on('aap_id -> aap_id, 'empr_id-> empr_id).as(Poste._set.singleOpt)
      }
    }     

    def informe_siap_poste(empr_id: scala.Long): Future[Iterable[InformeP]] = Future[Iterable[InformeP]] {
        db.withConnection { implicit connection => 
            SQL("""SELECT m.aap_id, m.aap_direccion, b.barr_descripcion, COUNT(a.*) AS cantidad FROM siap.poste m
            LEFT JOIN siap.aap_poste am ON am.aap_id = m.aap_id AND am.empr_id = m.empr_id
            LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
            LEFT JOIN siap.barrio b ON b.barr_id = m.barr_id
            WHERE m.empr_id = {empr_id}
            GROUP BY m.aap_id, m.aap_direccion, b.barr_descripcion
            ORDER BY m.aap_id""").
            on(
                'empr_id -> empr_id
            ).
            as(InformeP._set *)
        }
    }
}