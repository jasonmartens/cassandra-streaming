package com.jasonmartens.cassandra

import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoints}
import com.typesafe.config.ConfigFactory

import collection.JavaConverters._

object Connector {
  private val config = ConfigFactory.load()
  val hosts: Seq[String] = config.getStringList("cassandra.hosts").asScala
  val port: Int = config.getInt("cassandra.port")
  val connector: CassandraConnection = ContactPoints(hosts, port).noHeartbeat().keySpace("phantom_test")
}
