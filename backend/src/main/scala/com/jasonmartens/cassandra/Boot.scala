package com.jasonmartens.cassandra

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.jasonmartens.shared.Protocol.Person

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.languageFeature.postfixOps
import scala.util.{ Failure, Success }

object Boot extends App with AppActorSystem with PeopleDbProvider with Webserver {
  override implicit lazy val system = ActorSystem("phantom_test")
  override implicit lazy val ctx = system.dispatcher
  implicit lazy val materializer = ActorMaterializer()

  val createResult = database.create()
  println(s"createResult: $createResult")

  //  loadSampleData()

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  bindingFuture.onComplete {
    case Success(b) ⇒
      val localAddress = b.localAddress
      println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }

  def loadSampleData(): Unit = {
    val fileIter = scala.io.Source.fromURL(getClass.getResource("/app_c.csv")).getLines()
    // skip the first line with the column names
    fileIter.next()
    for (line <- fileIter) {
      val fields = line.split(",").map {
        case f: String if f == "(S)" => "NaN"
        case otherwise => otherwise
      }
      //      println(s"Inserting: ${fields.mkString(" ")}")
      val result = Await.result(database.insertPerson(personFromCsv(fields)), 10 seconds)
      println(s"insert result: $result")
    }
  }

  def personFromCsv(data: Array[String]): Person = {
    Person(
      UUID.randomUUID(),
      data(0),
      data(1).toInt, data(2).toInt, data(3).toFloat, data(4).toFloat, data(5).toFloat,
      data(6).toFloat, data(7).toFloat, data(8).toFloat, data(9).toFloat, data(10).toFloat
    )
  }
}
