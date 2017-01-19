package com.zendesk.cassandra

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.zendesk.shared.Protocol
import upickle.default._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.languageFeature.postfixOps
import scala.util.{Failure, Success}

object Boot extends App with AppActorSystem with PeopleDbProvider with Webserver {
  override implicit lazy val system = ActorSystem("phantom_test")
  override implicit lazy val ctx = system.dispatcher
  implicit lazy val materializer = ActorMaterializer()

  val createResult = database.create()
  println(s"createResult: $createResult")

  println("Streaming all people")
  val streamFuture = database.peopleStream
    .runForeach(row => println(row))
  Await.ready(streamFuture, 10 seconds)

  val retrievedSelf = Await.result(database.getPerson(1), 1 second)
  println(s"RetrievedSelf: $retrievedSelf")

  val outputSource: Source[Message, Any] =
    database.peopleStream.map(p =>
      TextMessage.Strict(write(Protocol.PersonMessage(p.id, p.name))))
  val inputSink: Sink[Message, Any] =
    Sink.foreach(m => println(s"Received message: $m"))

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  bindingFuture.onComplete {
    case Success(b) ⇒
      val localAddress = b.localAddress
      println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
