package project

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.io.Source
import chisel3.util._

/** NeuronWrapper
  *
  * @param slaveIO
  *   AxiStreamSlaveIf
  * @param masterIO
  *   AxiStreamExternalIf
  */
class NeuronWrapper extends Module {
  val sAxis = Wire(new AxiStreamSlaveIf(8))
  val slaveIO =
    IO(new AxiStreamExternalIf(8))
  slaveIO.suggestName("s_axis").connect(sAxis)

  val mAxis = Wire(new AxiStreamMasterIf(16))
  val masterIO = IO(Flipped(new AxiStreamExternalIf(16)))
  masterIO
    .suggestName("m_axis")
    .connect(mAxis)

  val neuron = Module(new Neuron(8))
  private val weightsCSV = readCSV("hardcoded_weights.csv")
  val weights = RegInit(
    VecInit.tabulate(1, 8) { (x, y) =>
      weightsCSV(x)(y).S(8.W)
    }
  )

  val io = IO(new Bundle {
    val outputB2SValues = Output(Vec(8, UInt(1.W)))
    val outputB2ISValues = Output(Vec(8, SInt(2.W)))
    val outputANDValues = Output(Vec(8, SInt(2.W)))
    val outputTreeAdder = Output(SInt((8 + 1).W))
    val outputStream = Output(UInt(1.W))
  })

  for (i <- 0 until 8) {
    io.outputB2SValues(i) := 0.U
    io.outputB2ISValues(i) := 0.S
    io.outputANDValues(i) := 0.S
  }
  io.outputTreeAdder := 0.S
  io.outputStream := 0.U

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

  sAxis.tready := RegInit(true.B)
  mAxis.data.tvalid := RegInit(false.B)
  mAxis.data.tlast := RegInit(false.B)
  mAxis.data.tdata := RegInit(0.U(16.W))
  mAxis.data.tkeep := RegInit("b1".U)

  object State extends ChiselEnum {
    val receiving, handling, sending = Value
  }

  val state = RegInit(State.receiving)

  val image = RegInit(VecInit(Seq.fill(8)(0.U(8.W))))
  val index = RegInit(0.U(3.W))

  val counter = RegInit(0.U(16.W))

  val minCycles = RegInit(0.U(10.W))

  def setImageAndWeight() = {
    neuron.io.inputPixels := image
    neuron.io.inputWeights := weights(0)
  }
  setImageAndWeight()

  switch(state) {
    // Step 1. Fill the image with 401 pixels
    is(State.receiving) {
      when(sAxis.data.tvalid) {
        image(index) := sAxis.data.tdata
        index := index + 1.U
        when(sAxis.data.tlast) {
          state := State.handling
          sAxis.tready := false.B
        }
      }
    }
    // Step 2. Process the information for 1024 cycles
    is(State.handling) {
      io.outputB2SValues := neuron.io.outputB2SValues
      io.outputB2ISValues := neuron.io.outputB2ISValues
      io.outputANDValues := neuron.io.outputANDValues
      io.outputTreeAdder := neuron.io.outputTreeAdder
      io.outputStream := neuron.io.outputStream

      counter := counter + neuron.io.outputStream
      minCycles := (minCycles + 1.U)
      when(minCycles === (1024.U - 1.U)) {
        state := State.sending
      }
    }
    // State 3. Return the information
    is(State.sending) {
      when(mAxis.tready) {
        mAxis.data.tlast := true.B
        mAxis.data.tvalid := true.B
        mAxis.data.tdata := counter
        // reinitialize everything
        image := VecInit(Seq.fill(8)(0.U(8.W)))
        index := 0.U
        counter := RegInit(0.U(16.W))

        minCycles := 0.U
        state := State.receiving
      }
    }
  }
}

object NeuronWrapper extends App {
  ChiselStage.emitSystemVerilogFile(
    new NeuronWrapper,
    args = Array(
      "--target-dir",
      "generated/project/"
    ),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info"
    )
  )
}
