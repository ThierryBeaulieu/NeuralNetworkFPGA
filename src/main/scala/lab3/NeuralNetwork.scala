package lab3

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import scala.math._

class NeuralNetwork extends Module {
  val io = IO(new Bundle {
    val outputState = Output(UInt(3.W))
    val outputMultiplication = Output(SInt(25.W))
    val outputUMultiplication = Output(UInt(25.W))
    val outputWeight = Output(SInt(8.W))
    val outputSigmoid = Output(UInt(25.W))
  })

  // AXI-Stream Connection
  val sAxis = Wire(new AxiStreamSlaveIf(8))
  val slaveIO =
    IO(new AxiStreamExternalIf(8))
  slaveIO.suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(8))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(8)))
  masterIO
    .suggestName("m_axis")
    .connect(mAxis)

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

  def sigmoid(x: Double): UInt = {
    val result = (pow(E, x) / (1 + pow(E, x)))
    val resultSInt = (result * pow(2, 7)).toInt.asUInt(8.W)
    resultSInt
  }

  def initSigmoid(sigmoidMemory: SyncReadMem[UInt]) = {
    // [4:4] [-4.0, 3.9375] = 8 / (2*8)
    for (i <- -128 until 128) {
      sigmoidMemory.write(
        (i.S).asUInt,
        sigmoid(i / 32.0)
      )
    }
  }

  val sigmoid: SyncReadMem[UInt] = SyncReadMem(pow(2, 8).toInt, UInt(8.W))
  initSigmoid(sigmoid)

  io.outputSigmoid := sigmoid.read((-128.S).asUInt)

  val rawData = readCSV("lab3/theta_0_int8.csv")
  val weights_hidden_layer1 = RegInit(
    VecInit.tabulate(25, 401) { (x, y) =>
      rawData(x)(y).S(8.W)
    }
  )
  io.outputWeight := weights_hidden_layer1(0)(0)
  io.outputUMultiplication := 0.U
  io.outputMultiplication := 0.S
  io.outputState := 0.U

  sAxis.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.U(8.W))
  mAxis.data.tkeep := RegInit("b1".U)

  val image = RegInit(VecInit(Seq.fill(401)(0.S(8.W))))
  val index = RegInit(0.U(9.W))

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

  val state = RegInit(State.receiving)

  when(state === State.receiving) {
    io.outputState := 1.U
    when(sAxis.data.tvalid) {
      image(index) := (sAxis.data.tdata).asSInt
      index := index + 1.U
      when(sAxis.data.tlast) {
        state := State.handling
        sAxis.tready := false.B
      }
    }
  }

  // [1:7] * [2:6] = [3:13] [12:13]
  // [-33554432, 33554431]
  val layer1 = RegInit(VecInit(Seq.fill(25)(0.S(25.W))))
  val pixelIndex = RegInit(0.U(9.W))
  val row = RegInit(0.U(5.W))
  when(state === State.handling) {
    io.outputState := 2.U

    layer1(row) := (layer1(row) + (weights_hidden_layer1(row)(
      pixelIndex
    ) * image(pixelIndex)))

    pixelIndex := (pixelIndex + 1.U)

    when(pixelIndex === (401.U - 1.U)) {
      row := (row + 1.U)
      pixelIndex := 0.U
    }

    when(row === 24.U && pixelIndex === (401.U - 1.U)) {
      state := State.sending
      row := 0.U
      pixelIndex := 0.U
    }
  }

  when(state === State.sending) {
    io.outputState := 3.U
    io.outputMultiplication := layer1(1) >> 13
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
