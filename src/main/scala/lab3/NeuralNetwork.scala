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
  val receiving, firstSigmoid, secondSigmoid, sending =
    Value
}

object NetworkHelper {
  def initializeIO(
      sAxis: AxiStreamSlaveIf,
      mAxis: AxiStreamMasterIf,
      outputWidth: Int
  ) = {
    sAxis.tready := RegInit(true.B)
    mAxis.data.tvalid := RegInit(false.B)
    mAxis.data.tlast := RegInit(false.B)
    mAxis.data.tdata := RegInit(0.S(outputWidth.W))
    mAxis.data.tkeep := RegInit("b1".U)
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

class HiddenLayer0(theta0: Vec[Vec[SInt]], sum: Vec[SInt], col: UInt)
    extends Module {

  def handlePixel(pixel: SInt) = {
    for (row <- 0 until 25) {
      sum(row) := (sum(row) + (pixel * theta0(row)(col)))
    }
    if (col == (401.U - 1.U)) {
      col := 0.U
    } else {
      col := col + 1.U
    }
  }
}

class NeuralNetwork(inputWidth: Int = 8, outputWidth: Int = 8) extends Module {
  val io = IO(new Bundle {
    val slaveIO = new AxiStreamExternalIf(inputWidth)
    val masterIO = Flipped(new AxiStreamExternalIf(inputWidth))
  })

  val sAxis: AxiStreamSlaveIf = Wire(new AxiStreamSlaveIf(outputWidth))
  val mAxis: AxiStreamMasterIf = Wire(new AxiStreamMasterIf(outputWidth))

  NetworkHelper.connectMaster(io.masterIO, mAxis)
  NetworkHelper.connectSlave(io.slaveIO, sAxis)
  NetworkHelper.initializeIO(sAxis, mAxis, outputWidth)

  val theta_Int8_csv = Utility.readCSV("lab3/theta0_Int8.csv")
  val theta0: Vec[Vec[SInt]] = RegInit(VecInit.tabulate(25, 401) { (x, y) =>
    theta_Int8_csv(x)(y).S(8.W)
  })

  val state = RegInit(State.receiving)
  val hiddenLayer0_result: Vec[SInt] = RegInit(VecInit(Seq.fill(25)(0.S(32.W))))
  var col = RegInit(0.U(9.W))
  val hiddenLayer0 = Module(new HiddenLayer0(theta0, hiddenLayer0_result, col))

  switch(state) {
    is(State.receiving) {
      when(sAxis.data.tvalid) {
        val pixel: SInt = sAxis.data.tdata
        // printf(p"${pixel}, ")
        hiddenLayer0.handlePixel(pixel)
        when(sAxis.data.tlast) {
          state := State.firstSigmoid
        }
      }
    }
    is(State.firstSigmoid) {
      for (i <- 0 until 25) {
        printf(p"${hiddenLayer0_result(i)}, ")
      }
    }
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
