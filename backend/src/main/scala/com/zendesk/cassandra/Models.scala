package com.zendesk.cassandra

import com.datastax.driver.core.ResultSet
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.reactivestreams._

import scala.concurrent.{ Future => ScalaFuture }

case class Person(id: Int, name: String)

abstract class People extends CassandraTable[People, Person] {
  object id extends IntColumn(this) with PartitionKey {}
  object name extends StringColumn(this)
}

abstract class ConcretePeople extends People with RootConnector {
  def insertNewRecord(person: Person): ScalaFuture[ResultSet] = {
    insert
      .value(_.id, person.id)
      .value(_.name, person.name)
      .future()
  }

  def findPersonById(id: Int): ScalaFuture[Option[Person]] = {
    select.where(_.id eqs id).one()
  }

}