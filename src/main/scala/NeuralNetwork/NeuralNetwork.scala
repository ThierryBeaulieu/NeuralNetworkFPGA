//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.5.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.5.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"
package lab2

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source

object Util {
  def readCSV(filePath: String): Array[Array[Int]] = {
    val source = Source.fromFile(filePath)
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

class NeuralNetwork extends Module {
  // AXI-Stream Connection
  val sAxis = Wire(new AxiStreamSlaveIf(8))
  IO(new AxiStreamExternalIf(8)).suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(8))
  IO(Flipped(new AxiStreamExternalIf(8))).suggestName("m_axis").connect(mAxis)

  
  val input_data = RegInit(VecInit(Seq.fill(401)(1.U(8.W)))) 
  val transferCount = RegInit(false.B)

  sAxis.tready := true.B
  mAxis.data.tvalid := false.B
  mAxis.data.tlast := false.B
  mAxis.data.tdata := 0.U
  mAxis.data.tkeep := "b1".U

  val index = RegInit(0.U(9.W))
  when(sAxis.data.tvalid) {
    index := index + 1.U
    input_data(index) := sAxis.data.tdata
    when(sAxis.data.tlast & index === input_data.length.U - 1.U) {
      transferCount := true.B
      index := 0.U
    }
  }
  val index2 = RegInit(0.U(9.W))
  when(transferCount) {
    mAxis.data.tvalid := true.B
    mAxis.data.tdata := input_data(index2)
    index2 := index2 + 1.U
    when(index2 === (input_data.length.U - 1.U)) {
      mAxis.data.tlast := true.B
      transferCount := false.B
      index2 := 0.U
    }
  }
}

object NeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuralNetwork,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
}
