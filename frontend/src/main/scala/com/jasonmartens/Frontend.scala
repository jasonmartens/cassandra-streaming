package com.jasonmartens

import com.jasonmartens.shared.Protocol
import com.jasonmartens.shared.Protocol.BackpressureMessage
import org.scalajs.dom.raw._

import scala.scalajs.js
import org.scalajs.dom
import upickle.default._

object Frontend extends js.JSApp {
  val queryButton = dom.document.getElementById("query").asInstanceOf[HTMLButtonElement]
  val requestButton = dom.document.getElementById("request").asInstanceOf[HTMLButtonElement]

  def main(): Unit = {
    queryButton.onclick = { (event: MouseEvent) ⇒
      queryDB()
      event.preventDefault()
    }
  }

  def queryDB(): Unit = {
    queryButton.disabled = true
    val playground = dom.document.getElementById("playground")
    playground.innerHTML = s"Querying Database..."
    val dbStream = new WebSocket(getWebsocketUri(dom.document))
    dbStream.onopen = { (event: Event) =>
      playground.insertBefore(p("dbStream connection was successful!"), playground.firstChild)
      requestButton.disabled = false

      val requestNumber = dom.document.getElementById("requestNumber").asInstanceOf[HTMLInputElement]
      requestNumber.focus()
      requestNumber.onkeypress = { (event: KeyboardEvent) ⇒
        if (event.keyCode == 13) {
          requestButton.click()
          event.preventDefault()
        }
      }
      requestButton.onclick = { (event: Event) ⇒
        val bpMessage = BackpressureMessage(requestNumber.value.toInt)
        dbStream.send(write(bpMessage))
        requestNumber.value = ""
        requestNumber.focus()
        event.preventDefault()
      }

      event
    }
    dbStream.onerror = { (event: ErrorEvent) ⇒
      playground.insertBefore(p(s"Failed: code: ${event.colno}"), playground.firstChild)
      queryButton.disabled = false
      requestButton.disabled = true
    }
    dbStream.onmessage = { (event: MessageEvent) ⇒
      val wsMsg = read[Protocol.Message](event.data.toString)
      wsMsg match {
        case Protocol.PersonMessage(id, name) ⇒ writeToArea(s"$name has id: $id")
        case what => writeToArea(s"Got a what!?!?! - $what")
      }
    }
    dbStream.onclose = { (event: Event) ⇒
      playground.insertBefore(p("Connection to chat lost. You can try to rejoin manually."), playground.firstChild)
      queryButton.disabled = false
      requestButton.disabled = true
    }

    def writeToArea(text: String): Unit =
      playground.insertBefore(p(text), playground.firstChild)
  }

  def getWebsocketUri(document: Document): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/ws"
  }

  def p(msg: String) = {
    val paragraph = dom.document.createElement("p")
    paragraph.innerHTML = msg
    paragraph
  }
}