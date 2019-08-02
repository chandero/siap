package controllers

import javax.inject.Inject
import java.time.Instant
import org.joda.time.DateTime
import models._
import dto._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import pdi.jwt.JwtSession
import play.api.Configuration

import utilities._

@Singleton
class UsuarioController @Inject()(
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository,
    perfilService: PerfilRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction,
    config: Configuration)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def todos(page_size:Long, current_page:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request);
    val total = usuarioService.cuenta(empr_id.get)
    usuarioService.todos(empr_id.get, page_size:Long, current_page:Long).map { usuarios =>
     Ok(Json.obj("usuarios" -> usuarios, "total" -> total))
    }
  }

  def paraCuadrilla() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request);
    usuarioService.paraCuadrilla(empr_id.get).map { usuarios =>
     Ok(Json.toJson(usuarios))
    }
  }

  def autenticar(usua_email: String, usua_clave: String): Action[AnyContent] =
    Action.async { request =>
      usuarioService.autenticar(usua_email, usua_clave).flatMap { esValido =>
        if (esValido) {
          usuarioService.buscarPorEmail(usua_email).flatMap { usuario =>
            var session = JwtSession()
            session = session + ("usua_id", usuario.get.usua_id.get)
            val token = session.serialize
            val user = new UsuarioDto(usuario.get.usua_id,
                                      usuario.get.usua_email,
                                      Some(""),
                                      usuario.get.usua_nombre,
                                      usuario.get.usua_apellido,
                                      Some(token),
                                      0)
            Future(Ok(Json.toJson(user)))
          }
        } else {
          Future(Unauthorized("Usuario o Contresaña Incorrecto!"))
        }
      }
    }

  def buscarporemail(usua_email: String): Action[AnyContent] = Action.async {
    usuarioService.buscarPorEmail(usua_email).map { usuario =>
      Ok(Json.toJson(usuario))
    }
  }

  def buscarporid(usua_id: Long) = authenticatedUserAction.async {
    val usuario = usuarioService.buscarPorId(usua_id).get
    Future.successful(Ok(Json.toJson(usuario)))
  }

  def userinfo(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      val token = request.headers.get("Authorization")
      var session = JwtSession.deserialize(token.get)
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val uep = perfilService.buscarPorUsuarioEmpresa(usua_id.get, empr_id.get)
      val usuario = usuarioService.buscarPorId(usua_id.get)
      usuario match {
          case None => {
           Future.successful(Forbidden("Amigo, no estas registrado"))
          }
          case Some(usuario) => {
            val empresa = empresaService.buscarPorId(empr_id.get)
              empresa match {
                case None => {
                  Future.successful(Forbidden("Amigo, no estas registrado"))
                }
                case Some(empresa) => {
                  var newsession = JwtSession()
                  newsession = newsession + ("usua_id", usuario.usua_id.get)
                  newsession = newsession + ("empr_id", empresa.empr_id.get)
                  var newtoken = newsession.serialize
                  var userinfo = new UserInfoDto(
                    usuario.usua_id.get,
                    usuario.usua_email,
                    usuario.usua_nombre,
                    usuario.usua_apellido,
                    empresa.empr_id.get,
                    empresa.empr_descripcion,
                    newtoken,
                    uep.get.perf_abreviatura.get
                  )
                  Future.successful(Ok(Json.toJson(userinfo)))
                }
              }
            }
          }
      }

  def guardarUsuario() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val json = request.body.asJson.get
    val usua_id = Utility.extraerUsuario(request)
    val empr_id = Utility.extraerEmpresa(request)
    val usuariodto = json.as[UsuarioDto]
    usuarioService.crear(usuariodto.usua_email, usuariodto.usua_clave.get, usuariodto.usua_nombre, usuariodto.usua_apellido, true, (new DateTime(Instant.now.getEpochSecond)).toString(), usua_id.get, empr_id.get, usuariodto.perf_id).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def borrarUsuario(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
    Future.successful(Ok("delete"))
  }

  def recuperarClave(usua_email:String) = Action.async { request: Request[AnyContent] =>
    val linkProtocol = config.get[String]("link.protocol")
    usuarioService.recuperar(linkProtocol,usua_email).map { result => 
      if (result){
        Ok(Json.toJson("true"))
      } else {
        NotAcceptable(Json.toJson("false"))
      }
    }
  }

  def linkValidator(link: String) = Action.async { request: Request[AnyContent] =>
    usuarioService.validarEnlace(link).map { result =>
      if (result) {
        Ok(Json.toJson("true"))
      } else {
        NotFound
      }
    }
  }

  def cambiarClave() = Action(parse.json) { request =>
    val link = (request.body \ "link").as[String]
    val clave = (request.body \ "password").as[String]
    val result:Boolean = usuarioService.cambiarClave(link, clave)
    if (result) {
      Ok(Json.toJson("true"))
    } else {
      NotFound
    }
  }
}
