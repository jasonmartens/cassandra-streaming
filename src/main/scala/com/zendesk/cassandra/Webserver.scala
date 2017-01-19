package com.zendesk.cassandra

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.directives.WebSocketDirectives
import akka.stream.Materializer
import akka.stream.scaladsl._

//object WebSocketDirectives extends WebSocketDirectives

trait Webserver {
  implicit val materializer: Materializer
  val outputSource: Source[Message, Any]
  val inputSink: Sink[Message, Any]

  val wsHandler: HttpRequest => HttpResponse = {
    case req @ HttpRequest(GET, Uri.Path("/ws"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => upgrade.handleMessagesWithSinkSource(inputSink, outputSource)
        case None => HttpResponse(NotFound, entity = "Not a valid websocket request")
      }
    case r: HttpRequest =>
      r.discardEntityBytes()
      HttpResponse(NotFound, entity = "Unknown Resource")
  }


}
