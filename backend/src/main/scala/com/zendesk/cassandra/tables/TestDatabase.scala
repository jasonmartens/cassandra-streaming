package com.zendesk.cassandra.tables

import com.outworkers.phantom.builder.query.CreateQuery
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl.{ KeySpace, comment }
import com.zendesk.cassandra.{ Connector, People, Person }

class TestDatabase(override val connector: CassandraConnection) extends Database[TestDatabase](connector) {

  object recipes extends People with Connector {
    override def autocreate(space: KeySpace): CreateQuery.Default[People, Person] = {
      create.ifNotExists()(space).`with`(comment eqs "This is a test string")
    }
  }
}

object TestDatabase extends TestDatabase(Connector.connector)