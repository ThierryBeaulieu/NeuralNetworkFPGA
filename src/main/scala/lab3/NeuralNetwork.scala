package lab3

import chisel3._
import scala.math.exp
import _root_.circt.stage.ChiselStage

// J'ai besoin de savoir le range de x pour établir
// Elle est où la virugle flottante
class NeuralNetwork extends Module {
  def sigmoid(x: Double): Double = {
    1.0 / (1.0 + exp(-x))
  }

}

object NeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuralNetwork,
    args = Array(
      "--target-dir",
      "generated/lab2/"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}
