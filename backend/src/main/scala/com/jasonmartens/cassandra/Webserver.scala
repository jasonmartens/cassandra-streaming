package com.jasonmartens.cassandra

import akka.NotUsed
import akka.http.javadsl.model.ws.Message
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl._
import akka.stream.{ FanInShape2, FlowShape, Graph, Materializer }
import com.jasonmartens.shared.Protocol
import com.jasonmartens.shared.Protocol.{ BackpressureMessage, Person }
import upickle.default._

trait Webserver extends PeopleDbProvider {
  implicit val materializer: Materializer

  val bpGraph = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val bpGraph: Graph[FanInShape2[Person, BackpressureMessage, Person], NotUsed] =
      new ManualBackpressure[Person, BackpressureMessage](0)
    val bpElem = b.add(bpGraph)
    val dbSource = b.add(database.peopleStream)
    val personToMessageFlow = b.add(
      Flow[Person].map(p => TextMessage.Strict(write(p)))
    )
    val tmToBPMessage = b.add(
      Flow[Message].map {
        case tm: TextMessage =>
          println(s"bpMessage: ${tm.getStrictText}"); read[BackpressureMessage](tm.getStrictText)
        case bm: BinaryMessage =>
          bm.dataStream.runWith(Sink.ignore)
          BackpressureMessage(0)
      }
    )

    tmToBPMessage.out ~> bpElem.in1
    dbSource.out ~> bpElem.in0
    bpElem.out ~> personToMessageFlow.in

    FlowShape(tmToBPMessage.in, personToMessageFlow.out)
  }

  val route: Route = get {
    pathSingleSlash {
      getFromResource("web/index.html")
    } ~
      // Scala-JS puts them in the root of the resource directory per default,
      // so that's where we pick them up
      path("frontend-launcher.js")(getFromResource("frontend-launcher.js")) ~
      path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js"))
  } ~
    getFromResourceDirectory("web") ~
    path("ws") {
      handleWebSocketMessages(Flow.fromGraph(bpGraph))
    }
}
