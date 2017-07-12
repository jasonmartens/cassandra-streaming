package com.jasonmartens.cassandra

import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.stream.{ Attributes, FanInShape2, Inlet, Outlet }
import com.jasonmartens.shared.Protocol.BackpressureMessage

class ManualBackpressure[T, U <: BackpressureMessage](initialDemand: Long) extends GraphStage[FanInShape2[T, U, T]] {

  private val dataIn = Inlet[T]("Data.in")
  private val backpressureIn = Inlet[U]("Backpressure.in")
  private val dataOut = Outlet[T]("Data.out")

  override def shape: FanInShape2[T, U, T] = new FanInShape2(dataIn, backpressureIn, dataOut)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      private var demand: Long = initialDemand
      private var isCompleted = false

      override def preStart(): Unit = {
        // a detached stage needs to start upstream demand
        // itself as it is not triggered by downstream demand
        pull(dataIn)
        pull(backpressureIn)
      }

      def pushData(): Unit = {
        while (demand > 0 && isAvailable(dataIn) && isAvailable(dataOut)) {
          val elem = grab(dataIn)
          if (!isClosed(dataIn)) pull(dataIn)
          println(s"Pushing $elem")
          push(dataOut, elem)
          demand -= 1
        }
        // Complete the stage if dataIn is closed & has no data, OR
        // if backpressureIn is closed
        if ((isClosed(dataIn) && !isAvailable(dataIn)) ||
          isClosed(backpressureIn)) {
          println("Completing stage")
          completeStage()
        }
      }

      // Block the stream when downstream demand is satisfied
      setHandler(dataIn, new InHandler {
        override def onPush(): Unit = {
          pushData()
        }

        // Complete when no upstream data is left
        override def onUpstreamFinish(): Unit = {
          println("dataIn - onUpstreamFinish")
          isCompleted = true
        }
      })

      // Accept backpressure messages from downstream
      setHandler(backpressureIn, new InHandler {
        override def onPush(): Unit = {
          val m: U = grab(backpressureIn)
          println(s"backpressureIn - onPush: message: $m")
          demand += m.demand
          pull(backpressureIn)
          pushData()
        }

        // If the backpressure stream completes, then we will not receive any additional demand
        // and can tear down the stream
        override def onUpstreamFinish(): Unit = {
          println("backpressureIn - onUpstreamFinish")
          isCompleted = true
        }
      })

      // Push whenever there is demand
      setHandler(dataOut, new OutHandler {
        override def onPull(): Unit = pushData()
      })
    }
}
