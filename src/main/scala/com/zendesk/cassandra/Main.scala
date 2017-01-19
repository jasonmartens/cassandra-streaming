package com.zendesk.cassandra

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.languageFeature.postfixOps

object Main extends App with AppActorSystem with PeopleDbProvider with Webserver {
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
    database.peopleStream.map(p => TextMessage.Strict(p.toString))
  val inputSink: Sink[Message, Any] =
    Sink.foreach(m => println(s"Received message: $m"))


  system.terminate()
}
