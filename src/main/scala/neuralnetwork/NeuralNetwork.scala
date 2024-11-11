//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.5.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.5.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"
package lab2

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source

class NeuralNetwork extends Module {
  // AXI-Stream Connection
  val sAxis = Wire(new AxiStreamSlaveIf(16))
  IO(new AxiStreamExternalIf(16)).suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(16))
  IO(Flipped(new AxiStreamExternalIf(16))).suggestName("m_axis").connect(mAxis)

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

  val LabelW = 10
  val InputW = 401

  val rawData = readCSV("weights.csv") 
  val weights = RegInit(VecInit.tabulate(LabelW, InputW){ (x, y) => rawData(x)(y).S(16.W) })

  val sending = RegInit(false.B)
  val output_data = RegInit(VecInit(Seq.fill(10)(0.S(16.W))))
  val transferCount = RegInit(0.U(4.W))
  val row = RegInit(0.U(9.W)) // 0 to 401

  sAxis.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.S(16.W))
  mAxis.data.tkeep := RegInit("b11".U)

  when(sAxis.data.tvalid) {
    for (col <- 0 until 10) {
      output_data(col) := output_data(col) + (weights(col)(row) * sAxis.data.tdata)
    }
    row := row + 1.U
    when(sAxis.data.tlast) {
      sending := true.B
    }
  }

  when(sending && mAxis.tready) {
    when (transferCount === output_data.length.U) {
      mAxis.data.tlast := true.B
      mAxis.data.tvalid := false.B
      output_data := VecInit(Seq.fill(10)(0.S(16.W)))
      transferCount := 0.U
      row := 0.U
      sending := false.B
    }.otherwise {
      mAxis.data.tlast := false.B
      mAxis.data.tvalid := true.B
      mAxis.data.tdata := output_data(transferCount)
      transferCount := transferCount + 1.U
    }
  }
}

object NeuralNetwork extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuralNetwork,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
}
