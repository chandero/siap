package controllers

import java.io.File
import play.api.Configuration
import com.google.inject.Inject
import com.typesafe.config.{Config, ConfigFactory}
import lib.WebpackBuildFile
import play.Environment
import play.api.mvc._

class FrontController @Inject() (cc: ControllerComponents, env: Environment, config: Configuration) extends AbstractController(cc){
    def index = Action {
    Ok(views.html.index.render(env, config.get[Int]("webpack.port"), WebpackBuildFile.jsBundle, WebpackBuildFile.cssBundle))
  }
}
