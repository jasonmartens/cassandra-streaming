package com.jasonmartens.cassandra

import java.util.UUID

import akka.actor.ActorSystem
import com.jasonmartens.shared.Protocol.Person
import com.outworkers.phantom.dsl.ResultSet

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object InsertSampleData extends App with AppActorSystem with PeopleDbProvider {
  override implicit lazy val system = ActorSystem("phantom_test")
  override implicit lazy val ctx = system.dispatcher

  val createResult = database.create()
  println(s"createResult: $createResult")

  val results = Await.result(loadSampleData(), 5 minutes)
  results.foreach(rs => println(s"rs: $rs"))
  if (results.forall(rs => rs.wasApplied())) {
    println("Inserted all rows successfully")
  } else {
    results.filter(_.wasApplied() == false).foreach { rs =>
      println(s"Failed to insert row: $rs")
    }
  }

  system.terminate()

  def loadSampleData(): Future[Iterator[ResultSet]] = {
    val fileIter = scala.io.Source.fromURL(getClass.getResource("/app_c.csv")).getLines()
    // skip the first line with the column names
    fileIter.next()

    val insertFutures: Iterator[Future[ResultSet]] =
      for (line <- fileIter) yield {
        val fields = line.split(",").map {
          case f: String if f == "(S)" => "NaN"
          case otherwise => otherwise
        }
        val res = database.insertPerson(personFromCsv(fields))
        res.onFailure { case ex: Exception => println(s"Failed to insert a row with exception $ex") }
        res
      }

    Future.sequence(insertFutures)
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
