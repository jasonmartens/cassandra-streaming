package com.zendesk.cassandra

import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl._

trait Webserver {
  implicit val materializer: Materializer
  val outputSource: Source[Message, Any]
  val inputSink: Sink[Message, Any]

  val route: Route = get {
    pathSingleSlash {
      getFromResource("web/index.html")
    } ~
      // Scala-JS puts them in the root of the resource directory per default,
      // so that's where we pick them up
      path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
      path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js"))
  } ~
  getFromResourceDirectory("web") ~
  path("ws") {
    extractRequest { request =>
      request.header[UpgradeToWebSocket] match {
        case Some(upgrade) =>
          complete(upgrade.handleMessagesWithSinkSource(inputSink, outputSource))
        case None =>
          complete(HttpResponse(NotFound, entity = "Not a valid websocket request"))
      }
    }
  }
}
