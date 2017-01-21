package com.jasonmartens.shared

import java.util.UUID

object Protocol {
  sealed trait Message
  case class Person(id: UUID, name: String, rank: Int, count: Int, prop100k: Float, cum_prop100k: Float,
    pctwhite: Float, pctblack: Float, pctapi: Float, pctaian: Float, pct2prace: Float, pcthispanic: Float) extends Message
  case class PersonMessage(id: Int, name: String) extends Message
  case class BackpressureMessage(demand: Long) extends Message
}
