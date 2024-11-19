package project

import chisel3._
import _root_.circt.stage.ChiselStage

object StochasticNeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new Neuron(4),
    args = Array(
      "--target-dir",
      "generated/project/"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}
