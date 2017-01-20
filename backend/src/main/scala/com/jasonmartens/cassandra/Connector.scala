package com.jasonmartens.cassandra

import com.outworkers.phantom.connectors.ContactPoints

object Connector {
  // This is a fictional series of IP addresses
  val hosts = Seq("192.168.42.45")
  val port = 9042
  ContactPoints
  val connector = ContactPoints(hosts, port).noHeartbeat().keySpace("phantom_test")
}
