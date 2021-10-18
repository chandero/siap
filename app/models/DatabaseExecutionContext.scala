package models

import javax.inject._

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext

/**
* Esta clase es un apuntador a un contexto de ejecución, configurado para apuntar a "database.dispatcher"
* en el archivo application.conf
*/
@Singleton
class DatabaseExecutionContext @Inject()(system: ActorSystem) extends CustomExecutionContext(system, "database.dispatcher")