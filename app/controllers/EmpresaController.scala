package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import pdi.jwt.JwtSession

import utilities._

import dto.EmpresaDto
import dto.ResultDto

@Singleton
class EmpresaController @Inject()(
    empresaService: EmpresaRepository,
    perfilService: PerfilRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction
    //auth: SecuredAuthenticator
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def todos(page_size:Long, current_page:Long): Action[AnyContent] = Action.async {
    val total = empresaService.cuenta()
    empresaService.todos(page_size, current_page).map { empresas =>
      //val result = new ResultDto[Empresa](empresas.toList, total)
      Ok(Json.obj("empresas" -> empresas, "total" -> total))
    }
  }

  def buscarPorId(empr_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empresa = empresaService.buscarPorId(empr_id)
    empresa match {
      case None => {
        Future.successful(NotFound(Json.toJson("false")))
      }
      case Some(empresa) => {
        Future.successful(Ok(Json.toJson(empresa)))
      }
    }

  }

  def buscarPorUsuario() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val token = request.headers.get("Authorization")
      token match {
        case None => {
          Future.successful(Forbidden("Dude, you’re not logged in."))
        }
        case Some(token) => {
          val session = JwtSession.deserialize(token)
          val usuaId = session.get("usua_id")
          usuaId match {
            case None => {
              Future.successful(Ok)
            }
            case Some(usuaId) => {
              empresaService.buscarPorUsuario(usuaId.as[Long]).map { empresas =>
                Ok(Json.toJson(empresas))
              }
            }
          }
        }
      }
  }

  def seleccionarEmpresa(empr_id: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val token = request.headers.get("Authorization")
    var session = JwtSession.deserialize(token.get)
    session = session + ("empr_id", empr_id)
    val empresa = empresaService.buscarPorId(empr_id)
    val empresaToken  = session.serialize
    val usua_id = Utility.extraerUsuario(request)
    val uep = perfilService.buscarPorUsuarioEmpresa(usua_id.get, empr_id)
    val company = new EmpresaDto(empresa.get.empr_id,
                                 empresa.get.empr_descripcion,
                                 empresaToken, 
                                 uep.get.perf_abreviatura.get, 
                                 empresa.get.muni_descripcion.get, 
                                 empresa.get.depa_descripcion.get, 
                                 Some(empresa.get.empr_sigla))
    Future.successful(Ok(Json.toJson(company)))    
    }

  def empresainfo() =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val token = request.headers.get("Authorization")
    var session = JwtSession.deserialize(token.get)
    var usua_id = Utility.extraerUsuario(request)
    var empr_id = Utility.extraerEmpresa(request)
    val empresa = empresaService.buscarPorId(empr_id.get)
    val empresaToken  = session.serialize
    val uep = perfilService.buscarPorUsuarioEmpresa(usua_id.get, empr_id.get)
    val company = new EmpresaDto(empresa.get.empr_id,
                                 empresa.get.empr_descripcion,
                                 empresaToken, uep.get.perf_abreviatura.get, empresa.get.muni_descripcion.get, empresa.get.depa_descripcion.get, Some(empresa.get.empr_sigla))
    Future.successful(Ok(Json.toJson(company)))    
    }    

    def guardarEmpresa() =  authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var empresa = json.as[Empresa]
      val usua_id = Utility.extraerUsuario(request)
      val empresanuevo = new Empresa(Some(0),empresa.empr_descripcion,empresa.empr_sigla,empresa.empr_identificacion,empresa.empr_direccion,empresa.empr_telefono,empresa.empr_concesion, empresa.empr_estado,usua_id.get,empresa.depa_id,empresa.muni_id, null, null)
      empresaService.crear(empresanuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizarEmpresa() =  authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var empresa = json.as[Empresa]
      val usua_id = Utility.extraerUsuario(request)
      val empresanuevo = new Empresa(empresa.empr_id,empresa.empr_descripcion,empresa.empr_sigla,empresa.empr_identificacion,empresa.empr_direccion,empresa.empr_telefono,empresa.empr_concesion, empresa.empr_estado,usua_id.get,empresa.depa_id,empresa.muni_id, null,null)
      if (empresaService.actualizar(empresanuevo)) {
          Future.successful(Ok(Json.toJson("true")))
      } else {
          Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }


    def borrarEmpresa(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      if (empresaService.borrar(id)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
    }
}
