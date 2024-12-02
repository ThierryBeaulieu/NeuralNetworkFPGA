package lab3

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import chisel3.util._

object Utility {
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

}

object State extends ChiselEnum {
  val receiving, firstHiddenLayer, firstSigmoid, secondHiddenLayer,
      secondSigmoid, sending =
    Value
}

class NeuralNetwork extends Module {

  val theta_Int8_csv = Utility.readCSV("lab3/theta0_Int8.csv")
  val theta0 = RegInit(VecInit.tabulate(25, 401) { (x, y) =>
    theta_Int8_csv(x)(y).S(8.W)
  })
  printf("\ntheta0_Int8\n")

  // Print the contents of theta0
  for (i <- 0 until 1) {
    for (j <- 0 until 401) {
      printf(p"theta0($i)($j) = ${theta0(i)(j)}\n")
    }
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
