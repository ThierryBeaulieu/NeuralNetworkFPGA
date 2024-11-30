package project

package project

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source

class MemoryTest(csvSelected: String) extends Module {

  val io = IO(new Bundle {
    val outputTreeAdder = Output(SInt((16).W))
  })

  private val weightsCSV = readCSV(csvSelected)
  val weights = VecInit.tabulate(10, 401) { (x, y) =>
    weightsCSV(x)(y).S(8.W)
  }

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

  io.outputTreeAdder := 0.S

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

}

object MemoryTest extends App {
  ChiselStage.emitSystemVerilogFile(
    new MemoryTest("weights.csv"),
    args = Array(
      "--target-dir",
      "generated/project/",
      "--log-level",
      "debug",
      "--log-file",
      "neuron_wrapper.log"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}
