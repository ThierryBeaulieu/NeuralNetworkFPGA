package lab3

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import scala.math._
import chisel3.util._

class NeuralNetwork extends Module {
  val io = IO(new Bundle {
    val outputState = Output(UInt(3.W))
    val outputMultiplication = Output(SInt(25.W))
    val outputUMultiplication = Output(UInt(25.W))
    val outputWeight = Output(SInt(8.W))
    val outputSigmoid0 = Output(UInt(8.W))
    val outputSigmoid1 = Output(UInt(8.W))
    val outputSigmoid2 = Output(UInt(8.W))
    val outputSigmoid3 = Output(UInt(8.W))
    val outputSigmoid4 = Output(UInt(8.W))
    val outputSigmoid5 = Output(UInt(8.W))
    val outputSigmoid6 = Output(UInt(8.W))
    val outputSigmoid7 = Output(UInt(8.W))
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

  def initSigmoidMemory(memory: SyncReadMem[UInt]) = {
    for (i <- -128 until 128) {
      memory.write(
        (i.S).asUInt,
        scalaSigmoid(i / 32.0)
      )
    }
  }

  def scalaSigmoid(x: Double): UInt = {
    val result = (pow(E, x) / (1 + pow(E, x)))
    val resultSInt = (result * pow(2, 7)).toInt.asUInt(8.W)
    resultSInt
  }

  val sigmoidMemory = SyncReadMem(256, UInt(8.W))
  initSigmoidMemory(sigmoidMemory)

  val rawData = readCSV("lab3/theta_0_int8.csv")
  val weights_hidden_layer1 = RegInit(
    VecInit.tabulate(25, 401) { (x, y) =>
      rawData(x)(y).S(8.W)
    }
  )

  io.outputSigmoid0 := 0.U
  io.outputSigmoid1 := 0.U
  io.outputSigmoid2 := 0.U
  io.outputSigmoid3 := 0.U
  io.outputSigmoid4 := 0.U
  io.outputSigmoid5 := 0.U
  io.outputSigmoid6 := 0.U
  io.outputSigmoid7 := 0.U

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
    val receiving, firstHiddenLayer, firstSigmoid, sending = Value
  }

  val state = RegInit(State.receiving)

  val hiddenLayer1 = RegInit(VecInit(Seq.fill(25)(0.S(25.W))))
  val pixelIndex = RegInit(0.U(9.W))
  val row = RegInit(0.U(5.W))

  val sigHiddenLayer1 = RegInit(VecInit(Seq.fill(26)(1.U(8.W))))

  val isLoaded = RegInit(false.B)

  switch(state) {
    is(State.receiving) {
      io.outputState := 1.U
      when(sAxis.data.tvalid) {
        image(index) := (sAxis.data.tdata).asSInt
        index := index + 1.U
        when(sAxis.data.tlast) {
          state := State.firstHiddenLayer
          sAxis.tready := false.B
        }
      }
    }
    is(State.firstHiddenLayer) {
      io.outputState := 2.U

      hiddenLayer1(row) := (hiddenLayer1(row) + (weights_hidden_layer1(row)(
        pixelIndex
      ) * image(pixelIndex)))

      pixelIndex := (pixelIndex + 1.U)

      when(pixelIndex === (401.U - 1.U)) {
        row := (row + 1.U)
        pixelIndex := 0.U
      }

      when(row === 24.U && pixelIndex === (401.U - 1.U)) {
        state := State.firstSigmoid
        row := 0.U
        pixelIndex := 0.U
      }
    }
    is(State.firstSigmoid) {
      io.outputState := 3.U
      for (i <- 1 until hiddenLayer1.length) {
        val addr = (hiddenLayer1(i - 1)(16, 9)).asUInt
        sigHiddenLayer1(i) := sigmoidMemory.read(addr)
      }
      isLoaded := true.B
      when(isLoaded === true.B) {
        state := State.sending
      }
    }
    is(State.sending) {
      io.outputState := 4.U
      io.outputSigmoid0 := sigHiddenLayer1(0)
      io.outputSigmoid1 := sigHiddenLayer1(1)
      io.outputSigmoid2 := sigHiddenLayer1(2)
      io.outputSigmoid3 := sigHiddenLayer1(3)
      io.outputSigmoid4 := sigHiddenLayer1(4)
      io.outputSigmoid5 := sigHiddenLayer1(5)
      io.outputSigmoid6 := sigHiddenLayer1(6)
      io.outputSigmoid7 := sigHiddenLayer1(7)
      // io.outputState := 4.U
      // io.outputMultiplication := hiddenLayer1(1) >> 13
    }
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
