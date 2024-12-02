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

object NNHelper {
  def initializeIO(sAxis: AxiStreamSlaveIf, mAxis: AxiStreamMasterIf) = {
    sAxis.tready := RegInit(true.B)
    mAxis.data.tvalid := RegInit(false.B)
    mAxis.data.tlast := RegInit(false.B)
    mAxis.data.tdata := RegInit(0.S(16.W))
    mAxis.data.tkeep := RegInit("b11".U)
  }

  def connectMaster(masterIO: AxiStreamExternalIf, mAxis: AxiStreamMasterIf) = {
    masterIO
      .suggestName("m_axis")
      .connect(mAxis)
  }

  def connectSlave(slaveIO: AxiStreamExternalIf, sAxis: AxiStreamSlaveIf) = {
    slaveIO.suggestName("s_axis").connect(sAxis)
  }

}

class NeuralNetwork extends Module {
  val io = IO(new Bundle {
    val slaveIO = new AxiStreamExternalIf(8)
    val masterIO = Flipped(new AxiStreamExternalIf(8))
  })

  val sAxis: AxiStreamSlaveIf = Wire(new AxiStreamSlaveIf(8))
  val mAxis: AxiStreamMasterIf = Wire(new AxiStreamMasterIf(8))

  NNHelper.connectMaster(io.masterIO, mAxis)
  NNHelper.connectSlave(io.slaveIO, sAxis)
  NNHelper.initializeIO(sAxis, mAxis)

  val theta_Int8_csv = Utility.readCSV("lab3/theta0_Int8.csv")
  val theta0 = RegInit(VecInit.tabulate(25, 401) { (x, y) =>
    theta_Int8_csv(x)(y).S(8.W)
  })

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
