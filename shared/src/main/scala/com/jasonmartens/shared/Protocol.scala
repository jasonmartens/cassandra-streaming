package com.jasonmartens.shared

object Protocol {
  sealed trait Message
  case class PersonMessage(id: Int, name: String) extends Message
  case class BackpressureMessage(demand: Long) extends Message
}
