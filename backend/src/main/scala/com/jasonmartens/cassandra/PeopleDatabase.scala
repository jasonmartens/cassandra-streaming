package com.jasonmartens.cassandra

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.jasonmartens.shared.Protocol.Person
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl.{DatabaseProvider, ResultSet}
import com.outworkers.phantom.reactivestreams._

import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

class PeopleDatabase(override val connector: CassandraConnection)(implicit val ctx: ExecutionContextExecutor) extends Database[PeopleDatabase](connector) {
  object People extends ConcretePeople with connector.Connector

  def insertPerson(p: Person): ScalaFuture[ResultSet] = {
    for {
      insert <- People.insertNewRecord(p)
    } yield insert
  }

  def peopleStream: Source[Person, NotUsed] = {
    Source.fromPublisher(People.publisher())
  }
}

trait PeopleDbProvider extends DatabaseProvider[PeopleDatabase] {
  implicit val ctx: ExecutionContextExecutor
  override def database: PeopleDatabase = new PeopleDatabase(Connector.connector)
}