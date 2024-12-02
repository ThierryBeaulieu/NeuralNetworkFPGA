package lab3

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import chisel3.util._

class NeuralNetwork extends Module {

  def readCSV(filePath: String): Array[Array[Int]] = {
    val source = Source.fromResource(filePath)
    val data = source
      .getLines()
      .map { line =>
        line.split(",").map(_.trim.toInt)
      }
      .toArray
    source.close()
    data
  }

  object State extends ChiselEnum {
    val receiving, firstHiddenLayer, firstSigmoid, secondHiddenLayer,
        secondSigmoid, sending =
      Value
  }

  val state = RegInit(State.receiving)

  switch(state) {
    is(State.receiving) {}
    is(State.firstHiddenLayer) {}
    is(State.firstSigmoid) {}
    is(State.secondHiddenLayer) {}
    is(State.secondSigmoid) {}
    is(State.sending) {}
  }

}

object NeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuralNetwork,
    args = Array(
      "--target-dir",
      "generated/lab3/"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}
