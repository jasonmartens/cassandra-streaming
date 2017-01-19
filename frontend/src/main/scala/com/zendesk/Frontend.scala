package com.zendesk

import com.zendesk.shared.Protocol
import org.scalajs.dom.raw._

import scala.scalajs.js
import org.scalajs.dom
import upickle.default._

object Frontend extends js.JSApp {
  val joinButton = dom.document.getElementById("join").asInstanceOf[HTMLButtonElement]
  val sendButton = dom.document.getElementById("send").asInstanceOf[HTMLButtonElement]

  def main(): Unit = {
    val nameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
    joinButton.onclick = { (event: MouseEvent) ⇒
      joinChat(nameField.value)
      event.preventDefault()
    }
    nameField.focus()
    nameField.onkeypress = { (event: KeyboardEvent) ⇒
      if (event.keyCode == 13) {
        joinButton.click()
        event.preventDefault()
      }
    }
  }

  def joinChat(name: String): Unit = {
    joinButton.disabled = true
    val playground = dom.document.getElementById("playground")
    playground.innerHTML = s"Trying to join chat as '$name'..."
    val dbStream = new WebSocket(getWebsocketUri(dom.document, name))
    dbStream.onopen = { (event: Event) ⇒
      playground.insertBefore(p("dbStream connection was successful!"), playground.firstChild)
      sendButton.disabled = false

      val messageField = dom.document.getElementById("message").asInstanceOf[HTMLInputElement]
      messageField.focus()
      messageField.onkeypress = { (event: KeyboardEvent) ⇒
        if (event.keyCode == 13) {
          sendButton.click()
          event.preventDefault()
        }
      }
      sendButton.onclick = { (event: Event) ⇒
        dbStream.send(messageField.value)
        messageField.value = ""
        messageField.focus()
        event.preventDefault()
      }

      event
    }
    dbStream.onerror = { (event: ErrorEvent) ⇒
      playground.insertBefore(p(s"Failed: code: ${event.colno}"), playground.firstChild)
      joinButton.disabled = false
      sendButton.disabled = true
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
      joinButton.disabled = false
      sendButton.disabled = true
    }

    def writeToArea(text: String): Unit =
      playground.insertBefore(p(text), playground.firstChild)
  }

  def getWebsocketUri(document: Document, nameOfChatParticipant: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/ws"
  }

  def p(msg: String) = {
    val paragraph = dom.document.createElement("p")
    paragraph.innerHTML = msg
    paragraph
  }
}