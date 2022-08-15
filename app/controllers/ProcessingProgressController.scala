package controllers

import javax.inject.Inject
import java.util.UUID

import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton

import pdi.jwt.JwtSession
import play.api.Configuration

import akka.NotUsed
import akka.stream.scaladsl.Source
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.EventSource.Event
import play.api.mvc.{AbstractController, ControllerComponents}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import utilities._
// import services.ProcessingProgressTracker
import models.ProcessEvent
import services.ProcessingProgressTrackerEvent
// import models.ProcessingStatus

class ProcessingProgressController @Inject()(cc: ControllerComponents,
                                            authenticatedUserAction: AuthenticatedUserAction)
                                            extends AbstractController(cc) with ImplicitJsonFormats {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(DateTimeSerializer) 

  def getProgressStatuses(sessionId: String) = Action {

    // val sourceOfSessionStatuses: Source[String, NotUsed] = ProcessingProgressTracker.getSourceOfStatuses(sessionId)
    //  .getOrElse(ProcessingProgressTracker.createSourceOfStatuses(sessionId))
    //  .map(sessionStatus => format(sessionStatus.entryName))
    val sourceOfSessionStatuses: Source[String, NotUsed] = ProcessingProgressTrackerEvent.getSourceOfStatuses(sessionId)
     .getOrElse(ProcessingProgressTrackerEvent.createSourceOfStatuses(sessionId))
     .map(sessionStatus => format(sessionStatus))


    Ok.chunked(sourceOfSessionStatuses via EventSource.flow)
      .as(ContentTypes.EVENT_STREAM)
      .withHeaders("Cache-Control" -> "no-cache")
      .withHeaders("Connection" -> "keep-alive")
  }

  private def format(message: ProcessEvent): String = {
    val event = write(message)
    s"$event\n\n"
  }
}
