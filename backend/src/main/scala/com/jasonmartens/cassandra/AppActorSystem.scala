package com.jasonmartens.cassandra

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait AppActorSystem {
  implicit val system: ActorSystem
  implicit def ctx: ExecutionContext
}
