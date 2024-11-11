//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.5.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.5.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"
package lab2

import chisel3._
import _root_.circt.stage.ChiselStage

class Example extends Module {
  // AXI-Stream Connection
  val sAxis = Wire(new AxiStreamSlaveIf(8))
  IO(new AxiStreamExternalIf(8)).suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(8))
  IO(Flipped(new AxiStreamExternalIf(8))).suggestName("m_axis").connect(mAxis)

  
  val evenCounter = RegInit(0.U(8.W))
  val transferCount = RegInit(false.B)

  sAxis.tready := true.B
  mAxis.data.tvalid := false.B
  mAxis.data.tlast := false.B
  mAxis.data.tdata := 0.U
  mAxis.data.tkeep := "b1".U

  when(sAxis.data.tvalid) {
    when(sAxis.data.tdata(0) === false.B) {
      evenCounter := evenCounter + 1.U
    }
    when(sAxis.data.tlast) {
      transferCount := true.B
    }
  }

  when(transferCount) {
    mAxis.data.tvalid := true.B
    mAxis.data.tlast := true.B
    mAxis.data.tdata := evenCounter
    evenCounter := 0.U
    transferCount := false.B
  }
}

object Example extends App {
  ChiselStage.emitSystemVerilogFile(
    new Example,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
}
