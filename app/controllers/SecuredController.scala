package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import pdi.jwt.JwtSession

import models._

@Singleton
class SecuredController @Inject()(usuarioService: UsuarioRepository, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc){

}