package com.jasonmartens.cassandra

import com.datastax.driver.core.ResultSet
import com.jasonmartens.shared.Protocol.Person
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

import scala.concurrent.{ Future => ScalaFuture }

abstract class People extends CassandraTable[People, Person] {
  object bucket extends StringColumn(this) with PartitionKey
  object id extends UUIDColumn(this)
  object name extends StringColumn(this)
  object rank extends IntColumn(this) with PrimaryKey with ClusteringOrder with Ascending
  object count extends IntColumn(this)
  object prop100k extends FloatColumn(this)
  object cum_prop100k extends FloatColumn(this)
  object pctwhite extends FloatColumn(this)
  object pctblack extends FloatColumn(this)
  object pctapi extends FloatColumn(this)
  object pctaian extends FloatColumn(this)
  object pct2prace extends FloatColumn(this)
  object pcthispanic extends FloatColumn(this)
}

abstract class ConcretePeople extends People with RootConnector {
  def insertNewRecord(person: Person): ScalaFuture[ResultSet] = {
    insert
      .value(_.bucket, "a")
      .value(_.id, person.id)
      .value(_.name, person.name)
      .value(_.rank, person.rank)
      .value(_.count, person.count)
      .value(_.prop100k, person.prop100k)
      .value(_.cum_prop100k, person.cum_prop100k)
      .value(_.pctwhite, person.pctwhite)
      .value(_.pctblack, person.pctblack)
      .value(_.pctapi, person.pctapi)
      .value(_.pctaian, person.pctaian)
      .value(_.pct2prace, person.pct2prace)
      .value(_.pcthispanic, person.pcthispanic)
      .future()
  }

  def findNameByRank(rank: Int): ScalaFuture[Option[Person]] = {
    select.where(_.rank eqs rank).one()
  }

}