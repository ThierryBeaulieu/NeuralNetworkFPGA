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

  val hiddenLayer1Data = readCSV("lab3/theta_0_int8.csv")
  val weights_hidden_layer1 = RegInit(
    VecInit.tabulate(25, 401) { (x, y) =>
      hiddenLayer1Data(x)(y).S(8.W)
    }
  )

  val hiddenLayer2Data = readCSV("lab3/theta_1_int8.csv")
  val weights_hidden_layer2 = RegInit(
    VecInit.tabulate(10, 25) { (x, y) =>
      hiddenLayer2Data(x)(y).S(8.W)
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
    val receiving, firstHiddenLayer, firstSigmoid, secondHiddenLayer,
        secondSigmoid, sending =
      Value
  }

  val state = RegInit(State.receiving)

  val hiddenLayer1 = RegInit(VecInit(Seq.fill(25)(0.S(25.W))))
  val pixelIndex = RegInit(0.U(9.W))
  val row1 = RegInit(0.U(5.W))
  val sigHiddenLayer1 = RegInit(VecInit(Seq.fill(26)(1.U(8.W))))
  val firstSigmoidLoaded = RegInit(false.B)

  val hiddenLayer2 = RegInit(VecInit(Seq.fill(10)(0.S(21.W))))
  val sigmoidIndex = RegInit(0.U(5.W))
  val row2 = RegInit(0.U(4.W))
  val sigHiddenLayer2 = RegInit(VecInit(Seq.fill(10)(0.U(8.W))))
  val secondSigmoidLoaded = RegInit(false.B)

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

      hiddenLayer1(row1) := (hiddenLayer1(row1) + (weights_hidden_layer1(row1)(
        pixelIndex
      ) * image(pixelIndex)))

      pixelIndex := (pixelIndex + 1.U)

      when(pixelIndex === (401.U - 1.U)) {
        row1 := (row1 + 1.U)
        pixelIndex := 0.U
      }

      when(row1 === 24.U && pixelIndex === (401.U - 1.U)) {
        state := State.firstSigmoid
        row1 := 0.U
        pixelIndex := 0.U
      }
    }
    is(State.firstSigmoid) {
      io.outputState := 3.U
      for (i <- 1 until hiddenLayer1.length) {
        val addr = (hiddenLayer1(i - 1)(16, 9)).asUInt
        sigHiddenLayer1(i) := sigmoidMemory.read(addr)
      }
      firstSigmoidLoaded := true.B
      when(firstSigmoidLoaded === true.B) {
        state := State.secondHiddenLayer
      }
    }
    is(State.secondHiddenLayer) {
      io.outputState := 4.U

      hiddenLayer2(row2) := (hiddenLayer2(row2) + (weights_hidden_layer2(row2)(
        sigmoidIndex
      ) * sigHiddenLayer1(sigmoidIndex)))

      sigmoidIndex := (sigmoidIndex + 1.U)

      when(sigmoidIndex === (25.U - 1.U)) {
        row2 := (row2 + 1.U)
        sigmoidIndex := 0.U
      }

      when(row2 === 9.U && sigmoidIndex === (25.U - 1.U)) {
        state := State.sending
        row2 := 0.U
        sigmoidIndex := 0.U
      }

      state := State.secondSigmoid
    }
    is(State.secondSigmoid) {
      io.outputState := 5.U

      for (i <- 0 until hiddenLayer2.length) {
        val addr = (hiddenLayer2(i)(14, 7)).asUInt
        sigHiddenLayer2(i) := sigmoidMemory.read(addr)
      }

      secondSigmoidLoaded := true.B
      when(secondSigmoidLoaded === true.B) {
        state := State.sending
      }
    }
    is(State.sending) {
      io.outputState := 6.U

      io.outputSigmoid0 := sigHiddenLayer2(0)
      io.outputSigmoid1 := sigHiddenLayer2(1)
      io.outputSigmoid2 := sigHiddenLayer2(2)
      io.outputSigmoid3 := sigHiddenLayer2(3)
      io.outputSigmoid4 := sigHiddenLayer2(4)
      io.outputSigmoid5 := sigHiddenLayer2(5)
      io.outputSigmoid6 := sigHiddenLayer2(6)
      io.outputSigmoid7 := sigHiddenLayer2(7)
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
