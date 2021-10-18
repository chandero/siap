package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import models.UsuarioRepository
import models.Usuario
import dto.UsuarioDto

class AuthenticatedUserAction @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext)
extends ActionBuilderImpl(parser) {

    private val logger = play.api.Logger(this.getClass)

    override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
        logger.debug("ENTERED AuthenticatedUserAction::invokeBlock")
        val token = request.headers.get("Authorization")
        token match {
            case None => {
                logger.debug("CAME INTO 'NONE'")
                Future.successful(Forbidden("Dude, you’re not logged in."))
            }
            case Some(token) => {
                logger.debug("CAME INTO 'SOME'")
                /* 
                val res: Future[Result] = block(request)
                res
                */
                block(request)
            }
        }
    }
}
