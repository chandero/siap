package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import java.io.{OutputStream, ByteArrayOutputStream}
import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import pdi.jwt.JwtSession

import utilities._

import dto._

@Singleton
class GraficaController @Inject()(
    graficaService: GraficaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {

    def siap_grafica_reporte_pendiente() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_pendiente(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }

    def siap_grafica_reporte_vencido() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_vencido(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }    

    def siap_grafica_reporte_potencia() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_potencia(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }  
    
    def siap_lista_reporte_potencia(aap_potencia: scala.Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_potencia(aap_potencia, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    } 
    
    def siap_lista_potencias() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_potencias(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    } 
    
    def siap_lista_medidas() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_medidas(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    } 

    def siap_lista_usos() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_usos(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    } 

    def siap_lista_sectores() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_sectores(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    } 

    def siap_lista_tecnologias() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_tecnologias(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    }     

    def siap_lista_barrios() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_barrios(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    }

    def siap_lista_veredas() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_veredas(empr_id.get).map { data =>
        Ok(Json.toJson(data))
      }
    }    

    def siap_grafica_reporte_potencia_tecnologia() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_potencia_tecnologia(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    } 
    
    def siap_lista_reporte_potencia_tecnologia(aap_tecnologia: String, aap_potencia: scala.Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_potencia_tecnologia(aap_tecnologia, aap_potencia, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    } 

    def siap_grafica_reporte_potencia_medida() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_potencia_medida(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }    
    
    def siap_lista_reporte_potencia_medida(aaco_descripcion: String, aap_potencia: scala.Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_potencia_medida(aaco_descripcion, aap_potencia, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }    

    def siap_grafica_reporte_sector() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_sector(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    } 

    def siap_lista_reporte_sector(tiba_descripcion: String) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_sector(tiba_descripcion, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }    
    
    def siap_grafica_reporte_uso() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_uso(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    } 
    
    def siap_lista_reporte_uso(aaus_descripcion: String) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_uso(aaus_descripcion, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }

    def siap_grafica_reporte_tecnologia() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_tecnologia(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }
    
    def siap_lista_reporte_tecnologia(aap_tecnologia: String) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_tecnologia(aap_tecnologia: String, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }

    def siap_grafica_reporte_medida() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_medida(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }  
    
    def siap_lista_reporte_medida(aaco_descripcion: String) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_medida(aaco_descripcion: String, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }

    def siap_grafica_reporte_barrio() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_barrio(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }  
    
    def siap_lista_reporte_barrio(barr_descripcion: String) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_lista_reporte_barrio(barr_descripcion: String, empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }

    def siap_grafica_reporte_vereda() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      graficaService.siap_grafica_reporte_vereda(empr_id.get).map { data =>
         Ok(Json.toJson(data))
      }
    }    
}