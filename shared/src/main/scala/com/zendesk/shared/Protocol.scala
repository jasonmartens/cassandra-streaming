package com.zendesk.shared

object Protocol {
  sealed trait Message
  case class PersonMessage(id: Int, name: String) extends Message
  case class ResponseMessage(contents: String) extends Message
}
