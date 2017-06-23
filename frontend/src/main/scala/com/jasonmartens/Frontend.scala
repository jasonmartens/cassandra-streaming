package com.jasonmartens

import com.jasonmartens.shared.Protocol
import com.jasonmartens.shared.Protocol.BackpressureMessage
import com.karasiq.bootstrap.BootstrapImplicits._
import com.karasiq.bootstrap.grid.GridSystem
import com.karasiq.bootstrap.icons.FontAwesome
import com.karasiq.bootstrap.navbar.{ NavigationBar, NavigationBarStyle, NavigationTab }
import com.karasiq.bootstrap.table.TableRow
import org.scalajs.dom
import org.scalajs.dom.raw._
import rx.{ Ctx, Var }
import upickle.default._

import scala.language.postfixOps
import scala.scalajs.js
import scalatags.JsDom.all._

object Frontend extends js.JSApp {
  private implicit val context = implicitly[Ctx.Owner] // Stops working if moved to main(), macro magic

  val queryButton: HTMLButtonElement = dom.document.getElementById("query").asInstanceOf[HTMLButtonElement]
  val requestButton: HTMLButtonElement = dom.document.getElementById("request").asInstanceOf[HTMLButtonElement]

  def main(): Unit = {
    val data: Var[Seq[TableRow]] = Var(Seq.empty)
    val testTable = new SurnameTable(data)
    queryButton.onclick = { (event: MouseEvent) ⇒
      queryDB(data)
      event.preventDefault()
    }

    val tableVisible = Var(false)
    val tabTitle = Var("Wait...")
    val navigationBar = NavigationBar()
      .withBrand("Cassandra DB Streaming")
      .withTabs(
        NavigationTab(tabTitle, "table", "table".fontAwesome(FontAwesome.fixedWidth), testTable, tableVisible.reactiveShow)
      )
      .withContentContainer(content ⇒ GridSystem.container(id := "main-container", GridSystem.mkRow(content)))
      .withStyles(NavigationBarStyle.inverse, NavigationBarStyle.fixedTop)
      .build()

    // Render page
    navigationBar.applyTo(dom.document.body)
  }

  def queryDB(data: Var[Seq[TableRow]]): Unit = {
    queryButton.disabled = true
    println(s"Querying Database...")
    val dbStream = new WebSocket(getWebsocketUri(dom.document))
    dbStream.onopen = { (event: Event) =>
      println("dbStream connection was successful!")
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
        requestNumber.focus()
        event.preventDefault()
      }

      event
    }
    dbStream.onerror = { (event: ErrorEvent) ⇒
      println(s"Failed: code: ${event.colno}")
      queryButton.disabled = false
      requestButton.disabled = true
    }
    dbStream.onmessage = { (event: MessageEvent) ⇒
      val wsMsg = read[Protocol.Message](event.data.toString)
      wsMsg match {
        case p: Protocol.Person ⇒
          // This is probably super inefficient, revisit and explore better options
          data() = Seq(TableRow(Seq(p.id.toString, p.name, p.rank.toString, p.count.toString, f"${p.prop100k}%.2f",
            f"${p.cum_prop100k}%.2f", f"${p.pctwhite}%3.2f%%", f"${p.pctblack}%3.2f%%", f"${p.pctapi}%3.2f%%",
            f"${p.pctaian}%3.2f%%", f"${p.pct2prace}%3.2f%%", f"${p.pcthispanic}%3.2f%%"))) ++ data.now
        case what => writeToArea(s"Got a what!?!?! - $what")
      }
    }

    dbStream.onclose = { (event: Event) ⇒
      println("All results retrieved, connection closed.")
      queryButton.disabled = false
      requestButton.disabled = true
    }

    def writeToArea(text: String): Unit =
      println(text)
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