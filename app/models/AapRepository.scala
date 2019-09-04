package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str, int, double, scalar, flatten }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

import utilities.Convert

case class AapAdicional(aap_id: Option[scala.Long],
                        tipo_id: Option[scala.Long],
                        aap_poste_altura: Option[Double],
                        aap_brazo: Option[String],
                        aap_collarin: Option[String],
                        aap_potencia: Option[Int],
                        aap_tecnologia: Option[String],
                        aap_modernizada_anho: Option[Int],
                        aap_rte: Option[String],
                        aap_poste_propietario: Option[String]
                        )

case class AapElemento(aap_id: Option[scala.Long],
                       aael_fecha: Option[DateTime],
                       aap_bombillo: Option[String],
                       aap_balasto: Option[String],
                       aap_arrancador: Option[String],
                       aap_condensador: Option[String],
                       aap_fotocelda: Option[String],
                       reti_id: Option[scala.Long],
                       repo_consecutivo: Option[Int]
                       )

case class AapHistoria(aap_id: Option[scala.Long],
                       aael_fecha: Option[DateTime],
                       aap_bombillo_retirado: Option[String],
                       aap_bombillo_instalado: Option[String],
                       aap_balasto_retirado: Option[String],
                       aap_balasto_instalado: Option[String],
                       aap_arrancador_retirado: Option[String],
                       aap_arrancador_instalado: Option[String],
                       aap_condensador_retirado: Option[String],
                       aap_condensador_instalado: Option[String],
                       aap_fotocelda_retirado: Option[String],
                       aap_fotocelda_instalado: Option[String],
                       reti_id: Option[scala.Long],
                       repo_consecutivo: Option[Int]
                       )


object AapElemento {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aaelWrites = new Writes[AapElemento] {
        def writes(aael: AapElemento) = Json.obj(
            "aap_id" -> aael.aap_id,
            "aael_fecha" -> aael.aael_fecha,
            "aap_bombillo" -> aael.aap_bombillo,
            "aap_balasto" -> aael.aap_balasto,
            "aap_arrancador" -> aael.aap_arrancador,
            "aap_condensador" -> aael.aap_condensador,
            "aap_fotocelda" -> aael.aap_fotocelda,
            "reti_id" -> aael.reti_id,
            "repo_consecutivo" -> aael.repo_consecutivo
        )
    }

    implicit val aaelReads: Reads[AapElemento] = (
       (__ \ "aap_id").readNullable[Long] and
       (__ \ "aael_fecha").readNullable[DateTime] and
       (__ \ "aap_bombillo").readNullable[String] and
       (__ \ "aap_balasto").readNullable[String] and
       (__ \ "aap_arrancador").readNullable[String] and
       (__ \ "aap_condensador").readNullable[String] and
       (__ \ "aap_fotocelda").readNullable[String] and
       (__ \ "reti_id").readNullable[scala.Long] and
       (__ \ "repo_consecutivo").readNullable[Int]
    )(AapElemento.apply _)

    val aapelementoSet = {
        get[Option[Long]]("aap_elemento.aap_id") ~
        get[Option[DateTime]]("aap_elemento.aael_fecha") ~
        get[Option[String]]("aap_bombillo") ~
        get[Option[String]]("aap_balasto") ~
        get[Option[String]]("aap_arrancador") ~
        get[Option[String]]("aap_condensador") ~
        get[Option[String]]("aap_fotocelda") ~
        get[Option[scala.Long]]("reti_id") ~
        get[Option[Int]]("repo_consecutivo") map {
         case aap_id ~
              aael_fecha ~
              aap_bombillo ~
              aap_balasto ~
              aap_arrancador ~
              aap_condensador ~
              aap_fotocelda ~
              reti_id ~
              repo_consecutivo => AapElemento(aap_id, 
                                              aael_fecha,
                                              aap_bombillo,
                                              aap_balasto,
                                              aap_arrancador,
                                              aap_condensador,
                                              aap_fotocelda,
                                              reti_id,
                                              repo_consecutivo)
        }
    }
}

object AapAdicional {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val adicionalWrites = new Writes[AapAdicional] {
        def writes(a: AapAdicional) = Json.obj(
            "aap_id" -> a.aap_id,
            "tipo_id" -> a.tipo_id,
            "aap_poste_altura" -> a.aap_poste_altura,
            "aap_brazo" -> a.aap_brazo,
            "aap_collarin" -> a.aap_collarin,
            "aap_potencia" -> a.aap_potencia,
            "aap_tecnologia" -> a.aap_tecnologia,
            "aap_modernizada_anho" -> a.aap_modernizada_anho,
            "aap_rte" -> a.aap_rte,
            "aap_poste_propietario" -> a.aap_poste_propietario
        )
    }

    implicit val adicionalReads: Reads[AapAdicional] = (
       (__ \ "aap_id").readNullable[scala.Long] and
       (__ \ "tipo_id").readNullable[scala.Long] and
       (__ \ "aap_poste_altura").readNullable[Double] and
       (__ \ "aap_brazo").readNullable[String] and
       (__ \ "aap_collarin").readNullable[String] and
       (__ \ "aap_potencia").readNullable[Int] and
       (__ \ "aap_tecnologia").readNullable[String] and
       (__ \ "aap_modernizada_anho").readNullable[Int] and
       (__ \ "aap_rte").readNullable[String] and
       (__ \ "aap_poste_propietario").readNullable[String]
    )(AapAdicional.apply _)

    val aapadicionalSet = {
        get[Option[scala.Long]]("aap_id") ~
        get[Option[scala.Long]]("tipo_id") ~
        get[Option[Double]]("aap_poste_altura") ~
        get[Option[String]]("aap_brazo") ~
        get[Option[String]]("aap_collarin") ~
        get[Option[Int]]("aap_potencia") ~
        get[Option[String]]("aap_tecnologia") ~
        get[Option[Int]]("aap_modernizada_anho") ~
        get[Option[String]]("aap_rte") ~ 
        get[Option[String]]("aap_poste_propietario") map {
         case aap_id ~
              tipo_id ~
              aap_poste_altura ~
              aap_brazo ~
              aap_collarin ~
              aap_potencia ~
              aap_tecnologia ~
              aap_modernizada_anho ~
              aap_rte ~
              aap_poste_propietario => AapAdicional(aap_id, 
                                        tipo_id,
                                        aap_poste_altura,
                                        aap_brazo,
                                        aap_collarin,
                                        aap_potencia,
                                        aap_tecnologia,
                                        aap_modernizada_anho,
                                        aap_rte,
                                        aap_poste_propietario)
        }
    }
}


object AapHistoria {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aaelhiWrites = new Writes[AapHistoria] {
        def writes(aaelhi: AapHistoria) = Json.obj(
            "aap_id" -> aaelhi.aap_id,
            "aael_fecha" -> aaelhi.aael_fecha,
            "aap_bombillo_retirado" -> aaelhi.aap_bombillo_retirado,
            "aap_bombillo_instalado" -> aaelhi.aap_bombillo_instalado,
            "aap_balasto_retirado" -> aaelhi.aap_balasto_retirado,
            "aap_balasto_instalado" -> aaelhi.aap_balasto_instalado,
            "aap_arrancador_retirado" -> aaelhi.aap_arrancador_retirado,
            "aap_arrancador_instalado" -> aaelhi.aap_arrancador_instalado,
            "aap_condensador_retirado" -> aaelhi.aap_condensador_retirado,
            "aap_condensador_instalado" -> aaelhi.aap_condensador_instalado,
            "aap_fotocelda_retirado" -> aaelhi.aap_fotocelda_retirado,
            "aap_fotocelda_instalado" -> aaelhi.aap_fotocelda_instalado,
            "reti_id" -> aaelhi.reti_id,
            "repo_consecutivo" -> aaelhi.repo_consecutivo
        )
    }

    implicit val aaelhiReads: Reads[AapHistoria] = (
       (__ \ "aap_id").readNullable[Long] and
       (__ \ "aael_fecha").readNullable[DateTime] and
       (__ \ "aap_bombillo_retirado").readNullable[String] and
       (__ \ "aap_bombillo_instalado").readNullable[String] and
       (__ \ "aap_balasto_retirado").readNullable[String] and
       (__ \ "aap_balasto_instalado").readNullable[String] and
       (__ \ "aap_arrancador_retirado").readNullable[String] and
       (__ \ "aap_arrancador_instalado").readNullable[String] and
       (__ \ "aap_condensador_retirado").readNullable[String] and
       (__ \ "aap_condensador_instalado").readNullable[String] and
       (__ \ "aap_fotocelda_retirado").readNullable[String] and
       (__ \ "aap_fotocelda_instalado").readNullable[String] and
       (__ \ "reti_id").readNullable[scala.Long] and
       (__ \ "repo_consecutivo").readNullable[Int]
    )(AapHistoria.apply _)

    val aapelementohistoriaSet = {
        get[Option[Long]]("aap_id") ~
        get[Option[DateTime]]("aael_fecha") ~
        get[Option[String]]("aap_bombillo_retirado") ~
        get[Option[String]]("aap_bombillo_instalado") ~
        get[Option[String]]("aap_balasto_retirado") ~
        get[Option[String]]("aap_balasto_instalado") ~
        get[Option[String]]("aap_arrancador_retirado") ~
        get[Option[String]]("aap_arrancador_instalado") ~
        get[Option[String]]("aap_condensador_retirado") ~
        get[Option[String]]("aap_condensador_instalado") ~
        get[Option[String]]("aap_fotocelda_retirado") ~
        get[Option[String]]("aap_fotocelda_instalado") ~
        get[Option[scala.Long]]("reti_id") ~
        get[Option[Int]]("repo_consecutivo") map {
         case aap_id ~
              aael_fecha ~
              aap_bombillo_retirado ~
              aap_bombillo_instalado ~
              aap_balasto_retirado ~
              aap_balasto_instalado ~
              aap_arrancador_retirado ~
              aap_arrancador_instalado ~
              aap_condensador_retirado ~
              aap_condensador_instalado ~
              aap_fotocelda_retirado ~
              aap_fotocelda_instalado ~
              reti_id ~
              repo_consecutivo => AapHistoria(aap_id, 
                                              aael_fecha,
                                              aap_bombillo_retirado,
                                              aap_bombillo_instalado,
                                              aap_balasto_retirado,
                                              aap_balasto_instalado,
                                              aap_arrancador_retirado,
                                              aap_arrancador_instalado,
                                              aap_condensador_retirado,
                                              aap_condensador_instalado,
                                              aap_fotocelda_retirado,
                                              aap_fotocelda_instalado,
                                              reti_id,
                                              repo_consecutivo)
        }
    }
}

case class Aap(aap_id: Option[Long], 
               aap_apoyo: Option[String], 
               aap_descripcion: Option[String], 
               aap_direccion: Option[String], 
               aap_lat: Option[String], 
               aap_lng: Option[String], 
               barr_id: Option[Long],
               tiba_id: Option[Long],
               empr_id: Long,
               aap_fechacreacion: Option[DateTime],
               aap_fechatoma: Option[DateTime],
               aap_modernizada: Option[Boolean], 
               aama_id: Option[Long],
               aacu_id: Option[Long],
               aap_medidor: Option[Boolean],
               aatc_id: Option[Long],
               aamo_id: Option[Long],
               aaus_id: Option[Long],
               aaco_id: Option[Long],
               usua_id: Long,
               aap_elemento: Option[AapElemento],
               historia: Option[List[AapHistoria]])

case class AapConsulta(aap_id: Option[Long], 
               aap_apoyo: Option[String], 
               aap_descripcion: Option[String], 
               aap_direccion: Option[String], 
               aap_lat: Option[String], 
               aap_lng: Option[String], 
               barr_descripcion: Option[String],
               tiba_descripcion: Option[String],
               empr_id: Long,
               aap_fechacreacion: Option[DateTime],
               aap_fechatoma: Option[DateTime],
               aap_modernizada: Option[Boolean], 
               aama_descripcion: Option[String],
               aacu_descripcion: Option[String],
               aap_medidor: Option[Boolean],
               aatc_descripcion: Option[String],
               aamo_descripcion: Option[String],
               aaus_descripcion: Option[String],
               aaco_descripcion: Option[String])               

case class Aap_medidor(aap_id: Option[Long], amet_id: Option[Long], aame_numero: Option[String], amem_id: Option[Long], empr_id: Option[Long], medi_id: Option[Long])

case class Aap_transformador(aap_id: Option[Long], empr_id: Option[Long], tran_id: Option[Long])

object Aap {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aapWrites = new Writes[Aap] {
     def writes(aap: Aap) = Json.obj(
         "aap_id" -> aap.aap_id,
         "aap_apoyo" -> aap.aap_apoyo,
         "aap_descripcion" -> aap.aap_descripcion,
         "aap_direccion" -> aap.aap_direccion,
         "aap_lat" -> aap.aap_lat,
         "aap_lng" -> aap.aap_lng,
         "barr_id" -> aap.barr_id,
         "tiba_id" -> aap.tiba_id,
         "empr_id" -> aap.empr_id,
         "aap_fechacreacion" -> aap.aap_fechacreacion,
         "aap_fechatoma" -> aap.aap_fechatoma,
         "aap_modernizada" -> aap.aap_modernizada,
         "aama_id" -> aap.aama_id,
         "aacu_id" -> aap.aacu_id,
         "aap_medidor" -> aap.aap_medidor,
         "aatc_id" -> aap.aatc_id,
         "aamo_id" -> aap.aamo_id,
         "aaus_id" -> aap.aaus_id,
         "aaco_id" -> aap.aaco_id,
         "usua_id" -> aap.usua_id,
         "aap_elemento" -> aap.aap_elemento,
         "historia" -> aap.historia
     )
    }

    implicit val aapReads: Reads[Aap] = (
       (__ \ "aap_id").readNullable[Long] and
        (__ \ "aap_apoyo").readNullable[String] and
        (__ \ "aap_descripcion").readNullable[String] and
        (__ \ "aap_direccion").readNullable[String] and
        (__ \ "aap_lat").readNullable[String] and
        (__ \ "aap_lng").readNullable[String] and
        (__ \ "barr_id").readNullable[Long] and
        (__ \ "tiba_id").readNullable[Long] and
        (__ \ "empr_id").read[Long] and
        (__ \ "aap_fechacreacion").readNullable[DateTime] and
        (__ \ "aap_fechatoma").readNullable[DateTime] and
        (__ \ "aap_modernizada").readNullable[Boolean] and
        (__ \ "aama_id").readNullable[Long] and
        (__ \ "aacu_id").readNullable[Long] and
        (__ \ "aap_medidor").readNullable[Boolean] and
        (__ \ "aatc_id").readNullable[Long] and
        (__ \ "aamo_id").readNullable[Long] and
        (__ \ "aaus_id").readNullable[Long] and
        (__ \ "aaco_id").readNullable[Long] and
        (__ \ "usua_id").read[Long] and
        (__ \ "aap_elemento").readNullable[AapElemento] and
        (__ \ "historia").readNullable[List[AapHistoria]]
    )(Aap.apply _)
}

object AapConsulta {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aapWrites = new Writes[AapConsulta] {
     def writes(aap: AapConsulta) = Json.obj(
         "aap_id" -> aap.aap_id,
         "aap_apoyo" -> aap.aap_apoyo,
         "aap_descripcion" -> aap.aap_descripcion,
         "aap_direccion" -> aap.aap_direccion,
         "aap_lat" -> aap.aap_lat,
         "aap_lng" -> aap.aap_lng,
         "barr_descripcion" -> aap.barr_descripcion,
         "tiba_descripcion" -> aap.tiba_descripcion,
         "empr_id" -> aap.empr_id,
         "aap_fechacreacion" -> aap.aap_fechacreacion,
         "aap_fechatoma" -> aap.aap_fechatoma,
         "aap_modernizada" -> aap.aap_modernizada,
         "aama_descripcion" -> aap.aama_descripcion,
         "aacu_descripcion" -> aap.aacu_descripcion,
         "aap_medidor" -> aap.aap_medidor,
         "aatc_descripcion" -> aap.aatc_descripcion,
         "aamo_descripcion" -> aap.aamo_descripcion,
         "aaus_descripcion" -> aap.aaus_descripcion,
         "aaco_descripcion" -> aap.aaco_descripcion
     )
    }

    implicit val aapReads: Reads[AapConsulta] = (
       (__ \ "aap_id").readNullable[Long] and
        (__ \ "aap_apoyo").readNullable[String] and
        (__ \ "aap_descripcion").readNullable[String] and
        (__ \ "aap_direccion").readNullable[String] and
        (__ \ "aap_lat").readNullable[String] and
        (__ \ "aap_lng").readNullable[String] and
        (__ \ "barr_descripcion").readNullable[String] and
        (__ \ "tiba_descripcion").readNullable[String] and
        (__ \ "empr_id").read[Long] and
        (__ \ "aap_fechacreacion").readNullable[DateTime] and
        (__ \ "aap_fechatoma").readNullable[DateTime] and
        (__ \ "aap_modernizada").readNullable[Boolean] and
        (__ \ "aama_descripcion").readNullable[String] and
        (__ \ "aacu_descripcion").readNullable[String] and
        (__ \ "aap_medidor").readNullable[Boolean] and
        (__ \ "aatc_descripcion").readNullable[String] and
        (__ \ "aamo_descripcion").readNullable[String] and
        (__ \ "aaus_descripcion").readNullable[String] and
        (__ \ "aaco_descripcion").readNullable[String]
    )(AapConsulta.apply _)

    val _set = {
        get[Option[Long]]("aap.aap_id") ~ 
          get[Option[String]]("aap.aap_apoyo") ~
          get[Option[String]]("aap.aap_descripcion") ~
          get[Option[String]]("aap.aap_direccion") ~
          get[Option[String]]("aap_lat") ~
          get[Option[String]]("aap_lng") ~
          get[Option[String]]("aap.barr_descripcion") ~
          get[Option[String]]("tiba_descripcion") ~
          get[Long]("aap.empr_id") ~
          get[Option[DateTime]]("aap.aap_fechacreacion") ~
          get[Option[DateTime]]("aap.aap_fechatoma") ~
          get[Option[Boolean]]("aap.aap_modernizada") ~
          get[Option[String]]("aap.aama_descripcion") ~
          get[Option[String]]("aap.aacu_descripcion") ~
          get[Option[Boolean]]("aap.aap_medidor") ~
          get[Option[String]]("aap.aatc_descripcion") ~
          get[Option[String]]("aap.aamo_descripcion") ~
          get[Option[String]]("aap.aaus_descripcion") ~
          get[Option[String]]("aap.aaco_descripcion") map {
            case aap_id ~ 
                 aap_apoyo ~ 
                 aap_descripcion ~ 
                 aap_direccion ~ 
                 aap_lat ~ 
                 aap_lng ~ 
                 barr_descripcion ~ 
                 tiba_descripcion ~
                 empr_id ~ 
                 aap_fechacreacion ~ 
                 aap_fechatoma ~
                 aap_modernizada ~
                 aama_descripcion ~
                 aacu_descripcion ~
                 aap_medidor ~
                 aatc_descripcion ~
                 aamo_descripcion ~
                 aaus_descripcion ~
                 aaco_descripcion => AapConsulta(aap_id, 
                                aap_apoyo, 
                                aap_descripcion, 
                                aap_direccion, 
                                aap_lat, 
                                aap_lng, 
                                barr_descripcion,
                                tiba_descripcion,
                                empr_id, 
                                aap_fechacreacion, 
                                aap_fechatoma,
                                aap_modernizada,
                                aama_descripcion,
                                aacu_descripcion,
                                aap_medidor,
                                aatc_descripcion,
                                aamo_descripcion,
                                aaus_descripcion,
                                aaco_descripcion)
        }
    }
}

object Aap_medidor {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val aameWrites = new Writes[Aap_medidor] {
        def writes(aame: Aap_medidor) = Json.obj(
            "aap_id" -> aame.aap_id,
            "amet_id" -> aame.amet_id,
            "aame_numero" -> aame.aame_numero,
            "amem_id" -> aame.amem_id,
            "empr_id" -> aame.empr_id,
            "medi_id" -> aame.medi_id
        )
    }

    implicit val aameReads: Reads[Aap_medidor] = (
       (__ \ "aap_id").readNullable[Long] and
       (__ \ "amet_id").readNullable[Long] and
       (__ \ "aame_numero").readNullable[String] and
       (__ \ "amem_id").readNullable[Long] and
       (__ \ "empr_id").readNullable[Long] and
       (__ \ "medi_id").readNullable[Long]
    )(Aap_medidor.apply _)

    val aapmedidorSet = {
        get[Option[Long]]("aap_id") ~ 
          get[Option[Long]]("amet_id") ~
          get[Option[String]]("aame_numero") ~
          get[Option[Long]]("amem_id") ~
          get[Option[Long]]("empr_id") ~
          get[Option[Long]]("medi_id") map {
            case aap_id ~ 
                 amet_id ~ 
                 aame_numero ~
                 amem_id ~ 
                 empr_id ~
                 medi_id => Aap_medidor(aap_id, 
                                amet_id, 
                                aame_numero,
                                amem_id,
                                empr_id,
                                medi_id)
        }
    }
}

object Aap_transformador {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val tWrites = new Writes[Aap_transformador] {
        def writes(t: Aap_transformador) = Json.obj(
            "aap_id" -> t.aap_id,
            "empr_id" -> t.empr_id,
            "tran_id" -> t.tran_id
        )
    }

    implicit val tReads: Reads[Aap_transformador] = (
       (__ \ "aap_id").readNullable[Long] and
       (__ \ "empr_id").readNullable[Long] and
       (__ \ "tran_id").readNullable[Long]
    )(Aap_transformador.apply _)

    val _set = {
        get[Option[Long]]("aap_id") ~ 
        get[Option[Long]]("empr_id") ~
        get[Option[Long]]("tran_id") map {
            case aap_id ~ 
                 empr_id ~
                 tran_id => Aap_transformador(aap_id, 
                                empr_id,
                                tran_id)
        }
    }
}

case class Activo(aap: Option[Aap], aame: Option[Aap_medidor], aatr: Option[Aap_transformador], aap_adicional: Option[AapAdicional], aap_elemento: Option[AapElemento], autorizacion: Option[String], esta_id: Option[Int])

object Activo {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val activoWrites = new Writes[Activo] {
        def writes(activo: Activo) = Json.obj(
            "aap" -> activo.aap,
            "aame" -> activo.aame,
            "aatr" -> activo.aatr,
            "aap_adicional" -> activo.aap_adicional,
            "aap_elemento" -> activo.aap_elemento,
            "autorizacion" -> activo.autorizacion,
            "esta_id" -> activo.esta_id
        )
    }

    implicit val activoReads: Reads[Activo] = (
       (__ \ "aap").readNullable[Aap] and
       (__ \ "aame").readNullable[Aap_medidor] and
       (__ \ "aatr").readNullable[Aap_transformador] and
       (__ \ "aap_adicional").readNullable[AapAdicional] and
       (__ \ "aap_elemento").readNullable[AapElemento] and
       (__ \ "autorizacion").readNullable[String] and
       (__ \ "esta_id").readNullable[Int]
    )(Activo.apply _)
}

class AapRepository @Inject()(eventoService:EventoRepository, dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    /**
    * Parsear un Aap desde un ResultSet
    */
    private val simple = {
        get[Option[Long]]("aap.aap_id") ~ 
          get[Option[String]]("aap.aap_apoyo") ~
          get[Option[String]]("aap.aap_descripcion") ~
          get[Option[String]]("aap.aap_direccion") ~
          get[Option[String]]("aap_lat") ~
          get[Option[String]]("aap_lng") ~
          get[Option[Long]]("aap.barr_id") ~
          get[Option[Long]]("tiba_id") ~
          get[Long]("aap.empr_id") ~
          get[Option[DateTime]]("aap.aap_fechacreacion") ~
          get[Option[DateTime]]("aap.aap_fechatoma") ~
          get[Option[Boolean]]("aap.aap_modernizada") ~
          get[Option[Long]]("aap.aama_id") ~
          get[Option[Long]]("aap.aacu_id") ~
          get[Option[Boolean]]("aap.aap_medidor") ~
          get[Option[Long]]("aap.aatc_id") ~
          get[Option[Long]]("aap.aamo_id") ~
          get[Option[Long]]("aap.aaus_id") ~
          get[Option[Long]]("aap.aaco_id") ~
          get[Long]("aap.usua_id") map {
            case aap_id ~ 
                 aap_apoyo ~ 
                 aap_descripcion ~ 
                 aap_direccion ~ 
                 aap_lat ~ 
                 aap_lng ~ 
                 barr_id ~ 
                 tiba_id ~
                 empr_id ~ 
                 aap_fechacreacion ~ 
                 aap_fechatoma ~
                 aap_modernizada ~
                 aama_id ~
                 aacu_id ~
                 aap_medidor ~
                 aatc_id ~
                 aamo_id ~
                 aaus_id ~
                 aaco_id ~
                 usua_id => Aap(aap_id, 
                                aap_apoyo, 
                                aap_descripcion, 
                                aap_direccion, 
                                aap_lat, 
                                aap_lng, 
                                barr_id,
                                tiba_id,
                                empr_id, 
                                aap_fechacreacion, 
                                aap_fechatoma,
                                aap_modernizada,
                                aama_id,
                                aacu_id,
                                aap_medidor,
                                aatc_id,
                                aamo_id,
                                aaus_id,
                                aaco_id,
                                usua_id,
                                null,
                                null)
        }
    }

    /**
     * 
     */
    def buscarSiguienteACrear(empr_id: scala.Long): Int = {
        db.withConnection { implicit connection =>
            val ultimo = SQL("""SELECT a.aap_id FROM siap.aap a WHERE a.empr_id = {empr_id} and a.aap_id <> 9999999
                                ORDER BY a.aap_id DESC LIMIT 1""").
                        on(
                            'empr_id -> empr_id
                        ).as(SqlParser.scalar[Int].single)
            (ultimo + 1)
        }
    }

    def buscarParaVerificar(aap_id: Long, empr_id: Long): Int = {
      db.withConnection { implicit connection =>
        var result = 0
        val _aapParser = int("aap_id") ~ int("esta_id") ~ int("aaco_id") map { case a ~ e ~ c => (a,e,c)}
        val a = SQL("""SELECT a.aap_id, a.esta_id, a.aaco_id FROM siap.aap a
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
                    if (a._3 == 3) {
                        result = 204
                    } else {
                        result = 200
                    }
                }
        }
        result
      }
    }

    /**
    * Recuperar un aap por su aap_id
    */
    def buscarPorId(aap_id: Long, empr_id:scala.Long): Option[Aap] = {
      db.withConnection { implicit connection => 
        val a = SQL("""SELECT a.*, b.*, t.* FROM siap.aap a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE aap_id = {aap_id} and empr_id = {empr_id}""").
        on('aap_id -> aap_id, 'empr_id -> empr_id).as(simple.singleOpt)
        a match {
            case None => None
            case Some(a) => 
                        val c =  SQL("SELECT * FROM siap.aap_elemento WHERE aap_id = {aap_id}").
                        on(
                            'aap_id -> a.aap_id
                        ).as(AapElemento.aapelementoSet.singleOpt)
                        val aap = a.copy(aap_elemento = c)
                        val h = SQL("SELECT * FROM siap.aap_elemento_historia WHERE aap_id = {aap_id} ORDER BY aael_fecha DESC").
                        on(
                            'aap_id -> a.aap_id
                        ).as(AapHistoria.aapelementohistoriaSet *)
                        val aaph = aap.copy(historia = Some(h))                        
                        Some(aaph)
        }
      }
    }

    /**
    * Recuperar un aap por su aap_id
    */
    def buscarParaEditar(aap_id: Long, empr_id: Long): Option[Activo] = {
      db.withConnection { implicit connection => 
        val a = SQL("""SELECT a.*, b.*, t.* FROM siap.aap a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE aap_id = {aap_id} and empr_id = {empr_id}""").
        on('aap_id -> aap_id, 'empr_id-> empr_id).as(simple.singleOpt)
        a match {
            case None => None
            case Some(a) => 
                        val aame = SQL("SELECT * FROM siap.aap_medidor WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                        on(
                            'aap_id -> a.aap_id,
                            'empr_id -> a.empr_id
                        ).as(Aap_medidor.aapmedidorSet.singleOpt)
                        val aatr = SQL("SELECT * FROM siap.aap_transformador WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                        on(
                            'aap_id -> a.aap_id,
                            'empr_id -> a.empr_id
                        ).as(Aap_transformador._set.singleOpt)                        
                        val aap_elemento =  SQL("SELECT * FROM siap.aap_elemento WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                        on(
                            'aap_id -> a.aap_id,
                            'empr_id -> a.empr_id
                        ).as(AapElemento.aapelementoSet.singleOpt)
                        val aap_n = a.copy(aap_elemento = aap_elemento, historia = None)

                        val aap_adicional = SQL("SELECT * FROM siap.aap_adicional WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                        on(
                            'aap_id -> a.aap_id,
                            'empr_id -> a.empr_id
                        ).as(AapAdicional.aapadicionalSet.singleOpt)

                        val estado = SQL("""SELECT esta_id FROM siap.aap WHERE aap_id = {aap_id} and empr_id = {empr_id}""").on(
                            'aap_id -> a.aap_id,
                            'empr_id -> a.empr_id
                        ).as(SqlParser.scalar[Int].singleOpt)
                        val activo = new Activo(Some(aap_n), aame, aatr, aap_adicional, aap_elemento, None, estado)
                        Some(activo)
        }
      }
    }    

    /**
    * Recuperar un aap por su aap_codigo
    */
    def buscarPorCodigo(aap_codigo: String, empr_id: Long): Option[Aap] = {
      db.withConnection { implicit connection => 
        val a = SQL("""SELECT a.*, b.*, t.* FROM siap.aap a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id WHERE aap_codigo = {aap_codigo} and empr_id = {empr_id} ORDER BY aap_codigo""").on('aap_codigo -> aap_codigo).as(simple.singleOpt)
            a match {
            case None => None
            case Some(a) => 
                        val c =  SQL("SELECT aael.* FROM siap.aap_elemento aael WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                        on(
                            'aap_id -> a.aap_id,
                            'empr_id -> empr_id
                        ).as(AapElemento.aapelementoSet.singleOpt)
                        val aap = a.copy(aap_elemento = c)
                        Some(aap)
        }

      }
    }

    /**
    * Recuperar un aap por su aap_apoyo
    */
    def buscarPorApoyo(aap_apoyo: String, empr_id: Long): Option[Aap] = {
      db.withConnection { implicit connection => 
        val a = SQL("""SELECT a.*, b.*, t.* FROM siap.aap a
               LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
               LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
               WHERE aap_apoyo = {aap_apoyo} and empr_id = {empr_id}""").on('aap_apoyo -> aap_apoyo, 'empr_id -> empr_id).as(simple.singleOpt)
        a match {
            case None => None
            case Some(a) => 
                        val c =  SQL("SELECT aael.* FROM siap.aap_elemento aael WHERE aap_id = {aap_id}").
                        on(
                            'aap_id -> a.aap_id
                        ).as(AapElemento.aapelementoSet.singleOpt)
                        val aap = a.copy(aap_elemento = c)
                        Some(aap)
        }

      }
    }

    /**
    * Recuperar un aap por su esta_id
    */
    def buscarPorEstado(esta_id: Long, empr_id: Long): Future[Option[Aap]] = Future {
      db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.aap WHERE esta_id = {esta_id} and empr_id = {empr_id} ORDER BY app_codigo").on('esta_id -> esta_id, 'empr_id -> empr_id).as(simple.singleOpt)
      }
    }(ec)

    /**
    * Recuperar un aap por su barr_id
    */
    def buscarPorBarrio(barr_id: Long, empr_id: Long): Future[Option[Aap]] = Future {
      db.withConnection { implicit connection => 
        SQL("SELECT * FROM siap.aap WHERE barr_id = {barr_id} and empr_id = {empr_id} ORDER BY app_codigo").on('barr_id -> barr_id, 'empr_id -> empr_id).as(simple.singleOpt)
      }
    }(ec)

    /**
     * buscarPorMaterial
     * @param codigo
     * @param tiel_id
     * @param empr_id
     * @return Iterable[Aap]
     */ 
    def buscarPorMaterial(codigo: String, tiel_id: scala.Long, empr_id: scala.Long): Future[Iterable[Aap]] = Future[Iterable[Aap]] {
        db.withConnection { implicit connection =>
           var lista_result = new ListBuffer[Aap]
           var query: String = """SELECT   a.aap_id, 
                        a.aap_apoyo, 
                        a.aap_descripcion, 
                        a.aap_direccion, 
                        a.aap_lat::text, 
                        a.aap_lng::text, 
                        a.barr_id,
                        b.tiba_id,
                        a.empr_id, 
                        a.aap_fechacreacion, 
                        a.usua_id, 
                        a.aaus_id, 
                        a.aap_modernizada, 
                        a.aatc_id, 
                        a.aap_medidor, 
                        a.aap_fechatoma, 
                        a.aama_id, 
                        a.aamo_id, 
                        a.aacu_id, 
                        a.aaco_id,
                        b.barr_descripcion,
                        t.tiba_descripcion  
                FROM siap.aap a
                LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
                LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
                LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
                WHERE a.empr_id = {empr_id} and a.esta_id <> 9 and a.aap_id IN (
                    SELECT re.aap_id AS aap_id FROM siap.reporte_evento re
                    INNER JOIN siap.aap a ON a.aap_id = re.aap_id and a.empr_id = re.empr_id
                    INNER JOIN siap.elemento e ON e.elem_id = re.elem_id
                    WHERE re.even_codigo_instalado = {codigo} and e.tiel_id = {tiel_id} and re.empr_id = {empr_id}
                    UNION
                    SELECT aeh.aap_id AS aap_id FROM siap.aap_elemento_historia aeh
                    INNER JOIN siap.aap a ON a.aap_id = aeh.aap_id and a.empr_id = aeh.empr_id
                    WHERE CASE 
	                        WHEN {tiel_id} = 1 THEN aeh.aap_bombillo_instalado = {codigo} 
	                        WHEN {tiel_id} = 2 THEN aeh.aap_balasto_instalado = {codigo}
	                        WHEN {tiel_id} = 3 THEN aeh.aap_arrancador_instalado = {codigo}
	                        WHEN {tiel_id} = 4 THEN aeh.aap_condensador_instalado = {codigo}
	                        WHEN {tiel_id} = 5 THEN aeh.aap_fotocelda_instalado = {codigo}
                          END
                          AND aeh.empr_id = {empr_id}
                )"""
            val lista:List[Aap] = SQL(query).on(
                'empr_id -> empr_id,
                'tiel_id -> tiel_id,
                'codigo -> codigo
            ).as(simple *)
            for ( e <- lista ) {
                val c = SQL("SELECT aael.* FROM siap.aap_elemento aael WHERE aap_id = {aap_id}").
                on(
                    'aap_id -> e.aap_id
                ).as(AapElemento.aapelementoSet.singleOpt)
            
                val aap = e.copy(aap_elemento = c)
                val h = SQL("SELECT * FROM siap.aap_elemento_historia WHERE aap_id = {aap_id} ORDER BY aael_fecha DESC").
                on(
                    'aap_id -> e.aap_id
                ).as(AapHistoria.aapelementohistoriaSet *)
                val aaph = aap.copy(historia = Some(h))                        
                lista_result += aaph
            }
            lista_result.toList            
        }
    }


  /**
  * Recuperar total de registros
  * @return total
  */
  def cuenta(empr_id: Long, filter: String): Long =  {
    db.withConnection{ implicit connection =>
      var query: String = 
      """
        SELECT COUNT(*) AS c FROM siap.aap a
        LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
        LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
        LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
        WHERE a.empr_id = {empr_id} AND a.esta_id <> 9
      """
      if (!filter.isEmpty){
          query = query + " and " + filter
      }
      val result = SQL(query).
      on(
          'empr_id -> empr_id
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }


  /**
  * Recuperar total de registros
  * @return total
  */
  def cuentaEliminados(empr_id: Long, filter: String): Long =  {
    db.withConnection{ implicit connection =>
      var query: String = 
      """
        SELECT COUNT(*) AS c FROM siap.aap a
        LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
        LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
        LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
        WHERE a.empr_id = {empr_id} and a.esta_id = 9
      """
      if (!filter.isEmpty){
          query = query + " and " + filter
      }
      val result = SQL(query).
      on(
          'empr_id -> empr_id
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }

    /**
    *  Recuperar todas la Aap de la empresa
    *  @param empr_id: Long
    *  @param page_size: Int
    *  @param current_page: Int
    */
    def todos(empr_id:Long, page_size:Long, current_page: Long, orderby:String, filter:String): Future[Iterable[Aap]] = Future[Iterable[Aap]] {
                var lista_result = new ListBuffer[Aap]
                db.withConnection { implicit connection => 
                var query: String =
                    """SELECT   a.aap_id, 
                                a.aap_apoyo, 
                                a.aap_descripcion, 
                                a.aap_direccion, 
                                a.aap_lat::text, 
                                a.aap_lng::text, 
                                a.barr_id,
                                b.tiba_id,
                                a.empr_id, 
                                a.aap_fechacreacion, 
                                a.usua_id, 
                                a.aaus_id, 
                                a.aap_modernizada, 
                                a.aatc_id, 
                                a.aap_medidor, 
                                a.aap_fechatoma, 
                                a.aama_id, 
                                a.aamo_id, 
                                a.aacu_id, 
                                a.aaco_id,
                                b.barr_descripcion,
                                t.tiba_descripcion,
                                a.esta_id
                        FROM siap.aap a
                        LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
                        LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
                        LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
                        WHERE a.empr_id = {empr_id} and a.aap_id <> 9999999 and a.esta_id <> 9"""
                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    }
                    query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
                    val lista:List[Aap]=SQL(query).
                    on(
                        'empr_id -> empr_id,
                        'page_size -> page_size,
                        'current_page -> current_page
                        ).as(simple *)
                    for ( e <- lista ) {
                        val c = SQL("SELECT aael.* FROM siap.aap_elemento aael WHERE aap_id = {aap_id}").
                        on(
                            'aap_id -> e.aap_id
                        ).as(AapElemento.aapelementoSet.singleOpt)
                    
                        val aap = e.copy(aap_elemento = c)

                        val h = SQL("SELECT * FROM siap.aap_elemento_historia WHERE aap_id = {aap_id} ORDER BY aael_fecha DESC").
                        on(
                            'aap_id -> e.aap_id
                        ).as(AapHistoria.aapelementohistoriaSet *)
                        val aaph = aap.copy(historia = Some(h))
                        lista_result += aaph
                    }
                }
        lista_result.toList
    }

    /**
    *  Recuperar todas la Aap de la empresa
    *  @param empr_id: Long
    *  @param page_size: Int
    *  @param current_page: Int
    */
    def todosEliminados(empr_id:Long, page_size:Long, current_page: Long, orderby:String, filter:String): Future[Iterable[Aap]] = Future[Iterable[Aap]] {
        var lista_result = new ListBuffer[Aap]
        db.withConnection { implicit connection => 
        var query: String =
            """SELECT   a.aap_id, 
                        a.aap_apoyo, 
                        a.aap_descripcion, 
                        a.aap_direccion, 
                        a.aap_lat::text, 
                        a.aap_lng::text, 
                        a.barr_id,
                        b.tiba_id,
                        a.empr_id, 
                        a.aap_fechacreacion, 
                        a.usua_id, 
                        a.aaus_id, 
                        a.aap_modernizada, 
                        a.aatc_id, 
                        a.aap_medidor, 
                        a.aap_fechatoma, 
                        a.aama_id, 
                        a.aamo_id, 
                        a.aacu_id, 
                        a.aaco_id,
                        b.barr_descripcion,
                        t.tiba_descripcion,
                        a.esta_id
                FROM siap.aap a
                LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
                LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
                LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
                WHERE a.empr_id = {empr_id} and a.aap_id <> 9999999 and a.esta_id = 9"""
            if (!filter.isEmpty) {
                query = query + " and " + filter
            }
            if (!orderby.isEmpty) {
                query = query + s" ORDER BY $orderby"
            }
            query = query + """
                LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
            val lista:List[Aap]=SQL(query).
            on(
                'empr_id -> empr_id,
                'page_size -> page_size,
                'current_page -> current_page
                ).as(simple *)
            for ( e <- lista ) {
                val c = SQL("SELECT aael.* FROM siap.aap_elemento aael WHERE aap_id = {aap_id}").
                on(
                    'aap_id -> e.aap_id
                ).as(AapElemento.aapelementoSet.singleOpt)
            
                val aap = e.copy(aap_elemento = c)

                val h = SQL("SELECT * FROM siap.aap_elemento_historia WHERE aap_id = {aap_id} ORDER BY aael_fecha DESC").
                on(
                    'aap_id -> e.aap_id
                ).as(AapHistoria.aapelementohistoriaSet *)
                val aaph = aap.copy(historia = Some(h))
                lista_result += aaph
            }
        }
        lista_result.toList
    }


    /**
    *  Recuperar todas la Aap de la empresa
    *  @param empr_id: Long
    *  @param page_size: Int
    *  @param current_page: Int
    */
    def aaps(empr_id:Long, filter:String): Future[Iterable[Aap]] = Future[Iterable[Aap]] {
                var lista_result = new ListBuffer[Aap]
                db.withConnection { implicit connection => 
                var query: String =
                    """SELECT aap_id, 
                                aap_apoyo, 
                                aap_descripcion, 
                                aap_direccion, 
                                aap_lat::text, 
                                aap_lng::text, 
                                a.barr_id,
                                b.tiba_id,
                                a.empr_id, 
                                aap_fechacreacion, 
                                a.usua_id, 
                                aaus_id, 
                                aap_modernizada, 
                                aatc_id, 
                                aap_medidor, 
                                aap_fechatoma, 
                                aama_id, 
                                aamo_id, 
                                aacu_id, 
                                aaco_id,
                                barr_descripcion,
                                tiba_descripcion,
                                a.esta_id
                        FROM siap.aap a
                        LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
                        LEFT JOIN siap.tipobarrio t ON b.tiba_id = t.tiba_id
                        LEFT JOIN siap.empresa e ON a.empr_id = e.empr_id
                        LEFT JOIN siap.municipio m on e.muni_id = m.muni_id
                        LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id and ad.empr_id = a.empr_id
                        WHERE a.empr_id = {empr_id} and a.aap_id <> 9999999 """

                    if (!filter.isEmpty) {
                        query = query + " and " + filter
                    }
                    /*
                    if (!orderby.isEmpty) {
                        query = query + s" ORDER BY $orderby"
                    }
                    */
                    val lista:List[Aap]=SQL(query).
                    on(
                        'empr_id -> empr_id
                        ).as(simple *)

                    for ( e <- lista ) {
                        val c = SQL("SELECT aael.* FROM siap.aap_elemento aael WHERE aap_id = {aap_id}").
                        on(
                            'aap_id -> e.aap_id
                        ).as(AapElemento.aapelementoSet.singleOpt)
                    
                        val aap = e.copy(aap_elemento = c)
                        val h = SQL("SELECT * FROM siap.aap_elemento_historia WHERE aap_id = {aap_id} ORDER BY aael_fecha DESC").
                        on(
                            'aap_id -> e.aap_id
                        ).as(AapHistoria.aapelementohistoriaSet *)
                        val aaph = aap.copy(historia = Some(h))                        
                        lista_result += aaph
                    }
                    lista_result.toList
                }   
    }    
    
    /**
    * Crear Aap
    * @param aap: Aap
    */
    def crear(activo: Activo, empr_id: Long, usua_id: Long) : Future[Long] = Future {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val aap = activo.aap.get
            val id: Long = SQL("""INSERT INTO siap.aap 
                               (aap_id,
                                aaco_id,
                                aap_apoyo, 
                                aap_descripcion, 
                                aap_direccion, 
                                aap_lat, 
                                aap_lng, 
                                barr_id, 
                                empr_id, 
                                esta_id,
                                aap_fechacreacion, 
                                usua_id, 
                                aaus_id, 
                                aap_modernizada, 
                                aatc_id, 
                                aap_medidor,                                 
                                aap_fechatoma, 
                                aama_id, 
                                aamo_id,
                                aacu_id) VALUES (
                                {aap_id},
                                {aaco_id},
                                {aap_apoyo}, 
                                {aap_descripcion}, 
                                {aap_direccion}, 
                                {aap_lat}, 
                                {aap_lng}, 
                                {barr_id}, 
                                {empr_id}, 
                                {esta_id}, 
                                {aap_fechacreacion}, 
                                {usua_id}, 
                                {aaus_id}, 
                                {aap_modernizada}, 
                                {aatc_id}, 
                                {aap_medidor}, 
                                {aap_fechatoma}, 
                                {aama_id},
                                {aamo_id}, 
                                {aacu_id})""").
            on(
                "aap_id" -> aap.aap_id,
                "aaco_id" -> aap.aaco_id,
                "aap_apoyo" -> aap.aap_apoyo,
                "aap_descripcion" -> aap.aap_descripcion,
                "aap_direccion" -> aap.aap_direccion,
                "aap_lat" -> aap.aap_lat,
                "aap_lng" -> aap.aap_lng,
                "barr_id" -> aap.barr_id,
                "empr_id" -> empr_id,
                "esta_id" -> 1,
                "aap_fechacreacion" -> aap.aap_fechacreacion,
                "usua_id" -> usua_id,
                "aaus_id" -> aap.aaus_id, 
                "aap_modernizada" -> aap.aap_modernizada,
                "aatc_id" -> aap.aatc_id,
                "aap_medidor" -> aap.aap_medidor, 
                "aap_fechatoma" -> aap.aap_fechatoma,
                "aama_id" -> aap.aama_id,
                "aamo_id" -> aap.aamo_id,
                "aacu_id" -> aap.aacu_id, 
            ).executeInsert().get

            activo.aame match {
                case None => None
                case Some(aame) =>
                    SQL("INSERT INTO siap.aap_medidor (aap_id, amem_id, amet_id, aame_numero, empr_id, medi_id) VALUES ({aap_id}, {amem_id}, {amet_id}, {aame_numero}, {empr_id}, {medi_id})").
                    on(
                        'aap_id -> aap.aap_id,
                        'amem_id -> aame.amem_id,
                        'amet_id -> aame.amet_id,
                        'aame_numero -> aame.aame_numero,
                        'empr_id -> empr_id,
                        'medi_id -> aame.medi_id
                    ).executeInsert()
            }

            activo.aatr match {
                case None => None
                case Some(aatr) =>
                    SQL("INSERT INTO siap.aap_transformador (aap_id, empr_id, tran_id) VALUES ({aap_id}, {empr_id}, {tran_id})").
                    on(
                        'aap_id -> aap.aap_id,
                        'empr_id -> empr_id,
                        'tran_id -> aatr.tran_id
                    ).executeInsert()
            }

            activo.aap_elemento match {
                case None => None
                case Some(aap_elemento) => 
                        SQL("INSERT INTO siap.aap_elemento (aap_id, aael_fecha, aap_bombillo, aap_balasto, aap_arrancador, aap_condensador, aap_fotocelda, reti_id, repo_consecutivo, empr_id) VALUES ({aap_id}, {aael_fecha}, {aap_bombillo}, {aap_balasto}, {aap_arrancador}, {aap_condensador}, {aap_fotocelda}, {reti_id}, {repo_consecutivo}, {empr_id})").
                            on(
                                'aap_id -> aap.aap_id,
                                'aael_fecha -> aap_elemento.aael_fecha, 
                                'aap_bombillo -> aap_elemento.aap_bombillo, 
                                'aap_balasto -> aap_elemento.aap_balasto, 
                                'aap_arrancador -> aap_elemento.aap_arrancador, 
                                'aap_condensador -> aap_elemento.aap_condensador,
                                'aap_fotocelda -> aap_elemento.aap_fotocelda, 
                                'reti_id -> aap_elemento.reti_id, 
                                'repo_consecutivo -> aap_elemento.repo_consecutivo,
                                'empr_id -> empr_id
                            ).executeInsert()
            }

            activo.aap_adicional match {
                case None => None
                case Some(aap_adicional) =>
                        SQL("INSERT INTO siap.aap_adicional (aap_id, tipo_id, aap_poste_altura, aap_brazo, aap_collarin, aap_potencia, aap_tecnologia, aap_modernizada_anho, aap_rte, aap_poste_propietario, empr_id) VALUES ({aap_id}, {tipo_id}, {aap_poste_altura}, {aap_brazo}, {aap_collarin}, {aap_potencia}, {aap_tecnologia}, {aap_modernizada_anho}, {aap_rte}, {aap_poste_propietario}, {empr_id})").
                            on(
                                'aap_id -> aap.aap_id,
                                'tipo_id -> aap_adicional.tipo_id,
                                'aap_poste_altura -> aap_adicional.aap_poste_altura,
                                'aap_brazo -> aap_adicional.aap_brazo,
                                'aap_collarin -> aap_adicional.aap_collarin,
                                'aap_potencia -> aap_adicional.aap_potencia,
                                'aap_tecnologia -> aap_adicional.aap_tecnologia,
                                'aap_modernizada_anho -> aap_adicional.aap_modernizada_anho,
                                'aap_rte -> aap_adicional.aap_rte,
                                'aap_poste_propietario -> aap_adicional.aap_poste_propietario,
                                'empr_id -> empr_id
                            ).executeInsert()
            }

            activo.autorizacion match {
                case Some(autorizacion) => SQL("""INSERT INTO siap.aap_codigo_autorizacion (aap_id, coau_codigo, empr_id) VALUES ({aap_id}, {coau_codigo}, {empr_id})""").
                                                on(
                                                  'aap_id -> aap.aap_id,
                                                  'coau_codigo -> autorizacion,
                                                  'empr_id -> empr_id
                                                ).executeInsert()

                                           SQL("""UPDATE siap.codigo_autorizacion SET coau_fechauso = {coau_fechauso}, coau_usua_id = {usua_id}, coau_estado = {coau_estado} WHERE coau_tipo = {coau_tipo} and coau_codigo = {coau_codigo} and empr_id = {empr_id}""").
                                               on(
                                                'coau_fechauso -> hora,
                                                'usua_id -> usua_id,
                                                'coau_estado -> 1,
                                                'empr_id -> empr_id,
                                                'coau_tipo -> 1,
                                                'coau_codigo -> autorizacion
                                               ).executeUpdate()
                case None => None
            }

            /*
            */
            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
            on(
                'audi_fecha -> fecha,
                'audi_hora -> hora,
                'usua_id -> aap.usua_id,
                'audi_tabla -> "aap", 
                'audi_uid -> id,
                'audi_campo -> "aap_id", 
                'audi_valorantiguo -> "",
                'audi_valornuevo -> aap.aap_id,
                'audi_evento -> "I").
                executeInsert()
            
            id            
        }
    }

    /**
        Actualizar Aap
        @param aap: Aap
     */
    def actualizar(activo: Activo, empr_id: scala.Long, usua_id: scala.Long) : Boolean = {
        val aap = activo.aap.get
        val aap_ant: Option[Aap] = buscarPorId(aap.aap_id.get, empr_id)
        

        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            var result: Boolean = false
            activo.aap match {
                case None => None
                case Some(aap) =>
                     result = SQL("""UPDATE siap.aap SET 
                                                aap_apoyo = {aap_apoyo}, 
                                                aap_descripcion = {aap_descripcion}, 
                                                aap_direccion = {aap_direccion}, 
                                                aap_lat = {aap_lat}, 
                                                aap_lng = {aap_lng}, 
                                                barr_id = {barr_id}, 
                                                empr_id = {empr_id}, 
                                                esta_id = {esta_id}, 
                                                aap_fechacreacion = {aap_fechacreacion},
                                                usua_id = {usua_id},
                                                aaus_id = {aaus_id}, 
                                                aap_modernizada = {aap_modernizada},
                                                aatc_id = {aatc_id}, 
                                                aama_id = {aama_id},
                                                aacu_id = {aacu_id},
                                                aamo_id = {aamo_id},
                                                aap_medidor = {aap_medidor},
                                                aap_fechatoma = {aap_fechatoma},
                                                aaco_id = {aaco_id}
                                              WHERE 
                                                aap_id = {aap_id}""").
                                            on(
                                                "aap_apoyo" -> aap.aap_apoyo,
                                                "aap_descripcion" -> aap.aap_descripcion,
                                                "aap_direccion" -> aap.aap_direccion,
                                                "aap_lat" -> aap.aap_lat,
                                                "aap_lng" -> aap.aap_lng,
                                                "barr_id" -> aap.barr_id,
                                                "empr_id" -> empr_id,
                                                "esta_id" -> 1,
                                                "aap_fechacreacion" -> aap.aap_fechacreacion,
                                                "usua_id" -> usua_id,
                                                "aaus_id" -> aap.aaus_id, 
                                                "aap_modernizada" -> aap.aap_modernizada,
                                                "aatc_id" -> aap.aatc_id, 
                                                "aama_id" -> aap.aama_id, 
                                                "aacu_id" -> aap.aacu_id, 
                                                "aap_medidor" -> aap.aap_medidor, 
                                                "aap_fechatoma" -> aap.aap_fechatoma,
                                                "aaco_id" -> aap.aaco_id,
                                                "aamo_id" -> aap.aamo_id,
                                                "aap_id" -> aap.aap_id
                                               ).executeUpdate() > 0

                    activo.aame match {
                        case None => None
                        case Some(aame) =>
                            val actualizo = SQL("UPDATE siap.aap_medidor SET medi_id = {medi_id}, amem_id = {amem_id}, amet_id = {amet_id}, aame_numero = {aame_numero} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                            on(
                                'aap_id -> aap.aap_id,
                                'amem_id -> aame.amem_id,
                                'amet_id -> aame.amet_id,
                                'aame_numero -> aame.aame_numero,
                                'medi_id -> aame.medi_id,
                                'empr_id -> empr_id
                            ).executeUpdate() > 0
                            if (!actualizo) {
                                SQL("INSERT INTO siap.aap_medidor (aap_id, amem_id, amet_id, aame_numero, empr_id, medi_id) VALUES ({aap_id}, {amem_id}, {amet_id}, {aame_numero}, {empr_id}, {medi_id})").
                                on(
                                    'aap_id -> aap.aap_id,
                                    'amem_id -> aame.amem_id,
                                    'amet_id -> aame.amet_id,
                                    'aame_numero -> aame.aame_numero,
                                    'empr_id -> empr_id,
                                    'medi_id -> aame.medi_id
                                ).executeInsert()
                            }
                    }

                    activo.aatr match {
                        case None => None
                        case Some(aatr) =>
                            val actualizo = SQL("UPDATE siap.aap_transformador SET tran_id = {tran_id} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                            on(
                                'aap_id -> aap.aap_id,
                                'tran_id -> aatr.tran_id,
                                'empr_id -> empr_id
                            ).executeUpdate() > 0
                            if (!actualizo) {
                                SQL("INSERT INTO siap.aap_transformador (aap_id, empr_id, tran_id) VALUES ({aap_id}, {empr_id}, {tran_id})").
                                on(
                                    'aap_id -> aap.aap_id,
                                    'empr_id -> empr_id,
                                    'tran_id -> aatr.tran_id
                                ).executeInsert()
                            }
                    }                    

                    activo.aap_elemento match {
                        case None => None
                        case Some(aap_elemento) => 
                            SQL("UPDATE siap.aap_elemento SET aael_fecha = {aael_fecha}, aap_bombillo = {aap_bombillo}, aap_balasto = {aap_balasto}, aap_arrancador = {aap_arrancador}, aap_condensador = {aap_condensador}, aap_fotocelda = {aap_fotocelda}, reti_id = {reti_id}, repo_consecutivo = {repo_consecutivo} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                            on(
                                'aap_id -> aap.aap_id,
                                'empr_id -> empr_id,
                                'aael_fecha -> aap_elemento.aael_fecha, 
                                'aap_bombillo -> aap_elemento.aap_bombillo, 
                                'aap_balasto -> aap_elemento.aap_balasto, 
                                'aap_arrancador -> aap_elemento.aap_arrancador, 
                                'aap_condensador -> aap_elemento.aap_condensador,
                                'aap_fotocelda -> aap_elemento.aap_fotocelda, 
                                'reti_id -> aap_elemento.reti_id, 
                                'repo_consecutivo -> aap_elemento.repo_consecutivo
                            ).executeUpdate()
                    }

                    activo.aap_adicional match {
                        case None => None
                        case Some(aap_adicional) =>
                            SQL("UPDATE siap.aap_adicional SET tipo_id = {tipo_id}, aap_poste_altura = {aap_poste_altura}, aap_brazo = {aap_brazo}, aap_collarin = {aap_collarin}, aap_potencia = {aap_potencia}, aap_tecnologia = {aap_tecnologia}, aap_modernizada_anho = {aap_modernizada_anho}, aap_rte = {aap_rte}, aap_poste_propietario = {aap_poste_propietario} WHERE aap_id = {aap_id} and empr_id = {empr_id}").
                            on(
                                'aap_id -> aap.aap_id,
                                'empr_id -> empr_id,
                                'tipo_id -> aap_adicional.tipo_id,
                                'aap_poste_altura -> aap_adicional.aap_poste_altura,
                                'aap_brazo -> aap_adicional.aap_brazo,
                                'aap_collarin -> aap_adicional.aap_collarin,
                                'aap_potencia -> aap_adicional.aap_potencia,
                                'aap_tecnologia -> aap_adicional.aap_tecnologia,
                                'aap_modernizada_anho -> aap_adicional.aap_modernizada_anho,
                                'aap_rte -> aap_adicional.aap_rte,
                                'aap_poste_propietario -> aap_adicional.aap_poste_propietario
                            ).executeUpdate()
                    }                                                

            if (aap_ant != None) {
             if (aap_ant.get.aama_id != aap.aama_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aama_id", 
                    'audi_valorantiguo -> aap_ant.get.aama_id,
                    'audi_valornuevo -> aap.aama_id,
                    'audi_evento -> "A").
                    executeInsert()
             }
             if (aap_ant.get.aap_apoyo != aap.aap_apoyo){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aap_apoyo", 
                    'audi_valorantiguo -> aap_ant.get.aap_apoyo,
                    'audi_valornuevo -> aap.aap_apoyo,
                    'audi_evento -> "A").
                    executeInsert()
             }  
             if (aap_ant.get.aap_descripcion != aap.aap_descripcion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aap_descripcion", 
                    'audi_valorantiguo -> aap_ant.get.aap_descripcion,
                    'audi_valornuevo -> aap.aap_descripcion,
                    'audi_evento -> "A").
                    executeInsert()
             }    
             if (aap_ant.get.aap_direccion != aap.aap_direccion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aap_direccion", 
                    'audi_valorantiguo -> aap_ant.get.aap_direccion,
                    'audi_valornuevo -> aap.aap_direccion,
                    'audi_evento -> "A").
                    executeInsert()
             }        
             if (aap_ant.get.aap_lat != aap.aap_lat){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aap_lat", 
                    'audi_valorantiguo -> aap_ant.get.aap_lat,
                    'audi_valornuevo -> aap.aap_lat,
                    'audi_evento -> "A").
                    executeInsert()
             }                        
             if (aap_ant.get.aap_lng != aap.aap_lng){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aap_lng", 
                    'audi_valorantiguo -> aap_ant.get.aap_lng,
                    'audi_valornuevo -> aap.aap_lng,
                    'audi_evento -> "A").
                    executeInsert()
             }  
             if (aap_ant.get.barr_id != aap.barr_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "barr_id", 
                    'audi_valorantiguo -> aap_ant.get.barr_id,
                    'audi_valornuevo -> aap.barr_id,
                    'audi_evento -> "A").
                    executeInsert()
             } 
             if (aap_ant.get.empr_id != aap.empr_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "empr_id", 
                    'audi_valorantiguo -> aap_ant.get.empr_id,
                    'audi_valornuevo -> aap.empr_id,
                    'audi_evento -> "A").
                    executeInsert()
             }
             if (aap_ant.get.aap_fechacreacion != aap.aap_fechacreacion){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "aap_fechacreacion", 
                    'audi_valorantiguo -> aap_ant.get.aap_fechacreacion,
                    'audi_valornuevo -> aap.aap_fechacreacion,
                    'audi_evento -> "A").
                    executeInsert()
             }
             if (aap_ant.get.usua_id != aap.usua_id){
                SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> aap.usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap.aap_id,
                    'audi_campo -> "usua_id", 
                    'audi_valorantiguo -> aap_ant.get.usua_id,
                    'audi_valornuevo -> aap.usua_id,
                    'audi_evento -> "A").
                    executeInsert()
             }        
            }     
            }         
            result            
        }
    }

    /**
    * Eliminar Aap 
    * @param aap_id: Long
    */

    def borrar(aap_id: Long, usua_id: Long, empr_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val count:Long = SQL("UPDATE siap.aap SET esta_id = 9 WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).executeUpdate

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()

            count > 0
        }
    }

    /**
    * Recuperar Aap 
    * @param aap_id: Long
    */

    def recuperar(aap_id: Long, usua_id: Long, empr_id: Long) : Boolean = {
        db.withConnection { implicit connection =>
            val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())
            val count:Long = SQL("UPDATE siap.aap SET esta_id = 1 WHERE aap_id = {aap_id} and empr_id = {empr_id}").
            on(
                'aap_id -> aap_id,
                'empr_id -> empr_id
            ).executeUpdate

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> usua_id,
                    'audi_tabla -> "aap", 
                    'audi_uid -> aap_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "R").
                    executeInsert()

            count > 0
        }
    }    
}