package com.jasonmartens.cassandra

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.language.postfixOps
import scala.languageFeature.postfixOps
import scala.util.{ Failure, Success }

object Boot extends App with AppActorSystem with PeopleDbProvider with Webserver {
  override implicit lazy val system = ActorSystem("phantom_test")
  override implicit lazy val ctx = system.dispatcher
  implicit lazy val materializer = ActorMaterializer()

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
